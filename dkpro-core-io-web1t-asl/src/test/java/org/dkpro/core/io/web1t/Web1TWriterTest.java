/*
 * Copyright 2011
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
package org.dkpro.core.io.web1t;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.frequency.Web1TFileAccessProvider;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.opennlp.OpenNlpPosTagger;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;

public class Web1TWriterTest
{
    private final int MIN_NGRAM = 1;
    private final int MAX_NGRAM = 3;

    @Test
    public void web1TFormatTestWithTwoMultiSlashedTypesAsFeaturePath()
        throws Exception
    {
        File folder = testContext.getTestOutputFolder();
        
        Web1TFileAccessProvider web1tProvider = prepareWeb1TFormatTest(folder, new String[] {
                Token.class.getName() + "/pos/PosValue", Token.class.getName() + "/lemma/value" });

        assertEquals(1, web1tProvider.getFrequency("TO")); // "to"
        assertEquals(1, web1tProvider.getFrequency("NNS")); // "sentences"
        assertEquals(1, web1tProvider.getFrequency("EX")); // "there"

        assertEquals(1, web1tProvider.getFrequency("write"));
        assertEquals(0, web1tProvider.getFrequency("written"));

    }

    @Test
    public void web1TFormatTestWithMultiSlashedTypesAsFeaturePath()
        throws Exception
    {
        File folder = testContext.getTestOutputFolder();
        
        Web1TFileAccessProvider web1tProvider = prepareWeb1TFormatTest(folder,
                new String[] { Token.class.getName() + "/lemma/value" });

        assertEquals(1, web1tProvider.getFrequency("write"));
        assertEquals(0, web1tProvider.getFrequency("written"));
        assertEquals(4, web1tProvider.getFrequency("sentence"));

    }

    @Test
    public void web1TFormatTest_randomFrequencies()
        throws Exception
    {
        File folder = testContext.getTestOutputFolder();
        
        Web1TFileAccessProvider web1tProvider = prepareWeb1TFormatTest(folder,
                new String[] { Token.class.getName() });

        assertEquals(4, web1tProvider.getFrequency("."));
        assertEquals(1, web1tProvider.getFrequency(","));
        assertEquals(3, web1tProvider.getFrequency("sentence"));
        assertEquals(1, web1tProvider.getFrequency("written"));

    }

    @Test(expected = ResourceInitializationException.class)
    public void web1TFormatTest_exceptionForInvalidMinFrequency1()
        throws Exception
    {
        writeWeb1TFormat(new String[] { Token.class.getName() }, -1);

    }

    @Test(expected = ResourceInitializationException.class)
    public void web1TFormatTest_exceptionForInvalidMinFrequency2()
        throws Exception
    {
        writeWeb1TFormat(new String[] { Token.class.getName() }, 0);

    }

    private void writeWeb1TFormat(String[] strings, int minFreq)
        throws UIMAException, IOException
    {
        CollectionReader reader = createReader(TextReader.class,
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TextReader.PARAM_PATTERNS, new String[] { "[+]**/*.txt" });

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription tagger = createEngineDescription(OpenNlpPosTagger.class);

        AnalysisEngineDescription lemmatizer = createEngineDescription(ClearNlpLemmatizer.class);

        AnalysisEngineDescription ngramWriter = createEngineDescription(Web1TWriter.class,
                Web1TWriter.PARAM_TARGET_LOCATION, testContext.getTestOutputFolder(),
                Web1TWriter.PARAM_INPUT_TYPES, strings, 
                Web1TWriter.PARAM_MIN_NGRAM_LENGTH, MIN_NGRAM, 
                Web1TWriter.PARAM_MAX_NGRAM_LENGTH, MAX_NGRAM,
                Web1TWriter.PARAM_MIN_FREQUENCY, minFreq);

        SimplePipeline.runPipeline(reader, segmenter, tagger, lemmatizer, ngramWriter);
    }

    private Web1TFileAccessProvider prepareWeb1TFormatTest(File target, String[] inputTypes)
        throws Exception
    {
        writeWeb1TFormat(target, inputTypes);

        Web1TFileAccessProvider web1tProvider = new Web1TFileAccessProvider("en", target,
                MIN_NGRAM, MAX_NGRAM);

        return web1tProvider;
    }

    private void writeWeb1TFormat(File target, String[] inputPath)
        throws Exception
    {
        CollectionReader reader = createReader(TextReader.class,
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TextReader.PARAM_PATTERNS, new String[] { "[+]**/*.txt" });

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription tagger = createEngineDescription(OpenNlpPosTagger.class);

        AnalysisEngineDescription lemmatizer = createEngineDescription(ClearNlpLemmatizer.class);

        AnalysisEngineDescription ngramWriter = createEngineDescription(Web1TWriter.class,
                Web1TWriter.PARAM_TARGET_LOCATION, target,
                Web1TWriter.PARAM_INPUT_TYPES, inputPath, 
                Web1TWriter.PARAM_MIN_NGRAM_LENGTH, MIN_NGRAM, 
                Web1TWriter.PARAM_MAX_NGRAM_LENGTH, MAX_NGRAM);

        SimplePipeline.runPipeline(reader, segmenter, tagger, lemmatizer, ngramWriter);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
