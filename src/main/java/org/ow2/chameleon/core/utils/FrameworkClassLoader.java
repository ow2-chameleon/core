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

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ow2.chameleon.core.ChameleonConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The classloader responsible to load the framework classes and providing access to the classes and resources from
 * the Jar's located in the 'libs' directory. The 'libs' directory contain non-bundle jars that are scanned and
 * 'exported' by the framework. This feature only work for Apache Felix (no Eclipse Equinox is not supported).
 * <p>
 * The Framework is loaded in a different classloader to access the jar contained in the 'libs' directory.
 * Notice that the framework jar must be in the Chameleon's classpath.
 */
public final class FrameworkClassLoader extends URLClassLoader {

    /**
     * The set of packages that need to be defined by the classloader (and not loaded by the parent).
     */
    private static List<String> PREFIXES = ImmutableList.of(
            "org.apache.felix.framework"
    );

    /**
     * The set of defined classes.
     */
    private Map<String, Class> classes = new HashMap<String, Class>();

    /**
     * A classloader limited to the jars contained in the 'libs' directory. This classloader is used to ensure the
     * resolution order: 1) the jars file from the 'libs' directory and 2) the parent class loader (classpath).
     *
     * The parent of this classloader can be configured using the {@code chameleon.libraries.parent} property.
     */
    protected final URLClassLoader libsClassLoader;

    /**
     * Gets an instance of {@link org.ow2.chameleon.core.utils.FrameworkClassLoader}.
     *
     * @param basedir the base directory. The 'libs' folder must be a direct child of this directory.
     * @return the instance of classloader.
     */
    public static ClassLoader getFrameworkClassLoader(final File basedir) {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return new FrameworkClassLoader(basedir, null);
            }
        });
    }

    /**
     * Gets an instance of {@link org.ow2.chameleon.core.utils.FrameworkClassLoader}.
     *
     * @param basedir       the base directory. The 'libs' folder must be a direct child of this directory.
     * @param configuration the Chameleon configuration.
     * @return the instance of classloader.
     */
    public static ClassLoader getFrameworkClassLoader(final File basedir, final Map<String, String> configuration) {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                if (configuration != null) {
                    return new FrameworkClassLoader(basedir, configuration.get("chameleon.libraries.parent"));
                } else {
                    return new FrameworkClassLoader(basedir, null);
                }
            }
        });
    }

    /**
     * Creates the classloader.
     * <p>
     * All jars from the 'libs' directory are added to the classloader.
     *
     * @param basedir               the base directory. The 'libs' folder must be a direct child of this directory.
     * @param librariesParentPolicy the delegation policy for the library classloader
     */
    private FrameworkClassLoader(File basedir, String librariesParentPolicy) {
        super(jars(new File(basedir.getAbsoluteFile(), "libs")), FrameworkClassLoader.class.getClassLoader());

        ClassLoader parent = null;
        if (librariesParentPolicy == null || "system".equalsIgnoreCase(librariesParentPolicy)) {
            parent = null;
        } else if ("application".equalsIgnoreCase(librariesParentPolicy)) {
            parent = FrameworkClassLoader.class.getClassLoader();
        } else if ("parent".equalsIgnoreCase(librariesParentPolicy)) {
            parent = FrameworkClassLoader.class.getClassLoader().getParent();
        } else {
            throw new IllegalArgumentException("Unrecognized 'chameleon.libraries.parent', are" +
                    " supported: {system, application and parent}");
        }

        libsClassLoader = new URLClassLoader(jars(new File(basedir.getAbsoluteFile(), "libs")),
                parent);
    }


    /**
     * Checks whether or not the given class name must be defined by the {@link org.ow2.chameleon.core.utils
     * .FrameworkClassLoader} or by the parent class loader.
     * <p>
     * It checks if the class is in one of the packages listed in {@link org.ow2.chameleon.core.utils
     * .FrameworkClassLoader#PREFIXES}.
     *
     * @param name the class name
     * @return {@code true} if the class must be defined by the framework's classloader, {@code false} otherwise.
     */
    public static boolean hasToBeDefined(String name) {
        for (String p : PREFIXES) {
            if (name.startsWith(p) || name.equals(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads a class.
     * If the class needs to be defined by this classloader, the byte code of the class is retrieved from the
     * classpath and defined. Otherwise, the parent classloader is used.
     * <p>
     * If the byte code of the class cannot be found, it delegates to the parent.
     * <p>
     * Defined classes are stored, and not redefined.
     *
     * @param name the class name
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        if (classes.containsKey(name)) {
            return classes.get(name);
        }

        if (hasToBeDefined(name)) {
            try {
                byte[] bytes = getByteCode(name);
                if (bytes.length != 0) {
                    Class c = defineClass(name, bytes, 0, bytes.length, FrameworkClassLoader.class.getProtectionDomain());
                    classes.put(name, c);
                    return c;
                }
            } catch (IOException e) { //NOSONAR
                // Do nothing, we are going to try with the parent classloader.
            }
        }

        // We need to ensure that classes are loaded from libs first, to avoid conflicts with the classpath.
        try {
            return libsClassLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            // Cannot be found with the lib classloader, just delegate to parent.
            // Yes, the same classes will be analyzed than the previous attempt, but in the right order.
            // Indeed, the url class loader delegates to the parent first, and then checks its own content.
            return super.loadClass(name);
        }
    }


    /**
     * Gets the byte code of the given class. The '.class' file is loaded using the parent classloader.
     *
     * @param classname the class name
     * @return the byte array containing the byte code. An empty array is returned if the '.class' file cannot be found.
     * @throws IOException if an error happens while reading the '.class' file
     */
    private byte[] getByteCode(String classname) throws IOException {
        URL url = this.getResource(classname.replace(".", "/") + ".class");
        if (url == null) {
            return new byte[0];
        }
        return IOUtils.toByteArray(url);
    }

    /**
     * Builds a array of URLs containing all the jar file from the given directory. If there are no jar files in this
     * directory or if the directory does not exist, an empty array is returned. The lookup is recursive.
     *
     * @param dir the directory
     * @return the array of urls.
     */
    public static URL[] jars(File dir) {
        List<URL> urls = new ArrayList<URL>();
        if (dir.isDirectory()) {
            for (File jar : FileUtils.listFiles(dir, new String[]{"jar"}, true)) {
                try {
                    urls.add(jar.toURI().toURL());
                } catch (MalformedURLException e) { //NOSONAR
                    // Cannot happen.
                }
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }

}
