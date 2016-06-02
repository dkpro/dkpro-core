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
package de.tudarmstadt.ukp.dkpro.core.mallet.internal;

import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.io.TextUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Create {@link TokenSequence}s from JCas'.
 * <p>
 * Either create a single token sequence from the whole document, or multiple sequences based on
 * covering annotations, e.g. one sequence for each sentence.
 * <p>
 * By default, the sequences are created from {@link Token}s found in the input document. In order to use
 * other annotations, e.g. lemmas, specify the feature path in the {@link #TokenSequenceGenerator(String)} constructor.
 */
public class TokenSequenceGenerator
{
    public static final String NONE_LABEL = "X"; // some label has to be set for Mallet instances
    public static final String WHITESPACE_CHAR_REPLACEMENT = "</s>";
    private String featurePath = Token.class.getCanonicalName();
    /**
     * ignore tokens that are shorter than this value. If <= 0 or negative, filter nothing.
     */
    private int minTokenLength = 0;
    private boolean lowercase = false;
    private boolean useCharacters = false;
    private Set<String> stopwords = Collections.emptySet();
    private String stopwordReplacement = "";

    /**
     * Default constructor. Uses {@link Token}s to create token sequences.
     */
    public TokenSequenceGenerator()
    {
    }

    /**
     * Use this constructor to specify a feature path (e.g. lemmas).
     *
     * @param featurePath a feature path, e.g. {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token/lemma/value}.
     */
    public TokenSequenceGenerator(String featurePath)
    {
        this.featurePath = featurePath;
    }

    /**
     * @param stopwordReplacement stopwords are replaced by this token.
     *                            If empty (default) stopwords are omitted.
     */
    public void setStopwordReplacement(String stopwordReplacement)
    {
        this.stopwordReplacement = stopwordReplacement;
    }

    /**
     * @param minTokenLength Tokens shorter than this value will be filtered out. Defaults to 0, i.e. nothing is filtered.
     */
    public void setMinTokenLength(int minTokenLength)
    {
        this.minTokenLength = minTokenLength;
    }

    /**
     * @param lowercase If true, all characters are lowercased during token sequence generation.
     */
    public void setLowercase(boolean lowercase)
    {
        this.lowercase = lowercase;
    }

    /**
     * @param useCharacters if set to true, generate a "token" from each character in the document text.
     */
    public void setUseCharacters(boolean useCharacters)
    {
        this.useCharacters = useCharacters;
    }

    public void setStopwords(File stopwordsFile)
            throws IOException
    {
        stopwords = TextUtils.readStopwordsFile(stopwordsFile, lowercase);
    }

    /**
     * Create a list containing only one {@link TokenSequence} from the whole document.
     *
     * @param aJCas a {@link JCas}
     * @return a list containing a single {@link TokenSequence}
     * @throws FeaturePathException
     */
    public List<TokenSequence> tokenSequences(JCas aJCas)
            throws FeaturePathException
    {
        return tokenSequences(aJCas, Optional.empty());
    }

    /**
     * Create a list containing {@link TokenSequence}s from the document.
     * <p>
     * If the covering type parameter is empty, the resulting list contains only one {@link TokenSequence}
     * for the whole document. Otherwise, iterate over the annotations
     * specified by the covering type, e.g. sentences, and create a dedicated token sequence for each one.
     *
     * @param aJCas a {@link JCas}
     * @return a list containing {@link TokenSequence}s
     * @throws FeaturePathException
     */
    public List<TokenSequence> tokenSequences(JCas aJCas, Optional<String> coveringType)
            throws FeaturePathException
    {
        return useCharacters ?
                characterSequences(aJCas, coveringType) :
                annotationSequences(aJCas, coveringType);
    }

    /**
     * Generate a list of {@link TokenSequence}s from characters.
     *
     * @param aJCas            a {@link JCas}
     * @param coveringTypeName if present, create a dedicated token sequence for each covering type, e.g. token or sentence
     * @return a list of {@link TokenSequence}s
     */
    private List<TokenSequence> characterSequences(JCas aJCas, Optional<String> coveringTypeName)
    {
        if (coveringTypeName.isPresent()) {
            Type coveringType = aJCas.getTypeSystem().getType(coveringTypeName.get());
            return CasUtil.select(aJCas.getCas(), coveringType).stream()
                    .map(AnnotationFS::getCoveredText)
                    .map(this::characterSequence)
                    .collect(Collectors.toList());
        }
        else {
            return Collections.singletonList(characterSequence(aJCas.getDocumentText()));
        }

    }

    /**
     * Generate a {@link TokenSequence} of all features (e.g. tokens or lemmas) covered by an
     * annotation (e.g. a sentence). If no coveringAnnotation is given (i.e. null), return all
     * features in the CAS.
     *
     * @param aJCas              a {@link JCas}
     * @param coveringAnnotation an Optional covering annotation from which tokens are selected, e.g. a {@link Sentence}
     * @return a {@link TokenSequence} holding all extracted tokens
     * @throws FeaturePathException if the annotation type specified in PARAM_TOKEN_FEATURE_PATH cannot be extracted.
     */
    private TokenSequence annotationSequence(JCas aJCas, Optional<AnnotationFS> coveringAnnotation)
            throws FeaturePathException
    {
        List<String> tokenSequence = new ArrayList<>();

        FeaturePathFactory.FeaturePathIterator<AnnotationFS> valueIterator =
                FeaturePathUtils.featurePathIterator(aJCas, featurePath, coveringAnnotation);

        /* iterate over tokens (optionally within covering annotation) */
        while (valueIterator.hasNext()) {
            String token = valueIterator.next().getValue();
            if (token.length() >= minTokenLength) {
                if (lowercase) {
                    token = token.toLowerCase();
                }
                if (stopwords.contains(token)) {
                    token = stopwordReplacement;
                }
                if (!token.isEmpty()) {
                    tokenSequence.add(token);
                }
            }
        }
        return new TokenSequence(tokenSequence.toArray());
    }

    /**
     * Return a collection of {@link TokenSequence}s from a single CAS. The coverage of each
     * token sequence is determined by the coveringTypeName parameter (e.g. sentence). If no coveringTypeName
     * is set, one token sequence is created from the whole CAS.
     *
     * @param aJCas            a {@link JCas}
     * @param coveringTypeName a  {@code Optional<String>} defining the covering annotation type name from which tokens are selected, e.g. {@code Sentence.getClass().getTokenFeaturePath()}
     * @return a {@code Collection<TokenSequence>}
     * @throws FeaturePathException if the annotation type specified in PARAM_TOKEN_FEATURE_PATH cannot be extracted.
     */
    private List<TokenSequence> annotationSequences(JCas aJCas, Optional<String> coveringTypeName)
            throws FeaturePathException
    {
        List<TokenSequence> tokenSequences = new ArrayList<>();
        if (coveringTypeName.isPresent()) {
            Type coveringType = aJCas.getTypeSystem().getType(coveringTypeName.get());

            /* iterate over covering annotations */
            for (AnnotationFS covering : CasUtil.select(aJCas.getCas(), coveringType)) {
                tokenSequences.add(annotationSequence(aJCas, Optional.of(covering)));
            }
        }
        else {
            /* add a single token sequence for the whole document */
            tokenSequences.add(annotationSequence(aJCas, Optional.empty()));
        }
        return tokenSequences;
    }

    /**
     * Generate a token sequence where each 'token' is a character. Non-alphabet characters are omitted.
     *
     * @param text a string
     * @return a {@link TokenSequence}
     */
    private TokenSequence characterSequence(String text)
    {
        if (lowercase) {
            text = text.toLowerCase();
        }
        return new TokenSequence(text.chars()
                .mapToObj(i -> (char) i)
                /* filter out strange characters */
                .filter(c -> Character.isAlphabetic(c) || Character.isDigit(c) || Character
                        .isWhitespace(c))
                /* replace whitespace characters */
                .map(c -> Character.isWhitespace(c) ? WHITESPACE_CHAR_REPLACEMENT : c)
                .toArray());
    }
}