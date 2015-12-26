/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class OpenNlpNamedEntityRecognizerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", "person", "SAP where John Doe works is in Germany .");

        String[] namedEntity = { "[ 10, 18]NamedEntity(person) (John Doe)" };

        AssertAnnotations.assertNamedEntity(namedEntity, select(jcas, NamedEntity.class));
    }
    
    @Test(expected=AnalysisEngineProcessException.class)
    public void testExceptionWithWrongMappingFileLocation()
        throws Exception
    {
        AnalysisEngine engine = createEngine(OpenNlpNamedEntityRecognizer.class,
                OpenNlpNamedEntityRecognizer.PARAM_PRINT_TAGSET, true,
                OpenNlpNamedEntityRecognizer.PARAM_NAMED_ENTITY_MAPPING_LOCATION, "");

        TestRunner.runTest(engine, "en", "SAP where John Doe works is in Germany .");
    }

    private JCas runTest(String language, String variant, String testDocument)
        throws Exception
    {
        AnalysisEngine engine = createEngine(OpenNlpNamedEntityRecognizer.class,
                OpenNlpNamedEntityRecognizer.PARAM_VARIANT, variant,
                OpenNlpNamedEntityRecognizer.PARAM_PRINT_TAGSET, true);

        return TestRunner.runTest(engine, language, testDocument);
    }


    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
