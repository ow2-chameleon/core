package org.ow2.chameleon.core.utils;

import org.junit.Test;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Check that LogBackUtil do behave correctly.
 */
public class LogbackUtilTest {


    @Test
    public void testOnNonInteractive() throws IOException {
        File baseDirectory = new File("target/test-classes/configurations/regular");
        assertThat(baseDirectory).isDirectory();
        ChameleonConfiguration configuration = new ChameleonConfiguration(baseDirectory);
        configuration.initialize(null);
        Logger logger = LogbackUtil.configure(configuration);
        assertThat(logger).isNotNull();
    }

    @Test
    public void testOnInteractive() throws IOException {
        File baseDirectory = new File("target/test-classes/configurations/regular");
        assertThat(baseDirectory).isDirectory();
        ChameleonConfiguration configuration = new ChameleonConfiguration(baseDirectory);
        configuration.setInteractiveModeEnabled(true);
        configuration.initialize(null);
        Logger logger = LogbackUtil.configure(configuration);
        assertThat(logger.isDebugEnabled()).isTrue();
        assertThat(logger).isNotNull();
    }
}
