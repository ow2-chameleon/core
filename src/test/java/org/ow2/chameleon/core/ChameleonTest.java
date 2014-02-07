package org.ow2.chameleon.core;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

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

}
