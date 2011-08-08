/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.api.frequency;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregate;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.ExternalResourceFactory.bindResource;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ExternalResource;

public class Web1tFrequencyCountProviderTest
{
    public static class Annotator extends JCasAnnotator_ImplBase {
            final static String MODEL_KEY = "FrequencyProvider";
            @ExternalResource(key = MODEL_KEY)
            private FrequencyCountProviderBase model;

            @Override
            public void process(JCas aJCas)
                throws AnalysisEngineProcessException
            {
                System.out.println(model.getClass().getName());
            }
    }

    @Ignore
    @Test
    public void configureAggregatedExample() throws Exception {
            AnalysisEngineDescription aed1 = createPrimitiveDescription(Annotator.class);
            AnalysisEngineDescription aed2 = createPrimitiveDescription(Annotator.class);

            // Bind external resource to the aggregate
            AnalysisEngineDescription aaed = createAggregateDescription(aed1, aed2);
            bindResource(
                    aaed,
                    Annotator.MODEL_KEY,
                    Web1TFrequencyCountProvider.class,
                    Web1TFrequencyCountProvider.PARAM_LANGUAGE_CODE, "en"
            );

            // Check the external resource was injected
            AnalysisEngine ae = createAggregate(aaed);
            ae.process(ae.newJCas());
    }

    @Ignore
    @Test
    public void configureAggregatedExample2() throws Exception {
            AnalysisEngineDescription aed1 = createPrimitiveDescription(Annotator.class);
            AnalysisEngineDescription aed2 = createPrimitiveDescription(Annotator.class);

            // Bind external resource to the aggregate
            AnalysisEngineDescription aaed = createAggregateDescription(aed1, aed2);
            bindResource(
                    aaed,
                    Annotator.MODEL_KEY,
                    Web1TFrequencyCountProvider.class,
                    Web1TFrequencyCountProvider.PARAM_LANGUAGE_CODE, "en",
                    Web1TFrequencyCountProvider.PARAM_INDEX_FILE_1, "src/test/resources/web1t/index-1gms",
                    Web1TFrequencyCountProvider.PARAM_INDEX_FILE_2, "src/test/resources/web1t/index-2gms"
            );

            // Check the external resource was injected
            AnalysisEngine ae = createAggregate(aaed);
            ae.process(ae.newJCas());
    }
}