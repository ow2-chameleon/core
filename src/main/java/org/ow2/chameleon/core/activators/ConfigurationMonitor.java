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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An installer tracking a directory content and installing contained configuration (.cfg) files.
 */
public class ConfigurationMonitor implements BundleActivator, ServiceListener {
    // TODO Handle dynamic arrival of the configuration admin.

    /**
     * Un-managed configuration object.
     */
    private static final Configuration UNMANAGED_CONFIGURATION = new Configuration() {
        @Override
        public String getPid() {
            return "not managed";
        }

        @Override
        public Dictionary getProperties() {
            return null;
        }

        @Override
        public void update(Dictionary dictionary) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getFactoryPid() {
            return getPid();
        }

        @Override
        public void update() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setBundleLocation(String s) { }

        @Override
        public String getBundleLocation() { return null; }
    };

    /**
     * The directory.
     */
    private final File directory;
    /**
     * A logger.
     */
    private final Logger logger;
    /**
     * The file to bundle relation.
     */
    private final ConcurrentHashMap<File, Configuration> configurations = new ConcurrentHashMap<File, Configuration>();
    /**
     * The lock avoiding concurrent modifications of the bundles map.
     */
    private final Lock lock = new ReentrantLock();
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

    public ConfigurationMonitor(File directory, long polling) throws IOException {
        this.directory = directory;
        this.polling = polling;
        this.logger = LoggerFactory.getLogger(ConfigurationMonitor.class.getName() + "[" + directory.getName() + "]");

        if (!directory.isDirectory()) {
            logger.info("Monitored directory {} not existing - creating directory", directory.getAbsolutePath());
            FileUtils.forceMkdir(directory);
        }
    }

    public ConfigurationMonitor(File directory) throws IOException {
        this(directory, -1);
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        this.context = context;
        logger.info("Starting tracking configuration from {}", directory.getAbsolutePath());

        // Register file monitor
        startFileMonitoring();

        context.addServiceListener(this, "(" + Constants.OBJECTCLASS + "=" + ConfigurationAdmin.class.getName() + ")");

        // Retrieve all cfg files.
        Collection<File> files = FileUtils.listFiles(directory, new String[]{"cfg"}, true);

        ConfigurationAdmin admin = getConfigurationAdmin(context);

        for (File file : files) {
            readAndApplyConfiguration(file, admin);
        }
    }

    private void readAndApplyConfiguration(File file, ConfigurationAdmin admin) throws Exception {
        try {
            lock.lock();
            if (admin == null) {
                logger.warn("Cannot apply configuration "+ file.getName() + " - no configuration admin");
                configurations.put(file, UNMANAGED_CONFIGURATION);
            } else {
                Properties properties = read(file);
                String[] pid = parsePid(file.getName());
                Hashtable<String, String> ht = new Hashtable<String, String>();
                for (String k : properties.stringPropertyNames()) {
                    ht.put(k, properties.getProperty(k));
                }

                Configuration config = getConfiguration(pid[0], pid[1], admin);
                if (config.getBundleLocation() != null) {
                    config.setBundleLocation(null);
                }
                logger.info("A configuration will be pushed " + ht);
                config.update(ht);

                configurations.put(file, config);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the configuration admin service.
     *
     * @param context the bundle context.
     * @return the Configuration Admin service object
     * @throws Exception if the configuration admin is unavailable.
     */
    private ConfigurationAdmin getConfigurationAdmin(BundleContext context)
            throws Exception {
        // Should be there !
        ServiceReference ref = context
                .getServiceReference(ConfigurationAdmin.class.getName());
        if (ref == null) {
            return null;
        } else {
            return (ConfigurationAdmin) context.getService(ref);
        }
    }

    private void startFileMonitoring() throws Exception {
        if (polling == -1l) {
            logger.debug("No file monitoring for {}", directory.getAbsolutePath());
            return;
        }

        FileAlterationObserver observer = new FileAlterationObserver(directory, new SuffixFileFilter(".cfg"));
        observer.addListener(new FileMonitor());
        monitor = new FileAlterationMonitor(polling, observer);
        logger.info("Starting file monitoring for {} - polling : {} ms", directory.getName(), polling);
        monitor.start();
    }

    private Properties read(File file) throws IOException {
        Properties p = new Properties();
        InputStream in = new FileInputStream(file);
        p.load(in);
        in.close();
        return p;
    }

    /**
     * Parses cfg file associated PID. This supports both ManagedService PID and
     * ManagedServiceFactory PID
     *
     * @param path the path
     * @return structure {pid, factory pid} or {pid, <code>null</code> if not a
     *         Factory configuration.
     */
    String[] parsePid(String path) {
        String pid = path.substring(0, path.length() - ".cfg".length());
        int n = pid.indexOf('-');
        if (n > 0) {
            String factoryPid = pid.substring(n + 1);
            pid = pid.substring(0, n);
            return new String[]{pid, factoryPid};
        } else {
            return new String[]{pid, null};
        }
    }

    /**
     * Gets a Configuration object.
     *
     * @param pid        the pid
     * @param factoryPid the factory pid
     * @param cm         the config admin service
     * @return the Configuration object (used to update the configuration)
     * @throws Exception if the Configuration object cannot be retrieved
     */
    Configuration getConfiguration(String pid, String factoryPid,
                                   ConfigurationAdmin cm) throws Exception {
        Configuration newConfiguration;
        if (factoryPid != null) {
            newConfiguration = cm.createFactoryConfiguration(pid, null);
        } else {
            newConfiguration = cm.getConfiguration(pid, null);
        }
        return newConfiguration;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        context.removeServiceListener(this);
        if (monitor != null) {
            monitor.stop(5); // Wait 5 milliseconds.
            logger.debug("Stopping file monitoring of {}", directory.getAbsolutePath());
        }
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            logger.info("Configuration admin registered");
            // Retrieve all cfg files.
            Collection<File> files = FileUtils.listFiles(directory, new String[]{"cfg"}, true);

            try {
                ConfigurationAdmin admin = getConfigurationAdmin(context);
                for (File file : files) {
                    readAndApplyConfiguration(file, admin);
                }
            } catch (Exception e) {
                logger.error("Cannot get the configuration admin or push a configuration");
            }
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
            try {
                readAndApplyConfiguration(file, getConfigurationAdmin(context));
            } catch (Exception e) {
                logger.error("Cannot apply configuration from {}", file.getAbsolutePath(), e);
            }
        }

        @Override
        public void onFileChange(File file) {
            // Same as file create, as it will update it is needed.
            onFileCreate(file);
        }

        @Override
        public void onFileDelete(File file) {
            // If it was a bundle, uninstall it
            try {
                lock.lock();
                Configuration configuration = configurations.remove(file);
                if (configuration != null) {
                    try {
                        configuration.delete();
                    } catch (IOException e) {
                        logger.error("Cannot delete configuration {}", configuration.getPid(), e);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
