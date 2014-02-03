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

package org.ow2.chameleon.core.utils;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Utility function on bundles
 */
public class BundleHelper {

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
            try {
                JarFile jar = new JarFile(file);
                return jar.getManifest() != null && jar.getManifest().getMainAttributes() != null
                        && jar.getManifest().getMainAttributes().getValue("Bundle-ManifestVersion") != null;
            } catch (IOException e) {
                return false;
            }
        }

        if (file.isDirectory()) {
            File manifestFile = new File(file, "META-INF/MANIFEST.MF");
            if (!manifestFile.exists()) {
                return false;
            }

            FileInputStream stream = null;
            try {
                stream = new FileInputStream(manifestFile);
                Manifest manifest = new Manifest(stream);
                return manifest.getMainAttributes().getValue("Bundle-ManifestVersion") != null;
            } catch (IOException e) {
                return false;
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }

        return false;
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

    private BundleHelper() {
        // Avoid direct instantiation
    }

}
