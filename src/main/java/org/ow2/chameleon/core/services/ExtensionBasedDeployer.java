package org.ow2.chameleon.core.services;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 *  A default implementation of the deployer accepting file by extension.
 */
public class ExtensionBasedDeployer implements  Deployer {

    /**
     * The list of managed extensions (immutable).
     */
    private final List<String> extensions;

    /**
     * A logger.
     */
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public ExtensionBasedDeployer(String[] extensions) {
        this.extensions = ImmutableList.copyOf(extensions);
    }

    public ExtensionBasedDeployer(String extension) {
        this.extensions = ImmutableList.of(extension);
    }

    public ExtensionBasedDeployer(List<String> extensions) {
        this.extensions = ImmutableList.copyOf(extensions);
    }

    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public boolean accept(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        return extensions.contains(extension);
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
