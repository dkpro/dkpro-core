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
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.eval.EvalUtil;
import de.tudarmstadt.ukp.dkpro.core.eval.model.Span;
import de.tudarmstadt.ukp.dkpro.core.eval.report.Result;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2002Reader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class OpenNlpSentenceTrainerTest
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
                Conll2002Reader.class,
                Conll2002Reader.PARAM_PATTERNS, split.getTrainingFiles(),
                Conll2002Reader.PARAM_LANGUAGE, ds.getLanguage(),
                Conll2002Reader.PARAM_COLUMN_SEPARATOR, Conll2002Reader.ColumnSeparators.TAB.getName(),
                Conll2002Reader.PARAM_HAS_TOKEN_NUMBER, true,
                Conll2002Reader.PARAM_HAS_HEADER, true,
                Conll2002Reader.PARAM_HAS_EMBEDDED_NAMED_ENTITY, true);
        
        AnalysisEngineDescription trainer = createEngineDescription(
                OpenNlpSentenceTrainer.class,
                OpenNlpSentenceTrainer.PARAM_TARGET_LOCATION, new File(targetFolder, "model.bin"),
                //OpenNlpSentenceTrainer.PARAM_EOS_CHARACTERS, new char[] { '.', '?' },
                OpenNlpSentenceTrainer.PARAM_ABBREVIATION_DICTIONARY_LOCATION, 
                        "src/test/resources/dict/abbreviation_de.txt",
                OpenNlpSentenceTrainer.PARAM_NUM_THREADS, 2,
                OpenNlpSentenceTrainer.PARAM_LANGUAGE, ds.getLanguage());
        
        SimplePipeline.runPipeline(trainReader, trainer);
        
        // Apply model and collect labels
        System.out.println("Applying model to test data");
        CollectionReaderDescription testReader = createReaderDescription(
                Conll2002Reader.class,
                Conll2002Reader.PARAM_PATTERNS, split.getTestFiles(),
                Conll2002Reader.PARAM_LANGUAGE, ds.getLanguage(),
                Conll2002Reader.PARAM_COLUMN_SEPARATOR, Conll2002Reader.ColumnSeparators.TAB.getName(),
                Conll2002Reader.PARAM_HAS_TOKEN_NUMBER, true,
                Conll2002Reader.PARAM_HAS_HEADER, true,
                Conll2002Reader.PARAM_HAS_EMBEDDED_NAMED_ENTITY, true);
        
        AnalysisEngineDescription stripper = createEngineDescription(
                SentenceStripper.class);
        
        AnalysisEngineDescription segmenter = createEngineDescription(
                OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_WRITE_TOKEN, false,
                OpenNlpSegmenter.PARAM_SEGMENTATION_MODEL_LOCATION, new File(targetFolder, "model.bin"));

        List<Span<String>> actual = EvalUtil.loadSamples(
                iteratePipeline(testReader, stripper, segmenter), Sentence.class, null);
        System.out.printf("Actual samples: %d%n", actual.size());

        // Read reference data collect labels
        List<Span<String>> expected = EvalUtil.loadSamples(testReader, Sentence.class, null);
        System.out.printf("Expected samples: %d%n", expected.size());

        Result results = EvalUtil.dumpResults(targetFolder, expected, actual);
        
        assertEquals(0.937518, results.getFscore(), 0.0001);
        assertEquals(0.932157, results.getPrecision(), 0.0001);
        assertEquals(0.942941, results.getRecall(), 0.0001);
    }
    
    public static class SentenceStripper
        extends JCasAnnotator_ImplBase
    {
        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            for (Sentence s : select(aJCas, Sentence.class)) {
                s.removeFromIndexes();
            }
        }
    }

    @Before
    public void setup() throws IOException
    {
        DatasetFactory loader = new DatasetFactory(testContext.getCacheFolder());
        ds = loader.load("germeval2014-de");
    }    

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
