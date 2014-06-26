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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ow2.chameleon.core.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manages the configuration of the OSGi framework and of the chameleon container and services.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public class ChameleonConfiguration extends HashMap<String, String> {

    /**
     * The version of the configuration admin package.
     */
    private static final String CONFIGURATION_ADMIN_PACKAGE_VERSION = "1.5.0";

    /**
     * The version of the log service package.
     */
    private static final String LOG_SERVICE_PACKAGE_VERSION = "1.3.0";

    /**
     * The version of the slf4j api package.
     */
    private static final String SLF4J_PACKAGE_VERSION = "1.7.7";

    /**
     * The version of the logback classic api package.
     */
    private static final String LOGBACK_PACKAGE_VERSION = "1.1.2";

    private final File baseDirectory;
    private boolean interactiveModeEnabled;

    /**
     * Constructor for ChameleonConfiguration.
     *
     * @param baseDirectory a {@link java.io.File} object.
     */
    public ChameleonConfiguration(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Initializes the configuration.
     *
     * @param userProperties a {@link java.util.Map} object.
     * @throws java.io.IOException if any.
     */
    public void initialize(Map<String, Object> userProperties) throws IOException {
        loadChameleonProperties();
        loadSystemProperties();
        loadSystemPropertiesSpecifiedByTheUser(userProperties);
    }

    /**
     * Loads the chameleon properties file.
     *
     * @throws java.io.IOException if any.
     */
    public void loadChameleonProperties() throws IOException {
        FileInputStream stream = null;
        try {
            File file = new File(baseDirectory.getAbsoluteFile(), Constants.CHAMELEON_PROPERTIES_FILE);
            Properties ps = new Properties();

            // Load system properties first.
            ps.putAll(System.getProperties());

            // If the properties file exist, loads it.
            if (file.isFile()) {
                stream = new FileInputStream(file);
                ps.load(stream);
            }

            // Apply substitution
            Enumeration keys = ps.keys();
            while (keys.hasMoreElements()) {
                String k = (String) keys.nextElement();
                String v = (String) ps.get(k);
                v = StringUtils.substVars(v, k, null, ps);
                if (k.endsWith("extra") && containsKey(k)) {
                    // Append
                    put(k, get(k) + "," + v);
                } else {
                    // Replace
                    put(k, v);
                }

            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Retrieves the value from the configuration.
     *
     * @param key          the key
     * @param defaultValue the default value if the key is not present in the configuration
     * @return the stored value or the default value if none
     */
    public String get(String key, String defaultValue) {
        if (containsKey(key)) {
            return get(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Retrieves a directory from the configuration. A file object build from the stored value and relative from the
     * base directory is returned. If the 'create' flag is enabled, the directory is created.
     *
     * @param key    the key associated to the directory name
     * @param create create the directory if it does not exist
     * @return the file object
     */
    public File getDirectory(String key, boolean create) {
        String path = get(key);
        if (path == null) {
            return null;
        }
        File dir = new File(baseDirectory.getAbsoluteFile(), path);
        if (create && !dir.isDirectory()) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create directory " + path, e);
            }
        }
        return dir;
    }

    /**
     * Retrieves the value from the configuration.
     *
     * @param key          the key
     * @param defaultValue the default value if the key is not present in the configuration
     * @return the stored value or the default value if none
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return Boolean.valueOf(value);
        }
    }

    /**
     * Retrieves the value from the configuration.
     *
     * @param key          the key
     * @param defaultValue the default value if the key is not present in the configuration
     * @return the stored value or the default value if none
     */
    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(value);
        }
    }

    /**
     * Retrieves a file from the configuration. A file object build from the stored value and relative from the
     * base directory is returned. If the 'create' flag is enabled, the file is created.
     *
     * @param key    the key associated to the file name
     * @param create create the file if it does not exist
     * @return the file object
     */
    public File getFile(String key, boolean create) {
        String path = get(key);
        File file = new File(baseDirectory.getAbsoluteFile(), path);
        if (create && !file.isFile()) {
            try {
                FileUtils.forceMkdir(file.getParentFile());
                if (file.createNewFile()) {
                    return file;
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create file " + path, e);
            }
        }
        return file;
    }

    /**
     * Initializes the framework configuration.
     */
    public void initFrameworkConfiguration() {
        // By default we clean the cache.
        if (!containsKey("org.osgi.framework.storage.clean")) {
            put("org.osgi.framework.storage.clean", "onFirstInit");
        }

        if (!containsKey("ipojo.log.level")) {
            put("ipojo.log.level", "WARNING");
        }

        if (!containsKey("org.osgi.framework.storage")) {
            put("org.osgi.framework.storage", baseDirectory.getAbsolutePath() + "/chameleon-cache");
        }

        if (!containsKey("org.osgi.framework.system.packages.extra")) {
            // If not set, we use the regular exported packages.
            put("org.osgi.framework.system.packages.extra", getPackagesExportedByFramework());
        } else {
            // Else we append the regular packages to the given list
            // It may contain duplicates.
            String pcks = get(
                    "org.osgi.framework.system.packages.extra");
            put("org.osgi.framework.system.packages.extra",
                    getPackagesExportedByFramework() + "," + pcks);
        }
    }

    /**
     * Gets the packages exported by the frameworks.
     *
     * @return the export package clause
     */
    private static String getPackagesExportedByFramework() {
        return
                "org.osgi.service.cm; version=" + CONFIGURATION_ADMIN_PACKAGE_VERSION + "," +
                        "org.osgi.service.log; version=" + LOG_SERVICE_PACKAGE_VERSION + "," +
                        "org.slf4j; version=" + SLF4J_PACKAGE_VERSION + "," +
                        "org.slf4j.impl; version=" + SLF4J_PACKAGE_VERSION + "," +
                        "org.slf4j.spi; version=" + SLF4J_PACKAGE_VERSION + "," +
                        "org.slf4j.helpers; version=" + SLF4J_PACKAGE_VERSION + "," +
                        "ch.qos.logback.classic; version=" + LOGBACK_PACKAGE_VERSION + "," +
                        "org.ow2.chameleon.core.services, org.ow2.chameleon.core.activators";
    }

    /**
     * Loads system properties.
     *
     * @throws java.io.IOException if the system.properties file cannot be read.
     */
    public void loadSystemProperties() throws IOException {
        InputStream stream = null;
        try {
            File file = new File(baseDirectory.getAbsolutePath(), Constants.SYSTEM_PROPERTIES_FILE);
            Properties ps = new Properties();

            if (file.isFile()) {
                stream = new FileInputStream(file);
                ps.load(stream);
                Enumeration e = ps.propertyNames();
                while (e.hasMoreElements()) {
                    String k = (String) e.nextElement();
                    String v = StringUtils.substVars((String) ps.get(k), k,
                            null, System.getProperties());
                    System.setProperty(k, v);
                }
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Gets the base directory.
     *
     * @return the base directory
     */
    public File getBaseDirectory() {
        return baseDirectory.getAbsoluteFile();
    }

    /**
     * Is the interactive mode enabled?
     *
     * @return {@literal true} if the interactive mode is enabled, {@literal false} otherwise
     */
    public boolean isInteractiveModeEnabled() {
        return interactiveModeEnabled;
    }

    /**
     * enables/ disables the interactive mode.
     *
     * @param interactiveModeEnabled whether or not the interactive mode has to be enabled.
     */
    public void setInteractiveModeEnabled(boolean interactiveModeEnabled) {
        this.interactiveModeEnabled = interactiveModeEnabled;
    }

    /**
     * Gets a relative file from the the base directory.
     *
     * @param path the path to the file.
     * @return the file object
     */
    public File getRelativeFile(String path) {
        return new File(baseDirectory.getAbsoluteFile(), path);
    }

    /**
     * Loads the system properties specified by the user.
     *
     * @param userProperties the properties set by the user from the command line.
     */
    public void loadSystemPropertiesSpecifiedByTheUser(Map<String, Object> userProperties) {
        if (userProperties == null) {
            return;
        }

        for (Map.Entry<String, Object> p : userProperties.entrySet()) {
            System.setProperty(p.getKey(), p.getValue().toString());
        }
    }
}

