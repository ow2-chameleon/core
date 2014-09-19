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
