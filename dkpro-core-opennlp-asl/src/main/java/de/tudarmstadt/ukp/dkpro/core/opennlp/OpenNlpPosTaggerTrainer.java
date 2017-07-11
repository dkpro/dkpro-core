/*
 * Copyright 2016
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
import org.apache.uima.fit.descriptor.ResourceMetaData;

import de.tudarmstadt.ukp.dkpro.core.api.descriptors.ModelTrainerCapability;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.opennlp.internal.CasPosSampleStream;
import de.tudarmstadt.ukp.dkpro.core.opennlp.internal.OpenNlpTrainerBase;
import opennlp.tools.ml.BeamSearch;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.maxent.GISTrainer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.BaseModel;

/**
 * Train a POS tagging model for OpenNLP.
 */
@ModelTrainerCapability(jCasAnnotator = OpenNlpPosTagger.class, type = "pos")
@ResourceMetaData(name="OpenNLP POS-Tagger Trainer")
public class OpenNlpPosTaggerTrainer
    extends OpenNlpTrainerBase<CasPosSampleStream>
{
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;

    public static final String PARAM_ALGORITHM = "algorithm";
    @ConfigurationParameter(name = PARAM_ALGORITHM, mandatory = true, defaultValue = GISTrainer.MAXENT_VALUE)
    private String algorithm;
    
    public static final String PARAM_TRAINER_TYPE = "trainerType";
    @ConfigurationParameter(name = PARAM_TRAINER_TYPE, mandatory = true, defaultValue = EventTrainer.EVENT_VALUE)
    private String trainerType;

    public static final String PARAM_ITERATIONS = "iterations";
    @ConfigurationParameter(name = PARAM_ITERATIONS, mandatory = true, defaultValue = "100")
    private int iterations;

    public static final String PARAM_CUTOFF = "cutoff";
    @ConfigurationParameter(name = PARAM_CUTOFF, mandatory = true, defaultValue = "5")
    private int cutoff;

    /**
     * @see POSTaggerME#DEFAULT_BEAM_SIZE
     */
    public static final String PARAM_BEAMSIZE = "beamSize";
    @ConfigurationParameter(name = PARAM_BEAMSIZE, mandatory = true, defaultValue = "3")
    private int beamSize;
    
    public static final String PARAM_NUM_THREADS = ComponentParameters.PARAM_NUM_THREADS;
    @ConfigurationParameter(name = PARAM_NUM_THREADS, mandatory = true, defaultValue =  "1")
    private int numThreads;
    
    @Override
    public CasPosSampleStream makeSampleStream()
    {
        return new CasPosSampleStream();
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
        
        Callable<POSModel> trainTask = () -> {
            try {
                return POSTaggerME.train(language, getStream(), params,
                        new POSTaggerFactory(null, null));
            }
            catch (Throwable e) {
                getStream().close();
                throw e;
            }
        };
        
        return trainTask;
    }
}
