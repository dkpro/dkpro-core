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
package org.dkpro.core.opennlp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.ResourceUtils;
import org.dkpro.core.opennlp.internal.CasSentenceSampleStream;

import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.Parameters;
import eu.openminted.share.annotations.api.constants.OperationType;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.maxent.GISTrainer;
import opennlp.tools.sentdetect.SentenceDetectorFactory;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.TrainingParameters;

/**
 * Train a sentence splitter model for OpenNLP.
 */
@Component(OperationType.TRAINER_OF_MACHINE_LEARNING_MODELS)
@MimeTypeCapability(MimeTypes.APPLICATION_X_OPENNLP_SENT)
@Parameters(
        exclude = { 
                OpenNlpSentenceTrainer.PARAM_TARGET_LOCATION  })
@ResourceMetaData(name = "OpenNLP Sentence Splitter Trainer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class OpenNlpSentenceTrainer
    extends JCasConsumer_ImplBase
{
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
     * Training algorithm.
     */
    public static final String PARAM_ALGORITHM = "algorithm";
    @ConfigurationParameter(name = PARAM_ALGORITHM, mandatory = true, 
            defaultValue = GISTrainer.MAXENT_VALUE)
    private String algorithm;
    
    /**
     * Trainer type.
     */
    public static final String PARAM_TRAINER_TYPE = "trainerType";
    @ConfigurationParameter(name = PARAM_TRAINER_TYPE, mandatory = true, 
            defaultValue = EventTrainer.EVENT_VALUE)
    private String trainerType;

    /**
     * Number of training iterations.
     */
    public static final String PARAM_ITERATIONS = "iterations";
    @ConfigurationParameter(name = PARAM_ITERATIONS, mandatory = true, defaultValue = "100")
    private int iterations;

    /**
     * Frequency cut-off.
     */
    public static final String PARAM_CUTOFF = "cutoff";
    @ConfigurationParameter(name = PARAM_CUTOFF, mandatory = true, defaultValue = "5")
    private int cutoff;

    /**
     * End-of-sentence characters.
     */
    public static final String PARAM_EOS_CHARACTERS = "eosCharacters";
    @ConfigurationParameter(name = PARAM_EOS_CHARACTERS, mandatory = false)
    private char[] eosCharacters;

    /**
     * Location of the abbreviation dictionary.
     */
    public static final String PARAM_ABBREVIATION_DICTIONARY_LOCATION = "abbreviationDictionaryLocation";
    @ConfigurationParameter(name = PARAM_ABBREVIATION_DICTIONARY_LOCATION, mandatory = false)
    private String abbreviationDictionaryLocation;
    
    /**
     * Encoding of the abbreviation dictionary.
     */
    public static final String PARAM_ABBREVIATION_DICTIONARY_ENCODING = "abbreviationDictionaryEncoding";
    @ConfigurationParameter(name = PARAM_ABBREVIATION_DICTIONARY_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String abbreviationDictionaryEncoding;

    /**
     * Number of parallel threads.
     */
    public static final String PARAM_NUM_THREADS = ComponentParameters.PARAM_NUM_THREADS;
    @ConfigurationParameter(name = PARAM_NUM_THREADS, mandatory = true, defaultValue =  "1")
    private int numThreads;
    
    private CasSentenceSampleStream stream;
    private Dictionary abbreviationDictionary;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<SentenceModel> future;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        stream = new CasSentenceSampleStream();
        
        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ALGORITHM_PARAM, algorithm);
        params.put(TrainingParameters.TRAINER_TYPE_PARAM, trainerType);
        params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(iterations));
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(cutoff));
        params.put(TrainingParameters.THREADS_PARAM, Integer.toString(numThreads));
        
        if (abbreviationDictionaryLocation != null) {
            try {
                URL abbrevUrl = ResourceUtils.resolveLocation(abbreviationDictionaryLocation,
                        aContext);
                try (InputStream is = abbrevUrl.openStream()) {
                    abbreviationDictionary = Dictionary.parseOneEntryPerLine(
                            new InputStreamReader(is, abbreviationDictionaryEncoding));
                }
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        }
        else {
            abbreviationDictionary = null;
        }
        
        Callable<SentenceModel> trainTask = () -> {
            try {
                SentenceDetectorFactory factory = new SentenceDetectorFactory(language, true,
                        abbreviationDictionary, eosCharacters);
                return SentenceDetectorME.train(language, stream, factory, params);
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
        
        SentenceModel model;
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
}
