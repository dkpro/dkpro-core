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
package de.tudarmstadt.ukp.dkpro.core.performance;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TEIReader;

/**
 * @author zesch
 *
 */
public class ThroughputTest {

    public void testAggregate(CollectionReader reader, AnalysisEngineDescription aggr)
        throws IOException, UIMAException
    {

        AnalysisEngineDescription startTimerDesc = getTimerAED(false);
        AnalysisEngineDescription endTimerDesc   = getTimerAED(true);

        AnalysisEngineDescription timerAggregate = createEngineDescription(
                startTimerDesc,
                aggr,
                endTimerDesc
        );

        SimplePipeline.runPipeline(
                reader,
                timerAggregate
        );
    }

    public static CollectionReader getStandardReader(String languageCode) throws IOException, ResourceInitializationException {
        if (languageCode.equals("en")) {
            String brownPath = DKProContext.getContext().getWorkspace("toolbox_corpora").getAbsolutePath() + "/brown_tei/";

            return createCollectionReader(
                    TEIReader.class,
                    TEIReader.PARAM_LANGUAGE, "en",
                    TEIReader.PARAM_PATH, brownPath,
                    TEIReader.PARAM_PATTERNS, new String[] {INCLUDE_PREFIX + "*.xml.gz"}
            );
        }
        else {
            throw new IllegalArgumentException("No standard reader available for language code: " + languageCode);
        }
    }

    private AnalysisEngineDescription getTimerAED(boolean isFinalTimer)
        throws ResourceInitializationException
    {
        return createEngineDescription(
                ThroughputTestAE.class,
                ThroughputTestAE.PARAM_IS_DOWNSTREAM_TIMER, isFinalTimer
        );
    }
}
