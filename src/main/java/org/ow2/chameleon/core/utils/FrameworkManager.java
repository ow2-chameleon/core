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

import org.ow2.chameleon.core.Constants;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Framework keeper.
 */
public class FrameworkManager {

    private final Framework framework;
    private final List<BundleActivator> activators = new ArrayList<BundleActivator>();
    private final Logger logger = LoggerFactory.getLogger(FrameworkManager.class);

    public FrameworkManager(Framework framework) {
        this.framework = framework;
    }

    public FrameworkManager(Map<String, String> configuration) throws Exception {
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
        // Register a bundle listener to detect shutdown triggered from inside the OSGi platform
        framework.getBundleContext().addFrameworkListener(new FrameworkListener() {

            @Override
            public void frameworkEvent(FrameworkEvent event) {
                logger.warn("Framework event : " + event.getType());
                if (event.getType() == FrameworkEvent.STOPPED) {
                    logger.warn("System bundle stopped from inside");
                    framework.getBundleContext().removeFrameworkListener(this);
                    try {
                        stop();
                    } catch (BundleException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        });

        framework.getBundleContext().addBundleListener(new BundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                    logger.warn("bundle event : " + event.getType() + " on " + event.getBundle().getSymbolicName());
            }
        });
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
     * @throws InterruptedException if the method is interupted during the
     *                              waiting time.
     */
    public void stop() throws BundleException, InterruptedException {
        if (framework.getState() == Bundle.ACTIVE) {
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
    }

    public void addActivators(List<BundleActivator> activators) {
        this.activators.addAll(activators);
    }
}
