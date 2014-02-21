package de.tudarmstadt.ukp.dkpro.core.auebtools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class AuebTaggerTest
{

    @Test
    public void auebTaggerTest_simplified()
        throws Exception
    {
        runTest("gr",
                "Αντικείμενο της συνάντησης ήταν η κατάσταση στη χώρα μας",
                new String[] { "Αντικείμενο", "της",     "συνάντησης", "ήταν", "η",       "κατάσταση", "στη",     "χώρα", "μας"},
                new String[] { "noun",        "article", "noun",       "verb", "article", "noun",      "article", "noun", "pronoun"},
                new String[] { "N",           "ART",     "N",          "V",    "ART",     "N",         "ART",     "N",    "PR"}
        );
    }
    
    private JCas runTest(
            String language,
            String testDocument,
            String[] tokens,
            String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AnalysisEngine engine = createEngine(
                
                AuebTagger.class
        );

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));

        return jcas;
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
