/*
 * Copyright 2013 OW2 Chameleon
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ow2.chameleon.core.utils;

import org.ow2.chameleon.core.Chameleon;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.ow2.chameleon.core.Constants;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Framework keeper.
 */
public class FrameworkManager {

    private final Framework framework;
    private final List<BundleActivator> activators = new ArrayList<BundleActivator>();
    private final Logger logger = LoggerFactory.getLogger(FrameworkManager.class);
    private final ChameleonConfiguration configuration;
    private final Chameleon chameleon;

    public FrameworkManager(Chameleon chameleon, ChameleonConfiguration configuration) throws Exception {
        this.configuration = configuration;
        this.chameleon = chameleon;
        framework = FrameworkUtil.create(configuration);
    }

    public void addActivators(BundleActivator... activators) {
        Collections.addAll(this.activators, activators);
    }

    public Framework get() {
        return framework;
    }

    /**
     * Initializes and Starts the Chameleon frameworks. It configure the
     * embedded OSGi framework and deploys bundles
     *
     * @return the Bundle Context.
     * @throws BundleException if a bundle cannot be installed or started
     *                         correctly.
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
                            logger.warn("Shelbie system Service unregistering - shutting down sequence detected");
                            try {
                                chameleon.stop();
                            } catch (Exception e) {
                                logger.error("Error during the framework stopping process", e);
                            }
                        }
                    }
                }, "(" + org.osgi.framework.Constants.OBJECTCLASS + "=" + "org.ow2.shelbie.core.system.SystemService)");
            } catch (InvalidSyntaxException e) {
                logger.error("LDAP Syntax error", e);
            }
        }

        framework.start();

        for (BundleActivator activator : activators) {
            try {
                activator.start(framework.getBundleContext());
            } catch (Exception e) {
                logger.error("Cannot start internal activator : {}", activator, e);
                throw new BundleException("Cannot start internal activator : " + activator + " : " + e.getMessage(),
                        e);
            }
        }

        return framework;
    }

    /**
     * Stops the underlying framework.
     *
     * @throws BundleException      should not happen.
     * @throws InterruptedException if the method is interrupted during the
     *                              waiting time.
     */
    public void stop() throws BundleException, InterruptedException {
        // Stopping activators
        for (BundleActivator activator : activators) {
            try {
                activator.stop(framework.getBundleContext());
            } catch (Exception e) {
                logger.error("Error during the stopping of {}", activator, e);
            }
        }
        framework.stop();
        framework.waitForStop(Constants.OSGI_STOP_TIMEOUT);
    }

    public void addActivators(List<BundleActivator> activators) {
        this.activators.addAll(activators);
    }
}
