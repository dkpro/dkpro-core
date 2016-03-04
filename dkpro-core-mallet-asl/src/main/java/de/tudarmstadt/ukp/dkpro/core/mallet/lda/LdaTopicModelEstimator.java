/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.mallet.lda;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import de.tudarmstadt.ukp.dkpro.core.mallet.MalletModelEstimator;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;

/**
 * Estimate an LDA topic model using Mallet and write it to a file. It stores all incoming CAS' to
 * Mallet {@link Instance}s before estimating the model, using a {@link ParallelTopicModel}.
 */
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" }
)
public class LdaTopicModelEstimator
        extends MalletModelEstimator
{
    /**
     * The number of topics to estimate for the topic model.
     */
    public static final String PARAM_N_TOPICS = "nTopics";
    @ConfigurationParameter(name = PARAM_N_TOPICS, mandatory = true, defaultValue = "10")
    private int nTopics;

    /**
     * The number of iterations during model estimation. Default: 1000.
     */
    public static final String PARAM_N_ITERATIONS = "nIterations";
    @ConfigurationParameter(name = PARAM_N_ITERATIONS, mandatory = true, defaultValue = "1000")
    private int nIterations;

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
    public static final String PARAM_USE_SYMMETRIC_ALPHA = "useSymmetricAlpha";
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
     * The sum of alphas over all topics. Default: 1.0.
     * <p>
     * Another recommended value is 50 / T (number of topics).
     */
    public static final String PARAM_ALPHA_SUM = "alphaSum";
    @ConfigurationParameter(name = PARAM_ALPHA_SUM, mandatory = true, defaultValue = "1.0f")
    private float alphaSum;

    /**
     * Beta for a single dimension of the Dirichlet prior. Default: 0.01.
     */
    public static final String PARAM_BETA = "beta";
    @ConfigurationParameter(name = PARAM_BETA, mandatory = true, defaultValue = "0.01f")
    private float beta;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
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
        new File(getTargetLocation()).getParentFile().mkdirs();
        ParallelTopicModel model = new ParallelTopicModel(nTopics, alphaSum, beta);
        model.addInstances(getInstanceList());
        model.setNumThreads(getNumThreads());
        model.setNumIterations(nIterations);
        model.setBurninPeriod(burninPeriod);
        model.setOptimizeInterval(optimizeInterval);
        model.setRandomSeed(randomSeed);
        model.setSaveSerializedModel(saveInterval, getTargetLocation());
        model.setSymmetricAlpha(useSymmetricAlpha);
        model.setTopicDisplay(displayInterval, displayNTopicWords);
        model.estimate();
        model.write(new File(getTargetLocation()));
    }
}
