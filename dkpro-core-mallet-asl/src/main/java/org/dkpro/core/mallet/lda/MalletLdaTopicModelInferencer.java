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
package org.dkpro.core.mallet.lda;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.featurepath.FeaturePathException;
import org.dkpro.core.api.io.sequencegenerator.PhraseSequenceGenerator;
import org.dkpro.core.api.io.sequencegenerator.StringSequenceGenerator;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.mallet.MalletModelTrainer;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Infers the topic distribution over documents using a Mallet {@link ParallelTopicModel}.
 */
@Component(OperationType.TOPIC_EXTRACTOR)
@ResourceMetaData(name = "Mallet LDA Topic Model Inferencer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" },
        outputs = { "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution" }
)
public class MalletLdaTopicModelInferencer
        extends JCasAnnotator_ImplBase
{
    private static final String NONE_LABEL = "X";

    /**
     * Location from which the model is read. This is either a local path or a classpath location.
     * In the latter case, the model artifact (if any) is searched as well.
     */
    public final static String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    private File modelLocation;

    /**
     * The annotation type to use as tokens.
     */
    public final static String PARAM_TYPE_NAME = "typeName";
    @ConfigurationParameter(name = PARAM_TYPE_NAME, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String typeName;

    /**
     * The number of iterations during inference.
     */
    public final static String PARAM_N_ITERATIONS = "nIterations";
    @ConfigurationParameter(name = PARAM_N_ITERATIONS, mandatory = true, defaultValue = "100")
    private int nIterations;

    /**
     * The number of iterations before hyper-parameter optimization begins.
     */
    public final static String PARAM_BURN_IN = "burnIn";
    @ConfigurationParameter(name = PARAM_BURN_IN, mandatory = true, defaultValue = "1")
    private int burnIn;

    /**
     * The number of iterations between saved samples.
     */
    public final static String PARAM_THINNING = "thinning";
    @ConfigurationParameter(name = PARAM_THINNING, mandatory = true, defaultValue = "5")
    private int thinning;

    /**
     * Minimum topic proportion for the document-topic assignment.
     */
    public final static String PARAM_MIN_TOPIC_PROB = "minTopicProb";
    @ConfigurationParameter(name = PARAM_MIN_TOPIC_PROB, mandatory = true, defaultValue = "0.2")
    private double minTopicProb;

    /**
     * Maximum number of topics to assign. If not set (or &lt;= 0), the number of topics in the
     * model divided by 10 is set.
     */
    public final static String PARAM_MAX_TOPIC_ASSIGNMENTS = "maxTopicAssignments";
    @ConfigurationParameter(name = PARAM_MAX_TOPIC_ASSIGNMENTS, mandatory = true, defaultValue = "0")
    private int maxTopicAssignments;

    /**
     * The annotation type to use for the model. For lemmas, use
     * {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token/lemma/value}
     */
    public static final String PARAM_TOKEN_FEATURE_PATH = 
            MalletModelTrainer.PARAM_TOKEN_FEATURE_PATH;
    @ConfigurationParameter(name = PARAM_TOKEN_FEATURE_PATH, mandatory = true, 
            defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String tokenFeaturePath;
    /**
     * Ignore tokens (or lemmas, respectively) that are shorter than the given value.
     */
    public static final String PARAM_MIN_TOKEN_LENGTH = "minTokenLength";
    @ConfigurationParameter(name = PARAM_MIN_TOKEN_LENGTH, mandatory = true, defaultValue = "3")
    private int minTokenLength;

    /**
     * If set to true (default: false), all tokens are lowercased.
     */
    public static final String PARAM_LOWERCASE = "lowercase";
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = true, defaultValue = "false")
    private boolean lowercase;

    private TopicInferencer inferencer;
    private Pipe malletPipe;
    private StringSequenceGenerator sequenceGenerator;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        ParallelTopicModel model;
        try {
            getLogger().info("Loading model file " + modelLocation);
            model = ParallelTopicModel.read(modelLocation);

            if (maxTopicAssignments <= 0) {
                maxTopicAssignments = model.getNumTopics() / 10;
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        getLogger().info("Model loaded.");

        inferencer = model.getInferencer();
        malletPipe = new TokenSequence2FeatureSequence(model.getAlphabet());
        try {
            sequenceGenerator = new PhraseSequenceGenerator.Builder()
                    .featurePath(tokenFeaturePath)
                    .minTokenLength(minTokenLength)
                    .lowercase(lowercase)
                    .buildStringSequenceGenerator();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        try {
            List<String[]> tokenSequences = sequenceGenerator.tokenSequences(aJCas);

            if (tokenSequences.isEmpty()) {
                getLogger().warn("Empty document.");
            }
            else {
                DocumentMetaData metadata = DocumentMetaData.get(aJCas);

                /* create Mallet Instance */
                TokenSequence ts = new TokenSequence(tokenSequences.get(0));
                Instance instance = new Instance(ts, NONE_LABEL, metadata.getDocumentId(),
                        metadata.getDocumentUri());


                /* infer topic distribution across document */
                TopicDistribution topicDistributionAnnotation = new TopicDistribution(aJCas);
                double[] topicDistribution = inferencer.getSampledDistribution(
                        malletPipe.instanceFrom(instance), nIterations, thinning, burnIn);

                /* convert data type (Mallet output -> UIMA double array) */
                DoubleArray da = new DoubleArray(aJCas, topicDistribution.length);
                da.copyFromArray(topicDistribution, 0, 0, topicDistribution.length);
                topicDistributionAnnotation.setTopicProportions(da);

                /* assign topics to document according to topic distribution */
                int[] assignedTopicIndexes = assignTopics(topicDistribution);
                IntegerArray topicIndexes = new IntegerArray(aJCas, assignedTopicIndexes.length);
                topicIndexes.copyFromArray(assignedTopicIndexes, 0, 0, assignedTopicIndexes.length);
                topicDistributionAnnotation.setTopicAssignment(topicIndexes);

                aJCas.addFsToIndexes(topicDistributionAnnotation);
            }
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * Assign topics according to the following formula:
     * <p>
     * Topic proportion must be at least the maximum topic's proportion divided by the maximum
     * number of topics to be assigned. In addition, the topic proportion must not lie under the
     * minTopicProb. If more topics comply with these criteria, only retain the n
     * (maxTopicAssignments) largest values.
     *
     * @param topicDistribution a double array containing the document's topic proportions
     * @return an array of integers pointing to the topics assigned to the document
     * @deprecated this method should be removed at some point because assignment / topic tagging
     * should be done in a dedicated step (module).
     */
    // TODO: should return a boolean[] of the same size as topicDistribution
    // TODO: should probably be moved to a dedicated module because assignments (topic tagging)
    // should not be done at inference level
    @Deprecated
    private int[] assignTopics(final double[] topicDistribution)
    {
        /*
         * threshold is the largest value divided by the maximum number of topics or the fixed
         * number set as minTopicProb parameter.
         */
        double threshold = Math.max(Collections.max(
                Arrays.asList(ArrayUtils.toObject(topicDistribution))).doubleValue()
                        / maxTopicAssignments,
                minTopicProb);

        /*
         * assign indexes for values that are above threshold
         */
        List<Integer> indexes = new ArrayList<>(topicDistribution.length);
        for (int i = 0; i < topicDistribution.length; i++) {
            if (topicDistribution[i] >= threshold) {
                indexes.add(i);
            }
        }

        /*
         * Reduce assignments to maximum number of allowed assignments.
         */
        if (indexes.size() > maxTopicAssignments) {

            /* sort index list by corresponding values */
            Collections.sort(indexes, (aO1, aO2) -> 
                    Double.compare(topicDistribution[aO1], topicDistribution[aO2]));

            while (indexes.size() > maxTopicAssignments) {
                indexes.remove(0);
            }
        }

        return ArrayUtils.toPrimitive(indexes.toArray(new Integer[indexes.size()]));
    }
}
