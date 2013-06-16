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
