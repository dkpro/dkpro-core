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
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.resources.PlatformDetector;
import org.dkpro.core.io.text.TextReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class MeCabTaggerTest {
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
    public void testMeCabTaggerEngine() throws UIMAException, IOException {
        CollectionReaderDescription reader = createReaderDescription(
                TextReader.class, 
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources",
                TextReader.PARAM_LANGUAGE, "ja", 
                TextReader.PARAM_PATTERNS, new String[] { "[+]*.txt" });

        AnalysisEngineDescription JTagger = createEngineDescription(MeCabTagger.class);

        runPipeline(reader, JTagger);
    }

    @Test
    public void testMeCabTaggerFileInput() throws UIMAException, IOException {
        CollectionReaderDescription reader = createReaderDescription(
                TextReader.class, 
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources",
                TextReader.PARAM_LANGUAGE, "ja", 
                TextReader.PARAM_PATTERNS, new String[] { "[+]test*.txt" });

        AnalysisEngine jTagger = createEngine(MeCabTagger.class);
        try {
            Collection<Sentence> sentences = null;
            Iterator<JCas> iterator = new JCasIterable(reader).iterator();
            if (iterator.hasNext()) {
                JCas doc1 = iterator.next();
                jTagger.process(doc1);
                sentences = JCasUtil.select(doc1, Sentence.class);
                assertEquals(3, sentences.size());
            }

            if (iterator.hasNext()) {
                JCas doc2 = iterator.next();
                jTagger.process(doc2);
                sentences = JCasUtil.select(doc2, Sentence.class);
                assertEquals(1, sentences.size());
            }
            if (iterator.hasNext()) {
                JCas doc3 = iterator.next();
                jTagger.process(doc3);
                sentences = JCasUtil.select(doc3, Sentence.class);
                assertEquals(7, sentences.size());
            }
        } finally {
            jTagger.destroy();
        }
    }
}
