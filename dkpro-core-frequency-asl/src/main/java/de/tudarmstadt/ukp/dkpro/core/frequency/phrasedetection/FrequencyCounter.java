/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.frequency.phrasedetection;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator.AnnotationStringSequenceGenerator;
import de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator.StringSequenceGenerator;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Count unigrams and bigrams in a collection.
 */
public class FrequencyCounter
        extends JCasFileWriter_ImplBase
{
    static final String COLUMN_SEP_REPLACEMENT = " ";
    static final String BIGRAM_SEPARATOR = COLUMN_SEP_REPLACEMENT;
    static final String COLUMN_SEPARATOR = "\t";

    /**
     * The feature path. Default: tokens.
     */
    public static final String PARAM_FEATURE_PATH = "featurePath";
    @ConfigurationParameter(name = PARAM_FEATURE_PATH, mandatory = false)
    private String featurePath;
    private static final String DEFAULT_FEATURE_PATH = Token.class.getCanonicalName();

    /**
     * Set this parameter if bigrams should only be counted when occurring within a covering type, e.g. sentences.
     */
    public static final String PARAM_COVERING_TYPE = "coveringType";
    @ConfigurationParameter(name = PARAM_COVERING_TYPE, mandatory = false)
    private String coveringType;

    /**
     * If true, all tokens are lowercased.
     */
    public static final String PARAM_LOWERCASE = "lowercase";
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = true, defaultValue = "false")
    private boolean lowercase;

    /**
     * Tokens occurring fewer times than this value are omitted. Default: 5.
     */
    public static final String PARAM_MIN_COUNT = "minCount";
    @ConfigurationParameter(name = PARAM_MIN_COUNT, mandatory = true, defaultValue = "5")
    private int minCount;

    /**
     * If true, sort output by count (descending order).
     */
    public static final String PARAM_SORT_BY_COUNT = "sortByCount";
    @ConfigurationParameter(name = PARAM_SORT_BY_COUNT, mandatory = true, defaultValue = "false")
    private boolean sortByCount;

    /**
     * If true, sort output alphabetically.
     */
    public static final String PARAM_SORT_BY_ALPHABET = "sortByAlphabet";
    @ConfigurationParameter(name = PARAM_SORT_BY_ALPHABET, mandatory = true, defaultValue = "false")
    private boolean sortByAlphabet;

    Optional<Comparator<String>> outputComparator;

    private Bag<String> counter;
    private StringSequenceGenerator sequenceGenerator;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);
        counter = new HashBag<>();

        /* set feature path to default */
        if (featurePath == null) {
            featurePath = DEFAULT_FEATURE_PATH;
        }

        /* init sequence generator */
        try {
            sequenceGenerator = new AnnotationStringSequenceGenerator.Builder()
                    .featurePath(featurePath)
                    .coveringType(coveringType)
                    .lowercase(lowercase)
                    .build();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        if (sortByAlphabet && sortByCount) {
            throw new ResourceInitializationException(new IllegalArgumentException(
                    "Can only sort either by count or alphabetically."));
        }
        if (sortByAlphabet) {
            outputComparator = Optional.of(String::compareTo);
        }
        else if (sortByCount) {
            outputComparator = Optional.of((o1, o2) ->
                    -Integer.compare(counter.getCount(o1), counter.getCount(o2)));
        }
        else {
            outputComparator = Optional.empty();
        }
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        try {
            for (String[] sequence : sequenceGenerator.tokenSequences(aJCas)) {
            /* iterate over sequences (e.g. sentences)*/
                /* count unigrams */
                Stream.of(sequence)
                        .map(s -> s.replaceAll(COLUMN_SEPARATOR, COLUMN_SEP_REPLACEMENT))
                        .forEach(s -> counter.add(s));

                /* count bigrams */
                for (int i = 0; i < sequence.length - 1; i++) {
                    /* replacing tabs as they are used as column separators */
                    counter.add(sequence[i].replaceAll(COLUMN_SEPARATOR, COLUMN_SEP_REPLACEMENT)
                            + BIGRAM_SEPARATOR
                            + sequence[i + 1].replaceAll(COLUMN_SEPARATOR, COLUMN_SEP_REPLACEMENT));
                }
            }
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        try {
            OutputStream os = CompressionUtils.getOutputStream(new File(getTargetLocation()));

            Stream<String> stream = counter.uniqueSet().stream()
                    .filter(token -> counter.getCount(token) >= minCount);
            if (outputComparator.isPresent()) {
                stream = stream.sorted(outputComparator.get());
            }
            stream.forEach(token -> {
                try {
                    os.write(
                            (token + COLUMN_SEPARATOR + counter.getCount(token) + "\n").getBytes());
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

}
