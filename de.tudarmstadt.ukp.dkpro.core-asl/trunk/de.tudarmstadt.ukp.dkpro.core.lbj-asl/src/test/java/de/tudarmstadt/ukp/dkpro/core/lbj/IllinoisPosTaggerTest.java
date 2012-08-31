package de.tudarmstadt.ukp.dkpro.core.lbj;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class IllinoisPosTaggerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", null, "This is a test . \n",
                new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
                new String[] { "ART",  "V",   "ART", "NN",   "PUNC" });

        runTest("en", null, "A neural net . \n",
                new String[] { "DT",  "NN",     "NN",  "." },
                new String[] { "ART", "NN",    "NN",  "PUNC" });

        runTest("en", null, "John is purchasing oranges . \n",
                new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
                new String[] { "NP",   "V",   "V",        "NN",     "PUNC" });
    }
    
    private void runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AnalysisEngine engine = createPrimitive(IllinoisPosTagger.class);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);
        
        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
