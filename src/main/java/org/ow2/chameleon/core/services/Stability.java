/*
 * #%L
 * OW2 Chameleon - Core
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
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

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An interface to check the stability of the running application.
 * <p>
 * Theoretically, the architecture of a dynamic system is considered as stable when there are no change in progress.
 * This concept is useful to determine when the system as complete its startup sequence, but also important ofter any
 * deployment action that may have change the underlying architecture.
 * <p>
 * The stability service is implemented by Chameleon itself, but is made to be extended so the application can
 * contribute {@link org.ow2.chameleon.core.services.StabilityChecker} to extend the stability computation.
 * <p>
 * Be aware that stability computation is blocking the caller thread.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.5 $Id
 */
public interface Stability {

    /**
     * Checks whether the current framework is stable or not. Be aware that this method blocks the calling thread
     * until stability is reached.
     *
     * @return {@code true} if the stability is reached, {@code false} otherwise.
     */
    boolean isStable();

    /**
     * Checks and retrieve the individual stability check result.
     *
     * @return the results. The returned map is ordered.
     */
    Map<StabilityChecker, StabilityResult> getStabilityResult();

}
