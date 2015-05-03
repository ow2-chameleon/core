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
package org.ow2.chameleon.core;


import org.ow2.chameleon.core.utils.jul.JulLogManager;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Chameleon's core main Entry Point.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public class Main {

    /**
     * The argument enabling the shell prompt.
     */
    public static final String INTERACTIVE_ARGUMENT = "--interactive";

    /**
     * Constructor to avoid creating a new Main object.
     */
    private Main() {
        //Do nothing
    }

    /**
     * Main method. Supported parameters are:
     * <ul>
     * <li>--interactive : enables interactive shell </li>
     * </ul>
     *
     * @param args the chameleon parameter.
     */
    public static void main(final String[] args) {
        Chameleon chameleon;
        try {
            chameleon = createChameleon(args);
        } catch (Exception e) {
            LoggerFactory.getLogger(Chameleon.class).error("Cannot initialize the Chameleon instance", e);
            return;
        }

        // Register a shutdown hook to cleanup everything when stopping the JVM.
        registerShutdownHook(chameleon);
        try {
            chameleon.start();
        } catch (Exception e) {
            LoggerFactory.getLogger(Chameleon.class).error("Cannot start the Chameleon instance", e);
        }
    }

    /**
     * Creates the Chameleon instance.The instance is not started.
     *
     * @param args the command line parameters.
     * @return the Chameleon instance
     * @throws java.io.IOException if the chameleon instance cannot be created correctly.
     */
    public static Chameleon createChameleon(String[] args) throws IOException {
        boolean interactive = isInteractiveModeEnabled(args);
        Map<String, Object> map = parseUserProperties(args);
        return new Chameleon(interactive, map);
    }

    /**
     * Parses all -Dxxx properties (as well as -Dxxx=yyy).
     *
     * @param args the chameleon argument (from the command line)
     * @return the parsed properties.
     */
    public static Map<String, Object> parseUserProperties(String[] args) {
        if (args == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<String, Object>();
        for (String arg : args) {
            if (arg.startsWith("-D")) {
                arg = arg.substring(2);
                if (arg.contains("=")) {
                    String k = arg.substring(0, arg.indexOf("="));
                    String v = arg.substring(arg.indexOf("=") + 1);
                    map.put(k, v);
                } else {
                    // It's a flag, add the value 'true'
                    map.put(arg, Boolean.TRUE);
                }
            }
        }
        return map;
    }

    /**
     * Registers a shutdown hook to stop nicely the embedded framework.
     *
     * @param chameleon the stopped chameleon
     */
    private static void registerShutdownHook(final Chameleon chameleon) {
        Runtime runtime = Runtime.getRuntime();
        Runnable hook = new Runnable() {
            /**
             * The closure runs when the JVM is shutting down.
             * If a chameleon instance was created, we stops it.
             */
            public void run() {
                try {
                    if (chameleon != null) {
                        chameleon.stop();
                    }
                } catch (Exception e) {
                    LoggerFactory.getLogger(Chameleon.class)
                            .error("Cannot stop the Chameleon instance on JVM shutdown", e);
                }
            }
        };
        runtime.addShutdownHook(new Thread(hook));
    }

    /**
     * Parses the --interactive parameter.
     *
     * @param args the parameters.
     * @return true if the interactive mode is enabled.
     */
    private static boolean isInteractiveModeEnabled(String[] args) {
        if (args == null) {
            return false;
        }
        for (String arg : args) {
            if (INTERACTIVE_ARGUMENT.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

}
