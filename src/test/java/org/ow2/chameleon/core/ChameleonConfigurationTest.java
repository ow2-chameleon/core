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

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the chameleon configuration
 */
public class ChameleonConfigurationTest {

    @After
    public void cleanup() {
        System.clearProperty("property");
    }

    @Test
    public void testRegular() throws IOException {
        File baseDirectory = new File("target/test-classes/configurations/regular");
        assertThat(baseDirectory).isDirectory();
        ChameleonConfiguration configuration = new ChameleonConfiguration(baseDirectory);
        configuration.initialize(null);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getBaseDirectory().getAbsolutePath()).isEqualTo(baseDirectory.getAbsolutePath());

        // Booleans
        // Missing properties
        assertThat(configuration.getBoolean("nope", true)).isTrue();
        assertThat(configuration.getBoolean("nope", false)).isFalse();
        // Existing properties
        assertThat(configuration.getBoolean("chameleon.runtime.monitoring", true)).isFalse();
        assertThat(configuration.getBoolean("chameleon.application.monitoring", false)).isTrue();

        // Strings.
        assertThat(configuration.get("nope", "hello")).isEqualTo("hello");
        assertThat(configuration.get("nope", null)).isNull();
        assertThat(configuration.get("nope")).isNull();
        assertThat(configuration.get("chameleon.core")).isEqualTo("core");
        assertThat(configuration.get("chameleon.core", "fail")).isEqualTo("core");

        // Integers.
        assertThat(configuration.getInt("nope", 1)).isEqualTo(1);
        assertThat(configuration.getInt("integer", 1)).isEqualTo(10);

        assertThat(configuration.isInteractiveModeEnabled()).isFalse();

        // Try to retrieve directories and files
        assertThat(configuration.getFile("file", false)).isFile();
        assertThat(configuration.getFile("file_not_exist", false)).doesNotExist();
        assertThat(configuration.getFile("file_to_create", true)).isFile();
        assertThat(configuration.getDirectory("conf", false)).isDirectory();
        assertThat(configuration.getDirectory("dir_not_exist", false)).doesNotExist();
        assertThat(configuration.getDirectory("dir_to_create", true)).isDirectory();
        assertThat(configuration.getDirectory("nope", false)).isNull();

        // Retrieve relative files
        assertThat(configuration.getRelativeFile("a_file.txt")).isFile();
        assertThat(configuration.getRelativeFile("conf/chameleon.properties")).isFile();

    }

    @Test
    public void testWithSystemProperties() throws IOException {
        File baseDirectory = new File("target/test-classes/configurations/system");
        assertThat(baseDirectory).isDirectory();
        ChameleonConfiguration configuration = new ChameleonConfiguration(baseDirectory);
        configuration.initialize(null);

        assertThat(System.getProperty("property")).isEqualTo("value");

    }

    @Test
    public void testWithUserProperties() throws IOException {
        assertThat(System.getProperty("property")).isNull();
        File baseDirectory = new File("target/test-classes/configurations/system");
        assertThat(baseDirectory).isDirectory();
        ChameleonConfiguration configuration = new ChameleonConfiguration(baseDirectory);
        Map<String, Object> user = ImmutableMap.<String, Object>of("property", "value");
        configuration.initialize(user);

        assertThat(System.getProperty("property")).isEqualTo("value");

    }

    @Test
    public void testFrameworkConfigurationFromEmptyConfiguration() {
        File baseDirectory = new File("target/test-classes/configurations/regular");
        assertThat(baseDirectory).isDirectory();
        ChameleonConfiguration configuration = new ChameleonConfiguration(baseDirectory);

        configuration.initFrameworkConfiguration();

        assertThat(configuration.get("ipojo.log.level")).isEqualTo("WARNING");
        assertThat(configuration.get("org.osgi.framework.system.packages.extra")).isNotNull();
    }
}
