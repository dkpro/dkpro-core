/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.udpipe;

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

public class UDPosTaggerTest
{
    
    @Test
    public void testNorwegian()
        throws Exception
    {
        runTest("no", null,
                "Magnus Carlsen trengte bare de fire partiene med lynsjakk for å slå utfordreren Sergej Karjakin.",
                new String[] { "PROPN", "PROPN", "VERB", "ADV", "DET", "NUM", "NOUN", "ADP", "NOUN",
                        "ADP", "PART", "VERB", "NOUN", "PROPN", "PROPN" },
                new String[] { "PROPN", "PROPN", "VERB", "ADV", "DET", "NUM", "NOUN", "ADP", "NOUN",
                        "ADP", "PART", "VERB", "NOUN", "PROPN", "PROPN" });
    }
    
    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", null, "This is a test .",
                new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
                new String[] { "DET",  "VERB",   "DET", "NOUN",   "PUNCT" });

        runTest("en", null, "A neural net .",
                new String[] { "DT",  "JJ",     "NN",  "." },
                new String[] { "DET", "ADJ",    "NOUN",  "PUNCT" });

        runTest("en", null, "John is purchasing oranges .",
                new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
                new String[] { "PROPN",   "VERB",   "VERB",        "NOUN",     "PUNCT" });
    }
        
    private JCas runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        //AssumeResource.assumeResource(UDPosTagger.class, "multiple", language, variant);

        AnalysisEngine engine = createEngine(UDPipePosTagger.class,
                UDPipePosTagger.PARAM_VARIANT, variant);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
        
        return jcas;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
