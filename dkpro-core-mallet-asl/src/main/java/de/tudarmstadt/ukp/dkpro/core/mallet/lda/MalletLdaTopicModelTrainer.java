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
package de.tudarmstadt.ukp.dkpro.core.mallet.lda;

import java.io.File;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import de.tudarmstadt.ukp.dkpro.core.mallet.MalletModelTrainer;

/**
 * Estimate an LDA topic model using Mallet and write it to a file. It stores all incoming CAS' to
 * Mallet {@link Instance}s before estimating the model, using a {@link ParallelTopicModel}.
 * <p>
 * Set {@link #PARAM_TOKEN_FEATURE_PATH} to define what is considered as a token (Tokens, Lemmas,
 * etc.).
 * <p>
 * Set {@link #PARAM_COVERING_ANNOTATION_TYPE} to define what is considered a document (sentences,
 * paragraphs, etc.).
 */
@ResourceMetaData(name = "Mallet LDA Topic Model Trainer")
public class MalletLdaTopicModelTrainer
        extends MalletModelTrainer
{
    /**
     * The number of topics to estimate.
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
     * The number of iterations before hyper-parameter optimization begins. Default: 100
     */
    public static final String PARAM_BURNIN_PERIOD = "burninPeriod";
    @ConfigurationParameter(name = PARAM_BURNIN_PERIOD, mandatory = true, defaultValue = "100")
    private int burninPeriod;

    /**
     * Interval for optimizing Dirichlet hyper-parameters. Default: 50
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
     * Define how frequently a serialized model is saved to disk during estimation. Default: 0 (only
     * save when estimation is done).
     */
    public static final String PARAM_SAVE_INTERVAL = "saveInterval";
    @ConfigurationParameter(name = PARAM_SAVE_INTERVAL, mandatory = true, defaultValue = "0")
    private int saveInterval;

    /**
     * Use a symmetric alpha value during model estimation? Default: false.
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
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        try {
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

            getLogger().info("Writing model to " + getTargetLocation());
            File targetFile = new File(getTargetLocation());
            if (targetFile.getParentFile() != null) {
                targetFile.getParentFile().mkdirs();
            }
            model.write(targetFile);
        }
        catch (IOException | SecurityException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

}
