package org.ow2.chameleon.core.activators;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.ow2.chameleon.core.services.AbstractDeployer;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.utils.BundleHelper;

import java.io.File;
import java.util.*;

/**
 * Bundle deployer.
 */
public class BundleDeployer extends AbstractDeployer implements BundleActivator {

    //TODO How does uninstallation and reference: work together when file is deleted.
    //Can the bundle access un-accessed classes during the stop ?

    Map<File, Bundle> bundles = new HashMap<File, Bundle>();
    private BundleContext context;

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        context.registerService(Deployer.class, this, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // The service will be withdrawn automatically.
    }

    @Override
    public boolean accept(File file) {
        // If the file does not exist anymore, isBundle returns false.
        return file.getName().endsWith(".jar") && (! file.isFile()  || BundleHelper.isBundle(file));
    }

    @Override
    public void onFileCreate(File file) {
        logger.debug("File creation event received for {}", file.getAbsoluteFile());

        synchronized (this) {
            if (bundles.containsKey(file)) {
                Bundle bundle = bundles.get(file);
                logger.info("Updating bundle {} - {}", bundle.getSymbolicName(), file.getAbsoluteFile());
                try {
                    bundle.update();
                    tryToStartUnstartedBundles(bundle);
                } catch (BundleException e) {
                    logger.error("Error during bundle update {} from {}", bundle.getSymbolicName(),
                            file.getAbsoluteFile(), e);
                }
            } else {
                logger.info("Installing bundle from {}", file.getAbsoluteFile());
                try {
                    Bundle bundle = context.installBundle("reference:" + file.toURI().toURL()
                            .toExternalForm());
                    bundles.put(file, bundle);
                    if (!BundleHelper.isFragment(bundle)) {
                        logger.info("Starting bundle {} - {}", bundle.getSymbolicName(), file.getAbsoluteFile());
                        bundle.start();
                    }
                    tryToStartUnstartedBundles(bundle);
                } catch (Exception e) {
                    logger.error("Error during bundle installation of {}", new Object[]{file.getAbsoluteFile(), e});
                }
            }
        }
    }

    /**
     * Iterates over the set of bundles and try to start unstarted bundles.
     * This method is called when holding the monitor lock.
     *
     * @param bundle the installed bundle triggering this attempt.
     */
    private void tryToStartUnstartedBundles(Bundle bundle) {
        for (Bundle b : bundles.values()) {
            if (bundle != b && b.getState() != Bundle.ACTIVE && !BundleHelper.isFragment(b)) {
                logger.debug("Trying to start bundle {} after having installed bundle {}", b.getSymbolicName(),
                        bundle.getSymbolicName());
                try {
                    b.start();
                } catch (BundleException e) {
                    logger.debug("Failed to start bundle {} after having installed bundle {}",
                            b.getSymbolicName(),
                            bundle.getSymbolicName(), e);
                }
            }
        }
    }

    /**
     * It's a good practice to install all bundles and then start them.
     * This method cannot be interrupted.
     *
     * @param files the set of file.
     */
    @Override
    public void open(Collection<File> files) {
        List<Bundle> toStart = new ArrayList<Bundle>();
        for (File file : files) {
            try {
                Bundle bundle = context.installBundle("reference:" + file.toURI().toURL()
                        .toExternalForm());
                bundles.put(file, bundle);
                if (!BundleHelper.isFragment(bundle)) {
                    toStart.add(bundle);
                }
            } catch (Exception e) {
                logger.error("Error during bundle installation of {}", new Object[]{file.getAbsoluteFile(), e});
            }
        }

        for (Bundle bundle : toStart) {
            try {
                bundle.start();
            } catch (BundleException e) {
                logger.error("Error during the starting of {}", new Object[]{bundle.getSymbolicName(),
                        e});
            }
        }
    }

    @Override
    public void onFileDelete(File file) {
        Bundle bundle;
        synchronized (this) {
            bundle = bundles.remove(file);
        }

        if (bundle != null) {
            try {
                logger.info("Uninstalling bundle {}", bundle.getSymbolicName());
                bundle.uninstall();
            } catch (BundleException e) {
                logger.error("Error during the un-installation of {}", new Object[]{bundle.getSymbolicName(),
                        e});
            }
        }
    }
}
