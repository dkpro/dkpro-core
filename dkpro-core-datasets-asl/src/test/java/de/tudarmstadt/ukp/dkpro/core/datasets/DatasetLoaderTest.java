/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.datasets;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

@Ignore("Normally we do not run this")
public class DatasetLoaderTest
{
    @Test
    public void testEnglishBrownCorpus()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder()).loadEnglishBrownCorpus();
        assertDatasetOk(ds);
    }

    @Test
    public void testEnglishGUMCorpus()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder()).loadEnglishGUMCorpus();
        assertDatasetOk(ds);
    }

    @Test
    public void testFrenchDeepSequoiaCorpus()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder())
                .loadFrenchDeepSequoiaCorpus();
        assertDatasetOk(ds);
    }

    @Test
    public void testGermEval2014NER()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder()).loadGermEval2014NER();
        assertDatasetOk(ds);
    }

    @Test
    public void testNEMGP()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder()).loadNEMGP();
        assertDatasetOk(ds);
    }

    @Test
    public void testUniversalDependencyTreebankV1_3()
        throws Exception
    {
        List<Dataset> dss = new DatasetLoader(DkproTestContext.getCacheFolder())
                .loadUniversalDependencyTreebankV1_3();
        for (Dataset ds : dss) {
            assertDatasetOk(ds);
        }
    }

    @Test
    public void testGermanHamburgDependencyTreebank()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder())
                .loadGermanHamburgDependencyTreebank();
        assertDatasetOk(ds);
    }

    @Test
    public void testAncientGreekAndLatinTreebank()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder())
                .loadAncientGreekAndLatingDependencyTreebank();
        assertDatasetOk(ds);
    }

    @Test
    public void testCatalanConll2009()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder()).loadCatalanConll2009();
        assertDatasetOk(ds);
    }

    @Test
    public void testGermanConll2009()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder()).loadGermanConll2009();
        assertDatasetOk(ds);
    }

    @Test
    public void testJapaneseConll2009()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder()).loadJapaneseConll2009();
        assertDatasetOk(ds);
    }

    @Test
    public void testSpanishConll2009()
        throws Exception
    {
        Dataset ds = new DatasetLoader(DkproTestContext.getCacheFolder()).loadSpanishConll2009();
        assertDatasetOk(ds);
    }

    private void assertDatasetOk(Dataset ds)
    {
        assertNotNull("Name not set", ds.getName());
        assertNotNull("Language not set", ds.getLanguage());
        assertNullOrExists(ds.getTestFiles());
        assertNullOrExists(ds.getTestFiles());
        assertNullOrExists(ds.getDevelopmentFiles());
        assertNullOrExists(ds.getLicenseFiles());
    }

    private void assertNullOrExists(File... aFiles)
    {
        if (aFiles != null) {
            for (File f : aFiles) {
                assertTrue("File does not exist: [" + f + "]", f.exists());
            }
        }
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
