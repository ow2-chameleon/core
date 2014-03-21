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
import java.util.Collection;

/**
 * An interface to extend deployer capability of  Chameleon.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public interface Deployer {

    /**
     * Does the current deployer accept the given file.
     * The accept method must not check file existence, as it may disable notification on file deletion.
     *
     * @param file the file
     * @return {@literal true} if the file is accepted by the current deployer, {@literal false} otherwise
     */
    boolean accept(File file);

    /**
     * Callback called when an accepted file is created.
     *
     * @param file the new file
     */
    void onFileCreate(File file);

    /**
     * Callback called when an accepted file is updated.
     *
     * @param file the updated file
     */
    void onFileChange(File file);

    /**
     * Callback called when an accepted file is deleted.
     *
     * @param file the file
     */
    void onFileDelete(File file);

    /**
     * Method called when the deployer is initialized.
     * This method is called once per monitored directory, with a potentially empty set of files (if there are no
     * file or if the directory does not contain any accepted file)
     *
     * @param files the set of accepted file currently present is a monitored directory.
     */
    void open(Collection<File> files);

    /**
     * Callback called when the deployer is closed.
     */
    void close();

}
