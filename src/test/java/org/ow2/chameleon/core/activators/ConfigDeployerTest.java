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
package org.ow2.chameleon.core.activators;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.ow2.chameleon.core.Chameleon;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Check the behavior of the config deployer.
 */
public class ConfigDeployerTest {

    public static final File CHAMELEON = new File("target/test/chameleon");
    public static final File APPLICATION = new File(CHAMELEON, "application");
    private Chameleon chameleon;
    private ServiceReference<ConfigurationAdmin> reference;

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
    public void testConfigurationDynamics() throws Exception {
        chameleon.start();

        ConfigurationAdmin admin = retrieveConfigurationAdmin();
        assertThat(admin).isNotNull();
        assertThat(admin.listConfigurations(null)).isNull();

        // Create a managed service configuration
        FileUtils.writeStringToFile(new File(APPLICATION, "my.app.configuration.cfg"),
                "property = value");

        waitPolling();

        assertThat(admin.listConfigurations(null)).hasSize(1);
        assertThat(admin.listConfigurations("(service.pid=my.app.configuration)")).hasSize(1);
        Configuration configuration = admin.listConfigurations("(service.pid=my.app.configuration)")[0];
        assertThat(configuration.getProperties().get("property")).isEqualTo("value");
        assertThat(configuration.getFactoryPid()).isNull();
        assertThat(configuration.getPid()).isEqualTo("my.app.configuration");

        // Create a managed service factory configuration
        FileUtils.writeStringToFile(new File(APPLICATION, "my.component-instance.cfg"),
                "key = v");

        waitPolling();

        assertThat(admin.listConfigurations(null)).hasSize(2);
        assertThat(admin.listConfigurations("(service.factoryPid=my.component)")).hasSize(1);
        configuration = admin.listConfigurations("(service.factoryPid=my.component)")[0];
        assertThat(configuration.getProperties().get("key")).isEqualTo("v");
        assertThat(configuration.getFactoryPid()).isEqualTo("my.component");
        assertThat(configuration.getPid()).isNotNull();

        // Update the first configuration
        FileUtils.writeStringToFile(new File(APPLICATION, "my.app.configuration.cfg"),
                "property = value2");

        waitPolling();

        assertThat(admin.listConfigurations(null)).hasSize(2);
        assertThat(admin.listConfigurations("(service.pid=my.app.configuration)")).hasSize(1);
        configuration = admin.listConfigurations("(service.pid=my.app.configuration)")[0];
        assertThat(configuration.getProperties().get("property")).isEqualTo("value2");
        assertThat(configuration.getFactoryPid()).isNull();
        assertThat(configuration.getPid()).isEqualTo("my.app.configuration");

        // Delete the configurations

        FileUtils.deleteQuietly(new File(APPLICATION, "my.app.configuration.cfg"));
        waitPolling();

        assertThat(admin.listConfigurations(null)).hasSize(1);
        assertThat(admin.listConfigurations("(service.pid=my.app.configuration)")).isNull();

        FileUtils.deleteQuietly(new File(APPLICATION, "my.component-instance.cfg"));
        waitPolling();
        assertThat(admin.listConfigurations(null)).isNull();
    }

    @Test
    public void testConfigurationAdminDynamics() throws Exception {
        // Create a managed service configuration
        FileUtils.writeStringToFile(new File(APPLICATION, "my.app.configuration.cfg"),
                "property = value");
        // Create a managed service factory configuration
        FileUtils.writeStringToFile(new File(APPLICATION, "my.component-instance.cfg"),
                "key = v");

        chameleon.start();

        ConfigurationAdmin admin = retrieveConfigurationAdmin();
        assertThat(admin).isNotNull();
        assertThat(admin.listConfigurations(null)).hasSize(2);

        // Stop the configuration admin
        Bundle bundle = reference.getBundle();
        bundle.stop();
        reference = null;

        // Create another configuration
        FileUtils.writeStringToFile(new File(APPLICATION, "my.second.component-instance-2.cfg"),
                "key = v");

        // Restart the bundle
        bundle.start();

        waitPolling();

        admin = retrieveConfigurationAdmin();
        assertThat(admin).isNotNull();
        assertThat(admin.listConfigurations(null)).hasSize(3);
    }

    @Test
    public void testUnmanagedConfiguration() throws IOException {
        assertThat(UnmanagedConfiguration.INSTANCE.getPid()).isEqualTo(UnmanagedConfiguration.NOT_MANAGED);
        assertThat(UnmanagedConfiguration.INSTANCE.getFactoryPid()).isEqualTo(UnmanagedConfiguration.NOT_MANAGED);
        assertThat(UnmanagedConfiguration.INSTANCE.getProperties()).isNull();

        try {
            UnmanagedConfiguration.INSTANCE.update();
            fail("Unsupported Operation expected");
        } catch (UnsupportedOperationException e) {
            // Ok.
        }

        try {
            UnmanagedConfiguration.INSTANCE.update(null);
            fail("Unsupported Operation expected");
        } catch (UnsupportedOperationException e) {
            // Ok.
        }

        try {
            UnmanagedConfiguration.INSTANCE.delete();
            fail("Unsupported Operation expected");
        } catch (UnsupportedOperationException e) {
            // Ok.
        }

        // Equal and Hashcode
        assertThat(UnmanagedConfiguration.INSTANCE.equals(new Object())).isFalse();
        assertThat(UnmanagedConfiguration.INSTANCE.equals(UnmanagedConfiguration.INSTANCE)).isTrue();
        assertThat(UnmanagedConfiguration.INSTANCE.hashCode()).isNotEqualTo(0);
    }

    private ConfigurationAdmin retrieveConfigurationAdmin() throws InterruptedException {
        reference = null;
        for (int i = 0; i < 1000; i++) {
            reference =
                    chameleon.context().getServiceReference(ConfigurationAdmin.class);
            if (reference == null) {
                Thread.sleep(10);
            } else {
                break;
            }
        }
        if (reference != null) {
            return chameleon.context().getService(reference);
        } else {
            return null;
        }
    }


    private void waitPolling() throws InterruptedException {
        Thread.sleep(2500);
    }

}
