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

import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.ow2.chameleon.core.utils.BundleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Core activator.
 * A bit different from the DirectoryBundleMonitor as it handles the interactive case.
 */
public class CoreActivator implements BundleActivator {

    private final boolean interactive;
    private final File directory;
    private final Logger logger;
    private BundleContext context;

    /**
     * Creates the core activator.
     *
     * @param directory   the core directory
     * @param interactive flag enabling the interactive mode
     */
    public CoreActivator(File directory, boolean interactive) {
        this.directory = directory;
        this.interactive = interactive;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void installBundles() {
        Collection<File> files = FileUtils.listFiles(directory, new String[]{"jar"}, true);
        List<Bundle> toStart = new ArrayList<Bundle>();
        for (File file : files) {
            if (BundleHelper.isBundle(file)) {
                // Skip the interactive shell if disabled
                if (isInteractiveShell(file) && !interactive) {
                    continue;
                }

                try {
                    logger.debug("Installing bundle from {}", file.getAbsolutePath());
                    Bundle bundle = context.installBundle("reference:" + file.toURI().toURL().toExternalForm());
                    if (!BundleHelper.isFragment(bundle)) {
                        toStart.add(bundle);
                    }
                } catch (Exception e) {
                    logger.error("Error when install bundle from {}", new Object[]{file.getAbsolutePath(), e});
                }
            }
        }

        for (Bundle bundle : toStart) {
            try {
                logger.debug("Starting bundle {}", bundle.getSymbolicName());
                bundle.start();
            } catch (BundleException e) {
                logger.error("Error when start bundle {}", new Object[]{bundle.getSymbolicName(), e});
            }
        }
    }

    private boolean isInteractiveShell(File file) {
        return file.getName().startsWith("shelbie-startup-console");
    }

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        installBundles();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}