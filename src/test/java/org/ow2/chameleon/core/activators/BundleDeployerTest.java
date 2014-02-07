package org.ow2.chameleon.core.activators;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.ow2.chameleon.core.Chameleon;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

/**
 * Check the behavior of the Bundle Deployer.
 */
public class BundleDeployerTest {

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
    public void testDeployingSomeBundles() throws Exception {
        chameleon.start();
        int numberOfBundles = chameleon.context().getBundles().length;

        FileUtils.copyInputStreamToFile(bundle()
                .set(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME, "my-bundle")
                .set(org.osgi.framework.Constants.BUNDLE_VERSION, "1.0.0")
                .build(withBnd()), new File(APPLICATION, "bundle1.jar"));

        FileUtils.copyInputStreamToFile(bundle()
                .set(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME, "my-bundle-2")
                .set(org.osgi.framework.Constants.BUNDLE_VERSION, "1.0.0")
                .build(withBnd()), new File(APPLICATION, "bundle2.jar"));

        waitPolling();

        assertThat(chameleon.context().getBundles().length).isEqualTo(numberOfBundles + 2);

        FileUtils.copyInputStreamToFile(bundle()
                .set(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME, "my-bundle-3")
                .set(org.osgi.framework.Constants.BUNDLE_VERSION, "1.0.0")
                .build(withBnd()), new File(APPLICATION, "bundle3.jar"));

        waitPolling();

        assertThat(chameleon.context().getBundles().length).isEqualTo(numberOfBundles + 3);

        FileUtils.deleteQuietly(new File(APPLICATION, "bundle2.jar"));

        waitPolling();

        assertThat(chameleon.context().getBundles().length).isEqualTo(numberOfBundles + 2);

    }

    @Test
    public void testBundleDynamics() throws Exception {

        // Create a bundle in application
        FileUtils.copyInputStreamToFile(bundle()
                .set(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME, "my-bundle")
                .set(org.osgi.framework.Constants.BUNDLE_VERSION, "1.0.0")
                .build(withBnd()), new File(APPLICATION, "bundle1.jar"));

        chameleon.start();
        int numberOfBundles = chameleon.context().getBundles().length;

        FileUtils.copyInputStreamToFile(bundle()
                .set(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME, "my-bundle-2")
                .set(org.osgi.framework.Constants.BUNDLE_VERSION, "1.0.0")
                .build(withBnd()), new File(APPLICATION, "bundle2.jar"));

        waitPolling();

        assertThat(chameleon.context().getBundles().length).isEqualTo(numberOfBundles + 1);

        // Update the first bundle.
        FileUtils.copyInputStreamToFile(bundle()
                .set(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME, "my-bundle")
                .set(org.osgi.framework.Constants.BUNDLE_VERSION, "1.1.0")
                .build(withBnd()), new File(APPLICATION, "bundle1.jar"));

        waitPolling();

        assertThat(chameleon.context().getBundles().length).isEqualTo(numberOfBundles + 1);

        FileUtils.deleteQuietly(new File(APPLICATION, "bundle2.jar"));

        waitPolling();

        assertThat(chameleon.context().getBundles().length).isEqualTo(numberOfBundles);

        // Check the version of the updated bundle
        Bundle bundle = getBundle("my-bundle");
        assertThat(bundle).isNotNull();
        assertThat(bundle.getVersion().toString()).isEqualTo("1.1.0");

    }

    private void waitPolling() throws InterruptedException {
        Thread.sleep(2500);
    }

    private Bundle getBundle(String sn) {
        for (Bundle b : chameleon.context().getBundles()) {
            if (sn.equals(b.getSymbolicName())) {
                return b;
            }
        }
        return null;
    }

}
