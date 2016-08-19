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
import java.io.IOException;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.datasets.Dataset;
import de.tudarmstadt.ukp.dkpro.core.datasets.DatasetFactory;
import de.tudarmstadt.ukp.dkpro.core.eval.EvalUtil;
import de.tudarmstadt.ukp.dkpro.core.eval.model.Span;
import de.tudarmstadt.ukp.dkpro.core.eval.report.Result;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2002Reader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class OpenNlpNamedEntityRecognizerTrainerTest
{
    private Dataset germevalData;
    
    @Test
    public void test()
        throws Exception
    {
        File targetFolder = testContext.getTestOutputFolder();
        
        // Train model
        File model = new File(targetFolder, "model.bin");
        CollectionReaderDescription trainReader = createReaderDescription(
                Conll2002Reader.class,
                Conll2002Reader.PARAM_PATTERNS, germevalData.getTrainingFiles(),
                Conll2002Reader.PARAM_LANGUAGE, germevalData.getLanguage(),
                Conll2002Reader.PARAM_COLUMN_SEPARATOR, Conll2002Reader.ColumnSeparators.TAB.getName(),
                Conll2002Reader.PARAM_HAS_TOKEN_NUMBER, true,
                Conll2002Reader.PARAM_HAS_HEADER, true,
                Conll2002Reader.PARAM_HAS_EMBEDDED_NAMED_ENTITY, true);
        
        AnalysisEngineDescription trainer = createEngineDescription(
                OpenNlpNamedEntityRecognizerTrainer.class,
                OpenNlpNamedEntityRecognizerTrainer.PARAM_TARGET_LOCATION, model,
                OpenNlpNamedEntityRecognizerTrainer.PARAM_LANGUAGE, "de",
                OpenNlpNamedEntityRecognizerTrainer.PARAM_ITERATIONS, 10);
        
        SimplePipeline.runPipeline(trainReader, trainer);
        
        // Apply model and collect labels
        System.out.println("Applying model to test data");
        CollectionReaderDescription testReader = createReaderDescription(
                Conll2002Reader.class,
                Conll2002Reader.PARAM_PATTERNS, germevalData.getTestFiles(),
                Conll2002Reader.PARAM_LANGUAGE, "de",
                Conll2002Reader.PARAM_COLUMN_SEPARATOR, Conll2002Reader.ColumnSeparators.TAB.getName(),
                Conll2002Reader.PARAM_HAS_TOKEN_NUMBER, true,
                Conll2002Reader.PARAM_HAS_HEADER, true,
                Conll2002Reader.PARAM_HAS_EMBEDDED_NAMED_ENTITY, true,
                Conll2002Reader.PARAM_READ_NAMED_ENTITY, false);
        
        AnalysisEngineDescription ner = createEngineDescription(
                OpenNlpNamedEntityRecognizer.class,
                OpenNlpNamedEntityRecognizer.PARAM_PRINT_TAGSET, true,
                OpenNlpNamedEntityRecognizer.PARAM_MODEL_LOCATION, model);

        List<Span<String>> actual = EvalUtil.loadSamples(iteratePipeline(testReader, ner),
                NamedEntity.class, ne -> {
                    return ne.getValue();
                });
        System.out.printf("Actual samples: %d%n", actual.size());
        
        // Read reference data collect labels
        ConfigurationParameterFactory.setParameter(testReader, 
                Conll2002Reader.PARAM_READ_NAMED_ENTITY, true);
        List<Span<String>> expected = EvalUtil.loadSamples(testReader, NamedEntity.class,  
                ne -> { return ne.getValue(); });
        System.out.printf("Expected samples: %d%n", expected.size());

        Result results = EvalUtil.dumpResults(targetFolder, expected, actual);
        
        assertEquals(0.316834, results.getFscore(), 0.0001);
        assertEquals(0.868650, results.getPrecision(), 0.0001);
        assertEquals(0.193752, results.getRecall(), 0.0001);
    }
    
    @Before
    public void setup() throws IOException
    {
        DatasetFactory loader = new DatasetFactory(testContext.getCacheFolder());
        germevalData = loader.load("germeval2014-de");
    }    
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
