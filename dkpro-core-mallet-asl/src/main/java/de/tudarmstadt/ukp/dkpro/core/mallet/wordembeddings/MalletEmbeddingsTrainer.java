/*
 * Copyright 2017
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
package de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;

import cc.mallet.topics.WordEmbeddings;
import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.mallet.MalletModelTrainer;

/**
 * Compute word embeddings from the given collection using skip-grams.
 * <p>
 * Set {@link #PARAM_TOKEN_FEATURE_PATH} to define what is considered as a token (Tokens, Lemmas,
 * etc.).
 * <p>
 * Set {@link #PARAM_COVERING_ANNOTATION_TYPE} to define what is considered a document (sentences,
 * paragraphs, etc.).
 *
 * @since 1.9.0
 */
@ResourceMetaData(name = "Mallet Embeddings Trainer")
public class MalletEmbeddingsTrainer
        extends MalletModelTrainer
{
    /**
     * The number of negative samples to be generated for each token (default: 5).
     */
    public static final String PARAM_NUM_NEGATIVE_SAMPLES = "numNegativeSamples";
    @ConfigurationParameter(name = PARAM_NUM_NEGATIVE_SAMPLES, mandatory = true, defaultValue = "5")
    private int numNegativeSamples;

    /**
     * The dimensionality of the output word embeddings (default: 50).
     */
    public static final String PARAM_DIMENSIONS = "dimensions";
    @ConfigurationParameter(name = PARAM_DIMENSIONS, mandatory = true, defaultValue = "50")
    private int dimensions;

    /**
     * The context size when generating embeddings (default: 5).
     */
    public static final String PARAM_WINDOW_SIZE = "windowSize";
    @ConfigurationParameter(name = PARAM_WINDOW_SIZE, mandatory = true, defaultValue = "5")
    private int windowSize;

    /**
     * An example word that is output with its nearest neighbours once in a while (default: null,
     * i.e. none).
     */
    public static final String PARAM_EXAMPLE_WORD = "exampleWord";
    @ConfigurationParameter(name = PARAM_EXAMPLE_WORD, mandatory = false)
    private String exampleWord;

    /**
     * Ignore documents with fewer tokens than this value (default: 10).
     */
    public static final String PARAM_MIN_DOCUMENT_LENGTH = "minDocumentLength";
    @ConfigurationParameter(name = PARAM_MIN_DOCUMENT_LENGTH, mandatory = true, defaultValue = "10")
    private int minDocumentLength;

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        InstanceList instanceList = getInstanceList();
        Alphabet alphabet = instanceList.getDataAlphabet();
        int vocabSize = alphabet.size();

        getLogger().info(
                String.format("Computing word embeddings with %d dimensions for %d tokens...",
                        dimensions, vocabSize));

        if (vocabSize * dimensions * 2 > Integer.MAX_VALUE - 12) {
            throw new AnalysisEngineProcessException(new IllegalStateException(String.format(
                    "Maximum matrix size (number of words * number of columns/dimensions * 2 exceeded: %d * %d * 2 = %d",
                    vocabSize, dimensions, vocabSize * dimensions * 2)));
        }
        WordEmbeddings matrix = new WordEmbeddings(alphabet, dimensions, windowSize);
        matrix.setQueryWord(exampleWord);
        matrix.setMinDocumentLength(minDocumentLength);
        matrix.countWords(instanceList);
        matrix.train(instanceList, getNumThreads(), numNegativeSamples);

        assert getTargetLocation() != null;
        getLogger().info("Writing output to " + getTargetLocation());
        File targetFile = new File(getTargetLocation());
        if (targetFile.getParentFile() != null) {
            targetFile.getParentFile().mkdirs();
        }

        try {
            OutputStream outputStream = CompressionUtils.getOutputStream(targetFile);
            PrintWriter printWriter = new PrintWriter(outputStream);
            matrix.write(printWriter);
            printWriter.close();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
