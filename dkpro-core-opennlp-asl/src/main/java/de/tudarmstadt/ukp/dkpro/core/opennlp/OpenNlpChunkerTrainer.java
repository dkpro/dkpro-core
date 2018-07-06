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

import java.util.concurrent.Callable;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.opennlp.internal.CasChunkSampleStream;
import de.tudarmstadt.ukp.dkpro.core.opennlp.internal.OpenNlpTrainerBase;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import opennlp.tools.chunker.ChunkerFactory;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.ml.BeamSearch;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.maxent.GISTrainer;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;

/**
 * Train a chunker model for OpenNLP.
 */
@Component(OperationType.TRAINER_OF_MACHINE_LEARNING_MODELS)
@MimeTypeCapability(MimeTypes.APPLICATION_X_OPENNLP_CHUNK)
@ResourceMetaData(name = "OpenNLP Chunker Trainer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class OpenNlpChunkerTrainer
    extends OpenNlpTrainerBase<CasChunkSampleStream>
{
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;

    public static final String PARAM_ALGORITHM = "algorithm";
    @ConfigurationParameter(name = PARAM_ALGORITHM, mandatory = true, 
            defaultValue = GISTrainer.MAXENT_VALUE)
    private String algorithm;
    
    public static final String PARAM_TRAINER_TYPE = "trainerType";
    @ConfigurationParameter(name = PARAM_TRAINER_TYPE, mandatory = true, 
            defaultValue = EventTrainer.EVENT_VALUE)
    private String trainerType;

    public static final String PARAM_ITERATIONS = "iterations";
    @ConfigurationParameter(name = PARAM_ITERATIONS, mandatory = true, defaultValue = "100")
    private int iterations;

    public static final String PARAM_CUTOFF = "cutoff";
    @ConfigurationParameter(name = PARAM_CUTOFF, mandatory = true, defaultValue = "5")
    private int cutoff;

    /**
     * @see ChunkerME#DEFAULT_BEAM_SIZE
     */
    public static final String PARAM_BEAMSIZE = "beamSize";
    @ConfigurationParameter(name = PARAM_BEAMSIZE, mandatory = true, defaultValue = "3")
    private int beamSize;
    
    public static final String PARAM_NUM_THREADS = ComponentParameters.PARAM_NUM_THREADS;
    @ConfigurationParameter(name = PARAM_NUM_THREADS, mandatory = true, defaultValue = "1")
    private int numThreads;
    
    @Override
    public CasChunkSampleStream makeSampleStream()
    {
        return new CasChunkSampleStream();
    }
    
    @Override
    public Callable<? extends BaseModel> makeTrainer()
    {
        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ALGORITHM_PARAM, algorithm);
        params.put(TrainingParameters.TRAINER_TYPE_PARAM, trainerType);
        params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(iterations));
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(cutoff));
        params.put(TrainingParameters.THREADS_PARAM, Integer.toString(numThreads));
        params.put(BeamSearch.BEAM_SIZE_PARAMETER, Integer.toString(beamSize));
        
        Callable<ChunkerModel> trainTask = () -> {
            try {
                return ChunkerME.train(language, getStream(), params, new ChunkerFactory());
            }
            catch (Throwable e) {
                getStream().close();
                throw e;
            }
        };
        
        return trainTask;
    }
}
