/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.ldweb1t;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.frequency.resources.Web1TInMemoryFrequencyCountResource;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class LanguageDetectorWeb1TTest
{
    @Test
    public void web1tLanguageDetectorTest()
        throws Exception
    {
        ExternalResourceDescription en = createExternalResourceDescription(
                Web1TInMemoryFrequencyCountResource.class,
                Web1TInMemoryFrequencyCountResource.PARAM_MODEL_LOCATION,
                "src/test/resources/web1t/en/",
                Web1TInMemoryFrequencyCountResource.PARAM_LANGUAGE, "en",
                Web1TInMemoryFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, "2");

        ExternalResourceDescription de = createExternalResourceDescription(
                Web1TInMemoryFrequencyCountResource.class,
                Web1TInMemoryFrequencyCountResource.PARAM_MODEL_LOCATION,
                "src/test/resources/web1t/de/",
                Web1TInMemoryFrequencyCountResource.PARAM_LANGUAGE, "de",
                Web1TInMemoryFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, "2");

        List<ExternalResourceDescription> resources = new ArrayList<ExternalResourceDescription>();
        resources.add(en);
        resources.add(de);

        AnalysisEngineDescription engine = createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class), 
                createEngineDescription(LanguageDetectorWeb1T.class,
                        LanguageDetectorWeb1T.PARAM_MAX_NGRAM_SIZE, 2,
                        LanguageDetectorWeb1T.PARAM_FREQUENCY_PROVIDER_RESOURCES, resources));

        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("This is an English example.");
        
        runPipeline(jcas, engine);

        assertEquals("en", jcas.getDocumentLanguage());
    }
}
