/*
 * Copyright 2017
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

package de.tudarmstadt.ukp.dkpro.core.tokit;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * This segmenter splits sentences and tokens based on regular expressions that define the sentence
 * and token boundaries.
 * <p>
 * The default behavior is to split sentences by a line break and tokens by whitespace.
 */
@ResourceMetaData(name = "Regex Segmenter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class RegexSegmenter
    extends SegmenterBase
{
    private static final String LINEBREAK_PATTERN = "\n";
    private static final String WHITESPACE_PATTERN = "[\\s\n]+";

    /**
     * Defines the pattern that is used as token end boundary. Default: {@code [\s\n]+} (matching
     * whitespace and linebreaks.
     * <p>
     * When setting custom patterns, take into account that the final token is often terminated by a
     * linebreak rather than the boundary character. Therefore, the newline typically has to be
     * added to the group of matching characters, e.g. {@code "tokenized-text"} is correctly
     * tokenized with the pattern {@code [-\n]}.
     * 
     */
    public static final String PARAM_TOKEN_BOUNDARY_REGEX = "tokenBoundaryRegex";
    @ConfigurationParameter(name = PARAM_TOKEN_BOUNDARY_REGEX, mandatory = true, 
            defaultValue = WHITESPACE_PATTERN)
    private String tokenBoundaryRegex;
    private Pattern tokenBoundaryPattern;

    /**
     * Define the sentence boundary. Default: {@code \n} (assume one sentence per line).
     */
    public static final String PARAM_SENTENCE_BOUNDARY_REGEX = "sentenceBoundaryRegex";
    @ConfigurationParameter(name = PARAM_SENTENCE_BOUNDARY_REGEX, mandatory = true, 
            defaultValue = LINEBREAK_PATTERN)
    private String sentenceBoundaryRegex;
    private Pattern sentenceBoundaryPattern;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        tokenBoundaryPattern = Pattern.compile(tokenBoundaryRegex);
        sentenceBoundaryPattern = Pattern.compile(sentenceBoundaryRegex);
    };

    @Override
    protected void process(JCas aJCas, String text, int zoneBegin)
        throws AnalysisEngineProcessException
    {
        /* append trailing linebreak if necessary */
        text = text.endsWith("\n") ? text : text + "\n";

        if (isBlank(text)) {
            return;
        }

        if (isWriteSentence()) {
            createSentences(aJCas, text, zoneBegin);
        }

        createTokens(aJCas, text, zoneBegin);
    }

    /**
     * Create sentences using the boundary pattern defined in {@link #lineBreak}.
     * 
     * @param aJCas
     *            the {@link JCas}
     * @param text
     *            the text.
     * @param zoneBegin
     */
    private void createSentences(JCas aJCas, String text, int zoneBegin)
    {
        Matcher sentenceBoundaryMatcher = sentenceBoundaryPattern.matcher(text);
        int previousStart = 0;
        while (sentenceBoundaryMatcher.find()) {
            int end = sentenceBoundaryMatcher.start();
            Sentence sentence = new Sentence(aJCas, zoneBegin + previousStart, zoneBegin + end);
            sentence.addToIndexes(aJCas);
            previousStart = sentenceBoundaryMatcher.end();
        }
    }

    /**
     * Create tokens using the boundary pattern defined in {@link #whitespace}.
     * 
     * @param aJCas
     *            the {@link JCas}
     * @param text
     *            the text
     * @param zoneBegin
     */
    private void createTokens(JCas aJCas, String text, int zoneBegin)
    {
        Matcher tokenBoundaryMatcher = tokenBoundaryPattern.matcher(text);
        int previousStart = 0;
        while (tokenBoundaryMatcher.find()) {
            int end = tokenBoundaryMatcher.start();
            Token token = new Token(aJCas, zoneBegin + previousStart, zoneBegin + end);
            token.addToIndexes(aJCas);
            previousStart = tokenBoundaryMatcher.end();
        }
    }
}
