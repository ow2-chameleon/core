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
package org.ow2.chameleon.core.services;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the directory based deployer.
 */
public class DirectoryBasedDeployerTest {


    private File directory;

    @Before
    public void setUp() {
        directory = new File("target/test-data/files");
        directory.mkdirs();
    }

    @After
    public void tearDown() {
        FileUtils.deleteQuietly(directory);
    }

    @Test
    public void testOnDirectoryWithAFileInside() throws IOException {
        // Create an initial file
        File file = new File(directory, "file.txt");
        FileUtils.writeStringToFile(file, "hello");

        DirectoryBasedDeployer deployer = new DirectoryBasedDeployer(directory);
        Deployer spy = Mockito.spy(deployer);

        List<File> files = ImmutableList.of(file);
        spy.open(files);

        Mockito.verify(spy).onFileCreate(file);
    }

    @Test
    public void testThatOnFileCreateIsCalledOnFileChange() throws IOException {
        // Create an initial file
        File file = new File(directory, "file.txt");
        FileUtils.writeStringToFile(file, "hello");

        DirectoryBasedDeployer deployer = new DirectoryBasedDeployer(directory);
        Deployer spy = Mockito.spy(deployer);

        spy.onFileChange(file);

        Mockito.verify(spy).onFileCreate(file);
    }

    @Test
    public void testAccept() throws IOException {
        DirectoryBasedDeployer deployer = new DirectoryBasedDeployer(directory);

        File file1 = new File(directory, "foo");
        file1.createNewFile();
        assertThat(deployer.accept(file1)).isTrue();

        File file2 = File.createTempFile("tmp", "txt");
        assertThat(deployer.accept(file2)).isFalse();

        // This is an interesting case
        // The deployer just check that the file is potentially in the directory, but it may not exist.
        // This allows being notified when a file is deleted.
        File file3 = new File(directory, "do_not_exist");
        assertThat(deployer.accept(file3)).isTrue();

        assertThat(deployer.accept(directory)).isFalse();

        File sub = new File(directory, "sub");
        sub.mkdirs();
        File file4 = new File(sub, "foo");
        file4.createNewFile();

        assertThat(deployer.accept(file4)).isTrue();
    }
}
