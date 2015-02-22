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

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.LogManager;

/**
 * A custom implementation of {@link java.util.logging.LogManager} delegating to SLF4J / LogBack.
 */
public class JulLogManager extends LogManager {

    /**
     * Creates the {@link org.ow2.chameleon.core.utils.jul.JulLogManager}.
     */
    public JulLogManager() {
        super();
    }

    /**
     * Method to find a named logger. Just create the wrapped logger.
     *
     * @param name name of the logger
     * @return a new wrapper logger.
     */
    @Override
    public java.util.logging.Logger getLogger(final String name) {
        return new JulWrapper(name);
    }

    /**
     * Does nothing as the configuration is delegated to LogBack.
     *
     * @throws IOException       cannot be thrown
     * @throws SecurityException cannot be thrown
     */
    @Override
    public void readConfiguration() throws IOException, SecurityException {
    }

    /**
     * Does nothing as the configuration is delegated to LogBack.
     *
     * @throws IOException       cannot be thrown
     * @throws SecurityException cannot be thrown
     */
    @Override
    public void readConfiguration(InputStream inputStream) throws IOException, SecurityException {
    }

    /**
     * Not supported.
     *
     * @param l the listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * Not supported.
     *
     * @param l the listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    /**
     * Return always {@code null} because we don't store properties.
     *
     * @param name the property's name
     * @return {@code null}
     */
    @Override
    public String getProperty(String name) {
        return null;
    }

    /**
     * Get an enumeration of known logger names.
     * <p>
     * Note:  Loggers may be added dynamically as new classes are loaded. This method only reports on the loggers
     * that are currently registered. It is also important to note that this method only returns the name of a
     * Logger, not a strong reference to the Logger itself.
     * <p>
     * This method asks LogBack to retrieve the list of known loggers. The names are retrieved when this method is
     * called, so loggers created after this call are not listed.
     *
     * @return enumeration of logger name strings
     */
    @Override
    public Enumeration<String> getLoggerNames() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<String> names = new ArrayList<String>();
        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            names.add(logger.getName());
        }
        final Iterator<String> iterator = names.iterator();

        return new Enumeration<String>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public String nextElement() {
                return iterator.next();
            }
        };
    }

    /**
     * Add a named logger.  This does nothing and returns false if a logger with the same name is already
     * registered.
     * <p>
     * The Logger factory methods call this method to register each newly created Logger.
     * <p>
     * This method does not store the logger, so just returns {@code true} if the logger did not already exist.
     *
     * @param logger the new logger.
     * @return true if the argument logger was registered successfully, false if a logger of that name already
     * exists.
     */
    @Override
    public boolean addLogger(final java.util.logging.Logger logger) {
        if (! (LoggerFactory.getILoggerFactory() instanceof  LoggerContext)) {
            // Not using LogBack, fail over
            return true;
        }
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (ch.qos.logback.classic.Logger l : context.getLoggerList()) {
            if (l.getName().equals(logger.getName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Not Supported.
     */
    @Override
    public void reset() {
        // Do nothing.
    }
}
