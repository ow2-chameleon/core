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

package org.ow2.chameleon.core.activators;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.io.File;

import static org.ow2.chameleon.core.utils.BundleHelper.isBundle;

/**
 * Core activator.
 * A bit different from the DirectoryBundleMonitor as it handles the interactive case.
 */
public class CoreActivator extends  DirectoryBundleMonitor {

    private final boolean interactive;

    /**
     * Creates the core activator.
     * @param directory the core directory
     * @param interactive flag enabling the interactive mode
     */
    public CoreActivator(File directory, boolean interactive) {
        super(directory);
        this.interactive = interactive;
    }

    /**
     * Filters and installs bundles
     * @param file the jar file
     * @param context the bundle context
     * @return the bundle object, {@literal null} if this file is excluded.
     */
    protected Bundle installAndGet(File file, BundleContext context) {
        if (isBundle(file)) {
            // First, check if this is the interactive-shell bundle and if it is excluded
            if (isInteractiveShell(file)  && ! interactive) {
                // Exclude the shell
                return null;
            }
            try {
                lock.lock();
                // Do we already have this file.
                if (bundles.get(file) != null) {
                    // Update bundle
                    try {
                        bundles.get(file).update();
                        return bundles.get(file);
                    } catch (BundleException e) {
                        logger.error("Cannot update bundle {}", file.getAbsolutePath(), e);
                        return null;
                    }
                } else {
                    try {
                        Bundle bundle = context.installBundle("reference:" + file.toURI().toURL()
                                .toExternalForm());
                        bundles.put(file, bundle);
                        logger.info("Bundle " + bundle.getSymbolicName() + " installed");
                        return bundle;
                    } catch (Exception e) {
                        logger.error("Cannot install bundle {}", file.getAbsoluteFile(), e);
                        return null;
                    }
                }
            } finally {
                lock.unlock();
            }
        } else {
            return null;
        }
    }

    private boolean isInteractiveShell(File file) {
        return file.getName().startsWith("shelbie-startup-console");
    }
}
