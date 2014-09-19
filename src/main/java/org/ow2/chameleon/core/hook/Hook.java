package org.ow2.chameleon.core.hook;

import org.ow2.chameleon.core.ChameleonConfiguration;

/**
 * Hook letting custom code to be executed at different time of the Chameleon lifecycle.
 */
public interface Hook {

    /**
     * Callback called when Chameleon just starts. Nothing important was done so far.
     */
    public void initializing();

    /**
     * Callback called when the Chameleon instance is configured but not yet created.
     * @param configuration the configuration, that can be modified.
     */
    public void configured(ChameleonConfiguration configuration);

    /**
     * Callback called when the Chameleon instance is stopped just before leaving.
     */
    public void shuttingDown();

}
