/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.nlp4j;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class Nlp4JNamedEntityRecognizerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        long maxMemory = Runtime.getRuntime().maxMemory();
        Assume.assumeTrue("Insufficient max memory: " + maxMemory, maxMemory > 3700000000l);
        
        // Run the test pipeline. Note the full stop at the end of a sentence is preceded by a
        // whitespace. This is necessary for it to be detected as a separate token!
        JCas jcas = runTest("en", null, "SAP where John Doe works is in Germany .");

        // Define the reference data that we expect to get back from the test
        String[] namedEntity = { "[ 10, 14]NamedEntity(B-PERSON) (John)",
                "[ 15, 18]NamedEntity(L-PERSON) (Doe)", "[ 31, 38]NamedEntity(U-GPE) (Germany)" };

        // Compare the annotations created in the pipeline to the reference data
        AssertAnnotations.assertNamedEntity(namedEntity, select(jcas, NamedEntity.class));
    }

    // Auxiliary method that sets up the analysis engine or pipeline used in the test.
    // Typically, we have multiple tests per unit test file that each invoke this method.
    private JCas runTest(String language, String variant, String testDocument)
        throws Exception
    {
        AnalysisEngineDescription postagger = createEngineDescription(Nlp4JPosTagger.class);
        AnalysisEngineDescription lemmatizer = createEngineDescription(Nlp4JLemmatizer.class);
        AnalysisEngineDescription ner = createEngineDescription(Nlp4JNamedEntityRecognizer.class,
                Nlp4JNamedEntityRecognizer.PARAM_VARIANT, variant,
                Nlp4JNamedEntityRecognizer.PARAM_PRINT_TAGSET, true);

        AnalysisEngineDescription engine = createEngineDescription(postagger, lemmatizer, ner);
        
        // Here we invoke the TestRunner which performs basic whitespace tokenization and 
        // sentence splitting, creates a CAS, runs the pipeline, etc. TestRunner explicitly
        // disables automatic model loading. Thus, models used in unit tests must be explicitly
        // made dependencies in the pom.xml file.
        return TestRunner.runTest(engine, language, testDocument);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();}
