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
