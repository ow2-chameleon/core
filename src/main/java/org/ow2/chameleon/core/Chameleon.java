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

package org.ow2.chameleon.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;
import org.ow2.chameleon.core.activators.ConfigurationMonitor;
import org.ow2.chameleon.core.activators.CoreActivator;
import org.ow2.chameleon.core.activators.DirectoryBundleMonitor;
import org.ow2.chameleon.core.activators.LogActivator;
import org.ow2.chameleon.core.utils.FrameworkManager;
import org.ow2.chameleon.core.utils.LogbackUtil;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Chameleon main entry point.
 */
public class Chameleon {

    private final FrameworkManager manager;
    /**
     * Chameleon Logger.
     */
    private Logger logger;
    /**
     * List of activator to start during framework startup.
     */
    private List<BundleActivator> activators = new ArrayList<BundleActivator>();

    /**
     * Creates a Chameleon instance. This constructor does not allows to set the
     * core directory (so, uses 'core'), nor the chameleon properties.
     *
     * @param interactive is the debug mode enabled.
     * @throws Exception something wrong happens.
     */
    public Chameleon(boolean interactive)
            throws Exception {

        ChameleonConfiguration configuration = new ChameleonConfiguration();
        configuration.init();
        configuration.setSystemProperties();
        configuration.initFrameworkConfiguration();

        logger = initializeLoggingSystem(interactive);

        initializeActivatorList(configuration, interactive);

        manager = new FrameworkManager(configuration);
        manager.addActivators(activators);
    }

    /**
     * Initialized the logging framework (backend).
     *
     * @param interactive is the interactive mode enabled ?
     * @return the chameleon logger
     */
    public static Logger initializeLoggingSystem(boolean interactive) throws IOException {
        Logger log = LogbackUtil.configure(interactive);

        if (interactive) {
            log.debug("interactive mode enabled");
        }

        return log;
    }

    private void initializeActivatorList(ChameleonConfiguration configuration, boolean interactive) throws IOException {
        File core = configuration.getDirectory(Constants.CHAMELEON_CORE_PROPERTY, true);
        if (core == null) {
            throw new IllegalArgumentException("The " + Constants.CHAMELEON_CORE_PROPERTY + " property is missing in " +
                    "the " + Constants.CHAMELEON_PROPERTIES_FILE + " file.");
        }

        File runtime = configuration.getDirectory(Constants.CHAMELEON_RUNTIME_PROPERTY, true);
        if (runtime == null) {
            throw new IllegalArgumentException("The " + Constants.CHAMELEON_RUNTIME_PROPERTY + " property is missing in " +
                    "the " + Constants.CHAMELEON_PROPERTIES_FILE + " file.");
        }

        File application = configuration.getDirectory(Constants.CHAMELEON_APPLICATION_PROPERTY, true);
        if (application == null) {
            throw new IllegalArgumentException("The " + Constants.CHAMELEON_APPLICATION_PROPERTY + " property is missing in " +
                    "the " + Constants.CHAMELEON_PROPERTIES_FILE + " file.");
        }

        activators.add(new LogActivator(logger));
        activators.add(new CoreActivator(core, interactive));

        boolean monitoringRuntime = configuration.getBoolean(Constants.CHAMELEON_RUNTIME_MONITORING_PROPERTY, false);
        boolean monitoringApplication = configuration.getBoolean(Constants.CHAMELEON_APPLICATION_MONITORING_PROPERTY, true);
        int monitoringPeriod = configuration.getInt(Constants.CHAMELEON_MONITORING_PERIOD_PROPERTY, 2000);

        if (monitoringRuntime) {
            activators.add(new DirectoryBundleMonitor(runtime, monitoringPeriod));
            activators.add(new ConfigurationMonitor(runtime, monitoringPeriod));
        } else {
            activators.add(new DirectoryBundleMonitor(runtime));
            activators.add(new ConfigurationMonitor(runtime));
        }

        if (monitoringApplication) {
            activators.add(new DirectoryBundleMonitor(application, monitoringPeriod));
            activators.add(new ConfigurationMonitor(application, monitoringPeriod));
        } else {
            activators.add(new DirectoryBundleMonitor(application));
            activators.add(new ConfigurationMonitor(application));
        }
    }

    /**
     * Initializes and Starts the Chameleon frameworks. It configure the
     * embedded OSGi framework and deploys bundles
     *
     * @throws BundleException if a bundle cannot be installed or started
     *                         correctly.
     */
    public void start() throws BundleException {
        manager.start();
    }

    /**
     * Stops the underlying framework.
     *
     * @throws BundleException      should not happen.
     * @throws InterruptedException if the method is interrupted during the
     *                              waiting time.
     */
    public void stop() throws BundleException, InterruptedException {
        logger.info("Stopping Chameleon");
        manager.stop();
        logger.info("Chameleon stopped");
    }

}
