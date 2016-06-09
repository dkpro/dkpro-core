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
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.type.Phrase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private Map<String, Integer> unigrams;
    private Map<String, Integer> bigrams;
    private int vocabularySize;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        readCounts();
        vocabularySize = unigrams.size();

        /* set feature path to default */
        if (featurePath == null) {
            featurePath = DEFAULT_FEATURE_PATH;
        }
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        FeaturePathFactory.FeaturePathIterator<AnnotationFS> featurePathIterator;
        try {
            featurePathIterator = FeaturePathUtils
                    .featurePathIterator(aJCas, featurePath, Optional.empty());
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }

        /* start iteration if at least one annotation is present */
        if (featurePathIterator.hasNext()) {
            // stores the second token of a (potential) bigram
            Map.Entry<AnnotationFS, String> second = featurePathIterator.next();

            while (featurePathIterator.hasNext()) {
                // move forward to the previously second token of a bigram
                Map.Entry<AnnotationFS, String> first = second;
                assert first.getKey() instanceof Annotation;

                if (featurePathIterator.hasNext()) {
                    second = featurePathIterator.next(); // the second token of the bigram

                    String token1 = first.getValue()
                            .replaceAll(FrequencyCounter.COLUMN_SEPARATOR,
                                    FrequencyCounter.COLUMN_SEP_REPLACEMENT);
                    String token2 = second.getValue()
                            .replaceAll(FrequencyCounter.COLUMN_SEPARATOR,
                                    FrequencyCounter.COLUMN_SEP_REPLACEMENT);
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
                            /* bigram phrase */
                            new Phrase(aJCas, first.getKey().getBegin(), second.getKey().getEnd())
                                    .addToIndexes(aJCas);

                            /* skip second token of bigram to prevent overlapping phrases */
                            if (featurePathIterator.hasNext()) {
                                second = featurePathIterator.next();
                            }
                        }
                        else {
                            /* unigram phrase */
                            new Phrase(aJCas, first.getKey().getBegin(), first.getKey().getEnd())
                                    .addToIndexes(aJCas);
                        }
                    }
                    else {
                        /* out of vocabulary bigram, unigram phrase */
                        new Phrase(aJCas, first.getKey().getBegin(), first.getKey().getEnd())
                                .addToIndexes(aJCas);
                    }
                }
            }
            /* last token in sequence */
            new Phrase(aJCas, second.getKey().getBegin(), second.getKey().getEnd())
                    .addToIndexes(aJCas);
        }
    }

    private void readCounts()
            throws ResourceInitializationException
    {
        unigrams = new HashMap<>();
        bigrams = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(CompressionUtils
                    .getInputStream(modelLocation, new FileInputStream(modelLocation))));
            String line;
            boolean countingUnigrams = true;
            while ((line = reader.readLine()) != null) {
                if (line.equals(FrequencyCounter.NGRAM_SEPARATOR_LINE)) {
                    countingUnigrams = false;
                    continue;
                }
                String[] columns = line.split(FrequencyCounter.COLUMN_SEPARATOR);
                assert columns.length == 2;
                if (countingUnigrams) {
                    unigrams.put(columns[0], Integer.parseInt(columns[1]));
                }
                else {
                    bigrams.put(columns[0], Integer.parseInt(columns[1]));
                }
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }
}
