package org.ow2.chameleon.core.utils;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Check framework creation
 */
public class FrameworkUtilTest {

    public static final String FELIX = "org.apache.felix.framework.FrameworkFactory";

    @Test
    public void testGetFrameworkFactory() throws Exception {
         assertThat(FrameworkUtil.getFrameworkFactory()).isNotNull();
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(FrameworkUtil.getFrameworkFactory().newFramework(Maps.<String, String>newTreeMap())).isNotNull();
    }

    @Test
    public void testReadFelix() throws IOException {
        String content = FELIX;
        assertThat(FrameworkUtil.read(new ByteArrayInputStream(content.getBytes()))).isEqualTo(FELIX);
    }

    @Test
    public void testReadFelixEmptyLines() throws IOException {
        String content = "\n\n" + FELIX;
        assertThat(FrameworkUtil.read(new ByteArrayInputStream(content.getBytes()))).isEqualTo(FELIX);
    }

    @Test
    public void testReadFelixWithComments() throws IOException {
        String content = "#This is a comment\n \n " + FELIX;
        assertThat(FrameworkUtil.read(new ByteArrayInputStream(content.getBytes()))).isEqualTo(FELIX);
    }

    @Test
    public void testReadEmptyFile() throws IOException {
        String content = "";
        assertThat(FrameworkUtil.read(new ByteArrayInputStream(content.getBytes()))).isNull();
    }

    @Test
    public void testReadNull() throws IOException {
        assertThat(FrameworkUtil.read(null)).isNull();
    }
}
