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

package de.tudarmstadt.ukp.dkpro.core.mallet.lda;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static de.tudarmstadt.ukp.dkpro.core.mallet.lda.MalletLdaUtil.trainModel;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertTrue;

public class MalletLdaTopicModelInferencerTest
{
    private static final String TXT_DIR = "src/test/resources/txt";
    private static final String TXT_FILE_PATTERN = "[+]*.txt";

    private static final int N_TOPICS = 10;
    private static final int N_ITERATIONS = 50;
    private static final String LANGUAGE = "en";

    @Rule
    public DkproTestContext testContext = new DkproTestContext();

    @Test
    public void testInferencer()
            throws UIMAException, IOException
    {
        File modelFile = new File(testContext.getTestOutputFolder(), "model");
        trainModel(modelFile);

        // tag::example[]
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletLdaTopicModelInferencer.class,
                MalletLdaTopicModelInferencer.PARAM_MODEL_LOCATION, modelFile);
        // end::example[]

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, inferencer)) {
            for (TopicDistribution td : select(jcas, TopicDistribution.class)) {
                for (int i = 0; i < td.getTopicProportions().toArray().length; i++) {
                    double proportion = td.getTopicProportions().toArray()[i];
                    assertTrue(proportion > 0.0);
                    assertTrue(proportion < 1.0);
                }
            }
        }
    }
}
