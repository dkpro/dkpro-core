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
package de.tudarmstadt.ukp.dkpro.core.nlp4j;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class Nlp4JLemmatizerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", "This is a test .",
                new String[] { "this", "be",  "a",   "test", "."    });

        runTest("en", "A neural net .",
                new String[] { "a",   "neural", "net", "."    });

        runTest("en", "John is purchasing oranges .",
                new String[] { "john", "be",  "purchase", "orange", "."    });
    }

    private JCas runTest(String language, String testDocument, String[] aLemma)
        throws Exception
    {
        AnalysisEngineDescription engine = createEngineDescription(
                createEngineDescription(Nlp4JPosTagger.class),
                createEngineDescription(Nlp4JLemmatizer.class));

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertLemma(aLemma, select(jcas, Lemma.class));
        
        return jcas;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
