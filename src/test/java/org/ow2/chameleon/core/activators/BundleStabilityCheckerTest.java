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
package org.ow2.chameleon.core.activators;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import java.lang.reflect.Field;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BundleStabilityCheckerTest {


    @Test
    public void testBasicMethods() {
        BundleStabilityChecker checker = new BundleStabilityChecker(null);
        assertThat(checker.getName()).isNotNull().containsIgnoringCase("bundle");
        assertThat(checker.getPriority()).isEqualTo(0);
    }

    @Test
    public void testStabilityWhenWeHaveOnlyTheSystemBundle() {
        BundleContext context = mock(BundleContext.class);
        Bundle system = mock(Bundle.class);
        when(system.getState()).thenReturn(Bundle.ACTIVE);
        when(system.getHeaders()).thenReturn(new Hashtable<String, String>());
        when(context.getBundles()).thenReturn(new Bundle[] { system });
        BundleStabilityChecker checker = new BundleStabilityChecker(context);
        assertThat(checker.check().isStable).isTrue();
    }

    @Test
    public void testStabilityWhenWeHaveOnlyTheSeveralBundles() {
        Bundle system = mock(Bundle.class);
        when(system.getState()).thenReturn(Bundle.ACTIVE);
        when(system.getHeaders()).thenReturn(new Hashtable<String, String>());

        Bundle bundle1 = mock(Bundle.class);
        when(bundle1.getState()).thenReturn(Bundle.INSTALLED).thenReturn(Bundle.RESOLVED).thenReturn(Bundle.ACTIVE);
        when(bundle1.getHeaders()).thenReturn(new Hashtable<String, String>());

        Bundle bundle2 = mock(Bundle.class);
        when(bundle2.getState()).thenReturn(Bundle.INSTALLED).thenReturn(Bundle.RESOLVED).thenReturn(Bundle.ACTIVE);
        when(bundle2.getHeaders()).thenReturn(new Hashtable<String, String>());

        BundleContext context = mock(BundleContext.class);
        when(context.getBundles()).thenReturn(new Bundle[] { system, bundle1, bundle2 });
        BundleStabilityChecker checker = new BundleStabilityChecker(context);
        assertThat(checker.check().isStable).isTrue();
    }

    @Test
    public void testUnStabilityWhenWeHaveOnlyTheSeveralBundles() {
        Bundle system = mock(Bundle.class);
        when(system.getState()).thenReturn(Bundle.ACTIVE);
        when(system.getHeaders()).thenReturn(new Hashtable<String, String>());

        Bundle bundle1 = mock(Bundle.class);
        when(bundle1.getState()).thenReturn(Bundle.INSTALLED);
        when(bundle1.getHeaders()).thenReturn(new Hashtable<String, String>());

        Bundle bundle2 = mock(Bundle.class);
        when(bundle2.getState()).thenReturn(Bundle.INSTALLED).thenReturn(Bundle.RESOLVED).thenReturn(Bundle.ACTIVE);
        when(bundle2.getHeaders()).thenReturn(new Hashtable<String, String>());

        BundleContext context = mock(BundleContext.class);
        when(context.getBundles()).thenReturn(new Bundle[] { system, bundle1, bundle2 });
        BundleStabilityChecker checker = new BundleStabilityChecker(context, 3);
        assertThat(checker.check().isStable).isFalse();
    }

    @Test
    public void testStabilityWhenWeHaveOnlyTheSeveralBundlesAndWithAFragment() {
        Bundle system = mock(Bundle.class);
        when(system.getState()).thenReturn(Bundle.ACTIVE);
        when(system.getHeaders()).thenReturn(new Hashtable<String, String>());

        Bundle bundle1 = mock(Bundle.class);
        when(bundle1.getState()).thenReturn(Bundle.INSTALLED).thenReturn(Bundle.RESOLVED).thenReturn(Bundle.ACTIVE);
        when(bundle1.getHeaders()).thenReturn(new Hashtable<String, String>());

        Bundle bundle2 = mock(Bundle.class);
        when(bundle2.getState()).thenReturn(Bundle.INSTALLED).thenReturn(Bundle.RESOLVED).thenReturn(Bundle.RESOLVED);
        Dictionary<String, String> headers = new Hashtable<String, String>();
        headers.put(Constants.FRAGMENT_HOST, "whatever");
        when(bundle2.getHeaders()).thenReturn(headers);

        BundleContext context = mock(BundleContext.class);
        when(context.getBundles()).thenReturn(new Bundle[] { system, bundle1, bundle2 });
        BundleStabilityChecker checker = new BundleStabilityChecker(context);
        assertThat(checker.check().isStable).isTrue();
    }

}