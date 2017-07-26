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

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertSentence;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertToken;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Div;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;

public class RegexSegmenterTest
{
    @Test
    public void simpleExample()
        throws Exception
    {
        // NOTE: This file contains Asciidoc markers for partial inclusion of this file in the 
        // documentation. Do not remove these tags!
        // tag::example[]
        JCas jcas = JCasFactory.createText("This is sentence 1 .\nThis is number 2 .", "en");
        
        runPipeline(jcas,
                createEngineDescription(RegexSegmenter.class,
                        // Treat each line as a sentence
                        RegexSegmenter.PARAM_SENTENCE_BOUNDARY_REGEX, "\n",
                        // Use whitespace to detect tokens
                        RegexSegmenter.PARAM_TOKEN_BOUNDARY_REGEX, "\\s+"));
        
        for (Sentence s : select(jcas, Sentence.class)) {
            for (Token t : selectCovered(Token.class, s)) {
                System.out.printf("[%s] ", t.getCoveredText());
            }
            System.out.println();
        }
        // end::example[]
        
        assertToken(
                new String[] { "This", "is", "sentence", "1", ".", "This", "is", "number", "2", "." },
                select(jcas, Token.class));
        assertSentence(
                new String[] { 
                        "This is sentence 1 .",
                        "This is number 2 ." },
                select(jcas, Sentence.class));
    }
    
    @Test
    public void testWhitespace()
        throws ResourceInitializationException
    {
        String text = "This is a tokenized text .";
        String[] expectedSentences = new String[] { "This is a tokenized text ." };
        String[] expectedTokens = new String[] { "This", "is", "a", "tokenized", "text", "." };
        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(RegexSegmenter.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertSentence(expectedSentences, select(jcas, Sentence.class));
            assertToken(expectedTokens, select(jcas, Token.class));
        }
    }

    @Test
    public void testWhitespaceTwoLines()
        throws ResourceInitializationException
    {
        String text = "This is a tokenized text .\nAnother line with tokens .";
        String[] expectedTokens = new String[] { "This", "is", "a", "tokenized", "text", ".",
                "Another", "line", "with", "tokens", "." };
        String[] expectedSentences = new String[] { "This is a tokenized text .",
                "Another line with tokens ." };

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(RegexSegmenter.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertSentence(expectedSentences, select(jcas, Sentence.class));
            assertToken(expectedTokens, select(jcas, Token.class));
        }
    }

    @Test
    public void testWhitespaceNoPunctuation()
        throws ResourceInitializationException
    {
        String text = "This is a tokenized text";
        String[] expectedSentences = new String[] { "This is a tokenized text" };
        String[] expectedTokens = new String[] { "This", "is", "a", "tokenized", "text" };
        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(RegexSegmenter.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertSentence(expectedSentences, select(jcas, Sentence.class));
            assertToken(expectedTokens, select(jcas, Token.class));
        }
    }

    @Test
    public void testTrailingWhitespace()
        throws ResourceInitializationException
    {
        String text = "This is a tokenized text ";
        String[] expectedSentences = new String[] { "This is a tokenized text " };
        String[] expectedTokens = new String[] { "This", "is", "a", "tokenized", "text" };
        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(RegexSegmenter.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertSentence(expectedSentences, select(jcas, Sentence.class));
            assertToken(expectedTokens, select(jcas, Token.class));
        }
    }

    @Test
    public void testWhitespacePunctuation()
        throws ResourceInitializationException
    {
        String text = "This , is a tokenized text , with a final period .";
        String[] expectedSentences = new String[] {
                "This , is a tokenized text , with a final period ." };
        String[] expectedTokens = new String[] { "This", ",", "is", "a", "tokenized", "text", ",",
                "with", "a", "final", "period", "." };

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(RegexSegmenter.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertSentence(expectedSentences, select(jcas, Sentence.class));
            assertToken(expectedTokens, select(jcas, Token.class));
        }
    }

    @Test
    public void testRegex()
        throws ResourceInitializationException
    {
        String text = "This-is-a-text-.";
        String regex = "[-\n]";
        String[] expectedSentences = new String[] { "This-is-a-text-." };
        String[] expectedTokens = new String[] { "This", "is", "a", "text", "." };

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(RegexSegmenter.class,
                RegexSegmenter.PARAM_TOKEN_BOUNDARY_REGEX, regex);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            assertSentence(expectedSentences, select(jcas, Sentence.class));
            assertToken(expectedTokens, select(jcas, Token.class));
        }
    }

    @Test
    public void simpleExampleWithDivs()
            throws Exception
    {
        JCas jcas = JCasFactory.createText("This is sentence 1 .\nThis is number 2 .", "en");
        Div d = new Div(jcas, 0, 20);
        d.addToIndexes();
        d = new Div(jcas, 21, 39);
        d.addToIndexes();

        runPipeline(jcas,
                createEngineDescription(RegexSegmenter.class,
                        // Treat each line as a sentence
                        RegexSegmenter.PARAM_SENTENCE_BOUNDARY_REGEX, "\n",
                        // Use whitespace to detect tokens
                        RegexSegmenter.PARAM_TOKEN_BOUNDARY_REGEX, "\\s+"));

        for (Sentence s : select(jcas, Sentence.class)) {
            for (Token t : selectCovered(Token.class, s)) {
                System.out.printf("[%s] ", t.getCoveredText());
            }
            System.out.println();
        }

        assertToken(
                new String[] { "This", "is", "sentence", "1", ".", "This", "is", "number", "2", "." },
                select(jcas, Token.class));
        assertSentence(
                new String[] {
                        "This is sentence 1 .",
                        "This is number 2 ." },
                select(jcas, Sentence.class));
    }
}
