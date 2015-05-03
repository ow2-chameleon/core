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
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.CoreConstants;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * A JUL Logger implementation delegating to a SLF4J Logger.
 */
public class JulWrapper extends java.util.logging.Logger {

    private static LoggerContext loggerContext;

    static {
        // Initialize the logging context.
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (factory instanceof LoggerContext) {
            loggerContext = (LoggerContext) factory;
        } else {
            loggerContext = new LoggerContext();
            loggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
        }
    }

    /**
     * The wrapped logger.
     */
    private final Logger logger; //NOSONAR ignore naming convention - it's a wrapped instance.

    /**
     * Creates JUL Wrapper for given name.
     *
     * @param name the logger name, can be a class name.
     */
    protected JulWrapper(final String name) {
        super(name, null);
        this.logger = loggerContext.getLogger(name);

        super.setLevel(JulLevels.toJUL(this.logger.getEffectiveLevel()));
    }

    /**
     * Logs a message.
     *
     * @param record the log record
     */
    @Override
    public void log(final java.util.logging.LogRecord record) {
        if (record == null || record.getMessage() == null) {
            return;
        }
        final Level level = JulLevels.toSlf4J(record.getLevel());
        if (this.logger.isEnabledFor(level)) {
            String message = record.getMessage();
            try {
                Object[] parameters = record.getParameters();
                if (parameters != null && parameters.length != 0) {
                    message = MessageFormat.format(message, parameters);
                }
            } catch (Exception ex) { // NOSONAR
                // ignore parameter error
            }
            this.logger.log(null, Logger.FQCN, Level.toLocationAwareLoggerInteger(level), message, null, record.getThrown());
        }
    }

    /**
     * Sets level of the logger. The given level is translated to SLF4J level.
     *
     * @param newLevel the level
     */
    @Override
    public void setLevel(final java.util.logging.Level newLevel) {
        logger.setLevel(JulLevels.toSlf4J(newLevel));
        super.setLevel(newLevel);
    }

    /**
     * Gets the current log level.
     *
     * @return the JUL level computed from the current wrapped logger level
     */
    @Override
    public java.util.logging.Level getLevel() {
        final Level level = this.logger.getLevel();
        // The level can be null.
        if (level == null) {
            return null;
        }
        return JulLevels.toJUL(level);
    }

    /**
     * Checks whether or not the given level is currently 'loggable'.
     *
     * @param level the level to check
     * @return {@code true} if the given level is higher of equal to the current log level, {@code false} otherwise
     */
    @Override
    public boolean isLoggable(final java.util.logging.Level level) {
        return this.logger.isEnabledFor(JulLevels.toSlf4J(level));
    }

    /**
     * Gets the logger name.
     *
     * @return the name of the logger
     */
    @Override
    public String getName() {
        return this.logger.getName();
    }

    /**
     * Logs a message.
     *
     * @param level the level
     * @param msg   the message
     */
    @Override
    public void log(java.util.logging.Level level, String msg) {
        Level logbackLevel = JulLevels.toSlf4J(level);
        this.logger.log(null, Logger.FQCN, Level.toLocationAwareLoggerInteger(logbackLevel), msg, null, null);
    }

    /**
     * Logs a message.
     * The final message (formatted) is only computed is the given level is higher or equal to the current log level.
     *
     * @param level  the level
     * @param msg    the message
     * @param param1 an object used to format the message.
     */
    @Override
    public void log(java.util.logging.Level level, String msg, Object param1) {
        Level logbackLevel = JulLevels.toSlf4J(level);

        if (this.logger.isEnabledFor(logbackLevel)) {
            String fmtMsg = MessageFormat.format(msg, param1);
            this.logger.log(null, Logger.FQCN, Level.toLocationAwareLoggerInteger(logbackLevel), fmtMsg, null, null);
        }
    }

    /**
     * Logs a message.
     * The final message (formatted) is only computed is the given level is higher or equal to the current log level.
     *
     * @param level  the level
     * @param msg    the message
     * @param params parameters used to format the message.
     */
    @Override
    public void log(java.util.logging.Level level, String msg, Object[] params) {
        Level logbackLevel = JulLevels.toSlf4J(level);

        if (this.logger.isEnabledFor(logbackLevel)) {
            String fmtMsg = MessageFormat.format(msg, params);
            this.logger.log(null, Logger.FQCN, Level.toLocationAwareLoggerInteger(logbackLevel), fmtMsg, null, null);
        }
    }

    /**
     * Logs a message.
     * The final message (formatted) is only computed is the given level is higher or equal to the current log level.
     *
     * @param level  the level
     * @param msg    the message
     * @param thrown the log cause
     */
    @Override
    public void log(java.util.logging.Level level, String msg, Throwable thrown) {
        Level logbackLevel = JulLevels.toSlf4J(level);

        this.logger.log(null, Logger.FQCN, Level.toLocationAwareLoggerInteger(logbackLevel), msg, null, thrown);
    }

    /**
     * Logs a message about a thrown exception.
     *
     * @param sourceClass  the source class
     * @param sourceMethod the source method
     * @param thrown       the exception
     */
    @Override
    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        this.logger.error(sourceClass + '.' + sourceMethod, thrown);
    }

    /**
     * Logs a 'severe' message. 'Severe' is mapped to 'Error'.
     *
     * @param msg the message
     */
    @Override
    public void severe(String msg) {
        this.logger.error(msg);
    }

    /**
     * Logs a 'warning' message.
     *
     * @param msg the message
     */
    @Override
    public void warning(String msg) {
        this.logger.warn(msg);
    }

    /**
     * Logs an 'info' message.
     *
     * @param msg the message
     */
    @Override
    public void info(String msg) {
        this.logger.info(msg);
    }

    /**
     * Logs a 'config' message. 'Config' is mapped to 'debug'.
     *
     * @param msg the message
     */
    @Override
    public void config(String msg) {
        this.logger.debug(msg);
    }

    /**
     * Logs a 'fine' message. 'Fine' is mapped to 'debug'.
     *
     * @param msg the message
     */
    @Override
    public void fine(String msg) {
        this.logger.debug(msg);
    }

    /**
     * Logs a 'finer' message. 'Finer' is mapped to 'trace'.
     *
     * @param msg the message
     */
    @Override
    public void finer(String msg) {
        this.logger.trace(msg);
    }

    /**
     * Logs a 'finest' message. 'Finest' is mapped to 'trace'.
     *
     * @param msg the message
     */
    @Override
    public void finest(String msg) {
        this.logger.trace(msg);
    }
}
