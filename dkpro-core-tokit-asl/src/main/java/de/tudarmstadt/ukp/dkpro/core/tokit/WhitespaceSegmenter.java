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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * A strict whitespace tokenizer, i.e. tokenizes according to whitespaces and linebreaks only.
 * <p>
 * If {@code PARAM_WRITE_SENTENCES} is set to true, one sentence per line is assumed. Otherwise, no
 * sentences are created.
 * 
 * @deprecated Use {@link RegexSegmenter}
 */
@ResourceMetaData(name="Whitespace Segmenter")
@TypeCapability(
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
@Deprecated
public class WhitespaceSegmenter
    extends SegmenterBase
{
    // FIXME: this does not match Windows linebreaks ("\r\n")
    private static final Pattern lineBreak = Pattern.compile("\n");
    private static final Pattern whitespace = Pattern.compile("[ \n]+");

    @Override
    protected void process(JCas aJCas, String text, int zoneBegin)
        throws AnalysisEngineProcessException
    {
        /* append trailing linebreak if necessary */
        text = text.endsWith("\n") ? text : text + "\n";

        if (isWriteSentence()) {
            createSentences(aJCas, text);
        }

        createTokens(aJCas, text);
    }

    /**
     * Create sentences using the boundary pattern defined in {@link #lineBreak}.
     * 
     * @param aJCas
     *            the {@link JCas}
     * @param text
     *            the text.
     */
    private void createSentences(JCas aJCas, String text)
    {
        Matcher lineMatcher = lineBreak.matcher(text);
        int previousStart = 0;
        while (lineMatcher.find()) {
            int end = lineMatcher.start();
            Sentence sentence = new Sentence(aJCas, previousStart, end);
            sentence.addToIndexes(aJCas);
            previousStart = lineMatcher.end();
        }
    }

    /**
     * Create tokens using the boundary pattern defined in {@link #whitespace}.
     * 
     * @param aJCas
     *            the {@link JCas}
     * @param text
     *            the text
     */
    private void createTokens(JCas aJCas, String text)
    {
        Matcher whitespaceMatcher = whitespace.matcher(text);
        int previousStart = 0;
        while (whitespaceMatcher.find()) {
            int end = whitespaceMatcher.start();
            Token token = new Token(aJCas, previousStart, end);
            token.addToIndexes(aJCas);
            previousStart = whitespaceMatcher.end();
        }
    }
}
