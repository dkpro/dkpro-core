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
package org.dkpro.core.mecab;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.resources.PlatformDetector;
import org.dkpro.core.io.text.TextReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken;

/**
 * This test covers a case where mecab decides that a whitespace character is a own token. The
 * assumption for our wrapper is that a whitespace does not occur as own token. This case leads to
 * situations where the index breaks <code>null</code> initialized annotations are created.
 */
public class MeCabWhiteSpacesTest
{
    @BeforeEach
    public void prepare()
    {
        assumeFalse(System.getProperty("os.name").toLowerCase(Locale.US).contains("win"),
                "No Mecab binaries for Windows: Issue #1122");
        PlatformDetector pd = new PlatformDetector();
        assumeTrue(
                asList("linux-x86_64", "linux-x86_32", "osx-x86_64").contains(pd.getPlatformId()),
                "Unsupported platform");
    }

    @Test
    public void sequenceOfWhitespacesAtEndOfFile() throws UIMAException, IOException
    {
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources", TextReader.PARAM_LANGUAGE,
                "ja", TextReader.PARAM_PATTERNS, "[+]EoFWithSequenceOfWhitespacesAndBlanks.txt");
        AnalysisEngine jTagger = createEngine(MeCabTagger.class);
        try {
            JCas jcas = new JCasIterable(reader).iterator().next();
            jTagger.process(jcas);
            Collection<JapaneseToken> tokens = JCasUtil.select(jcas, JapaneseToken.class);
            assertEquals(2, tokens.size());
            // access all tokens
            Iterator<JapaneseToken> iterator = tokens.iterator();
            while (iterator.hasNext()) {
                System.out.println(iterator.next().getCoveredText());
            }
        }
        finally {
            jTagger.destroy();
        }
    }

    @Test
    public void whitespaceBeforeToken() throws UIMAException, IOException
    {
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources", TextReader.PARAM_LANGUAGE,
                "ja", TextReader.PARAM_PATTERNS,
                new String[] { "[+]TokenPreceedingWhitespace.txt" });

        AnalysisEngine jTagger = createEngine(MeCabTagger.class);
        try {
            JCas jcas = new JCasIterable(reader).iterator().next();
            jTagger.process(jcas);
            Collection<JapaneseToken> tokens = JCasUtil.select(jcas, JapaneseToken.class);
            List<String> stringTokens = new LinkedList<String>();
            Iterator<JapaneseToken> iterator = tokens.iterator();
            while (iterator.hasNext()) {
                JapaneseToken next = iterator.next();
                stringTokens.add(next.getCoveredText());
            }
            assertEquals(6, stringTokens.size());
        }
        finally {
            jTagger.destroy();
        }
    }

    @Test
    public void whitespaceIsAnnotatedAsToken() throws UIMAException, IOException
    {
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources", TextReader.PARAM_LANGUAGE,
                "ja", TextReader.PARAM_PATTERNS, new String[] { "[+]WhiteSpaceAsToken.txt" });

        AnalysisEngine jTagger = createEngine(MeCabTagger.class);
        try {
            JCas jcas = new JCasIterable(reader).iterator().next();
            jTagger.process(jcas);
            Collection<JapaneseToken> tokens = JCasUtil.select(jcas, JapaneseToken.class);
            List<String> stringTokens = new LinkedList<String>();
            Iterator<JapaneseToken> iterator = tokens.iterator();
            while (iterator.hasNext()) {
                JapaneseToken next = iterator.next();
                stringTokens.add(next.getCoveredText());
            }
            assertEquals(59, stringTokens.size());
        }
        finally {
            jTagger.destroy();
        }
    }

}
