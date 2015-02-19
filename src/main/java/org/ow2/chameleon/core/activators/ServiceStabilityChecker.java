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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.core.services.AbstractStabilityChecker;
import org.ow2.chameleon.core.services.StabilityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stability check verifying that we reach stability in term of services.
 * <p>
 * It checks that there are not service that have appeared or disappeared on a time window.
 */
public class ServiceStabilityChecker extends AbstractStabilityChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceStabilityChecker.class);

    private final BundleContext context;
    private final int attempts;

    public ServiceStabilityChecker(BundleContext context) {
        this(context, getDefaultNumberOfAttempts());
    }

    public ServiceStabilityChecker(BundleContext context, int maxAttempt) {
        this.context = context;
        this.attempts = maxAttempt;
    }

    /**
     * Gets the name of the checker. This is used to identify failed checks.
     *
     * @return the checker name, human-friendly.
     */
    @Override
    public String getName() {
        return "Service Stability";
    }

    /**
     * Gets the checker priority. Low priorities are executed first.
     *
     * @return the priority of the checker
     */
    @Override
    public int getPriority() {
        return 1;
    }

    /**
     * Tries to reach the stability criteria checked by the current checker.
     *
     * @return the stability result
     */
    @Override
    public StabilityResult check() {
        boolean serviceStability = false;
        int count = 0;
        int count1 = 0;
        int count2 = 0;
        while (!serviceStability && count < attempts) {
            try {
                // We use the getAllServiceReferences method to ignore classloading issues. Anyway, we are not using
                // the service, just counting them.
                ServiceReference[] refs = context.getAllServiceReferences(null, null);
                count1 = refs.length;
                grace();
                refs = context.getAllServiceReferences(null, null);
                count2 = refs.length;
                serviceStability = count1 == count2;
            } catch (Exception e) {
                LOGGER.warn("An exception was thrown while checking the service stability", e);
                serviceStability = false;
                // Nothing to do, while recheck the condition
            }
            count++;
        }

        if (count == attempts) {
            LOGGER.error("Service stability has not been reached after {} tries ({} != {})", attempts, count1, count2);
            return StabilityResult.unstable("Cannot reach the service stability");
        }
        return StabilityResult.stable();
    }
}
