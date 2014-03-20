package org.ow2.chameleon.core.services;

import java.io.File;

/**
 * A service letting adding / removing directory watchers.
 * @since 1.0.4
 */
public interface Watcher {

    /**
     * Adds a directory to the watcher. If `watch` is true, the directory is monitored,
     * otherwise only the initial provisioning is done.
     * @param directory the directory
     * @param watch {@literal true} to enable the <em>watch</em> mode.
     */
    public void add(File directory, boolean watch);

    /**
     * Adds a directory to the watcher. If `polling` is not -1, the directory is monitored,
     * otherwise only the initial provisioning is done.
     * @param directory the directory
     * @param polling the polling period in ms, -1 to disable the watch.
     */
    public void add(File directory, long polling);

    /**
     * If the directory is watched, stop it.
     * @param directory the directory
     * @return {@literal true} if the directory was watched and stopped.
     */
    public boolean stop(File directory);

}
