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

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
