/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.dl4j;

import de.tudarmstadt.ukp.dkpro.core.datasets.Dataset;
import de.tudarmstadt.ukp.dkpro.core.datasets.DatasetLoader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2002Reader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

public class Dl4jEmbeddingsTrainerTest
{
    private Dataset germevalData;

    @Test
    public void test()
        throws UIMAException, IOException
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
                Dl4jEmbeddingsTrainer.class,
                Dl4jEmbeddingsTrainer.PARAM_TARGET_LOCATION, model);
        
        SimplePipeline.runPipeline(trainReader, trainer);
    }

    @Before
    public void setup() throws IOException
    {
        DatasetLoader loader = new DatasetLoader(testContext.getCacheFolder());
        germevalData = loader.loadGermEval2014NER();
    }    

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}