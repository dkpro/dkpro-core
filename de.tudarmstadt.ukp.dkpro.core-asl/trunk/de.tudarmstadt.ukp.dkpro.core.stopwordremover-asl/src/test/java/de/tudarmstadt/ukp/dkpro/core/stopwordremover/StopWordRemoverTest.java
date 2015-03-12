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
package de.tudarmstadt.ukp.dkpro.core.stopwordremover;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

/**
 * Test cases for StopwordRemover.
 *
 * @author Carsten Schnober
 *
 */
public class StopWordRemoverTest
{
    private static final String STOPWORDSFILE_LOCATION1 = "src/test/resources/stopwords1.txt";
    private static final String STOPWORDSFILE_LOCATION2 = "src/test/resources/stopwords2.txt";
    private List<String> stopwords1;
    private List<String> stopwords2;

    @Before
    public void setUp()
        throws IOException
    {
        stopwords1 = FileUtils.readLines(new File(STOPWORDSFILE_LOCATION1));
        stopwords2 = FileUtils.readLines(new File(STOPWORDSFILE_LOCATION2));
    }

    /**
     * Simple test case: one stopword file.
     *
     * @throws ResourceInitializationException
     */
    @Test
    public void test()
        throws ResourceInitializationException
    {
        short expectedNTokens = 4;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/text.txt",
                TextReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
        AnalysisEngineDescription stopwordremover = createEngineDescription(StopWordRemover.class,
                StopWordRemover.PARAM_MODEL_LOCATION, STOPWORDSFILE_LOCATION1);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, stopwordremover)) {
            assertEquals(expectedNTokens, select(jcas, Token.class).size());
            for (Token token : select(jcas, Token.class)) {
                assertFalse(stopwords1.contains(token.getCoveredText()));
            }
        }
    }

    /**
     * Testing two stopword files with different language codes.
     *
     * @throws ResourceInitializationException
     */
    @Test
    public void test2Files()
        throws ResourceInitializationException
    {
        short expectedNTokens = 3;
        String[] stopwordFiles = new String[] {
                "[*]" + STOPWORDSFILE_LOCATION1,
                "[en]" + STOPWORDSFILE_LOCATION2 };

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/text.txt",
                TextReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
        AnalysisEngineDescription stopwordremover = createEngineDescription(StopWordRemover.class,
                StopWordRemover.PARAM_MODEL_LOCATION, stopwordFiles);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, stopwordremover)) {
            for (Token token : select(jcas, Token.class)) {
                assertFalse(stopwords1.contains(token.getCoveredText()));
                assertFalse(stopwords2.contains(token.getCoveredText()));
            }
            assertEquals(expectedNTokens, select(jcas, Token.class).size());
        }
    }

    /**
     * Testing two stopword files of identical language code.
     * <p>
     * This currently fails because StopwordRemover overwrites the contents of the first file with
     * those of the second file. See
     *
     * @throws ResourceInitializationException
     */
    @Test(expected = AssertionError.class)
    public void testFilesSameLanguage()
        throws ResourceInitializationException
    {
        short expectedNTokens = 6;
        String[] stopwordFiles = new String[] {
                "[*]" + STOPWORDSFILE_LOCATION1,
                "[*]" + STOPWORDSFILE_LOCATION2 };

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/text.txt",
                TextReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);
        AnalysisEngineDescription stopwordremover = createEngineDescription(StopWordRemover.class,
                StopWordRemover.PARAM_MODEL_LOCATION, stopwordFiles);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, stopwordremover)) {
            for (Token token : select(jcas, Token.class)) {
                assertFalse(stopwords1.contains(token.getCoveredText()));
                assertFalse(stopwords2.contains(token.getCoveredText()));
            }
            assertEquals(expectedNTokens, select(jcas, Token.class).size());
        }

    }
}
