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
package de.tudarmstadt.ukp.dkpro.core.lbj;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;
import edu.illinois.cs.cogcomp.pos.POSTagPlain;

public class IllinoisPosTaggerTest
{
    @Test
    public void testEnglishNative()
        throws Exception
    {
        File tempFile = File.createTempFile("dkpro", ".txt");
        FileUtils.write(tempFile, "This is a test .");
        POSTagPlain.main(new String[] { tempFile.getAbsolutePath() });
    }
    
    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", null, "This is a test . \n",
                new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
                new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

        runTest("en", null, "A neural net . \n",
                new String[] { "DT",  "NN",     "NN",  "." },
                new String[] { "POS_DET", "POS_NOUN",    "POS_NOUN",  "POS_PUNCT" });

        JCas jcas = runTest("en", null, "John is purchasing oranges . \n",
                new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
                new String[] { "POS_PROPN",   "POS_VERB",   "POS_VERB",        "POS_NOUN",     "POS_PUNCT" });
        
        String[] posTags = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC", "CD", "DT",
                "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS", "PDT",
                "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB", "VBD",
                "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] unmappedPos = {};

        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
    }

    private JCas runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AnalysisEngine engine = createEngine(IllinoisPosTagger.class);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
        
        return jcas;
    }


    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
