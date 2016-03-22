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
import de.tudarmstadt.ukp.dkpro.core.mallet.MalletModelEstimator;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.WordEmbedding;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Reads word embeddings from a file and adds {@link WordEmbedding} annotations to tokens/lemmas.
 * @since 1.9.0
 */
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" },
        outputs = { "de.tudarmstadt.ukp.dkpro.core.mallet.type.WordEmbedding" }
)
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
    // TODO: optionally read the embeddings from some disk-cache
    private Map<String, double[]> embeddings;

    /**
     * If set to true (default: false), the first line is interpreted as header line containing the number of entries and the dimensionality.
     * This should be set to true for models generated with Word2Vec.
     */
    public static final String PARAM_MODEL_HAS_HEADER = "modelHasHeader";
    @ConfigurationParameter(name = PARAM_MODEL_HAS_HEADER, mandatory = true, defaultValue = "false")
    private boolean modelHasHeader;

    /**
     * The annotation type to use for the model. Default: {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token}.
     * For lemmas, use {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token/lemma/value}
     */
    public static final String PARAM_TOKEN_FEATURE_PATH = MalletModelEstimator.PARAM_TOKEN_FEATURE_PATH;
    @ConfigurationParameter(name = PARAM_TOKEN_FEATURE_PATH, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String tokenFeaturePath;

    /**
     * If set to true (default: false), all tokens are lowercased.
     */
    public static final String PARAM_LOWERCASE = "lowercase";
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = true, defaultValue = "false")
    private boolean lowercase;

    /**
     * If set to true (default: false), the model is expected to be in Word2Vec binary format.
     * TODO: binary model format is not supported yet.
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
        Type type = aJCas.getTypeSystem().getType(tokenFeaturePath);

        CasUtil.select(aJCas.getCas(), type)
                .forEach(token -> addAnnotation(aJCas,
                        token.getCoveredText(), token.getBegin(), token.getEnd()));
    }

    private void addAnnotation(JCas aJCas, String text, int begin, int end)
    {
        if (lowercase) {
            text = text.toLowerCase();
        }
        if (embeddings.containsKey(text)) {
            double[] vector = embeddings.get(text);
            WordEmbedding embedding = new WordEmbedding(aJCas, begin, end);
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
