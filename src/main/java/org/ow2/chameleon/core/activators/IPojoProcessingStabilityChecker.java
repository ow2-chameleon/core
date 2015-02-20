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

import org.apache.felix.ipojo.extender.queue.QueueService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.core.services.AbstractStabilityChecker;
import org.ow2.chameleon.core.services.StabilityResult;
import org.ow2.chameleon.core.utils.BundleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Stability check verifying that we reach stability in term of services.
 * <p>
 * It checks that there are not service that have appeared or disappeared on a time window.
 */
public class IPojoProcessingStabilityChecker extends AbstractStabilityChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPojoProcessingStabilityChecker.class);

    private final BundleContext context;

    public IPojoProcessingStabilityChecker(BundleContext context) {
        this.context = context;
    }

    /**
     * Gets the name of the checker. This is used to identify failed checks.
     *
     * @return the checker name, human-friendly.
     */
    @Override
    public String getName() {
        return "iPOJO Processing Stability";
    }

    /**
     * Gets the checker priority. Low priorities are executed first.
     *
     * @return the priority of the checker
     */
    @Override
    public int getPriority() {
        return 2;
    }

    /**
     * Tries to reach the stability criteria checked by the current checker.
     *
     * @return the stability result
     */
    @Override
    public StabilityResult check() {
        int count = 0;
        int attempts = getDefaultNumberOfAttempts();
        try {
            Collection<ServiceReference<QueueService>> refs = context.getServiceReferences(QueueService.class, null);
            List<Object> queues = new ArrayList<Object>();
            for (ServiceReference<QueueService> ref : refs) {
                queues.add(context.getService(ref));
            }
            boolean emptiness = false;

            while (!emptiness && count < attempts) {
                emptiness = areAllQueuesEmpty(queues);
                if (!emptiness) {
                    grace(attempts, TimeUnit.MILLISECONDS);
                }
                count++;
            }

        } catch (InvalidSyntaxException e) { //NOSONAR
            // Cannot happen, filter is null
        }

        if (count == attempts) {
            LOGGER.error("iPOJO processing queues are not empty after {} tries", attempts);
            return StabilityResult.unstable("iPOJO Processing Queues are not empty");
        }
        return StabilityResult.stable();
    }


    /**
     * Checks whether or not all the iPOJO processing queue are empty or not.
     * Metrics are retrieved using reflection to avoid issues when iPOJO is in the classpath.
     *
     * @param queues the queues
     * @return {@literal true} if all the iPOJO processing queues are empty, {@literal false} otherwise.
     */
    private static boolean areAllQueuesEmpty(List<Object> queues) {
        boolean empty = true;

        for (Object q : queues) {
            try {
                Method currents = q.getClass().getMethod("getCurrents");
                Method waiters = q.getClass().getMethod("getWaiters");

                int cur = (Integer) currents.invoke(q);
                int wai = (Integer) waiters.invoke(q);

                LOGGER.debug("queue: " + q + " #current : " + cur + " / #waiting : " + wai);
                empty = empty && cur == 0 && wai == 0;
            } catch (Exception e) {
                LOGGER.error("Cannot analyze queue's metrics for {}", q, e);
                throw new IllegalArgumentException("Cannot analyze queue's metrics", e);
            }
        }
        return empty;
    }
}
