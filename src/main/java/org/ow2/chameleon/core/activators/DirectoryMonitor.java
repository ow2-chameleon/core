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
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.ow2.chameleon.core.services.Deployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Monitors a directory.
 * It tracks all deployer services exposed in the framework and delegate the file events to the adequate deployer.
 */
public class DirectoryMonitor implements BundleActivator, ServiceTrackerCustomizer {

    /**
     * A logger.
     */
    protected final Logger logger;
    /**
     * List of deployers
     */
    protected final List<Deployer> deployers = new ArrayList<Deployer>();
    /**
     * The directory.
     */
    private final File directory;
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
     * The lock avoiding concurrent modifications of the deployers map.
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * Service tracking to retrieve deployers.
     */
    private ServiceTracker tracker;
    private BundleContext context;

    public DirectoryMonitor(File directory, long polling) {
        this.directory = directory;
        this.polling = polling;
        this.logger = LoggerFactory.getLogger(DirectoryMonitor.class.getName() + "[" + directory.getName() + "]");

        if (polling == -1L) {
            // The polling is disabled
            return;
        }

        if (!directory.isDirectory()) {
            logger.info("Monitored directory {} not existing - creating directory", directory.getAbsolutePath());
            boolean created = this.directory.mkdirs();
            logger.debug("Monitored direction {} creation ? {}", directory.getAbsolutePath(), created);
        }

        // We observe all files.
        FileAlterationObserver observer = new FileAlterationObserver(directory, TrueFileFilter.INSTANCE);
        observer.addListener(new FileMonitor());
        logger.debug("Creating file alteration monitor for " + directory.getAbsolutePath() + " with a polling period " +
                "of " + polling);
        monitor = new FileAlterationMonitor(polling, observer);
    }

    public DirectoryMonitor(File directory) {
        this(directory, -1L);
    }

    /**
     * Acquires the write lock only and only if the write lock is not already held by the current thread.
     *
     * @return {@literal true} if the lock was acquired within the method, {@literal false} otherwise.
     */
    public boolean acquireWriteLockIfNotHeld() {
        if (!lock.isWriteLockedByCurrentThread()) {
            lock.writeLock().lock();
            return true;
        }
        return false;
    }

    /**
     * Releases the write lock only and only if the write lock is held by the current thread.
     *
     * @return {@literal true} if the lock has no more holders, {@literal false} otherwise.
     */
    public boolean releaseWriteLockIfHeld() {
        if (lock.isWriteLockedByCurrentThread()) {
            lock.writeLock().unlock();
        }
        return lock.getWriteHoldCount() == 0;
    }

    /**
     * Acquires the read lock only and only if no read lock is already held by the current thread.
     *
     * @return {@literal true} if the lock was acquired within the method, {@literal false} otherwise.
     */
    public boolean acquireReadLockIfNotHeld() {
        if (lock.getReadHoldCount() == 0) {
            lock.readLock().lock();
            return true;
        }
        return false;
    }

    /**
     * Releases the read lock only and only if the read lock is held by the current thread.
     *
     * @return {@literal true} if the lock has no more holders, {@literal false} otherwise.
     */
    public boolean releaseReadLockIfHeld() {
        if (lock.getReadHoldCount() != 0) {
            lock.readLock().unlock();
        }
        return lock.getReadHoldCount() == 0;
    }

    @Override
    public void start(final BundleContext context) throws IOException {
        this.context = context;
        logger.info("Starting installing resources from {}", directory.getAbsolutePath());
        this.tracker = new ServiceTracker(context, Deployer.class.getName(), this);

        // To avoid concurrency, we take the write lock here.
        try {
            acquireWriteLockIfNotHeld();

            // Arrives will be blocked until we release the write lock
            this.tracker.open();

            // Register file monitor
            startFileMonitoring();

        } finally {
            releaseWriteLockIfHeld();
        }

        // Initialization does not need the write lock, read is enough.

        //TODO Don't we call open twice there ?
        try {
            acquireReadLockIfNotHeld();
            if (directory.isDirectory()) {
                Collection<File> files = FileUtils.listFiles(directory, null, true);
                // We have to open the deployers with the set of accepted file.
                for (Deployer deployer : deployers) {
                    // Compute the list of accepted files
                    List<File> accepted = getAcceptedFilesByTheDeployer(files, deployer);
                    // Always call the open method, even with an empty set.
                    deployer.open(accepted);
                }
            }
        } finally {
            releaseReadLockIfHeld();
        }
    }

    /**
     * Filters the given list of file to return a collection containing only the file accepted by the deployer.
     * @param files the initial set of files
     * @param deployer the deployer
     * @return the set of accepted file, empty if none are accepted.
     */
    private List<File> getAcceptedFilesByTheDeployer(Collection<File> files, Deployer deployer) {
        List<File> accepted = new ArrayList<File>();
        for (File file : files) {
            if (deployer.accept(file)) {
                accepted.add(file);
            }
        }
        return accepted;
    }

    private void startFileMonitoring() throws IOException {
        if (polling == -1L) {
            logger.debug("No file monitoring for {}", directory.getAbsolutePath());
            return;
        }

        logger.info("Starting file monitoring for {} - polling : {} ms", directory.getName(), polling);
        try {
            monitor.start();
        } catch (Exception e) {
            throw new IOException("Cannot start the monitoring of " + directory.getAbsolutePath(), e);
        }
    }

    @Override
    public void stop(BundleContext context) {
        // To avoid concurrency, we take the write lock here.
        try {
            acquireWriteLockIfNotHeld();
            this.tracker.close();
            if (monitor != null) {
                logger.debug("Stopping file monitoring of {}", directory.getAbsolutePath());
                try {
                    monitor.stop();
                    logger.debug("File monitoring stopped");
                } catch (IllegalStateException e) {
                    logger.warn("Stopping an already stopped file monitor on " + directory.getAbsolutePath());
                    logger.debug(e.getMessage(), e);
                } catch (Exception e) {
                    logger.error("Something bad happened while trying to stop the file monitor", e);
                }
                monitor = null;
            }
        } finally {
            releaseWriteLockIfHeld();
        }

        // No concurrency involved from here.

        for (Deployer deployer : deployers) {
            deployer.close();
        }
    }

    @Override
    public Object addingService(ServiceReference reference) {
        Deployer deployer = (Deployer) context.getService(reference);

        try {
            acquireWriteLockIfNotHeld();
            deployers.add(deployer);
            Collection<File> files = FileUtils.listFiles(directory, null, true);
            List<File> accepted = getAcceptedFilesByTheDeployer(files, deployer);
            deployer.open(accepted);
        } finally {
            releaseWriteLockIfHeld();
        }

        return deployer;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object o) {
        // Cannot happen, deployers do not have properties.
    }

    @Override
    public void removedService(ServiceReference reference, Object o) {
        Deployer deployer = (Deployer) o;
        try {
            acquireWriteLockIfNotHeld();
            deployers.remove(deployer);
        } finally {
            releaseWriteLockIfHeld();
        }
    }

    private List<Deployer> getDeployersAcceptingFile(File file) {
        List<Deployer> depl = new ArrayList<Deployer>();
        try {
            acquireReadLockIfNotHeld();
            for (Deployer deployer : deployers) {
                if (deployer.accept(file)) {
                    depl.add(deployer);
                }
            }
        } finally {
            releaseReadLockIfHeld();
        }
        return depl;
    }

    private class FileMonitor extends FileAlterationListenerAdaptor {

        @Override
        public void onFileCreate(File file) {
            logger.info("File " + file + " created in " + directory);
            List<Deployer> depl = getDeployersAcceptingFile(file);

            // Callback called outside the protected region.
            logger.debug("Deployer handling creation of " + file.getName() + " : " + depl);
            for (Deployer deployer : depl) {
                try {
                    deployer.onFileCreate(file);
                } catch (Exception e) { //NOSONAR
                    logger.error("Error during the management of {} (creation) by {}",
                            file.getAbsolutePath(), deployer, e);
                }
            }
        }

        @Override
        public void onFileChange(File file) {
            logger.info("File " + file + " from " + directory + " changed");

            List<Deployer> depl = getDeployersAcceptingFile(file);

            logger.debug("Deployers handling change in " + file.getName() + " : " + depl);
            for (Deployer deployer : depl) {
                try {
                    deployer.onFileChange(file);
                } catch (Exception e) { //NOSONAR
                    logger.error("Error during the management of {} (change) by {}",
                            file.getAbsolutePath(), deployer, e);
                }
            }
        }

        @Override
        public void onFileDelete(File file) {
            logger.info("File " + file + " deleted from " + directory);

            List<Deployer> depl = getDeployersAcceptingFile(file);

            logger.debug("Deployer handling deletion of " + file.getName() + " : " + depl);
            for (Deployer deployer : depl) {
                try {
                    deployer.onFileDelete(file);
                } catch (Exception e) {  //NOSONAR
                    logger.error("Error during the management of {} (deletion) by {}",
                            file.getAbsolutePath(), deployer, e);
                }
            }
        }
    }
}
