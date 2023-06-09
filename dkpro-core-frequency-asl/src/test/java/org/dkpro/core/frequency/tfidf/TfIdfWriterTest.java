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
package org.dkpro.core.frequency.tfidf;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.frequency.tfidf.model.DfModel;
import org.dkpro.core.frequency.tfidf.util.TfidfUtils;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Unit test of {@link TfIdfWriter} and {@link TfIdfAnnotator}.
 */
public class TfIdfWriterTest
{
    @Test
    public void rawScoresTest(@TempDir File tempDir) throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/consumer/", //
                TextReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

        AnalysisEngineDescription aggregate = createEngineDescription( //
                createEngineDescription( //
                        BreakIteratorSegmenter.class),
                createEngineDescription( //
                        TfIdfWriter.class, //
                        TfIdfWriter.PARAM_FEATURE_PATH, Token.class.getName(), //
                        TfIdfWriter.PARAM_TARGET_LOCATION, new File(tempDir, "model")));

        // now create the tf and df files
        SimplePipeline.runPipeline(reader, aggregate);

        // check whether they were really created and contain the correct values
        DfModel dfModel = TfidfUtils.getDfModel(new File(tempDir, "model").toString());

        assertEquals(2, dfModel.getDf("example"));
        assertEquals(2, dfModel.getDf("sentence"));
        assertEquals(1, dfModel.getDf("funny"));
    }
}
