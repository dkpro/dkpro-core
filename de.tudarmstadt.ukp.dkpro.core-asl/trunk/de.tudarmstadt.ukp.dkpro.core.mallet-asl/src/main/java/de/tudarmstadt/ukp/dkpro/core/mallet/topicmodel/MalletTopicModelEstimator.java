/*******************************************************************************
 * Copyright 2014
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.mallet.topicmodel;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This component estimates an LDA model using Mallet. It stores all incoming CAS' to Mallet
 * {@link Instance}s before estimating the model, using a {@link ParallelTopicModel}.
 *
 * @author Carsten Schnober
 *
 */
public class MalletTopicModelEstimator
    extends JCasAnnotator_ImplBase
{
    /**
     * The annotation type to use for the topic model. Default:
     * de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token.
     */
    public static final String PARAM_TYPE_NAME = "typeName";
    @ConfigurationParameter(name = PARAM_TYPE_NAME, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String typeName;

    /**
     * The target model file location.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;

    /**
     * The number of topics to estimate for the topic model.
     */
    public static final String PARAM_N_TOPICS = "nTopics";
    @ConfigurationParameter(name = PARAM_N_TOPICS, mandatory = true, defaultValue = "10")
    private int nTopics;

    /**
     * The number of threads to use during model estimation. Default: 1.
     */
    public static final String PARAM_N_THREADS = "nThreads";
    @ConfigurationParameter(name = PARAM_N_THREADS, mandatory = true, defaultValue = "1")
    private int nThreads;

    /**
     * The number of iterations during model estimation. Default: 1000.
     */
    public static final String PARAM_N_ITERATIONS = "nIterations";
    @ConfigurationParameter(name = PARAM_N_ITERATIONS, mandatory = true, defaultValue = "1000")
    private int nIterations;

    /**
     * If set, uses lemmas instead of original text as features.
     */
    public static final String PARAM_USE_LEMMA = "useLemma";
    @ConfigurationParameter(name = PARAM_USE_LEMMA, mandatory = true, defaultValue = "false")
    private boolean useLemma;

    /**
     * The number of iterations before hyperparameter optimization begins. Default: 100
     */
    public static final String PARAM_BURNIN_PERIOD = "burninPeriod";
    @ConfigurationParameter(name = PARAM_BURNIN_PERIOD, mandatory = true, defaultValue = "100")
    private int burninPeriod;

    /**
     * Interval for optimizing Dirichlet hyperparameters. Default: 50
     */
    public static final String PARAM_OPTIMIZE_INTERVAL = "optimizeInterval";
    @ConfigurationParameter(name = PARAM_OPTIMIZE_INTERVAL, mandatory = true, defaultValue = "50")
    private int optimizeInterval;

    /**
     * Set random seed. If set to -1 (default), uses random generator.
     */
    public static final String PARAM_RANDOM_SEED = "randomSeed";
    @ConfigurationParameter(name = PARAM_RANDOM_SEED, mandatory = true, defaultValue = "-1")
    private int randomSeed;

    /**
     * Define how often to save a serialized model during estimation. Default: 0 (only save when
     * estimation is done).
     */
    public static final String PARAM_SAVE_INTERVAL = "saveInterval";
    @ConfigurationParameter(name = PARAM_SAVE_INTERVAL, mandatory = true, defaultValue = "0")
    private int saveInterval;

    /**
     * Use a symmatric alpha value during model estimation? Default: false.
     */
    public static final String PARAM_USE_SYMMETRIC_ALPHA = "useSymmetricAlph";
    @ConfigurationParameter(name = PARAM_USE_SYMMETRIC_ALPHA, mandatory = true, defaultValue = "false")
    private boolean useSymmetricAlpha;

    /**
     * The interval in which to display the estimated topics. Default: 50.
     */
    public static final String PARAM_DISPLAY_INTERVAL = "displayInterval";
    @ConfigurationParameter(name = PARAM_DISPLAY_INTERVAL, mandatory = true, defaultValue = "50")
    private int displayInterval;

    /**
     * The number of top words to display during estimation. Default: 7.
     */
    public static final String PARAM_DISPLAY_N_TOPIC_WORDS = "displayNTopicWords";
    @ConfigurationParameter(name = PARAM_DISPLAY_N_TOPIC_WORDS, mandatory = true, defaultValue = "7")
    private int displayNTopicWords;

    /**
     * Ignore tokens (or lemmas, respectively) that are shorter than the given value. Default: 3.
     */
    public static final String PARAM_MIN_TOKEN_LENGTH = "minTokenLength";
    @ConfigurationParameter(name = PARAM_MIN_TOKEN_LENGTH, mandatory = true, defaultValue = "3")
    private int minTokenLength;

    /**
     * If specific, the text contained in the given segmentation type annotations are fed as
     * separate units to the topic model estimator e.g.
     * {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.sentence}. Text that is not within
     * such annotations is ignored.
     * <p>
     * By default, the full document text is used as a document.
     */
    public static final String PARAM_MODEL_ENTITY_TYPE = "modelEntityType";
    @ConfigurationParameter(name = PARAM_MODEL_ENTITY_TYPE, mandatory = false)
    String modelEntityType;

    /**
     * The sum of alphas over all topics. Default: 1.0.
     * <p>
     * Another recommended value is 50 / T (number of topics).
     */
    public static final String PARAM_ALPHA_SUM = "alphaSum";
    @ConfigurationParameter(name = PARAM_ALPHA_SUM, mandatory = true, defaultValue = "1.0f")
    float alphaSum;

    /**
     * Beta for a single dimension of the Dirichlet prior. Default: 0.01.
     */
    public static final String PARAM_BETA = "beta";
    @ConfigurationParameter(name = PARAM_BETA, mandatory = true, defaultValue = "0.01f")
    float beta;

    protected static final String NONE_LABEL = "X"; // some label has to be set for Mallet instances
    protected InstanceList instanceList; // contains the Mallet instances

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        instanceList = new InstanceList(new TokenSequence2FeatureSequence());
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        DocumentMetaData metadata = DocumentMetaData.get(aJCas);
        try {
            for (TokenSequence ts : generateTokenSequences(aJCas)) {
                instanceList.addThruPipe(new Instance(ts, NONE_LABEL, metadata.getDocumentId(),
                        metadata.getDocumentUri()));
            }
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * Generate one or multiple TokenSequences from the given document. If
     * {@code PARAM_MODEL_ENTITY_TYPE} is set, an instance is generated from each segment annotated
     * with the given type. Otherwise, one instance is generated from the whole document.
     *
     * @param aJCas
     * @return
     * @throws FeaturePathException
     */
    private Collection<TokenSequence> generateTokenSequences(JCas aJCas)
        throws FeaturePathException
    {
        Collection<TokenSequence> tokenSequences;
        CAS cas = aJCas.getCas();
        Type tokenType = CasUtil.getType(cas, typeName);

        if (modelEntityType == null) {
            /* generate only one tokenSequence */
            tokenSequences = new ArrayList<>(1);
            tokenSequences.add(generateTokenSequence(aJCas, tokenType, useLemma, minTokenLength));
        }
        else {
            /* generate tokenSequences for every segment (e.g. sentence) */
            tokenSequences = new ArrayList<>();
            for (AnnotationFS segment : CasUtil.select(cas, CasUtil.getType(cas, modelEntityType))) {
                tokenSequences.add(generateTokenSequence(segment, tokenType));
            }
        }

        return tokenSequences;
    }

    /**
     * Generate a TokenSequence from the whole document.
     *
     * @param aJCas
     *            a CAS holding the document
     * @return a {@link TokenSequence}
     * @throws FeaturePathException
     *             if the annotation type specified in {@code PARAM_TYPE_NAME} cannot be extracted.
     */
    protected static TokenSequence generateTokenSequence(JCas aJCas, Type tokenType,
            boolean useLemma, int minTokenLength)
        throws FeaturePathException
    {
        TokenSequence tokenSequence = new TokenSequence();
        for (AnnotationFS token : CasUtil.select(aJCas.getCas(), tokenType)) {
            for (String tokenText : getTokensFromAnnotation(token, useLemma, minTokenLength)) {
                tokenSequence.add(tokenText);
            }
        }
        return tokenSequence;
    }

    private static Collection<String> getTokensFromAnnotation(AnnotationFS annotation,
            boolean useLemma, int minTokenLength)
    {
        Collection<String> tokens;
        if (useLemma) {
            tokens = new ArrayList<>();
            for (Lemma lemma : selectCovered(Lemma.class, annotation)) {
                String text = lemma.getValue();
                if (text.length() >= minTokenLength) {
                    tokens.add(text);
                }
            }
        }
        else {
            tokens = new ArrayList<>(1);
            String text = annotation.getCoveredText();
            if (text.length() >= minTokenLength) {
                tokens.add(text);
            }
        }
        return tokens;
    }

    /**
     * Generate an instance from the text covered by the given annotation.
     *
     * @param annotation
     *            an annotation representing a document segment, e.g. {@link Sentence}.
     * @param tokenType
     *            the type to use for representing tokens, usually {@link Token}, but could also be
     *            any other type.
     * @return
     */
    private TokenSequence generateTokenSequence(AnnotationFS annotation, Type tokenType)
    {
        TokenSequence tokenSequence = new TokenSequence();

        for (AnnotationFS token : CasUtil.selectCovered(tokenType, annotation)) {
            for (String tokenText : getTokensFromAnnotation(token, useLemma, minTokenLength)) {
                tokenSequence.add(tokenText);
            }
        }

        return tokenSequence;
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            generateParallelModel();
        }
        catch (IOException | SecurityException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void generateParallelModel()
        throws IOException, SecurityException
    {
        targetLocation.getParentFile().mkdirs();
        ParallelTopicModel model = new ParallelTopicModel(nTopics, alphaSum, beta);
        model.addInstances(instanceList);
        model.setNumThreads(nThreads);
        model.setNumIterations(nIterations);
        model.setBurninPeriod(burninPeriod);
        model.setOptimizeInterval(optimizeInterval);
        model.setRandomSeed(randomSeed);
        model.setSaveSerializedModel(saveInterval, targetLocation.getAbsolutePath());
        model.setSymmetricAlpha(useSymmetricAlpha);
        model.setTopicDisplay(displayInterval, displayNTopicWords);
        model.estimate();
        model.write(targetLocation);
    }
}
