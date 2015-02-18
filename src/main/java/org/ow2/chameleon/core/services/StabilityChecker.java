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
 * Service exposed to extend the stability computation. All provider exposing this service are involved in the
 * stability computation. The priority of each checker lets order the check in a sequence. Low priority are executed
 * first, while higher priority are executed at the end of the computation.
 * <p>
 * Be pretty careful when implementing this service:
 * <ul>
 * <li>The process ({@link #check()}) tries to reach the stability criteria</li>
 * <li>The caller thread is blocked, so fail the stability after a reasonable timeout</li>
 * </ul>
 *
 * @version $Id: 1.0.5 $Id
 * @see org.ow2.chameleon.core.services.Stability
 */
public interface StabilityChecker {

    /**
     * Gets the name of the checker. This is used to identify failed checks.
     *
     * @return the checker name, human-friendly.
     */
    String getName();

    /**
     * Gets the checker priority. Low priorities are executed first.
     *
     * @return the priority of the checker
     */
    int getPriority();

    /**
     * Tries to reach the stability criteria checked by the current checker.
     *
     * @return the stability result
     */
    StabilityResult check();

}
