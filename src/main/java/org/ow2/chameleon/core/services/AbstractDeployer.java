package org.ow2.chameleon.core.services;

import java.io.File;
import java.util.Collection;

/**
 * A default implementation of the deployer.
 */
public abstract class AbstractDeployer implements Deployer {


    @Override
    public void onFileCreate(File file) {
        //Do nothing
    }

    @Override
    public void onFileChange(File file) {
        onFileCreate(file);
    }

    @Override
    public void onFileDelete(File file) {
        //Do nothing
    }

    @Override
    public void open(Collection<File> files) {
        for (File file : files) {
            onFileCreate(file);
        }
    }

    @Override
    public void close() {
        //Do nothing
    }
}
