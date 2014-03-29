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
package de.tudarmstadt.ukp.dkpro.core.textnormalizer.frequency;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator;
import de.tudarmstadt.ukp.dkpro.core.frequency.resources.Web1TFrequencyCountResource;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.frequency.SharpSNormalizer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class SharpSNormalizerTest
{

    private ExternalResourceDescription frequencyProvider;

    @Before
    public void init(){
        frequencyProvider = createExternalResourceDescription(
                Web1TFrequencyCountResource.class,
                Web1TFrequencyCountResource.PARAM_MIN_NGRAM_LEVEL, "1",
                Web1TFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, "1",
                Web1TFrequencyCountResource.PARAM_INDEX_PATH, "src/test/resources/jweb1t");  
    }
    
    @Test
    public void testUmlautSharpSNormalizer() throws Exception
    {

        //check sharpS normalization
        test("süss", "süß");
    }

    public void test(String sourceText, String targetText) throws Exception
    {
        AggregateBuilder builder = new AggregateBuilder();
        builder.add(createEngineDescription(BreakIteratorSegmenter.class));
        builder.add(createEngineDescription(
                SharpSNormalizer.class,
                SharpSNormalizer.FREQUENCY_PROVIDER, frequencyProvider,
                SharpSNormalizer.PARAM_MIN_FREQUENCY_THRESHOLD,0));
        builder.add(createEngineDescription(ApplyChangesAnnotator.class), "source",
                "_InitialView", "target", "sharpS_cleaned");

        AnalysisEngine engine = builder.createAggregate();

        String text = sourceText;
        JCas jcas = engine.newJCas();
        jcas.setDocumentText(text);
        DocumentMetaData.create(jcas);

        engine.process(jcas);

        JCas view = jcas.getView("sharpS_cleaned");
        String normalizedText = targetText;
        assertEquals(normalizedText, view.getDocumentText());
    }    

}
