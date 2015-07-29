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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;

public class WhitespaceTokenizerTest
{
    @Test
    public void test()
        throws ResourceInitializationException
    {
        String text = "This is a tokenized text .";
        int expectedSentences = 1;
        int expectedTokens = 6;
        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(WhitespaceTokenizer.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertEquals(expectedSentences, select(jcas, Sentence.class).size());
            assertEquals(expectedTokens, select(jcas, Token.class).size());
        }
    }

    @Test
    public void testTwoLines()
        throws ResourceInitializationException
    {
        String text = "This is a tokenized text .\nAnother line with tokens .";
        int expectedSentences = 2;
        int expectedTokens = 11;
        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(WhitespaceTokenizer.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertEquals(expectedSentences, select(jcas, Sentence.class).size());
            assertEquals(expectedTokens, select(jcas, Token.class).size());
        }
    }

    @Test
    public void testNoPunctuation()
        throws ResourceInitializationException
    {
        String text = "This is a tokenized text";
        int expectedSentences = 1;
        int expectedTokens = 5;
        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(WhitespaceTokenizer.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertEquals(expectedSentences, select(jcas, Sentence.class).size());
            assertEquals(expectedTokens, select(jcas, Token.class).size());
        }
    }

    @Test
    public void testTrailingWhitespace()
        throws ResourceInitializationException
    {
        String text = "This is a tokenized text ";
        int expectedSentences = 1;
        int expectedTokens = 5;
        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(WhitespaceTokenizer.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertEquals(expectedSentences, select(jcas, Sentence.class).size());
            assertEquals(expectedTokens, select(jcas, Token.class).size());
        }
    }

    @Test
    public void testPunctuation()
        throws ResourceInitializationException
    {
        String text = "This , is a tokenized text , with a final period .";
        int expectedSentences = 1;
        int expectedTokens = 12;
        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(WhitespaceTokenizer.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertEquals(expectedSentences, select(jcas, Sentence.class).size());
            assertEquals(expectedTokens, select(jcas, Token.class).size());
        }
    }

    @Test
    public void testTwoLinesWindowsLineBreaks()
        throws ResourceInitializationException
    {
        String text = "This is a tokenized text .\r\nAnother line with tokens .";
        int expectedSentences = 2;
        int expectedTokens = 11;
        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(WhitespaceTokenizer.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertEquals(expectedSentences, select(jcas, Sentence.class).size());
            assertEquals(expectedTokens, select(jcas, Token.class).size());
        }
    }
}
