/*
 * Copyright 2016
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
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.datasets.Dataset;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.DatasetFactory;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.Split;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.eval.EvalUtil;
import de.tudarmstadt.ukp.dkpro.core.eval.model.Span;
import de.tudarmstadt.ukp.dkpro.core.eval.report.Result;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Writer;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class OpenNlpLemmatizerTrainerTest
{
    @Test
    public void test()
        throws Exception
    {
        File cache = DkproTestContext.getCacheFolder();
        File targetFolder = testContext.getTestOutputFolder();
        
        // Obtain dataset
        DatasetFactory loader = new DatasetFactory(cache);
        Dataset ds = loader.load("gum-en-conll-3.0.0");
        Split split = ds.getSplit(0.8);
        
        // Parameters
        boolean useExtendedTTTagset = false;
        int iterations = 10;
        
        // Train model
        System.out.println("Training model from training data");
        CollectionReaderDescription trainReader = createReaderDescription(
                Conll2006Reader.class,
                Conll2006Reader.PARAM_PATTERNS, split.getTrainingFiles(),
                Conll2006Reader.PARAM_USE_CPOS_AS_POS, useExtendedTTTagset,
                Conll2006Reader.PARAM_LANGUAGE, ds.getLanguage());
        
        AnalysisEngineDescription trainer = createEngineDescription(
                OpenNlpLemmatizerTrainer.class,
                OpenNlpLemmatizerTrainer.PARAM_TARGET_LOCATION, new File(targetFolder, "model.bin"),
                OpenNlpLemmatizerTrainer.PARAM_LANGUAGE, ds.getLanguage(),
                OpenNlpLemmatizerTrainer.PARAM_NUM_THREADS, 2,
                OpenNlpLemmatizerTrainer.PARAM_ITERATIONS, iterations);

        AnalysisEngineDescription trainWriter = createEngineDescription(
                Conll2006Writer.class,
                Conll2006Writer.PARAM_SINGULAR_TARGET, true,
                Conll2006Writer.PARAM_TARGET_LOCATION, new File(targetFolder, "in.conll"));

        SimplePipeline.runPipeline(trainReader, trainer, trainWriter);
        
        // Apply model and collect labels
        System.out.println("Applying model to test data");
        CollectionReaderDescription testReader = createReaderDescription(
                Conll2006Reader.class,
                Conll2006Reader.PARAM_PATTERNS, split.getTestFiles(),
                Conll2006Reader.PARAM_USE_CPOS_AS_POS, useExtendedTTTagset,
                Conll2006Reader.PARAM_READ_LEMMA, false,
                Conll2006Reader.PARAM_LANGUAGE, ds.getLanguage());
        
        AnalysisEngineDescription lemmatizer = createEngineDescription(
                OpenNlpLemmatizer.class,
                OpenNlpLemmatizer.PARAM_MODEL_LOCATION, new File(targetFolder, "model.bin"));

        AnalysisEngineDescription testWriter = createEngineDescription(
                Conll2006Writer.class,
                Conll2006Writer.PARAM_SINGULAR_TARGET, true,
                Conll2006Writer.PARAM_TARGET_LOCATION, new File(targetFolder, "out.conll"));
        
        List<Span<String>> actual = EvalUtil.loadSamples(iteratePipeline(testReader, lemmatizer, testWriter),
                Lemma.class, lemma -> {
                    return lemma.getValue();
                });
        System.out.printf("Actual samples: %d%n", actual.size());
        
        // Read reference data collect labels
        ConfigurationParameterFactory.setParameter(testReader, 
                Conll2006Reader.PARAM_READ_LEMMA, true);
        List<Span<String>> expected = EvalUtil.loadSamples(testReader, Lemma.class, lemma -> {
            return lemma.getValue();
        });
        System.out.printf("Expected samples: %d%n", expected.size());

        Result results = EvalUtil.dumpResults(targetFolder, expected, actual);
        
        assertEquals(0.961978, results.getFscore(), 0.0001);
        assertEquals(0.961978, results.getPrecision(), 0.0001);
        assertEquals(0.961978, results.getRecall(), 0.0001);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
