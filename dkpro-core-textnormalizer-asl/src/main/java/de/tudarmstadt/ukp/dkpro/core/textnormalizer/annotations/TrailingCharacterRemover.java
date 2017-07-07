/*
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.annotations;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Removing trailing character (sequences) from tokens, e.g. punctuation.
 */
@ResourceMetaData(name="Trailing Character Remover")
public class TrailingCharacterRemover
    extends JCasAnnotator_ImplBase
{
    /**
     * A regex to be trimmed from the end of tokens.
     * <p>
     * Default: {@code "[\\Q,-“^»*’()&amp;/\"'©§'—«·=\\E0-9A-Z]+"} (remove punctuations, special
     * characters and capital letters).
     */
    public static final String PARAM_PATTERN = "pattern";
    @ConfigurationParameter(name = PARAM_PATTERN, mandatory = true, defaultValue = "[\\Q,-“^»*’()&/\"'©§'—«·=\\E0-9A-Z]+")
    private String pattern;
    private Pattern suffixPattern;

    /**
     * All tokens that are shorter than the minimum token length after removing trailing chars are
     * completely removed. By default (1), empty tokens are removed. Set to 0 or a negative value if
     * no tokens should be removed.
     * <p>
     * Shorter tokens that do not have trailing chars removed are always retained, regardless of
     * their length.
     */
    public static final String PARAM_MIN_TOKEN_LENGTH = "minTokenLength";
    @ConfigurationParameter(name = PARAM_MIN_TOKEN_LENGTH, mandatory = true, defaultValue = "1")
    private int minTokenLength;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        suffixPattern = Pattern.compile(String.format(".*?(%s)$", pattern));
    };

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        List<Token> toRemove = new ArrayList<>();
        List<Token> tokens = new ArrayList<>(select(aJCas, Token.class));
        for (Token token : tokens) {
            Matcher suffixMatcher = suffixPattern.matcher(token.getCoveredText());
            if (suffixMatcher.matches()) {
                token.removeFromIndexes();
                token.setEnd(token.getEnd() - (suffixMatcher.end(1) - suffixMatcher.start(1)));
                token.addToIndexes();

                /* remove tokens that have become too short */
                if (minTokenLength > 0 && token.getEnd() - token.getBegin() < minTokenLength) {
                    toRemove.add(token);
                }
            }
        }

        for (Token token : toRemove) {
            token.removeFromIndexes(aJCas);
        }
    }
}
