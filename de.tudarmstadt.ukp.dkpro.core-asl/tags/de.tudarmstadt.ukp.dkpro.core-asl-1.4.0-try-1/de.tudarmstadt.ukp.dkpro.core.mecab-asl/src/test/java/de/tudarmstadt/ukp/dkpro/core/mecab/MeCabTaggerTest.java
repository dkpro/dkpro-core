/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.mecab;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static org.uimafit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class MeCabTaggerTest {
    @Test
    public void testMeCabTaggerEngine() throws UIMAException, IOException {
        CollectionReader cr = createCollectionReader(TextReader.class, TextReader.PARAM_PATH, "src/test/resources",
                TextReader.PARAM_LANGUAGE, "ja", TextReader.PARAM_PATTERNS, new String[] { "[+]*.txt" });

        AnalysisEngineDescription JTagger = createPrimitiveDescription(MeCabTagger.class);

        runPipeline(cr, JTagger);
    }

    @Test
    public void testMeCabTaggerFileInput() throws UIMAException, IOException {

        CollectionReader cr = createCollectionReader(TextReader.class, TextReader.PARAM_PATH, "src/test/resources",
                TextReader.PARAM_LANGUAGE, "ja", TextReader.PARAM_PATTERNS, new String[] { "[+]test*.txt" });

        AnalysisEngine jTagger = createPrimitive(MeCabTagger.class);
        try {
            Collection<Sentence> sentences = null;
            Iterator<JCas> iterator = new JCasIterable(cr).iterator();
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
