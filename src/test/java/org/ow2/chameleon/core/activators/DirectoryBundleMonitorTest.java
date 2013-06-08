package org.ow2.chameleon.core.activators;

import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the behavior of the directory bundle monitor
 */
public class DirectoryBundleMonitorTest {



    @Test
    public void testInitializationOnEmptyDirectory() throws Exception {
        File directory = mock(File.class);
        when(directory.isDirectory()).thenReturn(true);
        when(directory.listFiles()).thenReturn(new File[0]);
        DirectoryBundleMonitor monitor = new DirectoryBundleMonitor(directory);

        BundleContext context = mock(BundleContext.class);
        monitor.start(context);

        assertThat(monitor.bundles).isEmpty();
    }


}
