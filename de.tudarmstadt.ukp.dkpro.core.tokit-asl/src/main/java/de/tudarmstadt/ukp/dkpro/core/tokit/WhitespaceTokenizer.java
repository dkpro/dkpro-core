/*******************************************************************************
 * Copyright 2015
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
 ******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.tokit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
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
 */
public class WhitespaceTokenizer
    extends SegmenterBase
{
    private static final Pattern lineBreak = Pattern.compile("(.*?)\n");
    private static final Pattern whitespace = Pattern.compile("([^ \n]+)");

    @Override
    protected void process(JCas aJCas, String text, int zoneBegin)
        throws AnalysisEngineProcessException
    {
        /* append trailing linebreak if necessary */
        text = text.endsWith("\n") ? text : text + "\n";
        Matcher lineMatcher = lineBreak.matcher(text);

        /* detect sentences */
        if (isWriteSentence()) {
            while (lineMatcher.find() && lineMatcher.start() < text.length()) {
                Sentence sentence = new Sentence(aJCas, lineMatcher.start(1), lineMatcher.end(1));
                sentence.addToIndexes(aJCas);
            }
        }

        /* detect tokens */
        Matcher whitespaceMatcher = whitespace.matcher(text);
        while (whitespaceMatcher.find() && whitespaceMatcher.start() < text.length()) {
            Token token = new Token(aJCas, whitespaceMatcher.start(1), whitespaceMatcher.end(1));
            token.addToIndexes(aJCas);
        }

    }

}
