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
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceStabilityCheckerTest {

    @Test
    public void testBasics() {
        ServiceStabilityChecker checker = new ServiceStabilityChecker(null);
        assertThat(checker.getName()).containsIgnoringCase("service");
        assertThat(checker.getPriority()).isEqualTo(1);
    }

    @Test
    public void testServiceStability() throws InvalidSyntaxException {
        BundleContext context = mock(BundleContext.class);
        when(context.getAllServiceReferences(anyString(), anyString())).thenReturn(new ServiceReference[25], new
                ServiceReference[28], new ServiceReference[30], new ServiceReference[30]);
        ServiceStabilityChecker checker = new ServiceStabilityChecker(context);
        assertThat(checker.check().isStable).isTrue();
    }

    @Test
    public void testServiceUnStability() throws InvalidSyntaxException {
        BundleContext context = mock(BundleContext.class);
        when(context.getAllServiceReferences(anyString(), anyString())).thenReturn(new ServiceReference[25], new
                ServiceReference[28], new ServiceReference[30], new ServiceReference[31],
                new ServiceReference[32], new ServiceReference[33]);
        ServiceStabilityChecker checker = new ServiceStabilityChecker(context, 3);
        assertThat(checker.check().isStable).isFalse();
    }

}