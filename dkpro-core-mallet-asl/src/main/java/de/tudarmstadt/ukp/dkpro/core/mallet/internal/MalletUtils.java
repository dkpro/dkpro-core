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
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility methods for Mallet for creating {@link TokenSequence}s etc.
 */
public class MalletUtils
{
    public static final String NONE_LABEL = "X"; // some label has to be set for Mallet instances
    public static final String WHITESPACE_CHAR_REPLACEMENT = "</s>";

    /**
     * Generate a {@link TokenSequence} of all features (e.g. tokens or lemmas) covered by an
     * annotation (e.g. a sentence). If no coveringAnnotation is given (i.e. null), return all
     * features in the CAS.
     *
     * @param aJCas              a {@link JCas}
     * @param featurePath        a feature path, e.g. {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token} or {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token/lemma/value}
     * @param coveringAnnotation an Optional covering annotation from which tokens are selected, e.g. a {@link Sentence}
     * @param minTokenLength     an OptionalInt defining the minimum token length; all shorter tokens are omitted
     * @param lowercase          if true, all tokens are lowercased
     * @return a {@link TokenSequence} holding all extracted tokens
     * @throws FeaturePathException if the annotation type specified in PARAM_TOKEN_FEATURE_PATH cannot be extracted.
     */
    public static TokenSequence tokenSequence(JCas aJCas, String featurePath,
            Optional<AnnotationFS> coveringAnnotation, OptionalInt minTokenLength,
            boolean lowercase)
            throws FeaturePathException
    {
        List<String> tokenSequence = new ArrayList<>();

        FeaturePathFactory.FeaturePathIterator<AnnotationFS> valueIterator =
                FeaturePathUtils.featurePathIterator(aJCas, featurePath, coveringAnnotation);

        /* iterate over tokens */
        while (valueIterator.hasNext()) {
            String value = lowercase
                    ? valueIterator.next().getValue().toLowerCase()
                    : valueIterator.next().getValue();
            if (!minTokenLength.isPresent() || value.length() >= minTokenLength.getAsInt()) {
                tokenSequence.add(value);
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
     * @param featurePath      a feature path, e.g. (
     * @param coveringTypeName a  {@code Optional<String>} defining the covering annotation type name from which tokens are selected, e.g. {@code Sentence.getClass().getTokenFeaturePath()}
     * @param minTokenLength   an {@code OptionalInt} defining the minimum token length; all shorter tokens are omitted
     * @param lowercase        if true, all tokens are lowercased
     * @return a {@code Collection<TokenSequence>}
     * @throws FeaturePathException if the annotation type specified in PARAM_TOKEN_FEATURE_PATH cannot be extracted.
     */
    public static List<TokenSequence> tokenSequences(JCas aJCas, String featurePath,
            Optional<String> coveringTypeName, OptionalInt minTokenLength, boolean lowercase)
            throws FeaturePathException
    {
        List<TokenSequence> tokenSequences = new ArrayList<>();
        if (coveringTypeName.isPresent()) {
            Type coveringType = aJCas.getTypeSystem().getType(coveringTypeName.get());

            for (AnnotationFS covering : CasUtil.select(aJCas.getCas(), coveringType)) {
                TokenSequence ts = tokenSequence(aJCas, featurePath, Optional.of(covering),
                        minTokenLength, lowercase);
                tokenSequences.add(ts);
            }
        }
        else {
            TokenSequence ts = tokenSequence(aJCas, featurePath, Optional.empty(),
                    minTokenLength, lowercase);
            tokenSequences.add(ts);
        }
        return tokenSequences;
    }

    /**
     * Generate a token sequence where each 'token' is a character. Non-alphabet characters are omitted.
     *
     * @param text a string
     * @return a {@link TokenSequence}
     */
    private static TokenSequence characterSequence(String text)
    {
        return new TokenSequence(text.chars()
                .mapToObj(i -> (char) i)
                /* filter out strange characters */
                .filter(c -> Character.isAlphabetic(c) || Character.isDigit(c) || Character
                        .isWhitespace(c))
                /* replace whitespace characters */
                .map(c -> Character.isWhitespace(c) ? WHITESPACE_CHAR_REPLACEMENT : c)
                .toArray());
    }

    /**
     * Generate a character sequence for the whole document text.
     *
     * @param jCas      a {@link JCas}
     * @param lowercase if true, all tokens are lowercased
     * @return a {@link TokenSequence}
     */
    public static TokenSequence characterSequence(JCas jCas, boolean lowercase)
    {
        return characterSequence(lowercase
                ? jCas.getDocumentText().toLowerCase()
                : jCas.getDocumentText());
    }

    private static TokenSequence characterSequence(AnnotationFS coveringAnnotation,
            boolean lowercase)
    {
        return characterSequence(lowercase
                ? coveringAnnotation.getCoveredText().toLowerCase()
                : coveringAnnotation.getCoveredText());
    }

    /**
     * Generate a list of character sequences, one for each documentType (e.g. Sentence).
     *
     * @param aJCas            a {@link JCas}
     * @param documentTypeName a type name, e.g. {@code Sentence.class.getTypeName()}
     * @return a list of {@link TokenSequence}s
     */
    public static List<TokenSequence> characterSequences(JCas aJCas, String documentTypeName)
    {
        return characterSequences(aJCas, documentTypeName, false);
    }

    public static List<TokenSequence> characterSequences(JCas aJCas, String documentTypeName,
            boolean lowercase)
    {
        Type documentType = aJCas.getTypeSystem().getType(documentTypeName);
        return CasUtil.select(aJCas.getCas(), documentType).stream()
                .map(token -> characterSequence(token, lowercase))
                .collect(Collectors.toList());

    }

    public static List<TokenSequence> characterSequences(JCas aJCas,
            Optional<String> documentTypeName, boolean lowercase)
    {
        return documentTypeName.isPresent()
                ? characterSequences(aJCas, documentTypeName.get(), lowercase)
                : Collections.singletonList(characterSequence(aJCas, lowercase));
    }
}