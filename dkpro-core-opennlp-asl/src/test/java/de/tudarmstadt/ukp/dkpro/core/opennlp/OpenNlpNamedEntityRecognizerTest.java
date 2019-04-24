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
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;

// NOTE: This file contains Asciidoc markers for partial inclusion of this file in the documentation
// Do not remove these tags!
public class OpenNlpNamedEntityRecognizerTest
{
// tag::test[]
    @Test
    public void testEnglish()
        throws Exception
    {
        // Run the test pipeline. Note the full stop at the end of a sentence is preceded by a
        // whitespace. This is necessary for it to be detected as a separate token!
        JCas jcas = runTest("en", "person", "SAP where John Doe works is in Germany .");

        // Define the reference data that we expect to get back from the test
        String[] namedEntity = { "[ 10, 18]NamedEntity(person) (John Doe)" };

        // Compare the annotations created in the pipeline to the reference data
        AssertAnnotations.assertNamedEntity(namedEntity, select(jcas, NamedEntity.class));
    }
// end::test[]
    
    @Test(expected = AnalysisEngineProcessException.class)
    public void testExceptionWithWrongMappingFileLocation()
        throws Exception
    {
        AnalysisEngine engine = createEngine(OpenNlpNamedEntityRecognizer.class,
                OpenNlpNamedEntityRecognizer.PARAM_PRINT_TAGSET, true,
                OpenNlpNamedEntityRecognizer.PARAM_NAMED_ENTITY_MAPPING_LOCATION, "");

        TestRunner.runTest(engine, "en", "SAP where John Doe works is in Germany .");
    }

    @Test
    public void testGerman()
        throws Exception
    {
        // Run the test pipeline. Note the full stop at the end of a sentence is preceded by a
        // whitespace. This is necessary for it to be detected as a separate token!
        JCas jcas = runTest("de", "nemgp", "Markus arbeitet seit 10 Jahren bei SAP in Deutschland .");

        // Define the reference data that we expect to get back from the test
        String[] namedEntity = { 
                "[ 35, 38]NamedEntity(org) (SAP)",
                "[ 42, 53]NamedEntity(loc) (Deutschland)" };

        // Compare the annotations created in the pipeline to the reference data
        AssertAnnotations.assertNamedEntity(namedEntity, select(jcas, NamedEntity.class));
    }

// tag::test[]
    
    // Auxiliary method that sets up the analysis engine or pipeline used in the test.
    // Typically, we have multiple tests per unit test file that each invoke this method.
    private JCas runTest(String language, String variant, String testDocument)
        throws Exception
    {
        AssumeResource.assumeResource(OpenNlpNamedEntityRecognizer.class, "ner", language, variant);
        
        AnalysisEngine engine = createEngine(OpenNlpNamedEntityRecognizer.class,
                OpenNlpNamedEntityRecognizer.PARAM_VARIANT, variant,
                OpenNlpNamedEntityRecognizer.PARAM_PRINT_TAGSET, true);

        // Here we invoke the TestRunner which performs basic whitespace tokenization and 
        // sentence splitting, creates a CAS, runs the pipeline, etc. TestRunner explicitly
        // disables automatic model loading. Thus, models used in unit tests must be explicitly
        // made dependencies in the pom.xml file.
        return TestRunner.runTest(engine, language, testDocument);
    }
// end::test[]


    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
