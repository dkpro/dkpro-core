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

package org.dkpro.core.textnormalizer.transformation;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.transform.JCasTransformer_ImplBase;
import org.dkpro.core.io.text.StringReader;
import org.dkpro.core.textnormalizer.util.JCasHolder;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class JCasTransformer_ImplBaseTest
{
    @Test
    public void testCopyNoAnnotations()
        throws UIMAException, IOException
    {
        String inputText = "test";
        int exptectedTokens = 0;
        int expectedDocumentMetadata = 1;
        int expectedSentences = 0;

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, inputText,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription annotator = createEngineDescription(TestAnnotator.class);
        AnalysisEngineDescription transformer = createEngineDescription(TestTransformer.class);
        AnalysisEngineDescription holder = createEngineDescription(JCasHolder.class);

        SimplePipeline.runPipeline(reader, annotator, transformer, holder);
        JCas jcas = JCasHolder.get();
        assertEquals(exptectedTokens, select(jcas, Token.class).size());
        assertEquals(expectedSentences, select(jcas, Sentence.class).size());
        assertEquals(expectedDocumentMetadata, select(jcas, DocumentMetaData.class).size());
    }

    @Test
    public void testAllTypesToCopy()
        throws UIMAException, IOException
    {
        String inputText = "test";
        int expectedTokens = 2;
        int expectedDocumentMetadata = 1;
        int expectedSentences = 1;
        String[] typesToCopy = new String[] { Token.class.getName(), Sentence.class.getName() };

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, inputText,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription annotator = createEngineDescription(TestAnnotator.class);
        AnalysisEngineDescription transformer = createEngineDescription(TestTransformer.class,
                TestTransformer.PARAM_TYPES_TO_COPY, typesToCopy);
        AnalysisEngineDescription holder = createEngineDescription(JCasHolder.class);

        SimplePipeline.runPipeline(reader, annotator, transformer, holder);
        JCas jcas = JCasHolder.get();
        assertEquals(expectedTokens, select(jcas, Token.class).size());
        assertEquals(expectedSentences, select(jcas, Sentence.class).size());
        assertEquals(expectedDocumentMetadata, select(jcas, DocumentMetaData.class).size());
    }

    @Test
    public void testOneTypeToCopy()
        throws UIMAException, IOException
    {
        String inputText = "test";
        int expectedTokens = 2;
        int expectedDocumentMetadata = 1;
        int expectedSentences = 0;
        String[] typesToCopy = new String[] { Token.class.getName() };

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, inputText,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription annotator = createEngineDescription(TestAnnotator.class);
        AnalysisEngineDescription transformer = createEngineDescription(TestTransformer.class,
                TestTransformer.PARAM_TYPES_TO_COPY, typesToCopy);
        AnalysisEngineDescription holder = createEngineDescription(JCasHolder.class);

        SimplePipeline.runPipeline(reader, annotator, transformer, holder);
        JCas jcas = JCasHolder.get();
        assertEquals(expectedTokens, select(jcas, Token.class).size());
        assertEquals(expectedSentences, select(jcas, Sentence.class).size());
        assertEquals(expectedDocumentMetadata, select(jcas, DocumentMetaData.class).size());
    }

    public static class TestAnnotator
        extends JCasAnnotator_ImplBase
    {
        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            Token token1 = new Token(aJCas);
            token1.setBegin(0);
            token1.setEnd(1);
            token1.addToIndexes(aJCas);

            Token token2 = new Token(aJCas);
            token2.setBegin(2);
            token2.setEnd(3);
            token2.addToIndexes(aJCas);

            Sentence sentence = new Sentence(aJCas);
            sentence.setBegin(0);
            sentence.setEnd(3);
            sentence.addToIndexes(aJCas);
        }
    }

    public static class TestTransformer
        extends JCasTransformer_ImplBase
    {
        @Override
        public void process(JCas aInput, JCas aOutput)
            throws AnalysisEngineProcessException
        {
            // Just copy the text. DocumentMetaData has already been copied and the TYPES_TO_COPY
            // will be copied when this method returns.
            aOutput.setDocumentText(aInput.getDocumentText());
        }
    }
}
