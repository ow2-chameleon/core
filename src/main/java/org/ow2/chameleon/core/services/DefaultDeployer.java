package org.ow2.chameleon.core.services;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 *  A default implementation of the deployer.
 */
public class DefaultDeployer implements  Deployer {

    /**
     * The list of managed extensions (immutable).
     */
    private final List<String> extensions;

    /**
     * A logger.
     */
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public DefaultDeployer(String[] extensions) {
        this.extensions = ImmutableList.copyOf(extensions);
    }

    public DefaultDeployer(String extension) {
        this.extensions = ImmutableList.of(extension);
    }

    public DefaultDeployer(List<String> extensions) {
        this.extensions = ImmutableList.copyOf(extensions);
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public void onFileCreate(File file) { }

    @Override
    public void onFileChange(File file) {
        onFileCreate(file);
    }

    @Override
    public void onFileDelete(File file) { }

    @Override
    public void open(Collection<File> files) {
        for (File file : files) {
            onFileCreate(file);
        }
    }

    @Override
    public void close() { }
}
