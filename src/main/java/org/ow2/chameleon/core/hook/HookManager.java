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


import com.google.common.collect.ImmutableList;
import org.ow2.chameleon.core.ChameleonConfiguration;

import java.util.*;

/**
 * Manages the Chameleon hooks.
 */
public class HookManager {

    Collection<Hook> hooks = Collections.emptyList();

    public synchronized Collection<Hook> addHook(Hook hook) {
        hooks = new ImmutableList.Builder<Hook>().addAll(hooks).add(hook).build();
        return hooks;
    }

    public synchronized void load() {
        ServiceLoader<Hook> loader = ServiceLoader.load(Hook.class);
        for (Hook hook : loader) {
            addHook(hook);
        }
    }

    public void fireInitializing() {
        for (Hook hook : hooks) {
            hook.initializing();
        }
    }

    public void fireConfigured(final ChameleonConfiguration configuration) {
        for (Hook hook : hooks) {
            hook.configured(configuration);
        }
    }

    public void fireShuttingDown() {
        for (Hook hook : hooks) {
            hook.shuttingDown();
        }
    }

}
