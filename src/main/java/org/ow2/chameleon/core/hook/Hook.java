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
package org.ow2.chameleon.core.hook;

import org.ow2.chameleon.core.ChameleonConfiguration;

/**
 * Hook letting custom code to be executed at different time of the Chameleon lifecycle.
 */
public interface Hook {

    /**
     * Callback called when Chameleon just starts. Nothing important was done so far.
     */
    void initializing();

    /**
     * Callback called when the Chameleon instance is configured but not yet created.
     * @param configuration the configuration, that can be modified.
     */
    void configured(ChameleonConfiguration configuration);

    /**
     * Callback called when the Chameleon instance is stopped just before leaving.
     */
    void shuttingDown();

}
