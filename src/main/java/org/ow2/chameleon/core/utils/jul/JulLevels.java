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
package org.ow2.chameleon.core.utils.jul;

import ch.qos.logback.classic.Level;

/**
 * Java Util Logging / SLF4J Mapping:
 * <p>
 * <pre>
 *
 *    JUL Level     |   SLF4J Level
 *    ---------------------------------------
 *    ALL           |   ALL
 *    FINEST        |   TRACE
 *    FINER         |   TRACE
 *    FINE          |   DEBUG
 *    CONFIG        |   DEBUG
 *    INFO          |   INFO
 *    WARNING       |   WARN
 *    SEVERE        |   ERROR
 *    SEVERE        |   FATAL
 *    OFF           |   OFF
 * </pre>
 * As Chameleon is based on LogBack, this class relies on LogBack classes.
 */
public class JulLevels {

    private JulLevels() {
        // Avoid direct instantiation.
    }

    /**
     * Converts a JDK logging Level to a Log4j logging Level.
     *
     * @param level the JDK Level to convert.
     * @return converted Level.
     */
    public static Level toSlf4J(final java.util.logging.Level level) {
        switch (level.intValue()) {

            case Integer.MAX_VALUE: // OFF
                return Level.OFF;

            case Integer.MIN_VALUE: // ALL
            case 300: // FINEST
            case 400: // FINER
                // We use a Trace and not all All is not a valid level value, when logging message
                return Level.TRACE;

            case 500: // FINE
            case 700: // CONFIG
                return Level.DEBUG;

            case 800: // INFO
                return Level.INFO;

            case 900: // WARNING
                return Level.WARN;

            case 1000: // SEVERE
                return Level.ERROR;

            default: // unknown
                return Level.WARN;
        }
    }

    /**
     * Converts a SLF4J logging Level to a JDK logging Level.
     *
     * @param level SLF4J Level to convert.
     * @return converted Level.
     */
    public static java.util.logging.Level toJUL(final Level level) {

        switch (level.toInt()) {

            case Level.ALL_INT:
                return java.util.logging.Level.ALL;

            case Level.OFF_INT:
                return java.util.logging.Level.OFF;

            case Level.TRACE_INT:
                return java.util.logging.Level.FINEST;

            case Level.DEBUG_INT:
                return java.util.logging.Level.FINE;

            case Level.INFO_INT:
                return java.util.logging.Level.INFO;

            case Level.WARN_INT:
                return java.util.logging.Level.WARNING;

            case Level.ERROR_INT:
                return java.util.logging.Level.SEVERE;

            default: // unknown
                return java.util.logging.Level.WARNING;
        }
    }
}
