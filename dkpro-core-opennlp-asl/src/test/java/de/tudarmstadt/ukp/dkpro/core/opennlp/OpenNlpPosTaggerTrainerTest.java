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
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.eval.EvalUtil;
import de.tudarmstadt.ukp.dkpro.core.eval.model.Span;
import de.tudarmstadt.ukp.dkpro.core.eval.report.Result;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Reader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class OpenNlpPosTaggerTrainerTest
{
    @Test
    public void test()
        throws Exception
    {
        File cache = DkproTestContext.getCacheFolder();
        File targetFolder = testContext.getTestOutputFolder();
        
        // NOTE: This file contains Asciidoc markers for partial inclusion of this file in the 
        // documentation. Do not remove these tags!
        // tag::datasets[]
        // Obtain dataset
        DatasetFactory loader = new DatasetFactory(cache);
        Dataset ds = loader.load("gum-en-conll-2.3.2");
        Split split = ds.getSplit(0.8);
        
        // Train model
        System.out.println("Training model from training data");
        CollectionReaderDescription trainReader = createReaderDescription(
                Conll2006Reader.class,
                Conll2006Reader.PARAM_PATTERNS, split.getTrainingFiles(),
                Conll2006Reader.PARAM_USE_CPOS_AS_POS, true,
                Conll2006Reader.PARAM_LANGUAGE, ds.getLanguage());
        // end::datasets[]
        
        AnalysisEngineDescription trainer = createEngineDescription(
                OpenNlpPosTaggerTrainer.class,
                OpenNlpPosTaggerTrainer.PARAM_TARGET_LOCATION, new File(targetFolder, "model.bin"),
                OpenNlpPosTaggerTrainer.PARAM_LANGUAGE, ds.getLanguage(),
                OpenNlpPosTaggerTrainer.PARAM_NUM_THREADS, 2,
                OpenNlpPosTaggerTrainer.PARAM_ITERATIONS, 10);
        
        SimplePipeline.runPipeline(trainReader, trainer);
        
        // Apply model and collect labels
        System.out.println("Applying model to test data");
        CollectionReaderDescription testReader = createReaderDescription(
                Conll2006Reader.class,
                Conll2006Reader.PARAM_PATTERNS, split.getTestFiles(),
                Conll2006Reader.PARAM_READ_POS, false,
                Conll2006Reader.PARAM_LANGUAGE, ds.getLanguage());
        
        AnalysisEngineDescription postagger = createEngineDescription(
                OpenNlpPosTagger.class,
                OpenNlpPosTagger.PARAM_PRINT_TAGSET, true,
                OpenNlpPosTagger.PARAM_MODEL_LOCATION, new File(targetFolder, "model.bin"));

        List<Span<String>> actual = EvalUtil.loadSamples(iteratePipeline(testReader, postagger),
                POS.class, pos -> {
                    return pos.getPosValue();
                });
        System.out.printf("Actual samples: %d%n", actual.size());
        
        // Read reference data collect labels
        ConfigurationParameterFactory.setParameter(testReader, 
                Conll2006Reader.PARAM_READ_POS, true);
        List<Span<String>> expected = EvalUtil.loadSamples(testReader, POS.class, pos -> {
            return pos.getPosValue();
        });
        System.out.printf("Expected samples: %d%n", expected.size());

        Result results = EvalUtil.dumpResults(targetFolder, expected, actual);
        
        assertEquals(0.642212, results.getFscore(), 0.0001);
        assertEquals(0.642212, results.getPrecision(), 0.0001);
        assertEquals(0.642212, results.getRecall(), 0.0001);
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
