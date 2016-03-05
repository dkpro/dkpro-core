/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.mallet.MalletModelEstimator;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.WordEmbedding;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.apache.uima.fit.util.JCasUtil.select;

/**
 * Reads word embeddings from a file and adds {@link WordEmbedding} annotations to tokens/lemmas.
 */
public class WordEmbeddingsAnnotator
        extends JCasAnnotator_ImplBase
{
    /**
     * The file containing the word embeddings.
     * <p>
     * Currently only supports text file format.
     * </p>
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    private File modelLocation;
    private Map<String, double[]> embeddings;

    /**
     * If set to true (default: false), the first line is interpreted as header line containing the number of entries and the dimensionality.
     * This should be set to true for models generated with Word2Vec.
     */
    public static final String PARAM_MODEL_HAS_HEADER = "modelHasHeader";
    @ConfigurationParameter(name = PARAM_MODEL_HAS_HEADER, mandatory = true, defaultValue = "false")
    private boolean modelHasHeader;

    /**
     * If true (default: false), annotate lemmas instead of tokens.
     */
    public static final String PARAM_USE_LEMMAS = MalletModelEstimator.PARAM_USE_LEMMA;
    @ConfigurationParameter(name = PARAM_USE_LEMMAS, mandatory = true, defaultValue = "false")
    private boolean useLemmas;

    /**
     * If set to true (default: false), the model is expected to be in Word2Vec binary format.
     * TODO: this is not supported yet.
     */
    //    public static final String PARAM_MODEL_IS_BINARY = "modelIsBinary";
    //    @ConfigurationParameter(name = PARAM_MODEL_IS_BINARY, mandatory = true, defaultValue = "false")
    //    private boolean modelIsBinary;
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);
        try {
            embeddings = WordEmbeddingsUtils.readEmbeddingFileTxt(modelLocation, modelHasHeader);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        if (useLemmas) {
            select(aJCas, Lemma.class).stream()
                    .map(lemma -> Triple.of(lemma.getValue(), lemma.getBegin(), lemma.getEnd()))
                    .forEach(triple -> addAnnotation(aJCas, triple));
        }
        else {
            select(aJCas, Token.class).stream()
                    .map(token -> Triple
                            .of(token.getCoveredText(), token.getBegin(), token.getEnd()))
                    .forEach(triple -> addAnnotation(aJCas, triple));
        }
    }

    private void addAnnotation(JCas aJCas, Triple<String, Integer, Integer> triple)
    {
        String text = triple.getLeft();
        if (embeddings.containsKey(text)) {
            double[] vector = embeddings.get(text);
            WordEmbedding embedding = new WordEmbedding(
                    aJCas, triple.getMiddle(), triple.getRight());
            DoubleArray da = new DoubleArray(aJCas, vector.length);
            for (int i = 0; i < vector.length; i++) {
                da.set(i, vector[i]);
            }
            embedding.setWordEmbedding(da);
            embedding.addToIndexes(aJCas);
        }
        else {
            getLogger().debug(text + " not found in embeddings list.");
        }
    }
}
