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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.core.services.AbstractDeployer;
import org.ow2.chameleon.core.services.ExtensionBasedDeployer;

import java.io.File;
import java.util.Collection;

/**
 * A stupid deployer.
 */
@Component
@Provides
public class TextDeployer extends ExtensionBasedDeployer {

    public TextDeployer() {
        super("txt");
    }

    @Override
    public void onFileCreate(File file) {
        System.out.println("New file " + file.getAbsolutePath());
    }

    @Override
    public void onFileChange(File file) {
        System.out.println("File content changed :" + file.getAbsolutePath());
    }

    @Override
    public void onFileDelete(File file) {
        System.out.println("File " + file.getAbsolutePath() + " deleted");
    }

    @Override
    public void open(Collection<File> files) {
        System.out.println(files.size() + " text files found");
    }

    @Override
    public void close() {
        System.out.println("Bye bye");
    }
}
