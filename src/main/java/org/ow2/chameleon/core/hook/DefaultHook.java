package org.ow2.chameleon.core.hook;

import org.ow2.chameleon.core.ChameleonConfiguration;

/**
 * A default implementation of {@link org.ow2.chameleon.core.hook.Hook}. Methods do nothing.
 */
public class DefaultHook implements Hook {
    /**
     * Callback called when Chameleon just starts. Nothing important was done so far.
     */
    @Override
    public void initializing() {
        // Do nothing by default.
    }

    /**
     * Callback called when the Chameleon instance is configured but not yet created.
     *
     * @param configuration the configuration, that can be modified.
     */
    @Override
    public void configured(ChameleonConfiguration configuration) {
        // Do nothing by default.
    }

    /**
     * Callback called when the Chameleon instance is stopped just before leaving.
     */
    @Override
    public void shuttingDown() {
        // Do nothing by default.
    }
}
