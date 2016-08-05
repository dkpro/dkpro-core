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

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.embeddings.loader.VectorsConfiguration;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.sequencevectors.SequenceVectors;
import org.deeplearning4j.models.sequencevectors.iterators.AbstractSequenceIterator;
import org.deeplearning4j.models.sequencevectors.sequence.Sequence;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabConstructor;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Dl4jEmbeddingsTrainer
    extends JCasConsumer_ImplBase
{
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;
    
    /**
     * The dimensionality of the output word embeddings (default: 50).
     */
    public static final String PARAM_DIMENSIONS = "dimensions";
    @ConfigurationParameter(name = PARAM_DIMENSIONS, mandatory = true, defaultValue = "50")
    private int dimensions;
//
//    /**
//     * The context size when generating embeddings (default: 5).
//     */
//    public static final String PARAM_WINDOW_SIZE = "windowSize";
//    @ConfigurationParameter(name = PARAM_WINDOW_SIZE, mandatory = true, defaultValue = "5")
//    private int windowSize;
//
//    /**
//     * An example word that is output with its nearest neighbours once in a while (default: null, i.e. none).
//     */
//    public static final String PARAM_EXAMPLE_WORD = "exampleWord";
//    @ConfigurationParameter(name = PARAM_EXAMPLE_WORD, mandatory = false)
//    private String exampleWord;

    /**
     * Ignore documents with fewer tokens than this value.
     */
    public static final String PARAM_MIN_DOCUMENT_LENGTH = "minDocumentLength";
    @ConfigurationParameter(name = PARAM_MIN_DOCUMENT_LENGTH, mandatory = true, defaultValue = "5")
    private int minDocumentLength;

    public static final String PARAM_MIN_WORD_FREQUENCY = "minWordFrequency";
    @ConfigurationParameter(name = PARAM_MIN_WORD_FREQUENCY, mandatory = true, defaultValue = "5")
    private int minWordFrequency;

    private int sentenceCount;
    private List<Sequence<VocabWord>> instances;
    private AbstractCache<VocabWord> vocabCache;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        sentenceCount = 0;
        instances = new ArrayList<>();
        vocabCache = new AbstractCache.Builder<VocabWord>().build();
    }
   
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        for (Sentence s : select(aJCas, Sentence.class)) {
            Sequence<VocabWord> sentenceInstance = new Sequence<>();
            
            for (Token token : selectCovered(Token.class, s)) {
                String text = token.getCoveredText();
                if (StringUtils.isBlank(text)) {
                    continue;
                }
                
                // Add to cache
                VocabWord word = new VocabWord(1.0, text);
                vocabCache.addToken(word);
                
                // Make sure we use the version from the cache
                sentenceInstance.addElement(vocabCache.wordFor(word.getWord()));
            }

            sentenceInstance.setSequenceId(sentenceCount);
            sentenceCount++;
            
            instances.add(sentenceInstance);
        }
    }
    
    
    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        AbstractSequenceIterator<VocabWord> sequenceIterator =
                new AbstractSequenceIterator.Builder<>(instances).build();
        
        VocabConstructor<VocabWord> constructor = new VocabConstructor.Builder<VocabWord>()
                // FIXME, not sure if second param is correct
                .addSource(sequenceIterator, minDocumentLength) 
                .setTargetVocabCache(vocabCache)
                .build();    
        
        constructor.buildJointVocabulary(false, true);
        
        WeightLookupTable<VocabWord> lookupTable = new InMemoryLookupTable.Builder<VocabWord>()
                .lr(0.025)
                .vectorLength(dimensions)
                .useAdaGrad(false)
                .cache(vocabCache)
                .build();
        
        lookupTable.resetWeights(true);
        
        SequenceVectors<VocabWord> vectors = new SequenceVectors.Builder<VocabWord>(new VectorsConfiguration())
            .minWordFrequency(minWordFrequency)
            .lookupTable(lookupTable)
            .iterate(sequenceIterator)
            .vocabCache(vocabCache)
            .batchSize(250)
            .iterations(1)
            .epochs(1)
            .resetModel(false)
            .trainElementsRepresentation(true)
            .trainSequencesRepresentation(false)
            .elementsLearningAlgorithm(new SkipGram<VocabWord>())
            .build();
        
        vectors.fit();
        
        try {
            WordVectorSerializer.writeWordVectors(lookupTable, targetLocation);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
