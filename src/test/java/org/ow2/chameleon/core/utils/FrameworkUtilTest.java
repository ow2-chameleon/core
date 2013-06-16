package org.ow2.chameleon.core.utils;

import com.google.common.collect.Maps;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Check framework creation
 */
public class FrameworkUtilTest {

    @Test
    public void testGetFrameworkFactory() throws Exception {
         assertThat(FrameworkUtil.getFrameworkFactory()).isNotNull();
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(FrameworkUtil.getFrameworkFactory().newFramework(Maps.<String, String>newTreeMap())).isNotNull();
    }
}
