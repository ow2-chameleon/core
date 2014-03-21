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
 * A default implementation of the deployer.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public abstract class AbstractDeployer implements Deployer {


    /** {@inheritDoc} */
    @Override
    public void onFileCreate(File file) {
        //Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void onFileChange(File file) {
        onFileCreate(file);
    }

    /** {@inheritDoc} */
    @Override
    public void onFileDelete(File file) {
        //Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void open(Collection<File> files) {
        for (File file : files) {
            onFileCreate(file);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        //Do nothing
    }
}
