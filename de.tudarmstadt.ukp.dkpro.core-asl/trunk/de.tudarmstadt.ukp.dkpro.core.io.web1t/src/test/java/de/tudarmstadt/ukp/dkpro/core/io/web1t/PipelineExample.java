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
package de.tudarmstadt.ukp.dkpro.core.io.web1t;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.pipeline.SimplePipeline;

import com.googlecode.jweb1t.JWeb1TIndexer;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TEIReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class PipelineExample
{

    @Ignore
    @Test
    public void pipelineTest() throws Exception
    {
        String corpusPath = DKProContext.getContext().getWorkspace("toolbox_corpora").getAbsolutePath() + "/brown_tei/";
        CollectionReader reader = createCollectionReader(
                TEIReader.class,
                TEIReader.PARAM_PATH, corpusPath,
                TEIReader.PARAM_PATTERNS, new String[] { "[+]*.xml" }
        );

        AnalysisEngineDescription segmenter = createPrimitiveDescription(
                BreakIteratorSegmenter.class
        );

        AnalysisEngineDescription ngramWriter = createPrimitiveDescription(
                Web1TFormatWriter.class,
                Web1TFormatWriter.PARAM_TARGET_LOCATION, "target/web1t/",
                Web1TFormatWriter.PARAM_INPUT_TYPES, new String[] { Token.class.getName() },
                Web1TFormatWriter.PARAM_MIN_NGRAM_LENGTH, 1,
                Web1TFormatWriter.PARAM_MAX_NGRAM_LENGTH, 3,
                Web1TFormatWriter.PARAM_MIN_FREQUENCY, 2
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