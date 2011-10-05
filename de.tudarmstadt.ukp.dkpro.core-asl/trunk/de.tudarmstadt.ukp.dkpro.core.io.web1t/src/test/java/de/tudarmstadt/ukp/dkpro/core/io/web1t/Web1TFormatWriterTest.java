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
import org.junit.Test;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class Web1TFormatWriterTest {

    @Test
    public void web1TFormatTest() throws Exception {     
        CollectionReader reader = createCollectionReader(
                TextReader.class,
                ResourceCollectionReaderBase.PARAM_PATH, "src/test/resources/",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                        ResourceCollectionReaderBase.INCLUDE_PREFIX + "**/*.txt" }
        );
 
        AnalysisEngineDescription segmenter = createPrimitiveDescription(
                BreakIteratorSegmenter.class
        );
 
        AnalysisEngineDescription ngramWriter = createPrimitiveDescription(
                Web1TFormatWriter.class,
                Web1TFormatWriter.PARAM_OUTPUT_PATH, "target/",
                Web1TFormatWriter.PARAM_MIN_NGRAM_LENGTH, 1,
                Web1TFormatWriter.PARAM_MAX_NGRAM_LENGTH, 3
        );
        
        SimplePipeline.runPipeline(
                reader,
                segmenter,
                ngramWriter
        );
    }
}
