/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.mallet;

import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

/**
 * Utility methods for Mallet for creating {@link TokenSequence}s etc.
 */
public class MalletUtils
{
    public static final String NONE_LABEL = "X"; // some label has to be set for Mallet instances

    /**
     * Generate a TokenSequence from the whole document.
     *
     * @param aJCas          a CAS holding the document
     * @param tokenType      this type will be used as token, e.g. Token, N-gram etc.
     * @param useLemma       if this is true, use lemmas
     * @param minTokenLength the minimum token length to use
     * @return a {@link TokenSequence}
     * @throws FeaturePathException if the annotation type specified in {@code PARAM_TYPE_NAME} cannot be extracted.
     */
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
     */
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
     */
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
     * {@code PARAM_MODEL_ENTITY_TYPE} is set, an instance is generated from each segment annotated
     * with the given type. Otherwise, one instance is generated from the whole document.
     *
     * @param aJCas the current {@link JCas}
     * @return a list of {@link TokenSequence}s representing the documents (or e.g. sentences).
     * @throws FeaturePathException
     */
    public static Collection<TokenSequence> generateTokenSequences(JCas aJCas, String typeName,
            boolean useLemma, int minTokenLength, String modelEntityType)
            throws FeaturePathException
    {
        Collection<TokenSequence> tokenSequences;
        CAS cas = aJCas.getCas();
        Type tokenType = CasUtil.getType(cas, typeName);

        if (modelEntityType == null) {
            /* generate only one tokenSequence (for the whole document) */
            tokenSequences = new ArrayList<>(1);
            tokenSequences.add(generateTokenSequence(aJCas, tokenType, useLemma, minTokenLength));
        }
        else {
            /* generate tokenSequences for every segment (e.g. sentence) */
            tokenSequences = CasUtil.select(cas, CasUtil.getType(cas, modelEntityType)).stream()
                    .map(segment -> generateTokenSequence(minTokenLength, useLemma, segment,
                            tokenType))
                    .collect(Collectors.toList());
        }
        return tokenSequences;
    }
}
