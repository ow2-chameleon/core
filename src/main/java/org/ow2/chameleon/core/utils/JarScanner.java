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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the set of packages from a Jar file.
 */
public class JarScanner {

    /**
     * A regex that extract the version from a file name.
     * The group 1 contains the extracted version number without classifier or SNAPSHOT.
     */
    static final Pattern FUZZY_VERSION = Pattern.compile(".*-(\\d+)(\\.(\\d+)(\\.(\\d+))?)?([^a-zA-Z0-9](.*))?.jar",
            Pattern.DOTALL);

    /**
     * Guesses the version of the jar file based on naming rules.
     *
     * @param name the file name
     * @return the guessed version, {@code null} if it can't be guessed.
     */
    public static String version(String name) {
        StringBuilder result = new StringBuilder();
        Matcher m = FUZZY_VERSION.matcher(name);
        if (m.matches()) {
            String major = m.group(1);
            String minor = m.group(3);
            String micro = m.group(5);
            String qualifier = m.group(7);

            if (major != null) {
                result.append(major);
                if (minor != null) {
                    result.append(".");
                    result.append(minor);
                    if (micro != null) {
                        result.append(".");
                        result.append(micro);
                        if (qualifier != null) {
                            result.append(".");
                            cleanupModifier(result, qualifier);
                        }
                    } else if (qualifier != null) {
                        result.append(".0.");
                        cleanupModifier(result, qualifier);
                    } else {
                        result.append(".0");
                    }
                } else if (qualifier != null) {
                    result.append(".0.0.");
                    cleanupModifier(result, qualifier);
                } else {
                    result.append(".0.0");
                }
            }
        } else {
            // Does not match the file name syntax.
            return null;
        }
        return result.toString();
    }


    static void cleanupModifier(StringBuilder result, String modifier) {
        for (int i = 0; i < modifier.length(); i++) {
            char c = modifier.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || c == '-') { //NOSONAR
                result.append(c);
            } else {
                result.append('_');
            }
        }
    }

    /**
     * Builds the set of {@link org.ow2.chameleon.core.utils.Pckg} to export from the given jar file.
     * Notice that "META-INF" directory and sub-directories are not exported.
     *
     * @param jar     the jar file
     * @param version the version of the jar file. This version is used as 'exported version' for each packages.
     * @return the set of packages.
     */
    public static Set<Pckg> scan(JarFile jar, String version) {
        Enumeration<JarEntry> entries = jar.entries();

        Set<Pckg> packages = new HashSet<Pckg>();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && !entry.getName().startsWith("META-INF")) {
                final Pckg pckg = toPackage(entry, version);
                if (pckg != null) {
                    // We ignore the default-package.
                    packages.add(pckg);
                }
            }
        }

        return packages;
    }

    /**
     * Builds the set of {@link org.ow2.chameleon.core.utils.Pckg} to export from the given file (must be a jar file).
     * If the given file's name does not end with {@literal .jar}, {@code null} is returned.
     * <p>
     * This methods also try to guess the version of the jar file. If it can't be guesses, {@literal 0.0.0} is used.
     *
     * @param jarFile the file to scan
     * @return the set of packages to be exported
     * @throws IOException if the file cannot be read
     */
    public static Set<Pckg> scan(File jarFile) throws IOException {
        // Quick check: is it a jar file
        if (!jarFile.getName().endsWith(".jar")) {
            return null;
        }

        // Extract version
        String version = version(jarFile.getName());
        if (version == null) {
            version = "0.0.0";
        }

        JarFile jar = new JarFile(jarFile);

        return scan(jar, version);
    }

    /**
     * Transforms the give jar entry to it's java package name. The entry must be a file not a directory.
     *
     * @param entry   the entry
     * @param version the version
     * @return the package object
     */
    private static Pckg toPackage(JarEntry entry, String version) {
        String name = entry.getName();
        if (!name.contains("/")) {
            return null;
        }
        String dir = name.substring(0, name.lastIndexOf("/"));
        return new Pckg(dir.replace("/", "."), version);
    }

}
