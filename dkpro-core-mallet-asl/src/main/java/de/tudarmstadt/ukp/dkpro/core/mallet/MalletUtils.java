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
package de.tudarmstadt.ukp.dkpro.core.mallet;

import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathInfo;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

/**
 * Utility methods for Mallet for creating {@link TokenSequence}s etc.
 */
public class MalletUtils
{
    public static final String NONE_LABEL = "X"; // some label has to be set for Mallet instances
    public static final String WHITESPACE_CHAR_REPLACEMENT = "SPACE";

    /**
     * Generate a TokenSequence from the whole document.
     *
     * @param aJCas          a CAS holding the document
     * @param tokenType      this type will be used as token, e.g. Token, N-gram etc.
     * @param useLemma       if this is true, use lemmas
     * @param minTokenLength the minimum token length to use
     * @return a {@link TokenSequence}
     * @throws FeaturePathException if the annotation type specified in {@code PARAM_TOKEN_FEATURE_PATH} cannot be extracted.
     * @deprecated use {@link #generateTokenSequence(JCas, String, Optional, OptionalInt, boolean)} instead
     */
    @Deprecated
    public static TokenSequence generateTokenSequence(JCas aJCas, Type tokenType,
            boolean useLemma, int minTokenLength)
            throws FeaturePathException
    {
        TokenSequence tokenSequence = new TokenSequence();
        for (AnnotationFS segment : CasUtil.select(aJCas.getCas(), tokenType)) {
            tokenSequence
                    .addAll(getTokensFromAnnotation(segment, useLemma, minTokenLength).toArray());
        }
        return tokenSequence;
    }

    /**
     * Extract the token texts (or lemmas) covered by the given annotation (e.g. a {@link Sentence}).
     *
     * @param annotation     a covering annotation (e.g. {@link Sentence})
     * @param useLemma       if true, extract lemmas instead of token texts.
     * @param minTokenLength the minimum length for the tokens (or lemmas); shorter tokens ar omitted.
     * @return a list of strings
     * @deprecated no longer required when using {@link #generateTokenSequences(JCas, String, Optional, OptionalInt, boolean)}
     */
    @Deprecated
    private static Collection<String> getTokensFromAnnotation(AnnotationFS annotation,
            boolean useLemma, int minTokenLength)
    {
        Collection<String> tokens;
        if (useLemma) {
            tokens = new ArrayList<>();

            /* concatenate multiple lemmas: */
            // selectCovered(Lemma.class, annotation).stream()
            // .map(lemma -> lemma.getValue())
            // .filter(lemma -> lemma.length() >= minTokenLength)
            // .reduce((l1, l2) -> l1 + "_" + l2)
            // .ifPresent(token -> tokens.add(token));

            for (Lemma lemma : selectCovered(Lemma.class, annotation)) {
                String text = lemma.getValue();
                if (text.length() >= minTokenLength) {
                    tokens.add(text);
                }
            }
        }
        else {
            tokens = new ArrayList<>(1);
            String text = annotation.getCoveredText();
            if (text.length() >= minTokenLength) {
                tokens.add(text);
            }
        }
        return tokens;
    }

    /**
     * Generate an instance from the text covered by the given annotation.
     *
     * @param minTokenLength the minimum length for the tokens (or lemmas); shorter tokens ar omitted.
     * @param useLemma       if this is true, use lemmas
     * @param annotation     an annotation representing a document segment, e.g. {@link Sentence}.
     * @param tokenType      the type to use for representing tokens, usually {@link Token}, but could also be
     *                       any other type.
     * @return a {@link TokenSequence}
     * @deprecated use {@link #generateTokenSequence(JCas, String, Optional, OptionalInt, boolean)} instead
     */
    @Deprecated
    public static TokenSequence generateTokenSequence(int minTokenLength, boolean useLemma,
            AnnotationFS annotation, Type tokenType)
    {
        TokenSequence tokenSequence = new TokenSequence();

        for (AnnotationFS segment : CasUtil.selectCovered(tokenType, annotation)) {
            tokenSequence
                    .addAll(getTokensFromAnnotation(segment, useLemma, minTokenLength).toArray());
        }
        return tokenSequence;
    }

    /**
     * Generate one or multiple TokenSequences from the given document. If
     * {@code documentEntity} is set, an instance is generated from each segment annotated
     * with the given type. Otherwise, one instance is generated from the whole document.
     *
     * @param aJCas          the current {@link JCas}
     * @param documentEntity the entity determining a document, e.g. Sentence
     * @param minTokenLength all tokens with less characters are omitted
     * @param typeName       the annotation type of a token
     * @param useLemma       if true, use lemmas instead of tokens
     * @return a list of {@link TokenSequence}s representing the documents (or e.g. sentences).
     * @throws FeaturePathException
     * @deprecated use {@link #generateTokenSequences(JCas, String, Optional, OptionalInt, boolean)} instead
     */
    @Deprecated
    public static Collection<TokenSequence> generateTokenSequences(JCas aJCas, String typeName,
            boolean useLemma, int minTokenLength, String documentEntity)
            throws FeaturePathException
    {
        Collection<TokenSequence> tokenSequences;
        CAS cas = aJCas.getCas();
        Type tokenType = CasUtil.getType(cas, typeName);

        if (documentEntity == null) {
            /* generate only one tokenSequence (for the whole document) */
            tokenSequences = new ArrayList<>(1);
            tokenSequences.add(generateTokenSequence(aJCas, tokenType, useLemma, minTokenLength));
        }
        else {
            /* generate tokenSequences for every segment (e.g. sentence) */
            tokenSequences = CasUtil.select(cas, CasUtil.getType(cas, documentEntity)).stream()
                    .map(segment -> generateTokenSequence(minTokenLength, useLemma, segment,
                            tokenType))
                    .collect(Collectors.toList());
        }
        return tokenSequences;
    }

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
     * @throws FeaturePathException
     */
    public static TokenSequence generateTokenSequence(JCas aJCas, String featurePath,
            Optional<AnnotationFS> coveringAnnotation, OptionalInt minTokenLength,
            boolean lowercase)
            throws FeaturePathException
    {
        return new TokenSequence(extractAnnotationValues(aJCas, featurePath, coveringAnnotation,
                minTokenLength, lowercase)
                .toArray());
    }

    /**
     * Extract a list of feature values.
     *
     * @param aJCas              a {@link JCas}
     * @param featurePath        the feature path of the "tokens"
     * @param coveringAnnotation if set, extract only tokens covered by this annotation (e.g. a sentences); otherwise, all tokens in the CAS are extracted
     * @param minTokenLength     tokens that are shorter than this value are omitted
     * @param lowercase          if true, all tokens are lowercased
     * @return a list of strings
     * @throws FeaturePathException
     */
    public static List<String> extractAnnotationValues(JCas aJCas, String featurePath,
            Optional<AnnotationFS> coveringAnnotation, OptionalInt minTokenLength,
            boolean lowercase)
            throws FeaturePathException
    {
        String[] segments = featurePath.split("/", 2);
        String typeName = segments[0];

        Type type = aJCas.getTypeSystem().getType(typeName);
        if (type == null) {
            throw new IllegalStateException("Type [" + typeName + "] not found in type system");
        }

        FeaturePathInfo fpInfo = initFeaturePathInfo(segments);

        List<String> tokenSequence = new ArrayList<>();

        Collection<AnnotationFS> features = coveringAnnotation.isPresent()
                ? CasUtil.selectCovered(type, coveringAnnotation.get())
                : CasUtil.select(aJCas.getCas(), type);
        FeaturePathFactory.FeaturePathIterator<AnnotationFS> valueIterator =
                new FeaturePathFactory.FeaturePathIterator<>(features.iterator(), fpInfo);

        /* iterate over tokens */
        while (valueIterator.hasNext()) {
            String value = lowercase
                    ? valueIterator.next().getValue().toLowerCase()
                    : valueIterator.next().getValue();
            if (!minTokenLength.isPresent() || value.length() >= minTokenLength.getAsInt()) {
                tokenSequence.add(value);
            }
        }
        return tokenSequence;
    }

    /**
     * Return a collection of {@link TokenSequence}s from a single CAS. The coverage of each
     * token sequence is determined by the documentType parameter (e.g. sentence). If no documentTypeName
     * is set, one token sequence is created from the whole CAS.
     *
     * @param aJCas            a {@link JCas}
     * @param featurePath      a feature path, e.g. (
     * @param documentTypeName a  {@code Optional<String>} defining the covering annotation type name from which tokens are selected, e.g. {@code Sentence.getClass().getTokenFeaturePath()}
     * @param minTokenLength   an {@code OptionalInt} defining the minimum token length; all shorter tokens are omitted
     * @return a {@code Collection<TokenSequence>}
     * @throws FeaturePathException
     */
    public static List<TokenSequence> generateTokenSequences(JCas aJCas, String featurePath,
            Optional<String> documentTypeName, OptionalInt minTokenLength, boolean lowercase)
            throws FeaturePathException
    {
        List<TokenSequence> tokenSequences = new ArrayList<>();
        if (documentTypeName.isPresent()) {
            Type documentType = aJCas.getTypeSystem().getType(documentTypeName.get());

            for (AnnotationFS covering : CasUtil.select(aJCas.getCas(), documentType)) {
                TokenSequence ts = generateTokenSequence(aJCas, featurePath, Optional.of(covering),
                        minTokenLength, lowercase);
                tokenSequences.add(ts);
            }
        }
        else {
            TokenSequence ts = generateTokenSequence(aJCas, featurePath, Optional.empty(),
                    minTokenLength, lowercase);
            tokenSequences.add(ts);
        }
        return tokenSequences;
    }

    /**
     * Generate a feature path info.
     *
     * @param segments an array of strings previously split so that the first element represents the
     *                 feature type and the second element (if applicable) contains the feature path.
     * @return a {@link FeaturePathInfo}
     * @throws FeaturePathException if an error occurs during initialization of the feature path
     */

    private static FeaturePathInfo initFeaturePathInfo(String[] segments)
            throws FeaturePathException
    {
        FeaturePathInfo fpInfo = new FeaturePathInfo();
        fpInfo.initialize(segments.length > 1 ? segments[1] : "");
        return fpInfo;
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
     * @param jCas a {@link JCas}
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