package org.ow2.chameleon.core.utils;

import java.io.File;
import java.util.concurrent.ThreadFactory;

/**
 * The thread factory used to create the monitor thread.
 */
public class MonitorThreadFactory implements ThreadFactory {

    public static final String PREFIX = "monitor-";
    private final String name;

    public MonitorThreadFactory(File directory) {
        this.name = PREFIX + directory.getName();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(name);
        return thread;
    }
}
