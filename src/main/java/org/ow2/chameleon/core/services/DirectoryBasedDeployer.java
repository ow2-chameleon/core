package org.ow2.chameleon.core.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * A default implementation of the deployer accepting file from a specified directory. It also accept all files from
 * sub-directories.
 */
public class DirectoryBasedDeployer implements Deployer {

    private final File directory;

    /**
     * A logger.
     */
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public DirectoryBasedDeployer(File directory) {
        this.directory = directory;
    }

    public DirectoryBasedDeployer(String directory) {
        this.directory = new File(directory);
    }

    @Override
    public boolean accept(File file) {
        try {
            // Fail fast against NullPointerException
            if (directory == null) {
                throw new IllegalArgumentException("Directory must not be null");
            }

            if (!directory.isDirectory()) {
                throw new IllegalArgumentException("Not a directory: " + directory);
            }

            if (file == null) {
                return false;
            }

            if (!directory.exists()) {
                return false;
            }

            // Canonicalize paths (normalizes relative paths)
            String canonicalParent = directory.getCanonicalPath();
            String canonicalChild = file.getCanonicalPath();

            return FilenameUtils.directoryContains(canonicalParent, canonicalChild);
        } catch (IOException e) {
            logger.debug("Cannot check if {} is contained in {}", file.getAbsolutePath(),
                    directory.getAbsolutePath(), e);
            return false;
        }
    }

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
