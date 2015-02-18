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

/**
 * Class representing the result of a stability check.
 * The result can represent a stable or unstable state. In the unstable case, a reason is provided.
 *
 * @version $Id: 1.0.5 $Id
 */
public class StabilityResult {

    /**
     * Whether or not the check has found a stable state. This is not the global stability, only the part of
     * stability checked by the checker.
     */
    public final boolean isStable;

    /**
     * The reason. {@code null} on stability.
     */
    public final String reason;

    /**
     * The cause. {@code null} on stability.
     */
    public final Throwable cause;

    private StabilityResult(String reason, Throwable cause) {
        isStable = reason == null && cause == null;
        this.cause = cause;
        if (reason != null) {
            this.reason = reason;
        } else if (cause != null) {
            this.reason = cause.getMessage();
        } else {
            // Success.
            this.reason = null;
        }
    }

    /**
     * @return a result for stable state.
     */
    public static StabilityResult stable() {
        return new StabilityResult(null, null);
    }

    /**
     * @return a result for a check that has not been run.
     */
    public static StabilityResult notAvailable() {
        return new StabilityResult("Not Run", null);
    }

    /**
     * Creates a result when stability cannot be reached.
     *
     * @param reason the reason of instability.
     * @return a result for unstable state.
     */
    public static StabilityResult unstable(String reason) {
        return unstable(reason, null);
    }

    /**
     * Creates a result when stability cannot be reached.
     *
     * @param cause the cause of instability.
     * @return a result for unstable state.
     */
    public static StabilityResult unstable(Throwable cause) {
        return unstable(null, cause);
    }

    /**
     * Creates a result when stability cannot be reached.
     *
     * @param reason the reason of instability.
     * @param cause  the cause of instability.
     * @return a result for unstable state.
     */
    public static StabilityResult unstable(String reason, Throwable cause) {
        return new StabilityResult(reason, cause);
    }


}
