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

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class FrameworkClassLoaderTest {


    @Test
    public void testLoadFromClasspath() throws ClassNotFoundException {
        ClassLoader classLoader = FrameworkClassLoader.getFrameworkClassLoader(new File(""), null);
        // From classpath.
        Class c = classLoader.loadClass("org.junit.Test");
        assertThat(c).isNotNull();
    }

    @Test(expected = ClassNotFoundException.class)
    public void testThatLibsCannotLoadAClassFromClasspath() throws ClassNotFoundException {
        FrameworkClassLoader classLoader = (FrameworkClassLoader) FrameworkClassLoader.getFrameworkClassLoader(new File(""), null);
        classLoader.libsClassLoader.loadClass("org.junit.Test");
    }

    @Test
    public void testParentPolicy() throws ClassNotFoundException {
        FrameworkClassLoader classLoader = (FrameworkClassLoader) FrameworkClassLoader.getFrameworkClassLoader(new File(""),
                ImmutableMap.of("chameleon.libraries.parent", "parent"));
        Class c = classLoader.libsClassLoader.loadClass("sun.net.spi.nameservice.dns.DNSNameService");
        assertThat(c).isNotNull();
    }

    @Test(expected = ClassNotFoundException.class)
    public void testThatParentPolicyCannotLoadClassPathClass() throws ClassNotFoundException {
        FrameworkClassLoader classLoader = (FrameworkClassLoader) FrameworkClassLoader.getFrameworkClassLoader(new File(""),
                ImmutableMap.of("chameleon.libraries.parent", "parent"));
        classLoader.libsClassLoader.loadClass("org.junit.Test");
    }

    @Test
    public void testApplication() throws ClassNotFoundException {
        FrameworkClassLoader classLoader = (FrameworkClassLoader) FrameworkClassLoader.getFrameworkClassLoader(new File(""),
                ImmutableMap.of("chameleon.libraries.parent", "application"));
        Class c = classLoader.libsClassLoader.loadClass("sun.net.spi.nameservice.dns.DNSNameService");
        assertThat(c).isNotNull();
        c = classLoader.loadClass("org.junit.Test");
        assertThat(c).isNotNull();
    }

    @Test
    public void testLoadingFelixFactoryClass() throws ClassNotFoundException {
        FrameworkClassLoader classLoader = (FrameworkClassLoader) FrameworkClassLoader.getFrameworkClassLoader(new File(""));
        Class c = classLoader.loadClass("org.apache.felix.framework.FrameworkFactory");
        assertThat(c).isNotNull();
        c = classLoader.loadClass("org.apache.felix.framework.FrameworkFactory");
        assertThat(c).isNotNull();
    }

}