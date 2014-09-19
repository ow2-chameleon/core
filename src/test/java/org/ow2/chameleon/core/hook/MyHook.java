package org.ow2.chameleon.core.hook;


import org.ow2.chameleon.core.ChameleonConfiguration;

public class MyHook extends DefaultHook {

    public static boolean initCalled;
    public static boolean configuredCalled;
    public static boolean shuttingDownCalled;

    /**
     * Callback called when Chameleon just starts. Nothing important was done so far.
     */
    @Override
    public void initializing() {
        initCalled = true;
    }

    /**
     * Callback called when the Chameleon instance is configured but not yet created.
     *
     * @param configuration the configuration, that can be modified.
     */
    @Override
    public void configured(ChameleonConfiguration configuration) {
        configuredCalled = configuration != null;
    }

    /**
     * Callback called when the Chameleon instance is stopped just before leaving.
     */
    @Override
    public void shuttingDown() {
        shuttingDownCalled = true;
    }

}
