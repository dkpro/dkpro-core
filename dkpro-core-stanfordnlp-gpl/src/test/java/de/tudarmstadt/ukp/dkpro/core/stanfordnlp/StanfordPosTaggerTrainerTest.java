/**
 * Copyright 2007-2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

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

public class StanfordPosTaggerTrainerTest
{
    @Test
    public void test()
        throws Exception
    {
        File cache = DkproTestContext.getCacheFolder();
        File targetFolder = testContext.getTestOutputFolder();
        
        // Obtain dataset
        DatasetFactory loader = new DatasetFactory(cache);
        Dataset ds = loader.load("gum-en-conll-2.2.0");
        Split split = ds.getSplit(0.8);
        
        Dataset dsCluster = loader.load("stanford-egw4-reut-512-clusters-20130608");
        File clusters = dsCluster.getDataFiles()[0];
        
        // Train model
        System.out.println("Training model from training data");
        CollectionReaderDescription trainReader = createReaderDescription(
                Conll2006Reader.class,
                Conll2006Reader.PARAM_PATTERNS, split.getTrainingFiles(),
                Conll2006Reader.PARAM_USE_CPOS_AS_POS, true,
                Conll2006Reader.PARAM_LANGUAGE, ds.getLanguage());
        
        AnalysisEngineDescription trainer = createEngineDescription(
                StanfordPosTaggerTrainer.class,
                StanfordPosTaggerTrainer.PARAM_PARAMETER_FILE, 
                "src/test/resources/postagger/train-english.props",
                StanfordPosTaggerTrainer.PARAM_CLUSTER_FILE, clusters,
                StanfordPosTaggerTrainer.PARAM_TARGET_LOCATION, 
                new File(targetFolder, "model.bin"));
        
        SimplePipeline.runPipeline(trainReader, trainer);
        
        // Apply model and collect labels
        System.out.println("Applying model to test data");
        CollectionReaderDescription testReader = createReaderDescription(
                Conll2006Reader.class,
                Conll2006Reader.PARAM_PATTERNS, split.getTestFiles(),
                Conll2006Reader.PARAM_READ_POS, false,
                Conll2006Reader.PARAM_LANGUAGE, ds.getLanguage());
        
        AnalysisEngineDescription ner = createEngineDescription(
                StanfordPosTagger.class,
                StanfordPosTagger.PARAM_PRINT_TAGSET, true,
                StanfordPosTagger.PARAM_MODEL_LOCATION, new File(targetFolder, "model.bin"));

        List<Span<String>> actual = EvalUtil.loadSamples(iteratePipeline(testReader, ner),
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
        
        assertEquals(0.757051, results.getFscore(), 0.0001);
        assertEquals(0.749061, results.getPrecision(), 0.0001);
        assertEquals(0.765212, results.getRecall(), 0.0001);
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
