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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.util.JCasHolder;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class RegexTokenFilterTest
{

    private static final String FILTER_REGEX = "^[A-ZÖÜÄ].{2,}";

    @Test
    public void testNoMatch()
        throws Exception
    {
        String inputText = "Ich lebe in Braunschweig.";
        String filteredText = "Ich Braunschweig";
        boolean mustMatch = true;

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, inputText, StringReader.PARAM_LANGUAGE, "de");

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription filter = createEngineDescription(RegexTokenFilter.class,
                RegexTokenFilter.PARAM_REGEX, FILTER_REGEX,
                RegexTokenFilter.PARAM_MUST_MATCH, mustMatch);

        AnalysisEngineDescription holder = createEngineDescription(JCasHolder.class);

        StringBuilder outputText = new StringBuilder();
        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, filter, holder)) {
            for (Token token : select(jcas, Token.class)) {
                outputText.append(token.getCoveredText() + " ");
            }
            assertEquals(filteredText, outputText.toString().trim());
        }
    }

    @Test
    public void testMatch()
        throws Exception
    {
        String inputText = "Ich lebe in Braunschweig.";
        String filteredText = "lebe in .";
        boolean mustMatch = false;

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, inputText, StringReader.PARAM_LANGUAGE, "de");

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription filter = createEngineDescription(RegexTokenFilter.class,
                RegexTokenFilter.PARAM_REGEX, FILTER_REGEX,
                RegexTokenFilter.PARAM_MUST_MATCH, mustMatch);

        AnalysisEngineDescription holder = createEngineDescription(JCasHolder.class);

        StringBuilder outputText = new StringBuilder();
        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, filter, holder)) {
            for (Token token : select(jcas, Token.class)) {
                outputText.append(token.getCoveredText() + " ");
            }
            assertEquals(filteredText, outputText.toString().trim());
        }
    }
}
