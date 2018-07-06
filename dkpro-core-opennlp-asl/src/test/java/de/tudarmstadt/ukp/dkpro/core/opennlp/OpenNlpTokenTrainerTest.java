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
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.datasets.Dataset;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.DatasetFactory;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.Split;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.eval.EvalUtil;
import de.tudarmstadt.ukp.dkpro.core.eval.model.Span;
import de.tudarmstadt.ukp.dkpro.core.eval.report.Result;
import de.tudarmstadt.ukp.dkpro.core.io.conll.ConllUReader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class OpenNlpTokenTrainerTest
{
    private Dataset ds;
    
    @Test
    public void test()
        throws Exception
    {
        File targetFolder = testContext.getTestOutputFolder();
        
        Split split = ds.getDefaultSplit();
        
        // Train model
        System.out.println("Training model from training data");
        CollectionReaderDescription trainReader = createReaderDescription(
                ConllUReader.class,
                ConllUReader.PARAM_PATTERNS, split.getTrainingFiles(),
                ConllUReader.PARAM_LANGUAGE, ds.getLanguage());
        
        AnalysisEngineDescription trainer = createEngineDescription(
                OpenNlpTokenTrainer.class,
                OpenNlpTokenTrainer.PARAM_TARGET_LOCATION, new File(targetFolder, "model.bin"),
                OpenNlpTokenTrainer.PARAM_NUM_THREADS, 2,
                OpenNlpTokenTrainer.PARAM_LANGUAGE, ds.getLanguage());
        
        SimplePipeline.runPipeline(trainReader, trainer);
        
        // Apply model and collect labels
        System.out.println("Applying model to test data");
        CollectionReaderDescription testReader = createReaderDescription(
                ConllUReader.class,
                ConllUReader.PARAM_PATTERNS, split.getTestFiles(),
                ConllUReader.PARAM_LANGUAGE, ds.getLanguage());
        
        AnalysisEngineDescription stripper = createEngineDescription(
                TokenStripper.class);
        
        AnalysisEngineDescription segmenter = createEngineDescription(
                OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_WRITE_SENTENCE, false,
                OpenNlpSegmenter.PARAM_TOKENIZATION_MODEL_LOCATION, new File(targetFolder, "model.bin"));

        List<Span<String>> actual = EvalUtil.loadSamples(
                iteratePipeline(testReader, stripper, segmenter), Token.class, null);
        System.out.printf("Actual samples: %d%n", actual.size());

        // Read reference data collect labels
        List<Span<String>> expected = EvalUtil.loadSamples(testReader, Token.class, null);
        System.out.printf("Expected samples: %d%n", expected.size());

        Result results = EvalUtil.dumpResults(targetFolder, expected, actual);
        
        assertEquals(0.978346, results.getFscore(), 0.0001);
        assertEquals(0.980009, results.getPrecision(), 0.0001);
        assertEquals(0.976690, results.getRecall(), 0.0001);
    }
    
    public static class TokenStripper
        extends JCasAnnotator_ImplBase
    {
        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            for (Token s : select(aJCas, Token.class)) {
                s.removeFromIndexes();
            }
        }
    }

    @Before
    public void setup() throws IOException
    {
        DatasetFactory loader = new DatasetFactory(testContext.getCacheFolder());
        ds = loader.load("ud-en-conllu-1.4");
    }    
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
