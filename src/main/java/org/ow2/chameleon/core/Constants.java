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

/**
 * Constants.
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
    public static final int OSGI_STOP_TIMEOUT = 10000;

    /**
     * The default location of the logger configuration.
     */
    public static final String CHAMELEON_LOGGER_CONFIGURATION = "conf/logger.xml";

    public static final String CHAMELEON_CORE_PROPERTY = "chameleon.core";
    public static final String CHAMELEON_RUNTIME_PROPERTY = "chameleon.runtime";
    public static final String CHAMELEON_APPLICATION_PROPERTY = "chameleon.application";

    public static final String CHAMELEON_RUNTIME_MONITORING_PROPERTY = "chameleon.runtime.monitoring";
    public static final String CHAMELEON_APPLICATION_MONITORING_PROPERTY = "chameleon.application.monitoring";

    public static final String CHAMELEON_MONITORING_PERIOD_PROPERTY = "chameleon.monitoring.period";
}
