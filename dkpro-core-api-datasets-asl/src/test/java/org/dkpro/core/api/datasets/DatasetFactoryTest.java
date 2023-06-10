/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.api.datasets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DatasetFactoryTest
{
    @Disabled("Used at times for offline testing / development")
    @Test
    public void testOne()
        throws Exception
    {
        //Path cache = testContext.getTestOutputFolder().toPath();
        Path cache = Paths.get("target/test-output/testLoadOne");
        
        DatasetFactory df = new DatasetFactory(cache);
        {
            Dataset ds = df.load("updt-fa-1.3");
            assertDatasetOk(ds);
        }
    }
    
    @Disabled("Used at times for offline testing / development")
    @Test
    public void testLoadAll()
        throws Exception
    {
        Path cache = Paths.get("target/test-output/testLoadAll");
        
        DatasetFactory df = new DatasetFactory(cache);
        for (String id : df.listIds()) {
            Dataset ds = df.load(id);
            assertDatasetOk(ds);
        }
    }

    @Disabled("Used at times for offline testing / development")
    @Test
    public void testShared(@TempDir Path cache)
        throws Exception
    {
        DatasetFactory df = new DatasetFactory(cache);
        Dataset ds1 = df.load("perseus-el-2.1");
        assertDatasetOk(ds1);
        Dataset ds2 = df.load("perseus-la-2.1");
        assertDatasetOk(ds2);
    }

    @Disabled("Used at times for offline testing / development")
    @Test
    public void testLoadSimple(@TempDir Path cache)
        throws Exception
    {
        DatasetFactory df = new DatasetFactory(cache);
        Dataset ds = df.load("germeval2014-de");
        assertDatasetOk(ds);
    }

    @Disabled("Used at times for offline testing / development")
    @Test
    public void testLoadWithExplode(@TempDir Path cache)
        throws Exception
    {
        DatasetFactory df = new DatasetFactory(cache);
        Dataset ds = df.load("brown-en-teixml");
        assertDatasetOk(ds);
        
        assertFalse(Files.exists(cache.resolve("brownCorpus-TEI-XML/brown_tei/Corpus.xml")));
    }

    private void assertDatasetOk(Dataset ds)
    {
        
        System.out.printf("Dataset         : %s%n", ds.getName());
        System.out.printf("Data files      : %d%n", ds.getDataFiles().length);
        
        Split split = ds.getDefaultSplit();
        if (split != null) {
            System.out.printf("Training set    : %d%n",
                    split.getTrainingFiles() != null ? split.getTrainingFiles().length : "none");
            System.out.printf("Development set : %d%n",
                    split.getDevelopmentFiles() != null ? split.getDevelopmentFiles().length : "none");
            System.out.printf("Testing set     : %d%n",
                    split.getTestFiles() != null ? split.getTestFiles().length : "none");
        }
        
        assertNotNull("Name not set", ds.getName());
        assertNotNull("Language not set", ds.getLanguage());
        if (split != null) {
            assertNullOrExists(split.getTrainingFiles());
            assertNullOrExists(split.getTestFiles());
            assertNullOrExists(split.getDevelopmentFiles());
        }
        assertNullOrExists(ds.getLicenseFiles());
        assertNotNull(ds.getDataFiles());
        assertTrue(ds.getDataFiles().length > 0);
    }

    private void assertNullOrExists(File... aFiles)
    {
        if (aFiles != null) {
            for (File f : aFiles) {
                assertThat(f).exists();
            }
        }
    }
}
