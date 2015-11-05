/*******************************************************************************
 * Copyright 2015
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.examples.lda;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.mallet.topicmodel.MalletTopicModelInferencer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.File;
import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 * This pipeline infers topic distributions in a document set according to a previously estimated LDA topic model.
 * It uses five components:
 * <ol>
 * <li>A reader to read the input texts from disk: {@link TextReader}</li>
 * <li>A segmenter to split text into sentences and tokens: {@link OpenNlpSegmenter}</li>
 * <li>A stop word remover: {@link StopWordRemover}</li>
 * <li>The Mallet LDA topic model estimator: {@link MalletTopicModelInferencer}</li>
 * <li>A {@link CasDumpWriter} that writes all annotations, including the topic proportions, to the console.</li>
 * </ol>
 * <p>
 * The model file is expected to be located in {@code target/model.mallet}. If you run the {@link LdaEstimationPipeline},
 * it will create a suitable file there. The model has to be in the {@link cc.mallet.topics.ParallelTopicModel} format
 * as created by the DKPro {@link MalletTopicModelInferencer} or by Mallet directly.
 * <p>
 * The topic distributions are stored as document-wide {@code TopicDistribution} annotations in a double array where
 * each entry represents a topic weight.
 * <p>
 * This example uses very short documents and the model created by {@link LdaEstimationPipeline}
 * does not produce meaningful results. However, the example pipeline can be used as is for different
 * models and input sources.
 * </p>
 */
public class LdaInferencePipeline
{
    private static final File MODEL_FILE = new File("target/model.mallet");
    private static final String LANGUAGE = "en";
    private static final File STOPWORD_FILE = new File("src/main/resources/stopwords_en.txt");
    private static final String DEFAULT_SOURCE_DIR = "src/main/resources/texts/*";

    public static void main(String[] args)
            throws IOException, UIMAException
    {
        String inputDir = args.length > 0 ? args[0] : DEFAULT_SOURCE_DIR;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, inputDir,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
        AnalysisEngineDescription stopwordRemover = createEngineDescription(StopWordRemover.class,
                StopWordRemover.PARAM_MODEL_LOCATION, STOPWORD_FILE);
        AnalysisEngineDescription lda = createEngineDescription(MalletTopicModelInferencer.class,
                MalletTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE);
        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class);

        SimplePipeline.runPipeline(reader, segmenter, stopwordRemover, lda, writer);
    }
}
