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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.ow2.chameleon.core.Chameleon;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.ow2.chameleon.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets the root logger configured from the default logger configuration file.
 */
public class LogbackUtil {
    /**
     * Loads logback configuration and gets the root logger.
     * @param configuration the chameleon's configuration
     * @return the logger.
     */
    public static Logger configure(ChameleonConfiguration configuration) {
        Object obj = LoggerFactory.getILoggerFactory();
        if (!(obj instanceof LoggerContext)) {
            // We are not using the logback backend, exit.
            return LoggerFactory.getLogger(Constants.CHAMELEON_LOGGER_NAME);
        }

        // assume SLF4J is bound to logback in the current environment
        LoggerContext context = (LoggerContext) obj;
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            context.reset();
            configurator.doConfigure(configuration.getRelativeFile(Constants.CHAMELEON_LOGGER_CONFIGURATION));
            ch.qos.logback.classic.Logger logger = context.getLogger(Constants.CHAMELEON_LOGGER_NAME);
            if (configuration.isInteractiveModeEnabled()) {
                // Set level if we are in interactive mode.
                logger.setLevel(Level.DEBUG);
            }
            return logger;
        } catch (JoranException je) {
            Logger log = LoggerFactory.getLogger(Constants.CHAMELEON_LOGGER_NAME);
            log.error("Cannot configure the logging system from {}", Constants.CHAMELEON_LOGGER_CONFIGURATION, je);
            return log;
        }
    }

}
