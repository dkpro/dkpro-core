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
package org.dkpro.core.frequency.phrasedetection;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.featurepath.FeaturePathException;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.io.sequencegenerator.PhraseSequenceGenerator;
import org.dkpro.core.api.io.sequencegenerator.StringSequenceGenerator;
import org.dkpro.core.api.resources.CompressionUtils;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Count uni-grams and bi-grams in a collection.
 */
@ResourceMetaData(name = "Frequency Writer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class FrequencyWriter
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
    static final String NEWLINE_REGEX = "\r\n?|\n";

    /**
     * The feature path.
     */
    public static final String PARAM_FEATURE_PATH = "featurePath";
    @ConfigurationParameter(name = PARAM_FEATURE_PATH, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String featurePath;

    /**
     * Set this parameter if bigrams should only be counted when occurring within a covering type,
     * e.g. sentences.
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
     * Tokens occurring fewer times than this value are omitted.
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

    /**
     * Path of a file containing stopwords one work per line.
     */
    public static final String PARAM_STOPWORDS_FILE = "stopwordsFile";
    @ConfigurationParameter(name = PARAM_STOPWORDS_FILE, mandatory = true, defaultValue = "")
    private String stopwordsFile;

    /**
     * Stopwords are replaced by this value.
     */
    public static final String PARAM_STOPWORDS_REPLACEMENT = "stopwordsReplacement";
    @ConfigurationParameter(name = PARAM_STOPWORDS_REPLACEMENT, mandatory = true, defaultValue = "")
    private String stopwordsReplacement;

    /**
     * Regular expression of tokens to be filtered.
     */
    public static final String PARAM_FILTER_REGEX = "filterRegex";
    @ConfigurationParameter(name = PARAM_FILTER_REGEX, mandatory = true, defaultValue = "")
    private String filterRegex;

    /**
     * Value with which tokens matching the regular expression are replaced.
     */
    public static final String PARAM_REGEX_REPLACEMENT = "regexReplacement";
    @ConfigurationParameter(name = PARAM_REGEX_REPLACEMENT, mandatory = true, defaultValue = "")
    private String regexReplacement;

    private Bag<String> unigrams;
    private Bag<String> bigrams;
    private StringSequenceGenerator sequenceGenerator;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);
        if (sortByAlphabet && sortByCount) {
            throw new ResourceInitializationException(new IllegalArgumentException(
                    "Can only sort either by count or alphabetically."));
        }

        unigrams = new HashBag<>();
        bigrams = new HashBag<>();

        /* init sequence generator */
        try {
            sequenceGenerator = new PhraseSequenceGenerator.Builder()
                    .featurePath(featurePath)
                    .coveringType(coveringType)
                    .lowercase(lowercase)
                    .stopwordsFile(stopwordsFile)
                    .stopwordsReplacement(stopwordsReplacement)
                    .filterRegex(filterRegex)
                    .filterRegexReplacement(regexReplacement)
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
            /* iterate over sequences (e.g. sentences)*/
            for (String[] sequence : sequenceGenerator.tokenSequences(aJCas)) {
                /* iterate over tokens in sequence */
                for (int i = 0; i < sequence.length; i++) {
                    /* count unigrams */
                    String unigram = sequence[i]
                            .replaceAll(COLUMN_SEPARATOR, COLUMN_SEP_REPLACEMENT)
                            .replaceAll(NEWLINE_REGEX, COLUMN_SEP_REPLACEMENT);
                    unigrams.add(unigram);

                    /* count bigrams */
                    if (i + 1 < sequence.length) {
                        String bigram = unigram + BIGRAM_SEPARATOR + sequence[i + 1]
                                .replaceAll(COLUMN_SEPARATOR, COLUMN_SEP_REPLACEMENT)
                                .replaceAll(NEWLINE_REGEX, COLUMN_SEP_REPLACEMENT);
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
        getLogger().info("Vocabulary size: " + unigrams.uniqueSet().size());
        try {
            getLogger().info("Writing frequencies to " + getTargetLocation());
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

    /**
     * Write counter with counts from a bag to an output stream.
     *
     * @param os      an {@link OutputStream}
     * @param counter a {@link Bag} of string counter
     */
    private void writeNgrams(OutputStream os, Bag<String> counter)
    {
        /* create token stream */
        Stream<String> stream = counter.uniqueSet().stream()
                .filter(token -> counter.getCount(token) >= minCount);

        /* sort output */
        if (sortByAlphabet) {
            stream = stream.sorted(String::compareTo);
        }
        else if (sortByCount) {
            stream = stream.sorted((o1, o2) ->
                    -Integer.compare(counter.getCount(o1), counter.getCount(o2)));
        }

        /* write tokens with counts */
        stream.forEach(token -> {
            try {
                os.write((token + COLUMN_SEPARATOR + counter.getCount(token) + "\n").getBytes());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
