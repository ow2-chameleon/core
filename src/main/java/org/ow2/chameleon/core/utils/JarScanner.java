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
    private static final Pattern VERSION_EXTRACTOR =
            Pattern.compile(".*-([0-9]+(\\.([0-9]*)?(\\.([0-9]*))?)?)(-.*)?.jar");

    /**
     * Guesses the version of the jar file based on naming rules.
     *
     * @param name the file name
     * @return the guessed version, {@code null} if it can't be guessed.
     */
    public static String version(String name) {
        Matcher matcher = VERSION_EXTRACTOR.matcher(name);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
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
                packages.add(toPackage(entry, version));
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
        String dir = name.substring(0, name.lastIndexOf("/"));
        return new Pckg(dir.replace("/", "."), version);
    }

}
