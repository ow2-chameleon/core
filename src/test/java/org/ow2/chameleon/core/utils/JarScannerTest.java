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

import java.io.File;
import java.util.Set;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;

public class JarScannerTest {

    @Test
    public void testVersion() throws Exception {
        assertThat(JarScanner.version("acme-sample-1.0.0.jar")).isEqualTo("1.0.0");
        assertThat(JarScanner.version("acme-sample-1.0.0-SNAPSHOT.jar")).isEqualTo("1.0.0.SNAPSHOT");
        assertThat(JarScanner.version("acme-sample-1.0.0-dist.jar")).isEqualTo("1.0.0.dist");
        assertThat(JarScanner.version("acme-sample-1.0.0-SNAPSHOT-dist.jar")).isEqualTo("1.0.0.SNAPSHOT-dist");

        assertThat(JarScanner.version("acme-1.jar")).isEqualTo("1.0.0");
        assertThat(JarScanner.version("acme-1-dist.jar")).isEqualTo("1.0.0.dist");
        assertThat(JarScanner.version("acme.jar")).isNull();
        assertThat(JarScanner.version("acme.zip")).isNull();
    }

    public static final File JAR_ROOT = new File("src/test/resources/jars");

    @Test
    public void testScanAopAlliance() throws Exception {
        File file = new File(JAR_ROOT, "aopalliance-1.0.jar");
        Set<Pckg> packages = JarScanner.scan(file);

        assertThat(packages).containsOnly(
                new Pckg("org.aopalliance.intercept", "1.0.0"),
                new Pckg("org.aopalliance.aop", "1.0.0")
        );
    }

    @Test
    public void testScanMail() throws Exception {
        File file = new File(JAR_ROOT, "mail-1.4.7.jar");
        Set<Pckg> packages = JarScanner.scan(file);

        assertThat(packages).contains(
                new Pckg("com.sun.mail.auth", "1.4.7"),
                new Pckg("javax.mail", "1.4.7"));
    }

    /**
     * JDOM has a class without package.
     */
    @Test
    public void testScanJDOM() throws Exception {
        File file = new File(JAR_ROOT, "jdom-1.0.jar");
        Set<Pckg> packages = JarScanner.scan(file);

        assertThat(packages).contains(
                new Pckg("org.jdom", "1.0.0"))
                .hasSize(7);
    }


    /**
     * ModeShape has a non numeric classifier.
     */
    @Test
    public void testScanModeshape() throws Exception {
        File file = new File(JAR_ROOT, "modeshape-jcr-4.0.0.Alpha4.jar");
        Set<Pckg> packages = JarScanner.scan(file);

        for (Pckg p : packages) {
            assertThat(p.version).isEqualToIgnoringCase("4.0.0.Alpha4");
        }
    }



}