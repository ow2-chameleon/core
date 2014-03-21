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
package org.ow2.chameleon.core.services;

import java.io.File;

/**
 * A service letting adding / removing directory watchers.
 *
 * @author The OW2 Chameleon Team
 * @since 1.0.4
 * @version $Id: 1.0.4 $Id
 */
public interface Watcher {

    /**
     * Adds a directory to the watcher. If `watch` is true, the directory is monitored,
     * otherwise only the initial provisioning is done.
     *
     * @param directory the directory
     * @param watch     {@literal true} to enable the <em>watch</em> mode.
     */
    public void add(File directory, boolean watch);

    /**
     * Adds a directory to the watcher. If `polling` is not -1, the directory is monitored,
     * otherwise only the initial provisioning is done.
     *
     * @param directory the directory
     * @param polling   the polling period in ms, -1 to disable the watch.
     */
    public void add(File directory, long polling);

    /**
     * If the directory is watched, stop it.
     *
     * @param directory the directory
     * @return {@literal true} if the directory was watched and stopped.
     */
    public boolean stop(File directory);

}
