/*
 * Copyright 2013 OW2 Chameleon
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ow2.chameleon.core.activators;

import com.google.common.base.Function;
import com.sun.istack.internal.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.ow2.chameleon.core.utils.BundleHelper.isBundle;
import static org.ow2.chameleon.core.utils.BundleHelper.isFragment;
import static org.ow2.chameleon.core.utils.Functions.map;

/**
 * An installer tracking a directory content and installing contained bundles.
 * Bundles are automatically started (except if they are fragments)
 */
public class DirectoryBundleMonitor implements BundleActivator {


    /**
     * The directory.
     */
    private final File directory;
    /**
     * A logger.
     */
    protected final Logger logger;
    /**
     * The file to bundle relation.
     */
    protected final ConcurrentHashMap<File, Bundle> bundles = new ConcurrentHashMap<File, Bundle>();
    /**
     * The lock avoiding concurrent modifications of the bundles map.
     */
    protected final Lock lock = new ReentrantLock();
    /**
     * Polling period.
     * -1 to disable polling.
     */
    private final long polling;
    /**
     * A monitor listening file changes.
     */
    private FileAlterationMonitor monitor;
    /**
     * The bundle context.
     */
    private BundleContext context;

    public DirectoryBundleMonitor(File directory, long polling) {
        this.directory = directory;
        this.polling = polling;
        this.logger = LoggerFactory.getLogger(DirectoryBundleMonitor.class.getName() + "[" + directory.getName() + "]");

        if (!directory.isDirectory()) {
            logger.info("Monitored directory {} not existing - creating directory", directory.getAbsolutePath());
            this.directory.mkdirs();
        }
    }

    public DirectoryBundleMonitor(File directory) {
        this(directory, -1);
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        this.context = context;
        logger.info("Starting installing bundles from {}", directory.getAbsolutePath());

        // Register file monitor
        startFileMonitoring();

        // Retrieve all jar files.
        Collection<File> files = FileUtils.listFiles(directory, new String[]{"jar"}, true);

        // Filter out non bundle.
        // Bundles are installed (but not started yet).
        Iterable<Bundle> detected = map(files, new Function<File, Bundle>() {

            @Override
            public Bundle apply(@Nullable java.io.File file) {
                return installAndGet(file, context);
            }
        });

        // Bundles are installed, start them if needed.
        for (Bundle bundle : detected) {
            if (!isFragment(bundle) && bundle.getState() != Bundle.ACTIVE) {
                bundle.start();
                logger.info("Bundle " + bundle.getSymbolicName() + " started");
            }
        }
    }

    protected Bundle installAndGet(File file, BundleContext context) {
        if (isBundle(file)) {
            try {
                lock.lock();
                // Do we already have this file.
                if (bundles.get(file) != null) {
                    // Update bundle
                    try {
                        bundles.get(file).update();
                        return bundles.get(file);
                    } catch (BundleException e) {
                        logger.error("Cannot update bundle {}", file.getAbsolutePath(), e);
                        return null;
                    }
                } else {
                    try {
                        Bundle bundle = context.installBundle("reference:" + file.toURI().toURL()
                                .toExternalForm());
                        bundles.put(file, bundle);
                        logger.info("Bundle " + bundle.getSymbolicName() + " installed");
                        return bundle;
                    } catch (Exception e) {
                        logger.error("Cannot install bundle {}", file.getAbsoluteFile(), e);
                        return null;
                    }
                }
            } finally {
                lock.unlock();
            }
        } else {
            return null;
        }
    }

    private void startFileMonitoring() throws Exception {
        if (polling == -1l) {
            logger.debug("No file monitoring for {}", directory.getAbsolutePath());
            return;
        }

        FileAlterationObserver observer = new FileAlterationObserver(directory, new SuffixFileFilter(".jar"));
        observer.addListener(new FileMonitor());
        monitor = new FileAlterationMonitor(polling, observer);
        logger.info("Starting file monitoring for {} - polling : {} ms", directory.getName(), polling);
        monitor.start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (monitor != null) {
            logger.debug("Stopping file monitoring of {}", directory.getAbsolutePath());
            monitor.stop(5); // Wait 5 milliseconds.
        }
    }

    private class FileMonitor extends FileAlterationListenerAdaptor {

        /**
         * A jar file was created.
         *
         * @param file the file
         */
        @Override
        public void onFileCreate(File file) {
            logger.info("File " + file + " created in " + directory);
            Bundle bundle = installAndGet(file, context);
            if (bundle != null && !isFragment(bundle)) {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    logger.error("Cannot start bundle {}", file.getAbsolutePath(), e);
                }
            }
        }

        @Override
        public void onFileChange(File file) {
            // Same as file create, as it will update it is needed.
            onFileCreate(file);
        }

        @Override
        public void onFileDelete(File file) {
            logger.info("File " + file + " deleted from " + directory);
            // If it was a bundle, uninstall it
            try {
                lock.lock();
                Bundle bundle = bundles.remove(file);
                if (bundle != null) {
                    try {
                        bundle.uninstall();
                    } catch (BundleException e) {
                        logger.error("Cannot uninstall bundle {}", bundle.getSymbolicName(), e);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
