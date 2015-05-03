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
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.core.activators.BundleStabilityChecker;
import org.ow2.chameleon.core.activators.IPojoProcessingStabilityChecker;
import org.ow2.chameleon.core.activators.ServiceStabilityChecker;
import org.ow2.chameleon.core.hook.MyHook;
import org.ow2.chameleon.core.services.AbstractStabilityChecker;
import org.ow2.chameleon.core.services.Stability;
import org.ow2.chameleon.core.services.StabilityChecker;
import org.ow2.chameleon.core.services.StabilityResult;
import org.ow2.chameleon.core.utils.jul.JulLogManager;
import org.ow2.chameleon.core.utils.jul.JulWrapper;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void testLoggingManagerSet() throws BundleException {
        java.util.logging.LogManager logManager = java.util.logging.LogManager.getLogManager();
        assertThat(logManager).isInstanceOf(JulLogManager.class);
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(getClass().getName());
        assertThat(logger).isInstanceOf(JulWrapper.class);
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

    @Test
    public void testHook() throws BundleException, InterruptedException {
        assertThat(MyHook.initCalled).isTrue();
        chameleon.start();
        assertThat(MyHook.configuredCalled).isTrue();
        chameleon.stop();
        assertThat(MyHook.shuttingDownCalled).isTrue();
    }

    @Test
    public void testStability() throws BundleException {
        assertThat(chameleon).isNotNull();
        assertThat(chameleon.context()).isNull();
        chameleon.start();
        assertThat(chameleon.context()).isNotNull();
        assertThat(chameleon.framework()).isNotNull();
        ServiceReference<Stability> reference = chameleon.context().getServiceReference(Stability.class);
        assertThat(reference).isNotNull();
        Stability stability = chameleon.context().getService(reference);
        assertThat(stability).isNotNull();
        assertThat(stability.waitForStability()).isTrue();
        assertThat(stability.isStable()).isTrue();

        // Enforce check order
        Set<StabilityChecker> set = stability.getStabilityResult().keySet();
        Iterator<StabilityChecker> iterator = set.iterator();
        assertThat(iterator.next()).isInstanceOf(BundleStabilityChecker.class);
        assertThat(iterator.next()).isInstanceOf(ServiceStabilityChecker.class);
        assertThat(iterator.next()).isInstanceOf(IPojoProcessingStabilityChecker.class);
    }

    @Test
    public void testStabilityWithCustomChecker() throws BundleException {
        assertThat(chameleon).isNotNull();
        assertThat(chameleon.context()).isNull();
        chameleon.start();
        assertThat(chameleon.context()).isNotNull();
        assertThat(chameleon.framework()).isNotNull();

        chameleon.context().registerService(StabilityChecker.class, new AbstractStabilityChecker() {
            @Override
            public String getName() {
                return "my custom checker";
            }

            @Override
            public int getPriority() {
                return 4;
            }

            @Override
            public StabilityResult check() {
                return StabilityResult.stable();
            }
        }, null);

        ServiceReference<Stability> reference = chameleon.context().getServiceReference(Stability.class);
        assertThat(reference).isNotNull();
        Stability stability = chameleon.context().getService(reference);
        assertThat(stability).isNotNull();
        assertThat(stability.waitForStability()).isTrue();
        assertThat(stability.isStable()).isTrue();
    }

    @Test
    public void testStabilityWithCustomCheckerThatStayUnstable() throws BundleException {
        assertThat(chameleon).isNotNull();
        assertThat(chameleon.context()).isNull();
        chameleon.start();
        assertThat(chameleon.context()).isNotNull();
        assertThat(chameleon.framework()).isNotNull();

        chameleon.context().registerService(StabilityChecker.class, new AbstractStabilityChecker() {
            @Override
            public String getName() {
                return "my custom checker";
            }

            @Override
            public int getPriority() {
                return 4;
            }

            @Override
            public StabilityResult check() {
                return StabilityResult.unstable("Bad mood");
            }
        }, null);

        ServiceReference<Stability> reference = chameleon.context().getServiceReference(Stability.class);
        assertThat(reference).isNotNull();
        Stability stability = chameleon.context().getService(reference);
        assertThat(stability).isNotNull();
        assertThat(stability.isStable()).isFalse();
        assertThat(stability.waitForStability()).isFalse();
        assertThat(stability.isStable()).isFalse();
    }

}
