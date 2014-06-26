/*
 * #%L
 * OW2 Chameleon - Core
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.core.activators;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.services.Watcher;
import org.ow2.chameleon.core.utils.MonitorThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Monitors a directory.
 * It tracks all deployer services exposed in the framework and delegate the file events to the adequate deployer.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public class DirectoryMonitor implements BundleActivator, Watcher, ServiceTrackerCustomizer<Deployer, Deployer> {

    /**
     * A logger.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(DirectoryMonitor.class);
    /**
     * List of deployers.
     */
    protected final List<Deployer> deployers = new ArrayList<Deployer>();
    /**
     * A monitor listening file changes.
     */
    private Map<File, FileAlterationMonitor> monitors = new LinkedHashMap<File, FileAlterationMonitor>();
    /**
     * The lock avoiding concurrent modifications of the deployers map.
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * Service tracking to retrieve deployers.
     */
    private ServiceTracker<Deployer, Deployer> tracker;

    /**
     * The bundle context.
     */
    private BundleContext context;

    /**
     * The service registration.
     */
    private ServiceRegistration<Watcher> reg;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final BundleContext context) throws IOException {
        this.context = context;
        LOGGER.info("Starting watcher service configured for {}", monitors.keySet());
        this.tracker = new ServiceTracker<Deployer, Deployer>(context, Deployer.class.getName(), this);

        // To avoid concurrency, we take the write lock here.
        try {
            acquireWriteLockIfNotHeld();

            // Arrives will be blocked until we release the write lock
            this.tracker.open();
            for (Map.Entry<File, FileAlterationMonitor> entry : monitors.entrySet()) {
                if (entry.getValue() != null) {
                    LOGGER.info("Starting file monitoring for {}", entry.getKey().getName());
                    try {
                        entry.getValue().start();
                    } catch (Exception e) {
                        throw new IOException("Cannot start the monitoring of " + entry.getKey().getAbsolutePath(), e);
                    }
                } else {
                    LOGGER.debug("No file monitoring for {}", entry.getKey().getName());
                }
            }
            reg = context.registerService(Watcher.class, this, null);
        } finally {
            releaseWriteLockIfHeld();
        }
    }

    /**
     * Filters the given list of file to return a collection containing only the file accepted by the deployer.
     *
     * @param files    the initial set of files
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext context) {
        // To avoid concurrency, we take the write lock here.
        try {
            acquireWriteLockIfNotHeld();
            this.tracker.close();
            if (reg != null) {
                reg.unregister();
                reg = null;
            }
            for (Map.Entry<File, FileAlterationMonitor> entry : monitors.entrySet()) {
                if (entry.getValue() != null) {
                    LOGGER.debug("Stopping file monitoring of {}", entry.getKey().getAbsolutePath());
                    try {
                        entry.getValue().stop();
                        LOGGER.debug("File monitoring stopped");
                    } catch (IllegalStateException e) {
                        LOGGER.warn("Stopping an already stopped file monitor on {}.",
                                entry.getKey().getAbsolutePath());
                        LOGGER.debug(e.getMessage(), e);
                    } catch (Exception e) {
                        LOGGER.error("Something bad happened while trying to stop the file monitor", e);
                    }
                }
            }
            monitors.clear();
            this.context = null;
        } finally {
            releaseWriteLockIfHeld();
        }

        // No concurrency involved from here (tracker closed)
        for (Deployer deployer : deployers) {
            deployer.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Deployer addingService(ServiceReference<Deployer> reference) {
        Deployer deployer = context.getService(reference);
        try {
            acquireWriteLockIfNotHeld();
            deployers.add(deployer);
            for (File directory : monitors.keySet()) {
                Collection<File> files = FileUtils.listFiles(directory, null, true);
                List<File> accepted = getAcceptedFilesByTheDeployer(files, deployer);
                LOGGER.info("Opening deployer {} for directory {}.", deployer, directory.getAbsolutePath());
                deployer.open(accepted);
            }
        } finally {
            releaseWriteLockIfHeld();
        }

        return deployer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifiedService(ServiceReference<Deployer> reference, Deployer o) {
        // Cannot happen, deployers do not have properties.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removedService(ServiceReference<Deployer> reference, Deployer deployer) {
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

    /**
     * {@inheritDoc}
     * <p/>
     * Adds a directory to the watcher. If `watch` is true, the directory is monitored,
     * otherwise only the initial provisioning is done.
     */
    @Override
    public boolean add(File directory, boolean watch) {
        if (watch) {
            return add(directory, 2000L);
        } else {
            return add(directory, -1L);
        }
    }

    /**
     * Checks whether a directory is already monitored.
     * It checks if a parent directory is in the monitored list and is really monitored. This avoid creating too much
     * monitor threads.
     *
     * @param directory the directory
     * @return {@literal 0} if the directory is already monitored, {@literal 1} if a parent directory is monitored,
     * {@literal 2} if the directory is there but not monitored,  {@literal 3} if neither a directory nor its
     * parents are monitored.
     */
    private int isDirectoryAlreadyMonitored(File directory) {
        try {
            acquireWriteLockIfNotHeld();
            if (monitors.containsKey(directory)) {
                // Well, that the easy case, we have the exact same directory.
                // But is it monitored ?
                if (monitors.get(directory) != null) {
                    // Yes it is
                    return 0;
                } else {
                    return 2;
                }
            } else {
                // Check whether we are monitoring a parent directory
                for (Map.Entry<File, FileAlterationMonitor> entry : monitors.entrySet()) {
                    File dir = entry.getKey();
                    if (FilenameUtils.directoryContains(dir.getCanonicalPath(), directory.getCanonicalPath())
                            && entry.getValue() != null) {
                        // Directory is a child of dir, and this parent is already monitored.
                        return 1;
                    }
                }
                // No monitored parent.
                return 3;
            }
        } catch (IOException e) {
            LOGGER.error("Cannot determine whether the directory is already monitored or not", e);
            return 3;
        } finally {
            releaseWriteLockIfHeld();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Adds a directory to the watcher. If `polling` is not -1, the directory is monitored,
     * otherwise only the initial provisioning is done.
     */
    @Override
    public boolean add(File directory, long polling) {
        try {
            acquireWriteLockIfNotHeld();
            final int status = isDirectoryAlreadyMonitored(directory);
            if (status <= 1) {
                // Not supported
                if (status == 1) {
                    LOGGER.warn("Cannot add {} to the Directory Monitor, a parent directory is already monitored.",
                            directory);
                } else {
                    // 0
                    LOGGER.warn("Cannot add {} to the Directory Monitor,the directory is already monitored.",
                            directory);
                }
                return false;
            }

            if (polling == -1L && status == 2) {
                // Nothing to do.
                LOGGER.warn("Cannot add {} to the Directory Monitor, the directory is already there as not monitor " +
                        "(as requested).", directory);
                return false;
            }

            if (polling == -1L) {
                // Status = 3 -> add directory.
                // Disable polling.
                monitors.put(directory, null);
                // Are we started or not ?
                if (context != null) {
                    openDeployers(directory);
                }
                return true;
            } else {
                if (!directory.isDirectory()) {
                    LOGGER.info("Monitored directory {} not existing - creating directory", directory.getAbsolutePath());
                    boolean created = directory.mkdirs();
                    LOGGER.debug("Monitored direction {} creation ? {}", directory.getAbsolutePath(), created);
                }

                // if status is in {2, 3}, set the file alteration monitor

                // We observe all files as deployers will filter out undesirable files.
                final FileAlterationMonitor monitor = createFileAlterationMonitor(directory, polling);

                // Are we started or not ?
                if (context != null) {
                    monitor.start();
                    openDeployers(directory);
                }

                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Cannot start the file monitoring on {}", directory, e);
            return false;
        } finally {
            releaseWriteLockIfHeld();
        }
    }

    private FileAlterationMonitor createFileAlterationMonitor(File directory, long polling) {
        FileAlterationObserver observer = new FileAlterationObserver(directory, TrueFileFilter.INSTANCE);
        observer.addListener(new FileMonitor(directory));
        LOGGER.debug("Creating file alteration monitor for " + directory.getAbsolutePath() + " with a polling period " +
                "of " + polling);
        final FileAlterationMonitor monitor = new FileAlterationMonitor(polling, observer);
        monitor.setThreadFactory(new MonitorThreadFactory(directory));
        monitors.put(directory, monitor);
        return monitor;
    }

    /**
     * Open the deployers on the given directory.
     *
     * @param directory the directory
     */
    private void openDeployers(File directory) {
        Collection<File> files = FileUtils.listFiles(directory, null, true);
        for (Deployer deployer : deployers) {
            List<File> accepted = getAcceptedFilesByTheDeployer(files, deployer);
            LOGGER.info("Opening deployer {} for directory {}.", deployer, directory.getAbsolutePath());
            deployer.open(accepted);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If the directory is watched, stop it.
     */
    @Override
    public boolean removeAndStopIfNeeded(File directory) {
        try {
            acquireWriteLockIfNotHeld();
            FileAlterationMonitor monitor = monitors.remove(directory);
            if (monitor != null) {
                try {
                    monitor.stop();
                } catch (IllegalStateException e) {
                    LOGGER.warn("Stopping an already stopped file monitor on {}.",
                            directory.getAbsolutePath());
                    LOGGER.debug(e.getMessage(), e);
                } catch (Exception e) {
                    LOGGER.error("Something bad happened while trying to stop the file monitor on {}", directory, e);
                }
                return true;
            }
            return false;
        } finally {
            releaseWriteLockIfHeld();
        }
    }

    private class FileMonitor extends FileAlterationListenerAdaptor {

        private final File directory;

        /**
         * Creates a new file monitor notified whenever a file from the given directory is created, updated, or deleted.
         *
         * @param directory the directory
         */
        public FileMonitor(File directory) {
            this.directory = directory;
        }

        @Override
        public void onFileCreate(File file) {
            LOGGER.info("File " + file + " created in " + directory);
            List<Deployer> depl = getDeployersAcceptingFile(file);

            // Callback called outside the protected region.
            LOGGER.debug("Deployer handling creation of " + file.getName() + " : " + depl);
            for (Deployer deployer : depl) {
                try {
                    deployer.onFileCreate(file);
                } catch (Exception e) { //NOSONAR
                    LOGGER.error("Error during the management of {} (creation) by {}",
                            file.getAbsolutePath(), deployer, e);
                }
            }
        }

        @Override
        public void onFileChange(File file) {
            LOGGER.info("File " + file + " from " + directory + " changed");

            List<Deployer> depl = getDeployersAcceptingFile(file);

            LOGGER.debug("Deployers handling change in " + file.getName() + " : " + depl);
            for (Deployer deployer : depl) {
                try {
                    deployer.onFileChange(file);
                } catch (Exception e) { //NOSONAR
                    LOGGER.error("Error during the management of {} (change) by {}",
                            file.getAbsolutePath(), deployer, e);
                }
            }
        }

        @Override
        public void onFileDelete(File file) {
            LOGGER.info("File " + file + " deleted from " + directory);

            List<Deployer> depl = getDeployersAcceptingFile(file);

            LOGGER.debug("Deployer handling deletion of " + file.getName() + " : " + depl);
            for (Deployer deployer : depl) {
                try {
                    deployer.onFileDelete(file);
                } catch (Exception e) {  //NOSONAR
                    LOGGER.error("Error during the management of {} (deletion) by {}",
                            file.getAbsolutePath(), deployer, e);
                }
            }
        }
    }
}
