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
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the behavior of the directory monitor
 */
public class DirectoryMonitorTest {

    private File directory;
    private DirectoryMonitor monitor;

    @Before
    public void setUp() {
        directory = new File("target/test-data/files");
        FileUtils.deleteQuietly(directory);
        directory.mkdirs();
        monitor = new DirectoryMonitor(directory, 10L);
    }

    @Test
    public void testWithoutDeployers() throws Exception {
        BundleContext context = mock(BundleContext.class);
        monitor.start(context);
        monitor.stop(context);
    }

    @Test
    public void testWithOneDeployerButNoFile() throws Exception {
        SpyingDeployer spy = new SpyingDeployer();
        BundleContext context = mock(BundleContext.class);
        monitor.deployers.add(spy);
        monitor.start(context);
        assertThat(spy.created).isEmpty();
        monitor.stop(context);
    }

    @Test
    public void testWithOneDeployerWithOneFile() throws Exception {
        SpyingDeployer spy = new SpyingDeployer();
        createFile("file1");
        BundleContext context = mock(BundleContext.class);
        monitor.deployers.add(spy);
        monitor.start(context);
        assertThat(spy.created.get(0).getName()).isEqualTo("file1");
        monitor.stop(context);
    }

    @Test
    public void testWithOneDeployerWithSeveralFiles() throws Exception {
        SpyingDeployer spy = new SpyingDeployer();
        createFile("file1");
        createFile("file2");
        createFile("file3");
        BundleContext context = mock(BundleContext.class);
        monitor.deployers.add(spy);
        monitor.start(context);
        assertThat(spy.created.get(0).getName()).isEqualTo("file1");
        assertThat(spy.created.get(1).getName()).isEqualTo("file2");
        assertThat(spy.created.get(2).getName()).isEqualTo("file3");
        monitor.stop(context);
    }

    @Test
    public void testFileDynamics() throws Exception {
        SpyingDeployer spy = new SpyingDeployer();
        BundleContext context = mock(BundleContext.class);
        monitor.deployers.add(spy);
        monitor.start(context);
        assertThat(spy.created).isEmpty();
        assertThat(spy.updated).isEmpty();
        assertThat(spy.deleted).isEmpty();

        // Create a new file
        createFile("file1");
        waitPolling();
        assertThat(spy.created.get(0).getName()).isEqualTo("file1");
        assertThat(spy.updated).isEmpty();
        assertThat(spy.deleted).isEmpty();

        // Update the file
        updateFile("file1");
        waitPolling();
        assertThat(spy.updated.get(0).getName()).isEqualTo("file1");

        // Delete the file
        deleteFile("file1");
        waitPolling();
        assertThat(spy.updated.size()).isGreaterThanOrEqualTo(1);
        assertThat(spy.deleted.get(0).getName()).isEqualTo("file1");

        monitor.stop(context);
    }

    @Test
    public void testDeployersDynamics() throws Exception {
        SpyingDeployer spy = new SpyingDeployer();
        SpyingDeployer spy2 = new SpyingDeployer();
        ServiceReference<Deployer> reference = mock(ServiceReference.class);
        ServiceReference<Deployer> reference2 = mock(ServiceReference.class);
        BundleContext context = mock(BundleContext.class);
        when(context.getService(reference)).thenReturn(spy);
        when(context.getService(reference2)).thenReturn(spy2);

        createFile("file1");

        monitor.start(context);
        // Inject a new service
        monitor.addingService(reference);

        // Check we have been called on 'open'
        assertThat(spy.created.get(0).getName()).isEqualTo("file1");
        assertThat(spy.updated).hasSize(0);

        // Update the file and check the notification
        updateFile("file1");
        waitPolling();
        assertThat(spy.updated.get(0).getName()).isEqualTo("file1");
        assertThat(spy.updated.size()).isGreaterThanOrEqualTo(1);

        // Create a second file and the notification
        createFile("file2");
        waitPolling();
        assertThat(spy.created.get(1).getName()).isEqualTo("file2");
        assertThat(spy.updated.size()).isGreaterThanOrEqualTo(1);

        // Inject a second service.
        monitor.addingService(reference2);
        // The 'open' method should have been called with 2 files.
        assertThat(spy2.created).hasSize(2);

        // Remove the first spy.
        monitor.removedService(reference, spy);

        // We should not see this event anymore on the first spy, while the second is notified.
        assertThat(spy.updated.size()).isGreaterThanOrEqualTo(1);
        int originalSize = spy.updated.size();
        updateFile("file2");
        waitPolling();
        assertThat(spy.updated).hasSize(originalSize);
        assertThat(spy2.updated.size()).isGreaterThanOrEqualTo(1);

        // Cleanup.
        monitor.removedService(reference2, spy2);
        monitor.stop(context);
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
        public void onFileCreate(File file) {
            created.add(file);
        }

        @Override
        public void onFileChange(File file) {
            try {
                System.out.println(FileUtils.readFileToString(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            updated.add(file);
        }

        @Override
        public void onFileDelete(File file) {
            deleted.add(file);
        }
    }

}
