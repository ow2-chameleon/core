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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.ow2.chameleon.core.services.AbstractDeployer;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.utils.BundleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Deployer installing and starting bundles.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public class BundleDeployer extends AbstractDeployer implements BundleActivator {
    /**
     * The URL prefix to enable 'reference'.
     */
    public static final String REFERENCE_URL_PREFIX = "reference:";

    /**
     * Flag indicated whether we use the {@literal reference://} protocol.
     */
    private final boolean useReference;

    /**
     * The managed bundles.
     */
    Map<File, Bundle> bundles = new HashMap<File, Bundle>();

    /**
     * The bundle context.
     */
    private BundleContext context;

    /**
     * A logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BundleDeployer.class);

    /**
     * Creates a bundle deployer.
     *
     * @param useReferences a boolean.
     */
    public BundleDeployer(boolean useReferences) {
        this.useReference = useReferences;
    }

    /** {@inheritDoc} */
    @Override
    public void start(BundleContext context) {
        this.context = context;
        context.registerService(Deployer.class, this, null);
    }

    /** {@inheritDoc} */
    @Override
    public void stop(BundleContext context) {
        // The service will be withdrawn automatically.
    }

    /** {@inheritDoc} */
    @Override
    public boolean accept(File file) {
        // If the file does not exist anymore, isBundle returns false.
        return file.getName().endsWith(".jar") && (!file.isFile() || BundleHelper.isBundle(file));
    }

    /** {@inheritDoc} */
    @Override
    public void onFileCreate(File file) {
        LOGGER.debug("File creation event received for {}", file.getAbsoluteFile());

        synchronized (this) {
            if (bundles.containsKey(file)) {
                Bundle bundle = bundles.get(file);
                LOGGER.info("Updating bundle {} - {}", bundle.getSymbolicName(), file.getAbsoluteFile());
                try {
                    bundle.update();
                    // Then try to start other not started bundles.
                    tryToStartUnstartedBundles(bundle);
                    // If the bundle we just update is not started, try to start it.
                    // Obviously, this action is not done on fragment.
                    if (bundle.getState() != Bundle.ACTIVE  && ! BundleHelper.isFragment(bundle)) {
                        bundle.start();
                    }
                } catch (BundleException e) {
                    LOGGER.error("Error during bundle update {} from {}", bundle.getSymbolicName(),
                            file.getAbsoluteFile(), e);
                } catch (IllegalStateException e) {
                    LOGGER.error("Cannot update the bundle " + file.getAbsolutePath() + " - the framework is either " +
                            "stopping or restarting");
                    LOGGER.debug("Invalid bundle context", e);
                }
            } else {
                LOGGER.info("Installing bundle from {}", file.getAbsoluteFile());
                try {
                    Bundle bundle;
                    if (useReference) {
                        bundle = context.installBundle(REFERENCE_URL_PREFIX + file.toURI().toURL()
                                .toExternalForm());
                    } else {
                        bundle = context.installBundle(file.toURI().toURL().toExternalForm());
                    }
                    bundles.put(file, bundle);
                    if (!BundleHelper.isFragment(bundle)) {
                        LOGGER.info("Starting bundle {} - {}", bundle.getSymbolicName(), file.getAbsoluteFile());
                        bundle.start();
                    }
                    tryToStartUnstartedBundles(bundle);
                } catch (Exception e) {
                    LOGGER.error("Error during bundle installation of {}", file.getAbsoluteFile(), e);
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
                LOGGER.debug("Trying to start bundle {} after having installed bundle {}", b.getSymbolicName(),
                        bundle.getSymbolicName());
                try {
                    b.start();
                } catch (BundleException e) {
                    LOGGER.debug("Failed to start bundle {} after having installed bundle {}",
                            b.getSymbolicName(),
                            bundle.getSymbolicName(), e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * It's a good practice to install all bundles and then start them.
     * This method cannot be interrupted.
     */
    @Override
    public void open(Collection<File> files) {
        List<Bundle> toStart = new ArrayList<Bundle>();
        for (File file : files) {
            try {
                Bundle bundle;
                if (useReference) {
                    bundle = context.installBundle("reference:" + file.toURI().toURL()
                            .toExternalForm());
                } else {
                    bundle = context.installBundle(file.toURI().toURL().toExternalForm());
                }
                bundles.put(file, bundle);
                if (!BundleHelper.isFragment(bundle)) {
                    toStart.add(bundle);
                }
            } catch (Exception e) {
                LOGGER.error("Error during bundle installation of {}", file.getAbsoluteFile(), e);
            }
        }

        for (Bundle bundle : toStart) {
            try {
                bundle.start();
            } catch (BundleException e) {
                LOGGER.error("Error during the starting of {}", bundle.getSymbolicName(), e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onFileDelete(File file) {
        Bundle bundle;
        synchronized (this) {
            bundle = bundles.remove(file);
        }

        if (bundle != null) {
            try {
                LOGGER.info("Uninstalling bundle {}", bundle.getSymbolicName());
                bundle.uninstall();
            } catch (BundleException e) {
                LOGGER.error("Error during the un-installation of {}", bundle.getSymbolicName(), e);
            }
        }
    }
}
