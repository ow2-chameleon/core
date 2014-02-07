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

import org.apache.commons.io.IOUtils;
import org.ow2.chameleon.core.utils.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manages properties
 */
public class ChameleonConfiguration extends HashMap<String, String> {

    private static final String CONFIGURATION_ADMIN_PACKAGE_VERSION = "1.5.0";
    private static final String LOG_SERVICE_PACKAGE_VERSION = "1.3.0";
    private static final String SLF4J_PACKAGE_VERSION = "1.7.5";

    private final File baseDirectory;
    private boolean interactiveModeEnabled;

    public ChameleonConfiguration(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public void initialize(Map<String, Object> userProperties) throws IOException {
        loadChameleonProperties();
        loadSystemProperties();
        loadSystemPropertiesSpecifiedByTheUser(userProperties);
    }

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
                if (k.endsWith("extra")  && containsKey(k)) {
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

    public String get(String key, String defaultValue) {
        if (containsKey(key)) {
            return get(key);
        } else {
            return defaultValue;
        }
    }

    public File getDirectory(String key, boolean create) {
        String path = get(key);
        if (path == null) {
            return null;
        }
        File dir = new File(baseDirectory.getAbsoluteFile(), path);
        if (create  && ! dir.isDirectory()) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                return null;
            }
        }
        return dir;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return Boolean.valueOf(value);
        }
    }

    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        } else {
            return Integer.valueOf(value);
        }
    }

    public File getFile(String key, boolean create) {
        String path = get(key);
        File file = new File(baseDirectory.getAbsoluteFile(), path);
        if (create  && ! file.isFile()) {
            try {
                if (file.getParentFile().mkdirs()  && file.createNewFile()) {
                    return file;
                } else {
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }
        return file;
    }

    public void initFrameworkConfiguration() {
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
            put("org.osgi.framework.system.packages.extra", getPackagesExportedByFramework());
        } else {
            String pcks = get(
                    "org.osgi.framework.system.packages.extra");
            put("org.osgi.framework.system.packages.extra",
                    getPackagesExportedByFramework() + "," + pcks);
        }
    }

    private static String getPackagesExportedByFramework() {
        return
                "org.osgi.service.cm; version=" + CONFIGURATION_ADMIN_PACKAGE_VERSION + "," +
                        "org.osgi.service.log; version=" + LOG_SERVICE_PACKAGE_VERSION + "," +
                        "org.slf4j; version=" + SLF4J_PACKAGE_VERSION + "," +
                        "org.slf4j.impl; version=" + SLF4J_PACKAGE_VERSION + "," +
                        "org.slf4j.spi; version=" + SLF4J_PACKAGE_VERSION + "," +
                        "org.slf4j.helpers; version=" + SLF4J_PACKAGE_VERSION + "," +
                        "org.ow2.chameleon.core.services, org.ow2.chameleon.core.activators";
    }

    /**
     * Loads system properties.
     *
     * @throws IOException if the system.properties file cannot be read.
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

    public File getBaseDirectory() {
        return baseDirectory.getAbsoluteFile();
    }

    public boolean isInteractiveModeEnabled() {
        return interactiveModeEnabled;
    }

    public void setInteractiveModeEnabled(boolean interactiveModeEnabled) {
        this.interactiveModeEnabled = interactiveModeEnabled;
    }

    public File getRelativeFile(String path) {
        return new File(baseDirectory.getAbsoluteFile(), path);
    }

    public void loadSystemPropertiesSpecifiedByTheUser(Map<String, Object> userProperties) {
        if (userProperties == null) {
            return;
        }

        for (Map.Entry<String, Object> p : userProperties.entrySet()) {
            System.setProperty(p.getKey(), p.getValue().toString());
        }
    }
}

