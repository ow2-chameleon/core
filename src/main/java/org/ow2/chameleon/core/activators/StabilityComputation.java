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

import org.osgi.framework.*;
import org.ow2.chameleon.core.services.AbstractStabilityChecker;
import org.ow2.chameleon.core.services.Stability;
import org.ow2.chameleon.core.services.StabilityChecker;
import org.ow2.chameleon.core.services.StabilityResult;
import org.ow2.chameleon.core.utils.BundleHelper;

import java.util.*;

/**
 * Activator exposing the {@link Stability} service.
 */
public class StabilityComputation implements BundleActivator, Stability {


    private BundleContext context;
    private ServiceRegistration<Stability> reg;

    /**
     * Stores the bundle context, and expose the stability service.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        this.reg = context.registerService(Stability.class, this, null);
    }

    /**
     * Unregister the stability service.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        this.context = null;
        BundleHelper.unregisterQuietly(reg);
    }

    private Set<StabilityChecker> getCheckers() {
        Collection<ServiceReference<StabilityChecker>> references;

        try {
            references = context.getServiceReferences(StabilityChecker.class, null);
        } catch (InvalidSyntaxException e) { //NOSONAR cannot happen, the filter is null
            // Ignored
            references = Collections.emptyList();
        }

        Set<StabilityChecker> checkers = new TreeSet<StabilityChecker>(new Comparator<StabilityChecker>() {
            @Override
            public int compare(StabilityChecker o1, StabilityChecker o2) {
                return ((Integer) o1.getPriority()).compareTo(o2.getPriority());
            }
        });
        for (ServiceReference<StabilityChecker> ref : references) {
            checkers.add(context.getService(ref));
        }

        checkers.add(new BundleStabilityChecker(context));
        checkers.add(new ServiceStabilityChecker(context));
        checkers.add(new IPojoProcessingStabilityChecker(context));

        return checkers;
    }

    /**
     * Checks whether the current framework is stable or not.
     *
     * @return {@code true} if the stability is reached, {@code false} otherwise.
     */
    @Override
    public boolean isStable() {
        // Configure the grace period and number of attempt to "small" values to reduce the blocking time
        final long originalGraceTimeInMillis = Long.getLong(AbstractStabilityChecker.STABILITY_GRACE, -1l);
        final int originalNumberOfAttempts = Integer.getInteger(AbstractStabilityChecker.STABILITY_ATTEMPTS, -1);

        try {

            // 3 milliseconds
            System.setProperty(AbstractStabilityChecker.STABILITY_GRACE, "3");
            // 3 attempts maximum
            System.setProperty(AbstractStabilityChecker.STABILITY_ATTEMPTS, "3");
            // So, the maximum sleep time should not be greater than 3 * 3 = 9ms per check.

            return waitForStability();

        } finally {
            // Cleanup...
            if (originalGraceTimeInMillis != -1) {
                System.setProperty(AbstractStabilityChecker.STABILITY_GRACE, Long.toString(originalGraceTimeInMillis));
            } else {
                System.clearProperty(AbstractStabilityChecker.STABILITY_GRACE);
            }

            if (originalNumberOfAttempts != -1) {
                System.setProperty(AbstractStabilityChecker.STABILITY_ATTEMPTS,
                        Integer.toString(originalNumberOfAttempts));
            } else {
                System.clearProperty(AbstractStabilityChecker.STABILITY_ATTEMPTS);
            }
        }
    }

    /**
     * Waits for the stability to be reached. This method may block the caller thread for quite some time depending
     * of the configured grace period and the maximum number of attempt.
     *
     * @return whether of not the stability has been reached.
     */
    @Override
    public boolean waitForStability() {
        Map<StabilityChecker, StabilityResult> results = getStabilityResult();
        for (StabilityResult result : results.values()) {
            if (!result.isStable) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks and retrieve the individual stability check result.
     *
     * @return the results. The returned map is ordered.
     */
    @Override
    public Map<StabilityChecker, StabilityResult> getStabilityResult() {
        Set<StabilityChecker> checkers = getCheckers();
        Map<StabilityChecker, StabilityResult> results = new LinkedHashMap<StabilityChecker, StabilityResult>();
        boolean hasNotBeenReached = false;
        for (StabilityChecker checker : checkers) {
            if (hasNotBeenReached) {
                results.put(checker, StabilityResult.notAvailable());
            } else {
                StabilityResult sr = checker.check();
                results.put(checker, sr);
                hasNotBeenReached = !sr.isStable;
            }
        }
        return results;
    }
}
