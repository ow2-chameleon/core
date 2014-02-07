package org.ow2.chameleon.core.activators;

import org.osgi.service.cm.Configuration;

import java.io.IOException;
import java.util.Dictionary;

/**
* Represents a configuration not pushed in the configuraiton admin.
*/
class UnmanagedConfiguration implements Configuration {

    public static final UnmanagedConfiguration INSTANCE = new UnmanagedConfiguration();
    public static final String NOT_MANAGED = "not managed";

    private UnmanagedConfiguration() {
        // Avoid direct instantiation.
    }

    @Override
    public String getPid() {
        return NOT_MANAGED;
    }

    @Override
    public Dictionary getProperties() {
        return null;
    }

    @Override
    public void update(Dictionary dictionary) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFactoryPid() {
        return getPid();
    }

    @Override
    public void update() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBundleLocation() {
        return null;
    }

    @Override
    public void setBundleLocation(String s) {
        // Do nothing.
    }

    @Override
    public String toString() {
        return NOT_MANAGED;
    }

    @Override
    public boolean equals(Object o) {
        return o != null  && o instanceof UnmanagedConfiguration && o.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return INSTANCE.hashCode();
    }
}
