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
package de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LexicalPhrase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create String sequences from JCas annotations. Use a
 * {@link PhraseSequenceGenerator.Builder#buildStringSequenceGenerator()} to create class instances.
 * <p>
 * Either create a single token sequence from the whole document, or multiple sequences based on
 * covering annotations, e.g. one sequence for each sentence.
 * <p>
 * By default, the sequences are created from {@link Token}s found in the input document. In order
 * to use other annotations, e.g. lemmas, specify the feature path in
 * {@link PhraseSequenceGenerator.Builder#featurePath(String)}.
 *
 * @since 1.9.0
 */
public class StringSequenceGenerator
{
    private PhraseSequenceGenerator psg;

    protected StringSequenceGenerator(PhraseSequenceGenerator.Builder builder)
            throws IOException
    {
        psg = builder.build();
    }

    /**
     * Generate a list of String sequences.
     *
     * @param aJCas
     *            the {@link JCas} to generate sequences from.
     * @return a list of string arrays.
     * @throws FeaturePathException
     *             if there was a problem creating the feature path.
     */
    public List<String[]> tokenSequences(JCas aJCas)
            throws FeaturePathException
    {
        return psg.tokenSequences(aJCas).stream()
                .map(this::phrases2String)
                .collect(Collectors.toList());
    }

    /**
     * Convert {@link LexicalPhrase} arrays to string arrays by extracting their texts.
     *
     * @param phrases an array of {@link LexicalPhrase}s.
     * @return an array of strings.
     */
    private String[] phrases2String(LexicalPhrase[] phrases)
    {
        return Stream.of(phrases)
                .map(LexicalPhrase::getText)
                .filter(string -> !string.isEmpty())
                .toArray(String[]::new);
    }
}
