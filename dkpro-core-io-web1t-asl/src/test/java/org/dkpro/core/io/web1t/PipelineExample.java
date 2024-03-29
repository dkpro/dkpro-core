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
package org.dkpro.core.io.web1t;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.api.resources.DkproContext;
import org.dkpro.core.io.tei.TeiReader;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.googlecode.jweb1t.JWeb1TIndexer;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PipelineExample
{

    @Disabled
    @Test
    public void pipelineTest() throws Exception
    {
        String corpusPath = DkproContext.getContext().getWorkspace("toolbox_corpora").getAbsolutePath() + "/brown_tei/";
        CollectionReader reader = createReader(
                TeiReader.class,
                TeiReader.PARAM_SOURCE_LOCATION, corpusPath,
                TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" }
        );

        AnalysisEngineDescription segmenter = createEngineDescription(
                BreakIteratorSegmenter.class
        );

        AnalysisEngineDescription ngramWriter = createEngineDescription(
                Web1TWriter.class,
                Web1TWriter.PARAM_TARGET_LOCATION, "target/web1t/",
                Web1TWriter.PARAM_INPUT_TYPES, new String[] { Token.class.getName() },
                Web1TWriter.PARAM_MIN_NGRAM_LENGTH, 1,
                Web1TWriter.PARAM_MAX_NGRAM_LENGTH, 3,
                Web1TWriter.PARAM_MIN_FREQUENCY, 2
        );

        SimplePipeline.runPipeline(
                reader,
                segmenter,
                ngramWriter
        );

        JWeb1TIndexer indexCreator = new JWeb1TIndexer("target/web1t/", 3);
        indexCreator.create();
    }
}
