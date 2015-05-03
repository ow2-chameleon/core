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
package org.ow2.chameleon.core.utils;

import org.junit.Test;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check that LogBackUtil do behave correctly.
 */
public class LogbackUtilTest {


    @Test
    public void testOnNonInteractive() throws IOException {
        File baseDirectory = new File("target/test-classes/configurations/regular");
        assertThat(baseDirectory).isDirectory();
        ChameleonConfiguration configuration = new ChameleonConfiguration(baseDirectory);
        configuration.initialize(null);
        Logger logger = LogbackUtil.configure(configuration);
        assertThat(logger).isNotNull();
    }

    @Test
    public void testOnInteractive() throws IOException {
        File baseDirectory = new File("target/test-classes/configurations/regular");
        assertThat(baseDirectory).isDirectory();
        ChameleonConfiguration configuration = new ChameleonConfiguration(baseDirectory);
        configuration.setInteractiveModeEnabled(true);
        configuration.initialize(null);
        Logger logger = LogbackUtil.configure(configuration);
        assertThat(logger.isDebugEnabled()).isTrue();
        assertThat(logger).isNotNull();
    }
}
