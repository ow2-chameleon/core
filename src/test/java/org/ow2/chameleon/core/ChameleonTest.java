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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Check the behavior of the Chameleon class.
 */
public class ChameleonTest {

    public static final File CHAMELEON = new File("target/test/chameleon");
    public static final File APPLICATION = new File(CHAMELEON, "application");
    private Chameleon chameleon;

    @Before
    public void init() throws Exception {
        FileUtils.deleteQuietly(APPLICATION);
        APPLICATION.mkdirs();
        chameleon = new Chameleon(CHAMELEON, false, null);
    }

    @After
    public void stop() throws BundleException, InterruptedException {
        chameleon.stop();
        chameleon = null;
    }

    @Test
    public void testStart() throws BundleException {
        assertThat(chameleon).isNotNull();
        assertThat(chameleon.context()).isNull();
        chameleon.start();
        assertThat(chameleon.context()).isNotNull();
        assertThat(chameleon.framework()).isNotNull();
    }

    @Test
    public void testInitializationFromConfiguration() throws Exception {
        File baseDirectory = new File("target/test-classes/configurations/regular");
        assertThat(baseDirectory).isDirectory();
        ChameleonConfiguration configuration = new ChameleonConfiguration(baseDirectory);
        chameleon = new Chameleon(configuration);
        assertThat(chameleon).isNotNull();
        assertThat(chameleon.context()).isNull();
        chameleon.start();
        assertThat(chameleon.context()).isNotNull();
        assertThat(chameleon.framework()).isNotNull();
    }

}
