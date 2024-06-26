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
package org.dkpro.core.cogroo;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.TestRunner;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class CogrooPosTaggerTest
{
    @Test
    public void testPortuguese()
        throws Exception
    {
        JCas jcas = runTest("pt", null, "Este é um teste .",
                new String[] { "pron-det", "v-fin", "art", "n", "." },
                new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS" });
                
//        String[] posTags = new String[] { "?", "adj", "adv", "art", "conj-c", "conj-s", "ec", "in",
//                "n", "num", "pp", "pron-det", "pron-indp", "pron-pers", "prop", "prp", "punc",
//                "v-fin", "v-ger", "v-inf", "v-pcp", "vp" };
//
//        AssertAnnotations.assertTagset(POS.class, "bosque", posTags, jcas);
    }

    private JCas runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AnalysisEngine engine = createEngine(CogrooPosTagger.class);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));

        return jcas;
    }
}
