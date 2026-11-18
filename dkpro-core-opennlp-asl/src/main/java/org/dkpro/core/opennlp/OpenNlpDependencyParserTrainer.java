/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.opennlp;

import java.util.concurrent.Callable;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.opennlp.internal.CasDependencySampleStream;
import org.dkpro.core.opennlp.internal.OpenNlpTrainerBase;

import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import opennlp.tools.depparse.DependencyParserME;
import opennlp.tools.depparse.FeedforwardDependencyTrainer;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;

/**
 * Trains a model for the {@link OpenNlpDependencyParser}. Uses the basic dependencies, tokens and
 * part-of-speech tags of each sentence. Sentences which do not form a single complete dependency
 * tree are skipped.
 */
@Component(OperationType.TRAINER_OF_MACHINE_LEARNING_MODELS)
@MimeTypeCapability(MimeTypes.APPLICATION_X_OPENNLP_DEPPARSE)
@ResourceMetaData(name = "OpenNLP Dependency Parser Trainer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
public class OpenNlpDependencyParserTrainer
    extends OpenNlpTrainerBase<CasDependencySampleStream>
{
    /**
     * Algorithm value which selects the feed-forward neural network parser instead of the maximum
     * entropy parser.
     */
    public static final String FEEDFORWARD = "FEEDFORWARD";

    /**
     * Store this language to the model instead of the document language.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE)
    private String language;

    /**
     * Training algorithm. Use {@link #FEEDFORWARD} to train a feed-forward neural network parser.
     * Any other value (e.g. {@code MAXENT}, {@code MAXENT_QN}, {@code PERCEPTRON}) trains a
     * transition classifier with the respective OpenNLP event trainer.
     */
    public static final String PARAM_ALGORITHM = "algorithm";
    @ConfigurationParameter(name = PARAM_ALGORITHM, defaultValue = "MAXENT")
    private String algorithm;

    /**
     * Number of training iterations (epochs for the {@link #FEEDFORWARD} parser).
     */
    public static final String PARAM_ITERATIONS = "iterations";
    @ConfigurationParameter(name = PARAM_ITERATIONS, defaultValue = "100")
    private int iterations;

    /**
     * Frequency cut-off (word cut-off for the {@link #FEEDFORWARD} parser).
     */
    public static final String PARAM_CUTOFF = "cutoff";
    @ConfigurationParameter(name = PARAM_CUTOFF, defaultValue = "5")
    private int cutoff;

    /**
     * Number of parallel threads.
     */
    public static final String PARAM_NUM_THREADS = ComponentParameters.PARAM_NUM_THREADS;
    @ConfigurationParameter(name = PARAM_NUM_THREADS, defaultValue = "1")
    private int numThreads;

    @Override
    public CasDependencySampleStream makeSampleStream()
    {
        return new CasDependencySampleStream();
    }

    @Override
    public Callable<? extends BaseModel> makeTrainer()
    {
        var params = new TrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(iterations));
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(cutoff));
        params.put(TrainingParameters.THREADS_PARAM, Integer.toString(numThreads));

        boolean feedforward = FEEDFORWARD.equals(algorithm);
        if (!feedforward) {
            params.put(TrainingParameters.ALGORITHM_PARAM, algorithm);
            params.put(TrainingParameters.TRAINER_TYPE_PARAM, EventTrainer.EVENT_VALUE);
        }

        return () -> {
            try {
                if (feedforward) {
                    return FeedforwardDependencyTrainer.train(language, getStream(), params);
                }
                return DependencyParserME.train(language, getStream(), params);
            }
            catch (Throwable e) {
                getStream().close();
                throw e;
            }
        };
    }
}
