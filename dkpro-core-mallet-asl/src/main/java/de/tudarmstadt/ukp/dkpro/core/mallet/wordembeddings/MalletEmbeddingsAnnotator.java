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
package de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.mallet.MalletModelTrainer;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.WordEmbedding;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FloatArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.embeddings.Vectorizer;
import org.dkpro.core.api.embeddings.binary.BinaryVectorizer;
import org.dkpro.core.api.embeddings.text.TextFormatVectorizer;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Reads word embeddings from a file and adds {@link WordEmbedding} annotations to tokens/lemmas.
 *
 * @since 1.9.0
 */
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" },
        outputs = { "de.tudarmstadt.ukp.dkpro.core.mallet.type.WordEmbedding" }
)
public class MalletEmbeddingsAnnotator
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

    public static final String PARAM_MODEL_IS_BINARY = "modelIsBinary";
    @ConfigurationParameter(name = PARAM_MODEL_IS_BINARY, mandatory = true, defaultValue = "false")
    private boolean modelIsBinary;
    private Vectorizer vectorizer;

    /**
     * Specify how to handle unknown tokens:
     * <ol>
     * <li>If this parameter is not specified, unknown tokens are not annotated.</li>
     * <li>If an empty float[] is passed, a random vector is generated that is used for each unknown token.</li>
     * <li>If a float[] is passed, each unknown token is annotated with that vector. The float must have the same length as the vectors in the model file.</li>
     * </ol>
     */
    public static final String PARAM_ANNOTATE_UNKNOWN_TOKENS = "annotateUnknownTokens";
    @ConfigurationParameter(name = PARAM_ANNOTATE_UNKNOWN_TOKENS, mandatory = true, defaultValue = "false")
    private boolean annotateUnknownTokens;

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
    public static final String PARAM_TOKEN_FEATURE_PATH = MalletModelTrainer.PARAM_TOKEN_FEATURE_PATH;
    @ConfigurationParameter(name = PARAM_TOKEN_FEATURE_PATH, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String tokenFeaturePath;

    /**
     * If set to true (default: false), all tokens are lowercased.
     */
    public static final String PARAM_LOWERCASE = "lowercase";
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = true, defaultValue = "false")
    private boolean lowercase;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);
        if (modelHasHeader && modelIsBinary) {
            throw new ResourceInitializationException(new IllegalArgumentException(
                    "The parameter PARAM_MODEL_HAS_HEADER is only valid for text-format model files."));
        }

        try {
            vectorizer = modelIsBinary ?
                    BinaryVectorizer.load(modelLocation) :
                    TextFormatVectorizer.load(modelLocation);
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

        for (AnnotationFS token : CasUtil.select(aJCas.getCas(), type)) {
            try {
                addAnnotation(aJCas, token.getCoveredText(), token.getBegin(), token.getEnd());
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    private void addAnnotation(JCas aJCas, String text, int begin, int end)
            throws IOException
    {
        if (lowercase) {
            text = text.toLowerCase();
        }
        Optional<float[]> vector = getVector(text);

        if (vector.isPresent()) {
            WordEmbedding embedding = new WordEmbedding(aJCas, begin, end);
            FloatArray array = new FloatArray(aJCas, vector.get().length);
            for (int i = 0; i < vector.get().length; i++) {
                array.set(i, vector.get()[i]);
            }
            embedding.setWordEmbedding(array);
            embedding.addToIndexes(aJCas);
        }
        else {
            getLogger().debug(text + " not found in embeddings list.");
        }
    }

    /**
     * If {@link #PARAM_ANNOTATE_UNKNOWN_TOKENS} is set to true, always return a vector retrieved
     * from the vectorizer, which should hold a stable random vector for unknown tokens.
     * Otherwise, return a vector for known tokens, or none if the token is unknown.
     *
     * @param token a token for which to look up an embedding
     * @return an {@code Optional<float[]>} that holds the token embedding or is empty if no embeddings is available for the token
     * @throws IOException if an I/O error occurs
     */
    private Optional<float[]> getVector(String token)
            throws IOException
    {
        if (annotateUnknownTokens) {
            return Optional.of(vectorizer.vectorize(token));
        }
        else {
            return vectorizer.contains(token) ?
                    Optional.of(vectorizer.vectorize(token)) :
                    Optional.empty();
        }
    }
}
