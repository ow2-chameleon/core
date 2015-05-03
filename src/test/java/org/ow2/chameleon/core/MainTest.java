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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.chameleon.core.utils.jul.JulLogManager;
import org.ow2.chameleon.core.utils.jul.JulWrapper;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Check the behavior of the main class, especially the argument parsing.
 */
public class MainTest {
    @Test
    public void testParseUserProperties() throws Exception {
        String[] args = new String[]{
                "-Dflag", "-Dpair1=value1", "-Dpair2=value2"
        };

        Map<String, Object> map = Main.parseUserProperties(args);
        Assert.assertEquals(map.get("flag"), Boolean.TRUE);
        Assert.assertEquals(map.get("pair1"), "value1");
        Assert.assertEquals(map.get("pair2"), "value2");
    }

    @Test
    public void testParseUserPropertiesWithInteractive() throws Exception {
        String[] args = new String[]{
                "-Dflag", "-Dpair1=value1", "-Dpair2=value2", "--interactive"
        };

        Map<String, Object> map = Main.parseUserProperties(args);
        Assert.assertEquals(map.get("flag"), Boolean.TRUE);
        Assert.assertEquals(map.get("pair1"), "value1");
        Assert.assertEquals(map.get("pair2"), "value2");
    }

    @After
    public void cleanup() {
        System.clearProperty(Chameleon.CHAMELEON_BASEDIR);
    }

    @Test
    public void testChameleonCreationWithoutArguments() throws Exception {
        System.setProperty(Chameleon.CHAMELEON_BASEDIR, "target/test/chameleon");
        Chameleon chameleon = Main.createChameleon(null);
        assertThat(chameleon).isNotNull();
    }

    @Test
    public void testJulFacade() throws IOException {
        System.setProperty(Chameleon.CHAMELEON_BASEDIR, "target/test/chameleon");
        Chameleon chameleon = Main.createChameleon(null);
        assertThat(chameleon).isNotNull();
        java.util.logging.LogManager logManager = java.util.logging.LogManager.getLogManager();
        assertThat(logManager).isInstanceOf(JulLogManager.class);
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(getClass().getName());
        assertThat(logger).isInstanceOf(JulWrapper.class);
    }

}
