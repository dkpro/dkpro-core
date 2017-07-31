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
package de.tudarmstadt.ukp.dkpro.core.nlp4j;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class Nlp4JPosTaggerTest
{
    private static final String[] ENGLISH_POS_TAGS = { "$", "''", ",", "-LRB-", "-RRB-", ".", ":",
            "ADD", "AFX", "CC", "CD", "DT", "EX", "FW", "GW", "HYPH", "IN", "JJ", "JJR", "JJS",
            "LS", "MD", "NFP", "NN", "NNP", "NNPS", "NNS", "PDT", "POS", "PRP", "PRP$", "RB",
            "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT",
            "WP", "WP$", "WRB", "XX", "``" };
    
    private static final String[] ENGLISH_POS_UNMAPPED = {};
    
    @Test
    public void testEnglishDetail()
        throws Exception
    {
        JCas jcas = runTest("en", null, "This is a test .",
                new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
                new String[] { "POS_DET",  "POS_VERB",   "POS_DET", "POS_NOUN",   "POS_PUNCT" });

        AssertAnnotations.assertTagset(POS.class, "ptb-emory", ENGLISH_POS_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb-emory", ENGLISH_POS_UNMAPPED, jcas);
    }
    
    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", null, "This is a test .",
                new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
                new String[] { "POS_DET",  "POS_VERB",   "POS_DET", "POS_NOUN",   "POS_PUNCT" });

        runTest("en", null, "A neural net .",
                new String[] { "DT",  "JJ",     "NN",  "." },
                new String[] { "POS_DET", "POS_ADJ",    "POS_NOUN",  "POS_PUNCT" });

        runTest("en", null, "John is purchasing oranges .",
                new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
                new String[] { "POS_PROPN",   "POS_VERB",   "POS_VERB",        "POS_NOUN",     "POS_PUNCT" });
    }
    
    private JCas runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AnalysisEngine engine = createEngine(Nlp4JPosTagger.class,
                Nlp4JPosTagger.PARAM_VARIANT, variant,
                Nlp4JPosTagger.PARAM_PRINT_TAGSET, true);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
        
        return jcas;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
