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

    public int getTimeFactor() {
        return Integer.getInteger("time.factor", 1);
    }

    public void grace(long amount, TimeUnit unit) {
        try {
            Thread.sleep(unit.toMillis(amount) * getTimeFactor());
        } catch (InterruptedException e) { //NOSONAR ignore the exception on purpose.
            // Ignored
        }
    }

}
