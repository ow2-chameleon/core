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

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * A default implementation of the deployer accepting file by extension.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public class ExtensionBasedDeployer extends AbstractDeployer {

    /**
     * The list of managed extensions (immutable).
     */
    private final List<String> extensions;

    /**
     * <p>Constructor for ExtensionBasedDeployer.</p>
     *
     * @param extensions an array of {@link java.lang.String} objects.
     */
    public ExtensionBasedDeployer(String[] extensions) {
        this(Arrays.asList(extensions));
    }

    /**
     * <p>Constructor for ExtensionBasedDeployer.</p>
     *
     * @param extension a {@link java.lang.String} object.
     */
    public ExtensionBasedDeployer(String extension) {
        this(new String[]{extension});
    }

    /**
     * <p>Constructor for ExtensionBasedDeployer.</p>
     *
     * @param extensions a {@link java.util.List} object.
     */
    public ExtensionBasedDeployer(List<String> extensions) {
        this.extensions = ImmutableList.copyOf(extensions);
    }

    /**
     * <p>Getter for the field <code>extensions</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getExtensions() {
        return extensions;
    }

    /** {@inheritDoc} */
    @Override
    public boolean accept(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        return extensions.contains(extension);
    }
}
