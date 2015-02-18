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
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Utility function on bundles.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public final class BundleHelper {

    /**
     * The constant MANIFEST.MF path.
     */
    public static final String MANIFEST = "META-INF/MANIFEST.MF";

    private BundleHelper() {
        // Avoid direct instantiation
    }

    /**
     * Checks whether the given file is a bundle or not.
     * The check is based on the {@literal Bundle-ManifestVersion} header.
     * If the file is a directory this method checks if the directory is an exploded bundle.
     * If the file is a jar file, it checks the manifest.
     *
     * @param file the file.
     * @return {@literal true} if it's a bundle, {@literal false} otherwise.
     */
    public static boolean isBundle(File file) {

        if (file.isFile() && file.getName().endsWith(".jar")) {
            JarFile jar = null;
            try {
                jar = new JarFile(file);
                return jar.getManifest() != null && jar.getManifest().getMainAttributes() != null
                        && jar.getManifest().getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME) != null;
                // We check the symbolic name because it's the only mandatory header
                // see http://wiki.osgi.org/wiki/Bundle-SymbolicName.
            } catch (IOException e) {
                LoggerFactory.getLogger(BundleHelper.class).error("Cannot check if the file {} is a bundle, " +
                        "cannot open it", file.getName(), e);
                return false;
            } finally {
                final JarFile finalJar = jar;
                IOUtils.closeQuietly(new Closeable() {
                    @Override
                    public void close() throws IOException {
                        if (finalJar != null) {
                            finalJar.close();
                        }
                    }
                });
            }
        }

        return isExplodedBundle(file);

    }

    private static boolean isExplodedBundle(File directory) {
        if (!directory.isDirectory()) {
            return false;
        }
        File manifestFile = new File(directory, MANIFEST);
        if (!manifestFile.exists()) {
            return false;
        }

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(manifestFile);
            Manifest manifest = new Manifest(stream);
            return manifest.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME) != null;
        } catch (IOException e) {
            LoggerFactory.getLogger(BundleHelper.class).error("Cannot check if the directory {} is a bundle, " +
                    "cannot read the manifest file", directory.getName(), e);
            return false;
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Checks if a bundle is a fragment.
     * It checks if the manifest contains the fragment
     * host header.
     *
     * @param bundle the bundle to check
     * @return true if the bundle is a fragment.
     */
    public static boolean isFragment(Bundle bundle) {
        Dictionary<String, String> headers = bundle.getHeaders();
        return headers.get(Constants.FRAGMENT_HOST) != null;
    }

    /**
     * Un-registers the service. It ignores all exception that can happen while unregistering the service.
     *
     * @param registration the registration
     */
    public static void unregisterQuietly(ServiceRegistration registration) {
        if (registration != null) {
            try {
                registration.unregister();
            } catch (Exception e) { //NOSONAR it's the goal of this method to hide these exceptions
                // Ignore it
            }
        }
    }

}
