/*******************************************************************************
 * Copyright 2014
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

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.util.JCasHolder;

public class AssertTransformations
{
    public static void assertTransformedText(String normalizedText, String inputText,
            String language, AnalysisEngineDescription... aEngines)
            throws ResourceInitializationException
    {
        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, inputText, StringReader.PARAM_LANGUAGE, language);


        List<AnalysisEngineDescription> engines = new ArrayList<AnalysisEngineDescription>();
        for (AnalysisEngineDescription e : aEngines) {
            engines.add(e);
        }

        engines.add(createEngineDescription(JCasHolder.class));


        for (JCas jcas : SimplePipeline.iteratePipeline(reader,
                engines.toArray(new AnalysisEngineDescription[engines.size()]))) {
            // iteratePipeline does not support CAS multipliers. jcas is not updated after the
            // multiplier. In order to access the new CAS, we use the JCasHolder (not thread-safe!)
            assertEquals(normalizedText, JCasHolder.get().getDocumentText());
        }
    }
}
