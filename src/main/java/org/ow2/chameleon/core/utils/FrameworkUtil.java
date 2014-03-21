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

import org.apache.commons.io.IOUtils;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Utility function to launch the underlying OSGi Framework.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public class FrameworkUtil {

    /**
     * Constant <code>FRAMEWORK_FACTORY="META-INF/services/org.osgi.framework.la"{trunked}</code>
     */
    public static final String FRAMEWORK_FACTORY = "META-INF/services/org.osgi.framework.launch.FrameworkFactory";

    private FrameworkUtil() {
        // Avoid direct instantiation
    }

    /**
     * Simple method to parse META-INF/services file for framework factory.
     * Currently, it assumes the first non-commented line is the class name of
     * the framework factory implementation.
     *
     * @return The created <tt>FrameworkFactory</tt> instance.
     * @throws java.lang.ClassNotFoundException if the framework factory class cannot be loaded.
     * @throws java.lang.IllegalAccessException if the framework factory instance cannot be created.
     * @throws java.lang.InstantiationException if the framework factory instance cannot be created.
     * @throws java.io.IOException              if the service file cannot be read
     */
    public static FrameworkFactory getFrameworkFactory() throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, IOException {
        URL url = FrameworkUtil.class.getClassLoader().getResource(
                FRAMEWORK_FACTORY);
        if (url != null) {
            InputStream stream = null;
            try {
                stream = url.openStream();
                String content = read(stream);
                if (content == null) {
                    throw new IOException("Could not read the framework factory service file (" +
                            FRAMEWORK_FACTORY + "), or the file is empty");
                }
                return (FrameworkFactory) Class.forName(content).newInstance();
            } finally {
                // The stream should have been closed already, but just in case we should ensure it is closed.
                IOUtils.closeQuietly(stream);
            }
        } else {
            throw new IOException("Cannot find the framework factory service file (" +
                    FRAMEWORK_FACTORY + "), check that you have an OSGi implementation in your classpath.");
        }
    }


    /**
     * <p>create.</p>
     *
     * @param configuration a {@link java.util.Map} object.
     * @return a {@link org.osgi.framework.launch.Framework} object.
     * @throws java.io.IOException if any.
     */
    public static Framework create(Map<String, String> configuration) throws IOException {
        try {
            return getFrameworkFactory().newFramework(configuration);
        } catch (ClassNotFoundException e) {
            throw new IOException("Cannot load the OSGi framework", e);
        } catch (IllegalAccessException e) {
            throw new IOException("Cannot initialize the OSGi framework", e);
        } catch (InstantiationException e) {
            throw new IOException("Cannot instantiate the OSGi framework", e);
        }
    }

    /**
     * <p>read.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public static String read(InputStream stream) throws IOException {
        // Fast null check
        if (stream == null) {
            return null;
        }
        try {
            List<String> lines = IOUtils.readLines(stream);
            for (String line : lines) {
                String l = line.trim();
                // Skip empty and comment lines
                if (!l.isEmpty() && !l.startsWith("#")) {
                    return l;
                }
            }
            return null;
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }


}