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
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.setParameter;
import static org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.api.datasets.DatasetFactory;
import org.dkpro.core.eval.EvalUtil;
import org.dkpro.core.io.conll.Conll2006Reader;
import org.dkpro.core.testing.TestCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class OpenNlpPosTaggerTrainerTest
{
    @Test
    public void test(@TempDir File targetFolder) throws Exception
    {
        var cache = TestCache.getCacheFolder();

        // NOTE: This file contains Asciidoc markers for partial inclusion of this file in the
        // documentation. Do not remove these tags!
        // tag::datasets[]
        // Obtain dataset
        var loader = new DatasetFactory(cache);
        var ds = loader.load("gum-en-conll-2.3.2");
        var split = ds.getSplit(0.8);

        // Train model
        System.out.println("Training model from training data");
        var trainReader = createReaderDescription( //
                Conll2006Reader.class, //
                Conll2006Reader.PARAM_PATTERNS, split.getTrainingFiles(), //
                Conll2006Reader.PARAM_USE_CPOS_AS_POS, true, //
                Conll2006Reader.PARAM_LANGUAGE, ds.getLanguage()); //
        // end::datasets[]

        var trainer = createEngineDescription( //
                OpenNlpPosTaggerTrainer.class, //
                OpenNlpPosTaggerTrainer.PARAM_TARGET_LOCATION, new File(targetFolder, "model.bin"), //
                OpenNlpPosTaggerTrainer.PARAM_LANGUAGE, ds.getLanguage(), //
                OpenNlpPosTaggerTrainer.PARAM_NUM_THREADS, 2, //
                OpenNlpPosTaggerTrainer.PARAM_ITERATIONS, 10);

        SimplePipeline.runPipeline(trainReader, trainer);

        // Apply model and collect labels
        System.out.println("Applying model to test data");
        var testReader = createReaderDescription( //
                Conll2006Reader.class, //
                Conll2006Reader.PARAM_PATTERNS, split.getTestFiles(), //
                Conll2006Reader.PARAM_READ_POS, false, //
                Conll2006Reader.PARAM_LANGUAGE, ds.getLanguage());

        var postagger = createEngineDescription( //
                OpenNlpPosTagger.class, //
                OpenNlpPosTagger.PARAM_PRINT_TAGSET, true, //
                OpenNlpPosTagger.PARAM_MODEL_LOCATION, new File(targetFolder, "model.bin"));

        var actual = EvalUtil.loadSamples(iteratePipeline(testReader, postagger), POS.class,
                pos -> pos.getPosValue());
        System.out.printf("Actual samples: %d%n", actual.size());

        // Read reference data collect labels
        setParameter(testReader, Conll2006Reader.PARAM_READ_POS, true);
        var expected = EvalUtil.loadSamples(testReader, POS.class, pos -> {
            return pos.getPosValue();
        });
        System.out.printf("Expected samples: %d%n", expected.size());

        var results = EvalUtil.dumpResults(targetFolder, expected, actual);

        assertEquals(0.642212, results.getFscore(), 0.0001);
        assertEquals(0.642212, results.getPrecision(), 0.0001);
        assertEquals(0.642212, results.getRecall(), 0.0001);
    }
}
