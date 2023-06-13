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
package org.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.api.datasets.Dataset;
import org.dkpro.core.api.datasets.DatasetFactory;
import org.dkpro.core.api.datasets.Split;
import org.dkpro.core.eval.EvalUtil;
import org.dkpro.core.eval.model.Span;
import org.dkpro.core.eval.report.Result;
import org.dkpro.core.io.conll.Conll2000Reader;
import org.dkpro.core.testing.TestCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class OpenNlpChunkerTrainerTest
{
    private Dataset ds;

    @Test
    public void test(@TempDir File targetFolder)
        throws Exception
    {
        Split split = ds.getDefaultSplit();
        
        // Train model
        System.out.println("Training model from training data");
        CollectionReaderDescription trainReader = createReaderDescription(
                Conll2000Reader.class,
                Conll2000Reader.PARAM_PATTERNS, split.getTrainingFiles(),
                Conll2000Reader.PARAM_LANGUAGE, ds.getLanguage());
        
        AnalysisEngineDescription trainer = createEngineDescription(
                OpenNlpChunkerTrainer.class,
                OpenNlpChunkerTrainer.PARAM_TARGET_LOCATION, new File(targetFolder, "model.bin"),
//                OpenNlpChunkerTrainer.PARAM_ALGORITHM, "PERCEPTRON",
//                OpenNlpChunkerTrainer.PARAM_CUTOFF, 0,
                OpenNlpChunkerTrainer.PARAM_NUM_THREADS, 2,
                OpenNlpChunkerTrainer.PARAM_LANGUAGE, ds.getLanguage(),
                OpenNlpChunkerTrainer.PARAM_ITERATIONS, 10);
        
        SimplePipeline.runPipeline(trainReader, trainer);
        
        // Apply model and collect labels
        System.out.println("Applying model to test data");
        CollectionReaderDescription testReader = createReaderDescription(
                Conll2000Reader.class,
                Conll2000Reader.PARAM_PATTERNS, split.getTestFiles(),
                Conll2000Reader.PARAM_READ_CHUNK, false,
                Conll2000Reader.PARAM_LANGUAGE, ds.getLanguage());
        
        AnalysisEngineDescription ner = createEngineDescription(
                OpenNlpChunker.class,
                OpenNlpChunker.PARAM_PRINT_TAGSET, true,
                OpenNlpChunker.PARAM_MODEL_LOCATION, new File(targetFolder, "model.bin"));

        List<Span<String>> actual = EvalUtil.loadSamples(iteratePipeline(testReader, ner),
                Chunk.class, chunk -> chunk.getChunkValue());
        System.out.printf("Actual samples: %d%n", actual.size());
        
        // Read reference data collect labels
        ConfigurationParameterFactory.setParameter(testReader, 
                Conll2000Reader.PARAM_READ_CHUNK, true);
        List<Span<String>> expected = EvalUtil.loadSamples(testReader, Chunk.class, chunk -> {
            return chunk.getChunkValue();
        });
        System.out.printf("Expected samples: %d%n", expected.size());

        Result results = EvalUtil.dumpResults(targetFolder, expected, actual);
        
        assertEquals(0.912441, results.getFscore(), 0.0001);
        assertEquals(0.914613, results.getPrecision(), 0.0001);
        assertEquals(0.910280, results.getRecall(), 0.0001);
    }
    
    @BeforeEach
    public void setup() throws IOException
    {
        DatasetFactory loader = new DatasetFactory(TestCache.getCacheFolder());
        ds = loader.load("conll2000-en");
    }    
}
