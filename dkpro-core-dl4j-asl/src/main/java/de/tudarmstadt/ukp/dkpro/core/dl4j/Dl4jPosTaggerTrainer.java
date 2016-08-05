/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.dl4j;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.sequencevectors.SequenceVectors;
import org.deeplearning4j.models.sequencevectors.serialization.VocabWordFactory;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher;
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class Dl4jPosTaggerTrainer
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;
    
    public static final String PARAM_EMBEDDINGS_LOCATION = "embeddingsLocation";
    @ConfigurationParameter(name = PARAM_EMBEDDINGS_LOCATION, mandatory = true)
    private File embeddingsLocation;
    
    public static final String PARAM_ITERATIONS = "iterations";
    @ConfigurationParameter(name = PARAM_ITERATIONS, mandatory = true, defaultValue = "100")
    private int iterations;

    public static final String PARAM_CUTOFF = "cutoff";
    @ConfigurationParameter(name = PARAM_CUTOFF, mandatory = true, defaultValue = "5")
    private int cutoff;

    private List<String[]> sentenceWords;
    private List<String[]> sentenceTags;
    private Set<String> tagset;
    
    private SequenceVectors<VocabWord> embeddings;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        sentenceWords = new ArrayList<>();
        sentenceTags = new ArrayList<>();
        tagset = new LinkedHashSet<>();
        
        try {
            embeddings = WordVectorSerializer.readSequenceVectors(new VocabWordFactory(),
                    embeddingsLocation);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        Collection<Sentence> sentences = select(aJCas, Sentence.class);
        for (Sentence s : sentences) {
            Collection<Token> tokens = selectCovered(Token.class, s);
            String[] words = new String[tokens.size()];
            String[] tags = new String[tokens.size()];
            
            int i = 0;
            for (Token t : tokens) {
                words[i] = t.getCoveredText().intern();
                tags[i] = t.getPos().getPosValue(); // Should already be interned
                tagset.add(t.getPos().getPosValue());
                i++;
            }
            
            sentenceWords.add(words);
            sentenceTags.add(tags);
        }
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        DataSetFetcher trainingDataFetcher = new BaseDataFetcher()
        {
            private int sentCursor = 0;
            
            @Override
            public void fetch(int aNumExamples)
            {
                List<DataSet> examples = new ArrayList<>();
                
                int limit = Math.min(sentCursor + aNumExamples, sentenceWords.size());
                
                for (; sentCursor < limit; sentCursor++) {
//                    INDArray features = embeddings.getWordVectorMatrix(word);
//                    INDArray label;
//                    DataSet ds = new DataSet(features, label);
                }
                
                initializeCurrFromList(examples);
            }
        };
        
        DataSetIterator trainingDataIterator = new BaseDatasetIterator(25, sentenceWords.size(),
                trainingDataFetcher);
        
        int inputLayerSize = trainingDataIterator.inputColumns();
        int numHiddenNodes = 100;
        int outputLayerSize = trainingDataIterator.totalOutcomes();
        
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(0.1)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(inputLayerSize).nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation("relu")
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation("softmax").weightInit(WeightInit.XAVIER)
                        .nIn(numHiddenNodes).nOut(outputLayerSize).build())
                .pretrain(false).backprop(true).build();


        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));  //Print score every 10 parameter updates

        for ( int n = 0; n < iterations; n++) {
            model.fit(trainingDataIterator);
        }
    }
}
