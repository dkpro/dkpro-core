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
        new DatasetLoader(DkproTestContext.getCacheFolder()).loadEnglishBrownCorpus();
    }

    @Test
    public void testEnglishGUMCorpus()
        throws Exception
    {
        new DatasetLoader(DkproTestContext.getCacheFolder()).loadEnglishGUMCorpus();
    }

    @Test
    public void testFrenchDeepSequoiaCorpus()
        throws Exception
    {
        new DatasetLoader(DkproTestContext.getCacheFolder()).loadFrenchDeepSequoiaCorpus();
    }

    @Test
    public void testGermEval2014NER()
        throws Exception
    {
        new DatasetLoader(DkproTestContext.getCacheFolder()).loadGermEval2014NER();
    }

    @Test
    public void testNEMGP()
        throws Exception
    {
        new DatasetLoader(DkproTestContext.getCacheFolder()).loadNEMGP();
    }

    @Test
    public void testUniversalDependencyTreebankV1_3()
        throws Exception
    {
        new DatasetLoader(DkproTestContext.getCacheFolder()).loadUniversalDependencyTreebankV1_3();
    }

    @Test
    public void testGermanHamburgDependencyTreebank()
        throws Exception
    {
        new DatasetLoader(DkproTestContext.getCacheFolder()).loadGermanHamburgDependencyTreebank();
    }
    
    @Test
    public void testAncientGreekAndLatinTreebank()
        throws Exception
    {
        new DatasetLoader(DkproTestContext.getCacheFolder()).loadAncientGreekAndLatingDependencyTreebank();
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
