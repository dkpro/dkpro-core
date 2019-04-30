/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.frequency.phrasedetection;

import static junit.framework.TestCase.assertTrue;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.text.StringReader;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LexicalPhrase;

public class PhraseAnnotatorTest
{
    @Rule
    public DkproTestContext testContext = new DkproTestContext();

    @Test
    public void test()
            throws UIMAException, IOException
    {
        File countsFile = new File("src/test/resources/phrasedetection/counts.txt");

        String sentence = "This is a first test that contains a first test example";
        String language = "en";
        int expectedPhrases = 9;
        float threshold = (float) 5.0;

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, sentence,
                StringReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription phraseAnnotator = createEngineDescription(PhraseAnnotator.class,
                PhraseAnnotator.PARAM_MODEL_LOCATION, countsFile,
                PhraseAnnotator.PARAM_DISCOUNT, 0,
                PhraseAnnotator.PARAM_THRESHOLD, threshold);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, phraseAnnotator)) {
            Collection<LexicalPhrase> phrases = JCasUtil.select(jcas, LexicalPhrase.class);
            assertEquals(expectedPhrases, phrases.size());
            assertTrue(phrases.stream()
                    .map(LexicalPhrase::getText)
                    .allMatch(sentence::contains));
        }
    }

    // TODO: implement test for other covering type parameter values
}
