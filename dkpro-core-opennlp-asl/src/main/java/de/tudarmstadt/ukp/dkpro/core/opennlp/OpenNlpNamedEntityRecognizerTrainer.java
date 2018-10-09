/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.opennlp.internal.CasNameSampleStream;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.Parameters;
import eu.openminted.share.annotations.api.constants.OperationType;
import opennlp.tools.ml.BeamSearch;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.maxent.GISTrainer;
import opennlp.tools.ml.maxent.quasinewton.QNTrainer;
import opennlp.tools.ml.perceptron.PerceptronTrainer;
import opennlp.tools.ml.perceptron.SimplePerceptronSequenceTrainer;
import opennlp.tools.namefind.BilouCodec;
import opennlp.tools.namefind.BioCodec;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.SequenceCodec;
import opennlp.tools.util.TrainingParameters;

/**
 * Train a named entity recognizer model for OpenNLP.
 */
@Component(OperationType.TRAINER_OF_MACHINE_LEARNING_MODELS)
@MimeTypeCapability(MimeTypes.APPLICATION_X_OPENNLP_NER)
@Parameters(
        exclude = { 
                OpenNlpNamedEntityRecognizerTrainer.PARAM_TARGET_LOCATION  })
@ResourceMetaData(name = "OpenNLP Named Entity Recognizer Trainer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
public class OpenNlpNamedEntityRecognizerTrainer
    extends JCasConsumer_ImplBase
{
    public static enum SequenceEncoding {
        BIO(BioCodec.class), BILOU(BilouCodec.class);
        
        private Class<? extends SequenceCodec<String>> codec;
        
        SequenceEncoding(Class<? extends SequenceCodec<String>> aCodec)
        {
            codec = aCodec;
        }
        
        private SequenceCodec<String> getCodec()
        {
            try {
                return codec.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    
    /**
     * Store this language to the model instead of the document language.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;

    /**
     * Location to which the output is written.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;

    /**
     * Regex to filter the {@link de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity#getValue()
     * named entity} by type.
     */
    public static final String PARAM_ACCEPTED_TAGS_REGEX = 
            ComponentParameters.PARAM_ACCEPTED_TAGS_REGEX;
    @ConfigurationParameter(name = PARAM_ACCEPTED_TAGS_REGEX, mandatory = false)
    protected String acceptedTagsRegex;

    /**
     * @see GISTrainer#MAXENT_VALUE
     * @see QNTrainer#MAXENT_QN_VALUE
     * @see PerceptronTrainer#PERCEPTRON_VALUE
     * @see SimplePerceptronSequenceTrainer#PERCEPTRON_SEQUENCE_VALUE
     */
    public static final String PARAM_ALGORITHM = "algorithm";
    @ConfigurationParameter(name = PARAM_ALGORITHM, mandatory = true, 
            defaultValue = PerceptronTrainer.PERCEPTRON_VALUE)
    private String algorithm;
    
    /**
     * Training algorithm.
     */
    public static final String PARAM_TRAINER_TYPE = "trainerType";
    @ConfigurationParameter(name = PARAM_TRAINER_TYPE, mandatory = true, 
            defaultValue = EventTrainer.EVENT_VALUE)
    private String trainerType;

    /**
     * Number of training iterations.
     */
    public static final String PARAM_ITERATIONS = "iterations";
    @ConfigurationParameter(name = PARAM_ITERATIONS, mandatory = true, defaultValue = "300")
    private int iterations;

    /**
     * Frequency cut-off.
     */
    public static final String PARAM_CUTOFF = "cutoff";
    @ConfigurationParameter(name = PARAM_CUTOFF, mandatory = true, defaultValue = "0")
    private int cutoff;

    /**
     * @see NameFinderME#DEFAULT_BEAM_SIZE
     */
    public static final String PARAM_BEAMSIZE = "beamSize";
    @ConfigurationParameter(name = PARAM_BEAMSIZE, mandatory = true, defaultValue = "3")
    private int beamSize;

    /**
     * File containing the feature generation specification.
     */
    public static final String PARAM_FEATURE_GEN_LOCATION = "featureGen";
    @ConfigurationParameter(name = PARAM_FEATURE_GEN_LOCATION, mandatory = false)
    private File featureGen;

    /**
     * Type of sequence encoding to use.
     */
    public static final String PARAM_SEQUENCE_ENCODING = "sequenceEncoding";
    @ConfigurationParameter(name = PARAM_SEQUENCE_ENCODING, mandatory = true, defaultValue = "BILOU")
    private SequenceEncoding sequenceEncoding;
    
    /**
     * Number of parallel threads.
     */
    public static final String PARAM_NUM_THREADS = ComponentParameters.PARAM_NUM_THREADS;
    @ConfigurationParameter(name = PARAM_NUM_THREADS, mandatory = true, defaultValue =  "1")
    private int numThreads;
    
    private CasNameSampleStream stream;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<TokenNameFinderModel> future;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        stream = new CasNameSampleStream();

        if (acceptedTagsRegex != null) {
            Pattern filterPattern = Pattern.compile(acceptedTagsRegex);
            stream.setNamedEntityFilter(namedEntity -> 
                    filterPattern.matcher(namedEntity.getValue()).matches());
        }
        
        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ALGORITHM_PARAM, algorithm);
//        params.put(TrainingParameters.TRAINER_TYPE_PARAM,
//                TrainerFactory.getTrainerType(params.getSettings()).name());
        params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(iterations));
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(cutoff));
        params.put(TrainingParameters.THREADS_PARAM, Integer.toString(numThreads));
        params.put(BeamSearch.BEAM_SIZE_PARAMETER, Integer.toString(beamSize));
        
        byte[] featureGenCfg = loadFeatureGen(featureGen);
        
        Callable<TokenNameFinderModel> trainTask = () -> {
            try {
                return NameFinderME.train(language, null, stream, params,
                        new TokenNameFinderFactory(featureGenCfg,
                                Collections.<String, Object>emptyMap(),
                                sequenceEncoding.getCodec()));
            }
            catch (Throwable e) {
                stream.close();
                throw e;
            }
        };
        
        future = executor.submit(trainTask);
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        if (!future.isCancelled()) {
            stream.send(aJCas);
        }
    }
    
    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        try {
            stream.close();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        TokenNameFinderModel model;
        try {
            model = future.get();
        }
        catch (InterruptedException | ExecutionException e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        try (OutputStream out = new FileOutputStream(targetLocation)) {
            model.serialize(out);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private byte[] loadFeatureGen(File aFile)
        throws ResourceInitializationException
    {
        byte[] featureGenCfg = null;
        if (aFile != null) {
            try (InputStream in = new FileInputStream(aFile)) {
                featureGenCfg = IOUtils.toByteArray(in);
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        }
        return featureGenCfg;
    }
}
