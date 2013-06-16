package org.ow2.chameleon.core.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

/**
 *  A default implementation of the deployer.
 */
public abstract class AbstractDeployer implements  Deployer {

    /**
     * A logger.
     */
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

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
