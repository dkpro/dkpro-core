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
package org.dkpro.core.hunpos;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.testing.util.HideOutput;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class HunPosTaggerTest
{
    @Before
    public void prepare()
    {
        Assume.assumeFalse("HunPos currently hangs indefinitely on Windows: Issue #1099",
                System.getProperty("os.name").toLowerCase(Locale.US).contains("win"));
        Assume.assumeTrue("HunPos does not run on OS X Catalina or higher",
                System.getProperty("os.name").toLowerCase(Locale.US).contains("mac") &&
                !System.getProperty("os.version").matches("10\\.([0-9]|1[0-4]).*"));
    }
    
//    @Test
//    public void testCatalan()
//        throws Exception
//    {
//        runTest("ca", null, "Aquesta és una prova .",
//                new String[] { "Pd-nsn--n-a", "Vcr3s", "N-msan", "Z" },
//                new String[] { "POS",  "POS", "POS",  "POS" });
//    }
    
    @Test
    public void testCroatian()
        throws Exception
    {
        runTest("hr", null, "Ovo je test .",
                new String[] { "Pd-nsn--n-a", "Vcr3s", "N-msan", "Z" },
                new String[] { "POS",  "POS", "POS",  "POS" });
    }

    @Test
    public void testDanish()
        throws Exception
    {
        runTest("da", null, "Dette er en test .",
                new String[] { "PD", "VA", "PI", "NC", "XP" },
                new String[] { "POS_PRON", "POS_VERB", "POS_PRON", "POS_NOUN", "POS_PUNCT" });
    }

    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", null, "This is a test .",
                new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
                new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

        runTest("en", null, "A neural net .",
                new String[] { "DT",  "JJ",     "NN",  "." },
                new String[] { "POS_DET", "POS_ADJ",    "POS_NOUN",  "POS_PUNCT" });

        runTest("en", null, "John is purchasing oranges .",
                new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
                new String[] { "POS_PROPN", "POS_VERB", "POS_VERB", "POS_NOUN", "POS_PUNCT" });
    }

    @Test
    public void testFarsi()
        throws Exception
    {
        runTest("fa", null, "این یک تست است . \n",
                new String[] { "DET", "PRO", "N_SING", "V_COP", "DELM" },
                new String[] { "POS_DET", "POS_PRON", "POS_NOUN", "POS_VERB", "POS_PUNCT" });
    }
    
    @Test
    public void testGerman()
        throws Exception
    {
        runTest("de", null, "Das ist ein Test .",
                new String[] { "PDS", "VAFIN", "ART", "NN",   "$."    },
                new String[] { "POS_PRON",  "POS_VERB",     "POS_DET", "POS_NOUN",   "POS_PUNCT" });
    }

    @Test
    public void testHungarian()
        throws Exception
    {
        runTest("hu", null, "Ez egy teszt .",
                new String[] { "NOUN", "ART", "NOUN", "PUNCT" },
                new String[] { "POS",  "POS", "POS",  "POS" });
    }

    @Test
    public void testPortuguese()
        throws Exception
    {
        runTest("pt", null, "Este é um teste .",
                new String[] {"pron-det", "v-fin", "art", "n", "punc" },
                new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });
        
        runTest("pt", "tbchp", "Este é um teste .",
                new String[] { "D", "SR-P", "D-UM", "N", "." },
                new String[] { "POS",    "POS", "POS", "POS", "POS" });
        
        runTest("pt", "mm", "Este é um teste .",
                new String[] { "PROSUB", "V",   "ART", "N",   "." },
                new String[] { "POS",    "POS", "POS", "POS", "POS" });

        runTest("pt", "bosque", "Este é um teste .",
                new String[] { "pron-det", "v-fin", "art", "n", "punc" },
                new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });
    }
    
    @Test
    public void testRussian()
        throws Exception
    {
        runTest("ru", null, "Это тест .",
                new String[] { "A", "S", "PUNC" },
                new String[] { "POS",  "POS", "POS" });
    }

    @Test
    public void testSlovenian()
        throws Exception
    {
        runTest("sl", null, "To je test .",
                new String[] { "zaimek-kazalni", "glagol-pomožni", "samostalnik-občno_ime",
                        "PUNC" },
                new String[] { "POS",  "POS", "POS",  "POS" });
    }

    @Test
    public void testSwedish()
        throws Exception
    {
        runTest("sv", null, "Detta är ett test .",
                new String[] { "PN_NEU_SIN_DEF_SUB/OBJ", "VB_PRS_AKT", "DT_NEU_SIN_IND",
                        "NN_NEU_SIN_IND_NOM", "DL_MAD" },
                new String[] { "POS", "POS", "POS", "POS", "POS" });
        
        runTest("sv", "paroletags", "Detta är ett test .",
                new String[] { "PF@NS0@S", "V@IPAS", "DI@NS@S", "NCNSN@IS", "FE" },
                new String[] { "POS",  "POS", "POS",  "POS", "POS" });
        
        runTest("sv", "suctags", "Detta är ett test .",
                new String[] { "PN_NEU_SIN_DEF_SUB/OBJ", "VB_PRS_AKT", "DT_NEU_SIN_IND",
                        "NN_NEU_SIN_IND_NOM", "DL_MAD" },
                new String[] { "POS", "POS", "POS", "POS", "POS" });
        
        // runTest("sv", "suc2x", "Detta är ett test .",
        // new String[] { "PN_NEU_SIN_DEF_SUB@OBJ", "VB_PRS_AKT", "DT_NEU_SIN_IND",
        // "NN_NEU_SIN_IND_NOM", "MAD" },
        // new String[] { "O", "O", "O", "O", "O" });
    }

    @Test
//  @Ignore("Platform specific")
    public void testOddCharacters()
        throws Exception
    {
        runTest("en", null, "² § ¶ § °",
                new String[] { "NNP", "NNP", "NNP", "NNP", "NNP" },
                new String[] { "POS_PROPN", "POS_PROPN", "POS_PROPN", "POS_PROPN", "POS_PROPN"});
    }

    /**
     * Generate a very large document and test it.
     * @throws Exception if an error occurs.
     */
    @Test
    @Ignore("Takes too long")
    public void hugeDocumentTest()
        throws Exception
    {
        // Start Java with -Xmx512m
        boolean run = Runtime.getRuntime().maxMemory() > (500000000);
        if (!run) {
            System.out.println("Test requires more heap than available, skipping");
        }
        Assume.assumeTrue(run);

        String text = "This is a test .\n";
        int reps = 4000000 / text.length();
        String testString = repeat(text, " ", reps);

        AnalysisEngineDescription engine = createEngineDescription(HunPosTagger.class);
        JCas jcas = TestRunner.runTest(engine, "en", testString);
        List<POS> actualTags = new ArrayList<POS>(select(jcas, POS.class));
        assertEquals(reps * 5, actualTags.size());

        // test POS annotations
        String[] expectedTags = { "DT", "VBZ", "DT", "NN", "." };
        String[] expectedTagClasses = { "POS_ART", "POS_V", "POS_ART", "POS_NN", "POS_PUNC" };

        for (int i = 0; i < actualTags.size(); i++) {
            POS posAnnotation = actualTags.get(i);
            assertEquals("In position " + i, expectedTagClasses[i % 5],
                    posAnnotation.getType().getShortName());
            assertEquals("In position " + i, expectedTags[i % 5], posAnnotation.getPosValue());
        }

        System.out.println("Successfully tagged document with " + testString.length() +
                " characters");
    }

    /**
     * Test using the same AnalysisEngine multiple times.
     * @throws Exception if an error occurs.
     */
    @Test
    @Ignore("Takes too long")
    public void multiDocumentTest()
        throws Exception
    {
        String testDocument = "This is a test .";
        String[] tags = { "DT", "VBZ", "DT", "NN", "." };
        String[] tagClasses = { "POS_ART", "POS_V", "POS_ART", "POS_NN", "POS_PUNC" };

        AnalysisEngine engine = createEngine(HunPosTagger.class);

        HideOutput hideOut = new HideOutput();
        try {
            for (int n = 0; n < 100; n++) {
                JCas aJCas = TestRunner.runTest(engine, "en", testDocument);

                AssertAnnotations.assertPOS(tagClasses, tags, select(aJCas, POS.class));
            }
        }
        finally {
            engine.destroy();
            hideOut.restoreOutput();
        }
    }
    
    private JCas runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AnalysisEngine engine = createEngine(HunPosTagger.class,
                HunPosTagger.PARAM_VARIANT, variant,
                HunPosTagger.PARAM_PRINT_TAGSET, true);

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
