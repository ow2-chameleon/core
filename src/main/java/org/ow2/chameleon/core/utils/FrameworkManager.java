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
package org.ow2.chameleon.core.utils;

import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.ow2.chameleon.core.Chameleon;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.ow2.chameleon.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Framework keeper.
 * Responsible for creating the OSGi framework instance.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public class FrameworkManager {

    private final Framework framework;
    private final List<BundleActivator> activators = new ArrayList<BundleActivator>();
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkManager.class);
    private final ChameleonConfiguration configuration;
    private final Chameleon chameleon;

    /**
     * Creates the framework manager.
     *
     * @param chameleon     a {@link org.ow2.chameleon.core.Chameleon} object.
     * @param configuration a {@link org.ow2.chameleon.core.ChameleonConfiguration} object.
     * @throws java.io.IOException if any.
     */
    public FrameworkManager(Chameleon chameleon, ChameleonConfiguration configuration) throws IOException {
        this.configuration = configuration;
        this.chameleon = chameleon;
        framework = FrameworkUtil.create(configuration.getBaseDirectory(), configuration);
    }

    /**
     * Gets the chameleon configuration. Do not change the configuration once the framework is started.
     *
     * @return the configuration
     */
    public ChameleonConfiguration configuration() {
        return configuration;
    }

    /**
     * Gets the held framework instance.
     *
     * @return a {@link org.osgi.framework.launch.Framework} object.
     */
    public Framework get() {
        return framework;
    }

    /**
     * Initializes and Starts the Chameleon frameworks. It configure the
     * embedded OSGi framework and deploys bundles
     *
     * @return the Bundle Context.
     * @throws org.osgi.framework.BundleException if a bundle cannot be installed or started
     *                                            correctly.
     */
    public Framework start() throws BundleException {
        framework.init();

        if (configuration.isInteractiveModeEnabled()) {
            // The interactive mode is enabled, to avoid issue during the stopping sequence we listen for a specific
            // event
            try {
                framework.getBundleContext().addServiceListener(new ServiceListener() {
                    @Override
                    public void serviceChanged(ServiceEvent event) {
                        if (event.getType() == ServiceEvent.UNREGISTERING) {
                            LOGGER.warn("Shelbie system Service leaving - shutting down sequence detected");
                            try {
                                chameleon.stop();
                            } catch (Exception e) {
                                LOGGER.error("Error during the framework stopping process", e);
                            }
                        }
                    }
                }, "(" + org.osgi.framework.Constants.OBJECTCLASS + "=" + "org.ow2.shelbie.core.system.SystemService)");
            } catch (InvalidSyntaxException e) {
                LOGGER.error("LDAP Syntax error", e);
            }
        }

        framework.start();

        for (BundleActivator activator : activators) {
            try {
                activator.start(framework.getBundleContext());
            } catch (Exception e) {
                LOGGER.error("Cannot start internal activator : {}", activator, e);
                throw new BundleException("Cannot start internal activator : " + activator + " : " + e.getMessage(),
                        e);
            }
        }

        return framework;
    }

    /**
     * Stops the underlying framework.
     *
     * @throws java.lang.InterruptedException     if the method is interrupted during the
     *                                            waiting time.
     */
    public void stop() throws InterruptedException {
        // Stopping activators
        for (BundleActivator activator : activators) {
            try {
                activator.stop(framework.getBundleContext());
            } catch (Exception e) {
                LOGGER.error("Error during the stopping of {}", activator, e);
            }
        }
        try {
            framework.stop();
            framework.waitForStop(Constants.OSGI_STOP_TIMEOUT);
        } catch (BundleException e) { //NOSONAR
            LOGGER.error("Cannot stop the framework gracefully : " + e.getMessage());
        }
    }

    /**
     * Adds activators to the framework.
     *
     * @param activators the set of activators.
     */
    public void addActivators(List<BundleActivator> activators) {
        this.activators.addAll(activators);
    }
}
