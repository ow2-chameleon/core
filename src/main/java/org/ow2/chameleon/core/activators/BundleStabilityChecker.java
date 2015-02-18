/*
 * #%L
 * OW2 Chameleon - Core
 * %%
 * Copyright (C) 2009 - 2015 OW2 Chameleon
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
package org.ow2.chameleon.core.activators;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.core.services.AbstractStabilityChecker;
import org.ow2.chameleon.core.services.StabilityResult;
import org.ow2.chameleon.core.utils.BundleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Stability check verifying the bundle state.
 *
 * It fails if at least one bundle does not reach its final state before a timeout. This timeout is impacted by the
 * "time.factor" system property.
 */
public class BundleStabilityChecker extends AbstractStabilityChecker {

    private final static int MAX_TRIES = 500;
    private static final Logger LOGGER = LoggerFactory.getLogger(BundleStabilityChecker.class);

    private final BundleContext context;
    private final int attempts;

    public BundleStabilityChecker(BundleContext context) {
        this(context, MAX_TRIES);
    }

    public BundleStabilityChecker(BundleContext context, int max) {
        this.context = context;
        this.attempts = max;
    }

    /**
     * Gets the name of the checker. This is used to identify failed checks.
     *
     * @return the checker name, human-friendly.
     */
    @Override
    public String getName() {
        return "Bundle Stability";
    }

    /**
     * Gets the checker priority. Low priorities are executed first.
     *
     * @return the priority of the checker
     */
    @Override
    public int getPriority() {
        return 0;
    }

    /**
     * Tries to reach the stability criteria checked by the current checker.
     *
     * @return the stability result
     */
    @Override
    public StabilityResult check() {
        boolean bundleStability = getBundleStability(context);
        int count = 0;
        while (!bundleStability && count < attempts) {
            grace(100, TimeUnit.MILLISECONDS);
            count++;
            bundleStability = getBundleStability(context);
        }

        if (count == attempts) {
            LOGGER.error("Bundle stability isn't reached after {} tries", attempts);
            for (Bundle bundle : context.getBundles()) {
                LOGGER.error("Bundle {} - {} -> {}", bundle.getBundleId(), bundle.getSymbolicName(),
                        bundle.getState());
            }
            return StabilityResult.unstable("Cannot reach the bundle stability");
        }

        return StabilityResult.stable();
    }


    /**
     * Are bundle stables.
     *
     * @param bc the bundle context
     * @return <code>true</code> if every bundles are activated.
     */
    public static boolean getBundleStability(BundleContext bc) {
        boolean stability = true;
        Bundle[] bundles = bc.getBundles();
        for (Bundle bundle : bundles) {
            if (BundleHelper.isFragment(bundle)) {
                stability = stability && (bundle.getState() == Bundle.RESOLVED);
            } else {
                stability = stability && (bundle.getState() == Bundle.ACTIVE);
            }
        }
        return stability;
    }
}
