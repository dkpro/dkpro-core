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
package de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.io.TextUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LexicalPhrase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generate sequences of phrases with optional stopword/regex-based filtering, and lowercasing.
 * Filtered tokens are added as {@link LexicalPhrase}s with empty text or a replacement of the text,
 * if {@link Builder#stopwordReplacement} and/or {@link Builder#filterRegexReplacement} were set.
 * <p>
 * Initialize with {@link Builder#build()}.
 * <p>
 * When strings instead of {@link LexicalPhrase}s should be output, use {@link Builder#buildStringSequenceGenerator()}.
 *
 * @since 1.9.0
 */
public class PhraseSequenceGenerator
{
    public static final String WHITESPACE_CHAR_REPLACEMENT = "</s>";

    private final boolean lowercase;
    private final Optional<String> coveringTypeName;
    private final String filterRegexReplacement;
    @SuppressWarnings("SpellCheckingInspection") private final Set<String> filterRegexes;
    private final String stopwordReplacement;
    private final Collection<String> stopwords;
    private final String featurePath;
    private final int minTokenLength;

    private final boolean useCharacters;

    private PhraseSequenceGenerator(Builder builder)
            throws IOException
    {
        this.lowercase = builder.lowercase;
        this.coveringTypeName = builder.coveringType;
        this.minTokenLength = builder.minTokenLength;
        this.featurePath = builder.featurePath;
        stopwords = builder.stopwordsFile.isPresent()
                ? TextUtils.readStopwordsURL(builder.stopwordsFile.get(), lowercase)
                : Collections.emptySet();
        this.stopwordReplacement = builder.stopwordsReplacement;
        this.filterRegexes = builder.filterRegexes;
        this.filterRegexReplacement = builder.filterRegexReplacement;
        this.useCharacters = builder.characters;
    }

    /**
     * Generate a list of {@link LexicalPhrase} sequences where each list element represents phrases
     * extracted from the covering types, e.g. a sentence. If no covering type was defined, the list
     * contains one element representing the whole document.
     *
     * @param aJCas
     *            a {@link JCas}
     * @return a list of {@link LexicalPhrase} arrays
     * @throws FeaturePathException
     *             if there was a problem creating the feature path.
     */
    public List<LexicalPhrase[]> tokenSequences(JCas aJCas)
            throws FeaturePathException
    {
        return useCharacters ? characterSequences(aJCas) : annotationSequences(aJCas);
    }

    /**
     * Extract a list of {@link LexicalPhrase} arrays from the {@link JCas}.
     * <p>
     * If {@link #coveringTypeName} is set, a dedicated array for each covering annotation is
     * extracted. Otherwise, the result contains only one element.
     *
     * @param aJCas
     *            a {@link JCas}
     * @return a list of {@link LexicalPhrase} arrays
     * @throws FeaturePathException
     *             if there was a problem creating the feature path.
     */
    private List<LexicalPhrase[]> annotationSequences(JCas aJCas)
            throws FeaturePathException
    {
        List<LexicalPhrase[]> phrases = new ArrayList<>();

        if (coveringTypeName.isPresent()) {
            Type coveringType = FeaturePathUtils
                    .getType(aJCas.getTypeSystem(), coveringTypeName.get());

            /* iterate over covering annotations */
            for (AnnotationFS covering : CasUtil.select(aJCas.getCas(), coveringType)) {
                phrases.add(annotationSequence(aJCas, Optional.of(covering)));
            }
        }
        else {
            /* add a single token sequence for the whole document */
            phrases.add(annotationSequence(aJCas, Optional.empty()));
        }
        return phrases;
    }

    /**
     * Extract a list of {@link LexicalPhrase} arrays from the {@link JCas}.
     * <p>
     * If {@link #coveringTypeName} is set, a dedicated array for each covering annotation is
     * extracted. Otherwise, the result contains only one element.
     *
     * @param aJCas
     *            a {@link JCas}
     * @return a list of {@link LexicalPhrase} arrays
     * @throws FeaturePathException
     *             if there was a problem creating the feature path.
     */
    private List<LexicalPhrase[]> characterSequences(JCas aJCas)
            throws FeaturePathException
    {
        if (coveringTypeName.isPresent()) {
            Type coveringType = FeaturePathUtils
                    .getType(aJCas.getTypeSystem(), coveringTypeName.get());

            return CasUtil.select(aJCas.getCas(), coveringType).stream()
                    .map(covering -> characterSequence(aJCas, covering.getCoveredText(),
                            covering.getBegin()))
                    .collect(Collectors.toList());
        }
        else {
            return Collections.singletonList(characterSequence(aJCas, aJCas.getDocumentText(), 0));
        }
    }

    /**
     * Generate an array of {@link LexicalPhrase}s from features (e.g. tokens or lemmas) covered by
     * an annotation (e.g. a sentence). If no coveringAnnotation is set, return all features in the
     * CAS.
     * <p>
     * Optionally, the tokens are filtered by stopwords and/or regular expressions. In matching
     * elements, the phrase texts are replaced according to {@link Builder#stopwordReplacement} and
     * {@link Builder#filterRegexReplacement}.
     *
     * @param aJCas
     *            a {@link JCas}
     * @param coveringAnnotation
     *            an Optional covering annotation from which tokens are selected, e.g. a
     *            {@link Sentence}
     * @return an array of {@link LexicalPhrase}s representing all extracted tokens
     * @throws FeaturePathException
     *             if the annotation type specified in PARAM_TOKEN_FEATURE_PATH cannot be extracted.
     */
    private LexicalPhrase[] annotationSequence(JCas aJCas,
            Optional<AnnotationFS> coveringAnnotation)
            throws FeaturePathException
    {
        List<LexicalPhrase> sequence = new ArrayList<>();

        FeaturePathFactory.FeaturePathIterator<AnnotationFS> valueIterator =
                FeaturePathUtils.featurePathIterator(aJCas, featurePath, coveringAnnotation);

        /* iterate over tokens (optionally within covering annotation) */
        while (valueIterator.hasNext()) {
            Map.Entry<AnnotationFS, String> entry = valueIterator.next();
            AnnotationFS annotation = entry.getKey();
            LexicalPhrase phrase = new LexicalPhrase(aJCas, annotation.getBegin(),
                    annotation.getEnd());

            String text = entry.getValue();

            /* transform text */
            text = text.length() < minTokenLength ? "" : text;
            text = lowercase ? text.toLowerCase() : text;
            text = stopwords.contains(text) ? stopwordReplacement : text;
            for (String filterRegex : filterRegexes) {
                text = text.matches(filterRegex) ? filterRegexReplacement : text;
            }

            phrase.setText(text);
            sequence.add(phrase);
        }
        return sequence.toArray(new LexicalPhrase[sequence.size()]);
    }

    /**
     * Generate a sequence of {@link LexicalPhrase}s based on characters.
     * <p>
     * Whitespaces are replaced by {@link #WHITESPACE_CHAR_REPLACEMENT}. All characters that are
     * neither alphabetic, digits, or whitespace are omitted.
     *
     * @param aJCas the {@link JCas}
     * @param text  the text to extract characters from
     * @param begin the begin of the first {@link LexicalPhrase} annotation
     * @return an array of {@link LexicalPhrase}s
     */
    private LexicalPhrase[] characterSequence(JCas aJCas, String text, int begin)
    {
        List<LexicalPhrase> sequence = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isAlphabetic(c) || Character.isDigit(c) || Character
                    .isWhitespace(c)) {
                String s = Character.isWhitespace(c) ?
                        WHITESPACE_CHAR_REPLACEMENT :
                        String.valueOf(c);
                if (lowercase) {
                    s = s.toLowerCase();
                }
                LexicalPhrase phrase = new LexicalPhrase(aJCas, begin + i, begin + i + 1);
                phrase.setText(s);
                sequence.add(phrase);
            }
        }
        return sequence.toArray(new LexicalPhrase[sequence.size()]);
    }

    /**
     * Builder for {@link PhraseSequenceGenerator}s.
     * <p>
     * Alternative constructs a {@link StringSequenceGenerator} with {@link #buildStringSequenceGenerator()}
     */
    public static class Builder
    {
        private boolean lowercase = false;
        private Optional<String> coveringType = Optional.empty();
        private int minTokenLength = 0;
        private Optional<URL> stopwordsFile = Optional.empty();
        private String stopwordsReplacement = "";
        private String featurePath = Token.class.getCanonicalName();
        @SuppressWarnings("SpellCheckingInspection") private Set<String> filterRegexes = new HashSet<>();
        private String filterRegexReplacement = "";
        private boolean characters = false;

        /**
         * @param featurePath set the feature path to use for creating token sequences.
         * @return a {@link Builder}
         */
        public Builder featurePath(String featurePath)
        {
            this.featurePath = featurePath;
            return this;
        }

        public Builder stopwordsFile(String stopwordsFile)
                throws MalformedURLException
        {
            if (stopwordsFile.isEmpty()) {
                this.stopwordsFile = Optional.empty();
                return this;
            } else {
                return stopwordsFile(new File(stopwordsFile));
            }
        }

        public Builder stopwordsFile(File stopwordsFile)
                throws MalformedURLException
        {
            URL url = stopwordsFile.toURI().toURL();
            return stopwordsURL(url);
        }

        /**
         * @param stopwordsURL set the location of the stopwords file
         * @return a {@link Builder}
         */
        public Builder stopwordsURL(URL stopwordsURL)
        {
            this.stopwordsFile = Optional.of(stopwordsURL);
            return this;
        }

        /**
         * @param stopwordsReplacement stopwords are replaced by this string or removed if replacement string is empty
         * @return a {@link Builder}
         */
        public Builder stopwordsReplacement(String stopwordsReplacement)
        {
            this.stopwordsReplacement = stopwordsReplacement == null ? "" : stopwordsReplacement;
            return this;
        }

        /**
         * @param minTokenLength tokens shorter than the given length are filtered out
         * @return a {@link Builder}
         */
        @SuppressWarnings("unused") public Builder minTokenLength(int minTokenLength)
        {
            this.minTokenLength = minTokenLength;
            return this;
        }

        /**
         * This method can be called multiple times in order to add multiple regular expressions for filtering.
         * If a token matches any of the regular expression, it is omitted.
         *
         * @param filterRegex Tokens matching this regular expression are filtered out.
         * @return a {@link Builder}
         */
        public Builder filterRegex(String filterRegex)
        {
            if (!filterRegex.isEmpty()) {
                this.filterRegexes.add(filterRegex);
            }
            return this;
        }

        /**
         * @param filterRegexReplacement tokens matching the {@link #filterRegexes} are replaced by this string. If this is empty, these tokens are removed.
         * @return a {@link Builder}
         */
        public Builder filterRegexReplacement(String filterRegexReplacement)
        {
            this.filterRegexReplacement =
                    filterRegexReplacement == null ? "" : filterRegexReplacement;
            return this;
        }

        /**
         * @param lowercase If true, all tokens are lowercased
         * @return a {@link Builder}
         */
        public Builder lowercase(boolean lowercase)
        {
            this.lowercase = lowercase;
            return this;
        }

        /**
         * @param coveringType if set, a separate string sequence is generated for each sequence covered
         *                     by the covering type, e.g. one sequence for each sentence.
         * @return a {@link Builder}
         */
        public Builder coveringType(String coveringType)
        {
            this.coveringType = coveringType == null || coveringType.isEmpty() ?
                    Optional.empty() :
                    Optional.of(coveringType);
            return this;
        }

        /**
         * If set to true, the generated phrases contain characters instead of tokens or other annotations.
         *
         * @param characters a boolean
         * @return a {@link Builder}
         */
        public Builder characters(boolean characters)
        {
            this.characters = characters;
            return this;
        }

        /**
         * Generate a {@link PhraseSequenceGenerator}
         *
         * @return a {@link PhraseSequenceGenerator} instance
         * @throws IOException if a stopwords file is specified but cannot be read
         */
        public PhraseSequenceGenerator build()
                throws IOException
        {
            return new PhraseSequenceGenerator(this);
        }

        /**
         * Generate a {@link StringSequenceGenerator} that directly returns Strings
         * instead of {@link LexicalPhrase}s.
         *
         * @return a {@link StringSequenceGenerator} instance
         * @throws IOException if a stopwords file is specified but cannot be read
         */
        public StringSequenceGenerator buildStringSequenceGenerator()
                throws IOException
        {
            return new StringSequenceGenerator(this);
        }
    }
}
