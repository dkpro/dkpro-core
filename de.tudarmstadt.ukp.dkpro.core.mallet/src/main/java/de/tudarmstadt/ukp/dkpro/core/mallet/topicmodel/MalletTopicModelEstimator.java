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
import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

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
        instanceList.addThruPipe(new Instance(generateTokenSequence(aJCas), NONE_LABEL,
                metadata.getDocumentId(), metadata.getDocumentUri()));
    }

    protected TokenSequence generateTokenSequence(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        TokenSequence tokenStream = new TokenSequence();
        try {
            for (Entry<AnnotationFS, String> entry : FeaturePathFactory.select(aJCas.getCas(),
                    typeName)) {
                if (useLemma) {
                    // TODO: can a token even cover multiple lemmas? If not, the if/else construct
                    // can be simplified.
                    for (Lemma lemma : selectCovered(Lemma.class, entry.getKey())) {
                        String text = lemma.getValue();
                        if (text.length() >= minTokenLength) {
                            tokenStream.add(text);
                        }
                    }
                }
                else {
                    String text = entry.getValue();
                    if (text.length() >= minTokenLength) {
                        tokenStream.add(text);
                    }
                }
            }
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }
        return tokenStream;
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.batchProcessComplete();

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
        ParallelTopicModel model = new ParallelTopicModel(nTopics);
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
