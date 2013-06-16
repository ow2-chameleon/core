package org.ow2.chameleon.core.services;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * An interface to extend deployer capability of  Chameleon.
 * Each deployer listens 1 or more extension and will be notified whenever a file of a monitored directory is
 * 'altered'.
 *
 * Several deployers can manage the same extension.
 */
public interface Deployer {

    public boolean accept(File file);

    public void onFileCreate(File file);

    public void onFileChange(File file);

    public void onFileDelete(File file);

    public void open(Collection<File> files);

    public void close();

}
