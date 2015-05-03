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
package org.ow2.chameleon.core.utils.jul;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the JUL Wrapper
 */
public class JulWrapperTest {

    @BeforeClass
    public static void init() {
        System.setProperty("java.util.logging.manager", JulLogManager.class.getName());
    }

    @AfterClass
    public static void cleanup() {
        System.clearProperty("java.util.logging.manager");
    }

    @Test
    public void test() {
        JulWrapper wrapper = new JulWrapper("test");
        assertThat(wrapper.getName()).isEqualTo("test");
        assertThat(wrapper.getLevel()).isNull();
        wrapper.setLevel(Level.WARNING);
        assertThat(wrapper.getLevel()).isNotNull();
        assertThat(wrapper.isLoggable(Level.FINE)).isFalse();

        // Try the different method, just checking we support them.
        wrapper.fine("hello");
        wrapper.finer("hello");
        wrapper.finest("hello");
        wrapper.config("hello");
        wrapper.info("hello");
        wrapper.warning("hello");
        wrapper.severe("hello");
        wrapper.throwing("org.acme", "method", new Exception("expected"));

        wrapper.log(Level.ALL, "hello");
        wrapper.log(Level.ALL, "hello {0}", "chameleon");
        wrapper.log(Level.ALL, "hello", new Exception("expected"));

        final LogRecord record = new LogRecord(Level.SEVERE, "hello {0}");
        record.setParameters(new Object[]{"wisdom"});
        record.setThrown(new Exception("expected"));
        record.setMillis(System.currentTimeMillis());
        wrapper.log(record);

        wrapper.log(null);
    }


}