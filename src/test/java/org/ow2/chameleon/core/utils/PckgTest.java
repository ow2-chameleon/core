/*
 * #%L
 * OW2 Chameleon - Core
 * %%
 * Copyright (C) 2009 - 2015 OW2 Chameleon
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
import org.osgi.framework.Version;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of {@link Pckg}
 */
public class PckgTest {


    @Test
    public void testCreationAndCompare() {
        Pckg package1 = new Pckg("org.acme", "1.0");
        Pckg package2 = new Pckg("org.acme", "1.0");
        Pckg package3 = new Pckg("org.acme", "1.0.1");
        Pckg package4 = new Pckg("org.acme.foo", "1.0");

        assertThat(package1.name).isEqualTo("org.acme");
        assertThat(package2.name).isEqualTo("org.acme");
        assertThat(package3.name).isEqualTo("org.acme");
        assertThat(package4.name).isEqualTo("org.acme.foo");

        assertThat(package1.version).isEqualTo("1.0");
        assertThat(package2.version).isEqualTo("1.0");
        assertThat(package3.version).isEqualTo("1.0.1");
        assertThat(package4.version).isEqualTo("1.0");

        assertThat(package1.version()).isEqualTo(new Version("1.0"));
        assertThat(package3.version()).isEqualTo(new Version("1.0.1"));

        assertThat(package1).isEqualTo(package2);
        assertThat(package1).isNotEqualTo(package3);
        assertThat(package1).isNotEqualTo(package4);
        assertThat(package3).isNotEqualTo(package4);
    }

    @Test
    public void testExportClause() {
        Pckg package1 = new Pckg("org.acme", "1.0");
        Pckg package3 = new Pckg("org.acme", "1.0.1");
        Pckg package4 = new Pckg("org.acme", "0.0.0");

        assertThat(package1.toExportClause()).isEqualTo("org.acme;version=1.0");
        assertThat(package3.toExportClause()).isEqualTo("org.acme;version=1.0.1");
        assertThat(package4.toExportClause()).isEqualTo("org.acme;version=0.0.0");

        assertThat(package1.toString()).isEqualTo("org.acme;version=1.0");
        assertThat(package3.toString()).isEqualTo("org.acme;version=1.0.1");
        assertThat(package4.toString()).isEqualTo("org.acme;version=0.0.0");
    }

}