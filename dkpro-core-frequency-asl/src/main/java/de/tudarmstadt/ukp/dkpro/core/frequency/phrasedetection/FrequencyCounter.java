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
    /**
     * When concatenating multiple tokens, this string is inserted between them.
     */
    static final String BIGRAM_SEPARATOR = " ";
    /**
     * Columns (i.e. tokens and counts) are separated by this character.
     */
    static final String COLUMN_SEPARATOR = "\t";
    /**
     * When hitting a column separator within a token, it is replaced by this token.
     */
    static final String COLUMN_SEP_REPLACEMENT = " ";

    /**
     * This string (a line) will separate unigrams from bigrams in the output file
     **/
    static final String NGRAM_SEPARATOR_LINE = "----------------------------------------------------";

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

    private Optional<Comparator<String>> outputComparator;

    private Bag<String> unigrams;
    private Bag<String> bigrams;
    private StringSequenceGenerator sequenceGenerator;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);
        unigrams = new HashBag<>();
        bigrams = new HashBag<>();

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
                    -Integer.compare(unigrams.getCount(o1), unigrams.getCount(o2)));
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
            /* iterate over sequences (e.g. sentences)*/
            for (String[] sequence : sequenceGenerator.tokenSequences(aJCas)) {
                /* iterate over tokens in sequence */
                for (int i = 0; i < sequence.length; i++) {
                    /* count unigrams */
                    String unigram = sequence[i]
                            .replaceAll(COLUMN_SEPARATOR, COLUMN_SEP_REPLACEMENT);
                    unigrams.add(unigram);

                    /* count bigrams */
                    if (i + 1 < sequence.length) {
                        String bigram = unigram + BIGRAM_SEPARATOR + sequence[i + 1]
                                .replaceAll(COLUMN_SEPARATOR, COLUMN_SEP_REPLACEMENT);
                        bigrams.add(bigram);
                    }
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

            writeNgrams(os, unigrams);
            os.write((NGRAM_SEPARATOR_LINE + "\n").getBytes());
            writeNgrams(os, bigrams);
            os.close();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private void writeNgrams(OutputStream os, Bag<String> unigrams)
    {
    /* create (sorted) token stream */
        Stream<String> stream = unigrams.uniqueSet().stream()
                .filter(token -> unigrams.getCount(token) >= minCount);
        if (outputComparator.isPresent()) {
            stream = stream.sorted(outputComparator.get());
        }

        /* write tokens */
        stream.forEach(token -> {
            try {
                os.write((token + COLUMN_SEPARATOR + unigrams.getCount(token) + "\n").getBytes());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
