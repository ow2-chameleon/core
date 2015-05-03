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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.services.DirectoryBasedDeployer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the behavior of the directory monitor
 */
public class DirectoryMonitorDynamicTest {

    private File directory;
    private DirectoryMonitor monitor;

    @Before
    public void setUp() {
        directory = new File("target/test-data/files");
        FileUtils.deleteQuietly(directory);
        directory.mkdirs();
        monitor = new DirectoryMonitor();
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(2000);
    }


    @Test
    public void testWithoutMonitors() throws Exception {
        BundleContext context = mock(BundleContext.class);
        monitor.start(context);
        monitor.stop(context);
    }

    @Test
    public void testWithoutMonitorButWithOneDeployerButNoFile() throws Exception {
        SpyingDeployer spy = new SpyingDeployer();
        BundleContext context = mock(BundleContext.class);
        monitor.deployers.add(spy);
        monitor.start(context);
        assertThat(spy.created).isEmpty();
        monitor.stop(context);
    }

    @Test
    public void testWithOneMonitorDeployerButNoFile() throws Exception {
        SpyingDeployer spy = new SpyingDeployer();
        BundleContext context = mock(BundleContext.class);
        monitor.deployers.add(spy);
        monitor.start(context);
        monitor.add(directory, true);
        assertThat(spy.created).isEmpty();
        monitor.stop(context);
    }

    @Test
    public void testWithOneDeployerWithOneFileAndMonitorArriveAfterward() throws Exception {
        SpyingDeployer spy = new SpyingDeployer();
        createFile("file1");
        BundleContext context = mock(BundleContext.class);
        ServiceReference<Deployer> reference = mock(ServiceReference.class);
        when(context.getService(reference)).thenReturn(spy);
        monitor.start(context);
        monitor.addingService(reference);
        assertThat(spy.created).isEmpty();
        monitor.add(directory, true);
        // Should be immediately called.
        assertThat(spy.created.get(0).getName()).isEqualTo("file1");
        monitor.stop(context);
    }

    @Test
    public void testWithOneDeployerWithSeveralFilesAndMonitorArriveAfterward() throws Exception {
        SpyingDeployer spy = new SpyingDeployer();
        createFile("file1");
        createFile("file2");
        createFile("file3");
        BundleContext context = mock(BundleContext.class);
        ServiceReference<Deployer> reference = mock(ServiceReference.class);
        when(context.getService(reference)).thenReturn(spy);
        monitor.start(context);
        monitor.addingService(reference);
        assertThat(spy.created).isEmpty();
        monitor.add(directory, false);
        // We cannot ensure the order.
        List<String> names = getFileNames(spy.created);
        assertThat(names).contains("file1");
        assertThat(names).contains("file2");
        assertThat(names).contains("file3");
        monitor.stop(context);
    }

    private List<String> getFileNames(List<File> files) {
        List<String> names = new ArrayList<String>();
        for (File f : files) {
            names.add(f.getName());
        }
        return names;
    }

    @Test
    public void testMonitorDynamics() throws Exception {
        SpyingDeployer spy = new SpyingDeployer();
        BundleContext context = mock(BundleContext.class);
        monitor.deployers.add(spy);
        monitor.start(context);
        assertThat(spy.created).isEmpty();
        assertThat(spy.updated).isEmpty();
        assertThat(spy.deleted).isEmpty();

        // Create a new file but no monitor.
        createFile("file1");
        waitPolling();
        assertThat(spy.created).isEmpty();
        assertThat(spy.updated).isEmpty();
        assertThat(spy.deleted).isEmpty();

        monitor.add(directory, 10);

        assertThat(spy.created.get(0).getName()).isEqualTo("file1");
        assertThat(spy.updated).isEmpty();
        assertThat(spy.deleted).isEmpty();

        // Update the file
        updateFile("file1");
        waitPolling();
        assertThat(spy.updated.get(0).getName()).isEqualTo("file1");

        // remove the monitor
        assertThat(monitor.removeAndStopIfNeeded(directory)).isTrue();

        // Delete the file
        deleteFile("file1");
        waitPolling();
        assertThat(spy.deleted).isEmpty();

        monitor.stop(context);
    }

    @Test
    public void testAddingMonitorWhenAlreadyThere() throws IOException {
        BundleContext context = mock(BundleContext.class);
        monitor.start(context);

        assertThat(monitor.add(directory, true)).isTrue();
        assertThat(monitor.add(directory, true)).isFalse();
        assertThat(monitor.add(directory, false)).isFalse();

        assertThat(monitor.add(new File(directory, "h"), true)).isFalse();

        assertThat(monitor.removeAndStopIfNeeded(directory)).isTrue();

        assertThat(monitor.add(directory, false)).isTrue();
        assertThat(monitor.add(directory, true)).isTrue();

        assertThat(monitor.removeAndStopIfNeeded(directory)).isTrue();

        assertThat(monitor.add(directory, false)).isTrue();
        assertThat(monitor.add(new File(directory, "h"), true)).isTrue();

        assertThat(monitor.removeAndStopIfNeeded(directory)).isFalse();
        assertThat(monitor.removeAndStopIfNeeded(new File(directory, "h"))).isTrue();
    }

    private void deleteFile(String filename) {
        File file = new File(directory, filename);
        file.delete();
    }

    private void updateFile(String filename) throws IOException {
        File file = new File(directory, filename);
        FileUtils.writeStringToFile(file, Long.toString(System.currentTimeMillis()));
    }

    private void waitPolling() throws InterruptedException {
        Thread.sleep(20);
    }

    private void createFile(String filename) throws IOException {
        File file = new File(directory, filename);
        file.createNewFile();
    }


    private class SpyingDeployer extends DirectoryBasedDeployer {

        public final List<File> created = new ArrayList<File>();
        public final List<File> updated = new ArrayList<File>();
        public final List<File> deleted = new ArrayList<File>();

        public SpyingDeployer() {
            super(directory);
        }

        @Override
        public void open(Collection<File> files) {
            created.addAll(files);
        }

        @Override
        public void onFileCreate(File file) {
            created.add(file);
        }

        @Override
        public void onFileChange(File file) {
            updated.add(file);
        }

        @Override
        public void onFileDelete(File file) {
            deleted.add(file);
        }
    }

}
