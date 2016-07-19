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
package de.tudarmstadt.ukp.dkpro.core.frequency.phrasedetection;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator.PhraseSequenceGenerator;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LexicalPhrase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.uima.fit.util.JCasUtil.select;

/**
 * Annotate phrases in a sentence. Depending on the provided unigrams and the threshold, these
 * comprise either one or two annotations (tokens, lemmas, ...).
 * <p>
 * In order to identify longer phrases, run the {@link FrequencyCounter} and this annotator
 * multiple times, each time taking the results of the previous run as input. From the second run on, set phrases
 * in the feature path parameter {@link #PARAM_FEATURE_PATH}.
 */
public class PhraseAnnotator
        extends JCasAnnotator_ImplBase
{
    /**
     * The feature path to use for building bigrams. Default: tokens.
     */
    public static final String PARAM_FEATURE_PATH = "featurePath";
    @ConfigurationParameter(name = PARAM_FEATURE_PATH, mandatory = false)
    private String featurePath;
    private static final String DEFAULT_FEATURE_PATH = Token.class.getCanonicalName();

    /**
     * The covering type to use for building multiple sequences per document, e.g. sentences.
     * TODO: implement covering type
     */
    //    public static final String PARAM_COVERING_TYPE = "coveringType";
    //    @ConfigurationParameter(name = PARAM_COVERING_TYPE, mandatory = false)
    //    private String coveringType;

    /**
     * If true, lowercase everything.
     */
    public static final String PARAM_LOWERCASE = "PARAM_LOWERCASE";
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = true, defaultValue = "false")
    private boolean lowercase;

    /**
     * The file providing the unigram and bigram unigrams to use.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    private String modelLocation;

    /**
     * The discount in order to prevent too many phrases consisting of very infrequent words to be formed.
     * A typical value is the minimum count set during model creation ({@link FrequencyCounter#PARAM_MIN_COUNT}),
     * which is by default set to 5.
     */
    public static final String PARAM_DISCOUNT = "discount";
    @ConfigurationParameter(name = PARAM_DISCOUNT, mandatory = true, defaultValue = "5")
    private int discount;

    /**
     * The threshold score for phrase construction. Default is 100. Lower values result in fewer phrases.
     * The value strongly depends on the size of the corpus and the token unigrams.
     */
    public static final String PARAM_THRESHOLD = "threshold";
    @ConfigurationParameter(name = PARAM_THRESHOLD, mandatory = true, defaultValue = "100")
    private float threshold;

    public static final String PARAM_STOPWORDS_FILE = "stopwordsFile";
    @ConfigurationParameter(name = PARAM_STOPWORDS_FILE, mandatory = true, defaultValue = "")
    private String stopwordsFile;

    public static final String PARAM_STOPWORDS_REPLACEMENT = "stopwordsReplacement";
    @ConfigurationParameter(name = PARAM_STOPWORDS_REPLACEMENT, mandatory = true, defaultValue = "")
    private String stopwordsReplacement;

    public static final String PARAM_FILTER_REGEX = "filterRegex";
    @ConfigurationParameter(name = PARAM_FILTER_REGEX, mandatory = true, defaultValue = "")
    private String filterRegex;

    public static final String PARAM_REGEX_REPLACEMENT = "regexReplacement";
    @ConfigurationParameter(name = PARAM_REGEX_REPLACEMENT, mandatory = true, defaultValue = "")
    private String regexReplacement;

    /**
     * Set this parameter if bigrams should only be counted when occurring within a covering type, e.g. sentences.
     */
    public static final String PARAM_COVERING_TYPE = "coveringType";
    @ConfigurationParameter(name = PARAM_COVERING_TYPE, mandatory = false)
    private String coveringType;

    private Map<String, Integer> unigrams;
    private Map<String, Integer> bigrams;
    private int vocabularySize;

    private PhraseSequenceGenerator sequenceGenerator;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        /* set feature path to default */
        if (featurePath == null) {
            featurePath = DEFAULT_FEATURE_PATH;
        }
        try {
            sequenceGenerator = new PhraseSequenceGenerator.Builder()
                    .featurePath(featurePath)
                    .coveringType(coveringType)
                    .lowercase(lowercase)
                    .stopwordsFile(stopwordsFile)
                    .stopwordsReplacement(stopwordsReplacement)
                    .filterRegex(filterRegex)
                    .filterRegexReplacement(regexReplacement)
                    .build();

            readCounts();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        vocabularySize = unigrams.size();
        getLogger().info("Vocabulary size: " + vocabularySize);

    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        List<LexicalPhrase[]> sequences;
        try {
            sequences = sequenceGenerator.tokenSequences(aJCas);
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }

        /* remove existing phrases */
        select(aJCas, LexicalPhrase.class).forEach(TOP::removeFromIndexes);

        for (LexicalPhrase[] sequence : sequences) {
        /* iterate over sequences in document */

            for (int i = 0; i < sequence.length; i++) {
            /* iterate over tokens within sequence */
                LexicalPhrase phrase1 = sequence[i];
                String token1 = phrase1.getText();
                LexicalPhrase newPhrase = phrase1;

                if (i < sequence.length - 1) {
                    /* do not look for bigram on last token */
                    LexicalPhrase phrase2 = sequence[i + 1];
                    String token2 = phrase2.getText();
                    String bigram = token1 + FrequencyCounter.BIGRAM_SEPARATOR + token2;

                    if (bigrams.containsKey(bigram)) {
                        assert unigrams.containsKey(token1);
                        assert unigrams.containsKey(token2);

                        /* compute score */
                        double score =
                                (double) ((bigrams.get(bigram) - discount) * vocabularySize) /
                                        (double) (unigrams.get(token1) * unigrams.get(token2));
                        getLogger().debug(bigram + "\t" + score);

                        if (score >= threshold) {
                        /* bigram phrase spanning two tokens found */
                            newPhrase = new LexicalPhrase(aJCas, phrase1.getBegin(),
                                    phrase2.getEnd());
                            newPhrase.setText(bigram);
                            i++;    // skip following token
                        }
                    }
                }

                newPhrase.addToIndexes(aJCas);
            }
        }
    }

    /**
     * Read the input file, adding unigrams and bigrams to the respective maps.
     *
     * @throws IOException if the input file cannot be read
     */
    private void readCounts()
            throws IOException
    {
        unigrams = new HashMap<>();
        bigrams = new HashMap<>();

        getLogger().info("Reading frequencies from " + modelLocation);
        BufferedReader reader = new BufferedReader(new InputStreamReader(CompressionUtils
                .getInputStream(modelLocation, new FileInputStream(modelLocation))));
        boolean countingUnigrams = true;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals(FrequencyCounter.NGRAM_SEPARATOR_LINE)) {
                /* this should only happen once per file */
                if (!countingUnigrams) {
                    throw new IllegalStateException(
                            "Error reading input file; contains multiple separation lines.");
                }
                countingUnigrams = false;
            }
            else {
                String[] columns = line.split(FrequencyCounter.COLUMN_SEPARATOR);
                if (columns.length != 2) {
                    throw new IllegalStateException("Invalid line in input file:\n" + line);
                }
                String token = columns[0];
                int count = Integer.parseInt(columns[1]);

                if (countingUnigrams) {
                    if (unigrams.containsKey(token)) {
                        throw new IllegalStateException(
                                "Duplicate token in input file: '" + token + "'.");
                    }
                    unigrams.put(token, count);
                }
                else {
                    if (bigrams.containsKey(token)) {
                        throw new IllegalStateException(
                                "Duplicate token in input file: '" + token + "'.");
                    }
                    bigrams.put(token, count);
                }
            }
        }
        reader.close();
    }
}
