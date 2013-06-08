/*
 * Copyright 2013 OW2 Chameleon
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ow2.chameleon.core;

import org.osgi.framework.BundleException;

/**
 * Chameleon's core main Entry Point.
 */
public class Main {

    /**
     * Constructor to avoid creating a new Main object.
     */
    private Main() { }

    /**
     * Main method. Supported parameters are:
     * <ul>
     * <li>--interactive : enables interactive shell </li>
     * </ul>
     * @param args the chameleon parameter.
     */
    public static void main(final String[] args) {
        Chameleon chameleon = null;
        try {
            chameleon  = createChameleon(args);
        } catch (Exception e) {
            System.err
                    .println("Cannot initialize Chameleon : " + e.getMessage());
            e.printStackTrace();
        }
        if (chameleon == null) {
            return;
        }

        registerShutdownHook(chameleon);
        try {
            chameleon.start();
        } catch (BundleException e) {
            System.err.println("Cannot start Chameleon : " + e.getMessage());
        }
    }

    /**
     * Creates the Chameleon instance.The instance is not started.
     * @param args the command line parameters.
     * @return the Chameleon instance
     * @throws Exception if the chameleon instance cannot be created correctly.
     */
    public static Chameleon createChameleon(String[] args) throws Exception {
        boolean interactive = isInteractiveModeEnabled(args);
        return new Chameleon(interactive);
    }

    /**
     * Registers a shutdown hook to stop nicely the embedded framework.
     * @param chameleon the stopped chameleon
     */
    private static void registerShutdownHook(final Chameleon chameleon) {
        Runtime runtime = Runtime.getRuntime();
        Runnable hook = new Runnable() {
            public void run() {
                try {
                    if (chameleon != null) {
                        chameleon.stop();
                    }
                } catch (BundleException e) {
                    System.err.println("Cannot stop Chameleon correctly : "
                            + e.getMessage());
                } catch (InterruptedException e) {
                    System.err.println("Unexpected Exception : "
                            + e.getMessage());
                    // nothing to do
                }
            }
        };
        runtime.addShutdownHook(new Thread(hook));
    }

    /**
     * Parses the --interactive parameter.
     * @param args the parameters.
     * @return true if the interactive mode is enabled.
     */
    private static boolean isInteractiveModeEnabled(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--interactive")) {
                return true;
            }
        }
        return false;

    }

}
