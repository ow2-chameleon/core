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

import org.osgi.service.cm.Configuration;

import java.io.IOException;
import java.util.Dictionary;

/**
 * Represents a configuration not pushed in the configuration admin.
 */
class UnmanagedConfiguration implements Configuration {

    /**
     * The singleton.
     */
    public static final UnmanagedConfiguration INSTANCE = new UnmanagedConfiguration();
    /**
     * The constant pid of not managed configuration.
     */
    public static final String NOT_MANAGED = "not managed";

    private UnmanagedConfiguration() {
        // Avoid direct instantiation.
    }

    /** {@inheritDoc} */
    @Override
    public String getPid() {
        return NOT_MANAGED;
    }

    /** {@inheritDoc} */
    @Override
    public Dictionary getProperties() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void update(Dictionary dictionary) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void delete() throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String getFactoryPid() {
        return getPid();
    }

    /** {@inheritDoc} */
    @Override
    public void update() throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String getBundleLocation() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setBundleLocation(String s) {
        // Do nothing.
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return NOT_MANAGED;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        return o instanceof UnmanagedConfiguration && o.hashCode() == hashCode(); //NOSONAR no need to check the class.
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return NOT_MANAGED.hashCode();
    }
}
