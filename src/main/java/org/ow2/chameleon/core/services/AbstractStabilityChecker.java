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
package org.ow2.chameleon.core.services;

import java.util.concurrent.TimeUnit;

/**
 * Provides useful method to ease the implementation of {@link org.ow2.chameleon.core.services.StabilityChecker}.
 */
public abstract class AbstractStabilityChecker implements StabilityChecker {

    /**
     * The time factory system property.
     */
    public static final String TIME_FACTOR = "time.factor";

    /**
     * The system property to configure the default grace time in milliseconds.
     */
    public static final String STABILITY_GRACE = "stability.grace";

    /**
     * The system property to configure the default number of attempt before giving up.
     */
    public static final String STABILITY_ATTEMPTS = "stability.attempts";

    /**
     * Retrieves the current time factor if set. The time factor is configure using the `time.factor` system property.
     *
     * @return the time factor, 1 if not set.
     */
    public static int getTimeFactor() {
        return Integer.getInteger(TIME_FACTOR, 1);
    }

    /**
     * Retrieves the default grace period (in millis). The default value is set using the `stability.grace` system
     * property. the default value is 100 ms.
     *
     * @return the default grace period.
     */
    public static long getDefaultGracePeriodInMillis() {
        return Long.getLong(STABILITY_GRACE, 100l);
    }

    /**
     * Retrieves the default number of attempts before declaring that the stability cannot be reached. The default
     * value is set using the `stability.attempts` system property. the default value is 500.
     *
     * @return the default number of attempts.
     */
    public static int getDefaultNumberOfAttempts() {
        return Integer.getInteger(STABILITY_ATTEMPTS, 500);
    }

    /**
     * Block the caller thread for the default grace period.
     */
    public static void grace() {
        grace(getDefaultGracePeriodInMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Waits for the given time period.
     *
     * @param amount the amount
     * @param unit   the unit
     */
    public static void grace(long amount, TimeUnit unit) {
        try {
            Thread.sleep(unit.toMillis(amount) * getTimeFactor());
        } catch (InterruptedException e) { //NOSONAR ignore the exception on purpose.
            // Ignored
        }
    }

}
