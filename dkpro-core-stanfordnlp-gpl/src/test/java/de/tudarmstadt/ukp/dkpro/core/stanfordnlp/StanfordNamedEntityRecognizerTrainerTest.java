/*
 * Copyright 2007-2019
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

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

import de.tudarmstadt.ukp.dkpro.core.api.datasets.Dataset;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.DatasetFactory;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.Split;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.eval.EvalUtil;
import de.tudarmstadt.ukp.dkpro.core.eval.model.Span;
import de.tudarmstadt.ukp.dkpro.core.eval.report.Result;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2002Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2002Reader.ColumnSeparators;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2002Writer;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class StanfordNamedEntityRecognizerTrainerTest
{
    private Dataset ds;

    @Test
    public void test()
        throws Exception
    {
        File targetFolder = testContext.getTestOutputFolder();

        System.out.println("Target Folder: " + targetFolder.getAbsolutePath());
        Split split = ds.getDefaultSplit();

        File model = new File(targetFolder, "ner-model.ser.gz");

        File properties = new File("ner/train-english.props");

        File[] trainingFiles = split.getTrainingFiles();
        for (File file : trainingFiles) {
            System.out.println("Training file: " + file.getAbsolutePath());
        }

        CollectionReaderDescription trainReader = createReaderDescription(Conll2002Reader.class,
                Conll2002Reader.PARAM_PATTERNS, split.getDevelopmentFiles(),
                Conll2002Reader.PARAM_LANGUAGE, ds.getLanguage(),
                Conll2002Reader.PARAM_COLUMN_SEPARATOR, ColumnSeparators.TAB.getName(),
                Conll2002Reader.PARAM_HAS_TOKEN_NUMBER, true, 
                Conll2002Reader.PARAM_HAS_HEADER, true, 
                Conll2002Reader.PARAM_HAS_EMBEDDED_NAMED_ENTITY, true);

        AnalysisEngineDescription trainer = createEngineDescription(
                StanfordNamedEntityRecognizerTrainer.class,
                StanfordNamedEntityRecognizerTrainer.PARAM_TARGET_LOCATION, model,
                StanfordNamedEntityRecognizerTrainer.PARAM_PROPERTIES_LOCATION, properties,
                StanfordNamedEntityRecognizerTrainer.PARAM_LABEL_SET, "noprefix",
                StanfordNamedEntityRecognizerTrainer.PARAM_RETAIN_CLASS, true);

        SimplePipeline.runPipeline(trainReader, trainer);

        // Apply model and collect labels
        System.out.println("Applying model to test data");
        CollectionReaderDescription testReader = createReaderDescription(Conll2002Reader.class,
                Conll2002Reader.PARAM_PATTERNS, split.getTestFiles(),
                Conll2002Reader.PARAM_LANGUAGE, "de", 
                Conll2002Reader.PARAM_COLUMN_SEPARATOR, ColumnSeparators.TAB.getName(),
                Conll2002Reader.PARAM_HAS_TOKEN_NUMBER, true, 
                Conll2002Reader.PARAM_HAS_HEADER, true, 
                Conll2002Reader.PARAM_HAS_EMBEDDED_NAMED_ENTITY, true,
                Conll2002Reader.PARAM_READ_NAMED_ENTITY, false);

        AnalysisEngineDescription ner = createEngineDescription(StanfordNamedEntityRecognizer.class,
                StanfordNamedEntityRecognizer.PARAM_PRINT_TAGSET, true,
                StanfordNamedEntityRecognizer.PARAM_MODEL_LOCATION, model);

        AnalysisEngineDescription writer = createEngineDescription(
                Conll2002Writer.class,
                Conll2002Writer.PARAM_SINGULAR_TARGET, true,
                Conll2002Writer.PARAM_TARGET_LOCATION, new File(targetFolder, "output.conll"));
        
        List<Span<String>> actual = EvalUtil.loadSamples(iteratePipeline(testReader, ner, writer),
                NamedEntity.class, NamedEntity::getValue);
        System.out.printf("Actual samples: %d%n", actual.size());

        // Read reference data collect labels
        ConfigurationParameterFactory.setParameter(testReader,
                Conll2002Reader.PARAM_READ_NAMED_ENTITY, true);
        List<Span<String>> expected = EvalUtil.loadSamples(testReader, NamedEntity.class,
                NamedEntity::getValue);
        System.out.printf("Expected samples: %d%n", expected.size());

        Result results = EvalUtil.dumpResults(targetFolder, expected, actual);

        // Using split.getTrainingFiles() with 10GB heap takes ~80 minutes to train
        // F-score     0.692730
        // Precision   0.765778
        // Recall      0.632405
        
        // 
        assertEquals(0.493260, results.getFscore(), 0.0001);
        assertEquals(0.621921, results.getPrecision(), 0.0001);
        assertEquals(0.408708, results.getRecall(), 0.0001);
    }

    @Before
    public void setup()
        throws IOException
    {
        DatasetFactory loader = new DatasetFactory(DkproTestContext.getCacheFolder());
        ds = loader.load("germeval2014-de");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
