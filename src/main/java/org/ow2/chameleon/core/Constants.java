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
package org.ow2.chameleon.core;

/**
 * Constants.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public class Constants {


    /**
     * The default chameleon properties file.
     */
    public static final String CHAMELEON_PROPERTIES_FILE = "conf/chameleon.properties";
    /**
     * The default system properties file.
     */
    public static final String SYSTEM_PROPERTIES_FILE = "conf/system.properties";

    /**
     * The chameleon logger name.
     */
    public static final String CHAMELEON_LOGGER_NAME = "org.ow2.chameleon";

    /**
     * OSGi stop timeout.
     */
    public static final int OSGI_STOP_TIMEOUT = 1000;

    /**
     * The default location of the logger configuration.
     */
    public static final String CHAMELEON_LOGGER_CONFIGURATION = "conf/logger.xml";

    /**
     * The property used to configure the core directory location.
     */
    public static final String CHAMELEON_CORE_PROPERTY = "chameleon.core";
    /**
     * The property used to configure the runtime directory location.
     */
    public static final String CHAMELEON_RUNTIME_PROPERTY = "chameleon.runtime";
    /**
     * The property used to configure the application directory location.
     */
    public static final String CHAMELEON_APPLICATION_PROPERTY = "chameleon.application";

    /**
     * The property used to enable / disable the monitoring of the runtime directory.
     * Monitoring means that new files will be handled, like a watch mode.
     * The monitoring of the runtime directory is disabled by default.
     */
    public static final String CHAMELEON_RUNTIME_MONITORING_PROPERTY = "chameleon.runtime.monitoring";

    /**
     * The property used to enable / disable the monitoring of the application directory.
     * Monitoring means that new files will be handled, like a watch mode.
     * The monitoring of the application directory is enabled by default.
     */
    public static final String CHAMELEON_APPLICATION_MONITORING_PROPERTY = "chameleon.application.monitoring";

    /**
     * The property used to configure the periodic polling to detect changed in monitored directories.
     */
    public static final String CHAMELEON_MONITORING_PERIOD_PROPERTY = "chameleon.monitoring.period";

    /**
     * The property used to configure the auto-refresh of bundles after un-installations and updates.
     */
    public static final String CHAMELEON_AUTO_REFRESH = "chameleon.auto.refresh";

    private Constants() {
        // Avoid direct instantiation
    }
}
