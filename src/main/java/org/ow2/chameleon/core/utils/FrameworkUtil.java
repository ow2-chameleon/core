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

package org.ow2.chameleon.core.utils;

import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

/**
 * Utility function to launch the underlying OSGi Framework.
 */
public class FrameworkUtil {

    /**
     * Simple method to parse META-INF/services file for framework factory.
     * Currently, it assumes the first non-commented line is the class name of
     * the framework factory implementation.
     * @return The created <tt>FrameworkFactory</tt> instance.
     * @throws Exception if any errors occur.
     **/
    public static FrameworkFactory getFrameworkFactory() throws Exception {
        URL url = FrameworkUtil.class.getClassLoader().getResource(
                "META-INF/services/org.osgi.framework.launch.FrameworkFactory");
        if (url != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(url
                    .openStream()));
            try {
                for (String s = br.readLine(); s != null; s = br.readLine()) {
                    s = s.trim();
                    // Try to load first non-empty, non-commented line
                    if ((s.length() > 0) && (s.charAt(0) != '#')) {
                        return (FrameworkFactory) Class.forName(s)
                                .newInstance();
                    }
                }
            } finally {
                br.close();
            }
        }

        throw new Exception("Could not find framework factory.");
    }


    public static Framework create(Map<String, String> configuration) throws Exception {
        return getFrameworkFactory().newFramework(configuration);
    }


}
