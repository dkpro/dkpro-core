/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
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
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.frequency.Web1TFileAccessProvider;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.opennlp.OpenNlpLemmatizer;
import org.dkpro.core.opennlp.OpenNlpPosTagger;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class Web1TWriterTest
{
    private static final int MIN_NGRAM = 1;
    private static final int MAX_NGRAM = 3;

    @Test
    public void web1TFormatTestWithTwoMultiSlashedTypesAsFeaturePath(@TempDir File folder)
        throws Exception
    {
        var web1tProvider = prepareWeb1TFormatTest(folder, Token.class.getName() + "/pos/PosValue",
                Token.class.getName() + "/lemma/value");

        assertEquals(4, web1tProvider.getFrequency("ART"));
        assertEquals(4, web1tProvider.getFrequency("ADJA"));
        assertEquals(1, web1tProvider.getFrequency("APPR"));

        assertEquals(2, web1tProvider.getFrequency("testsatz"));
        assertEquals(-1, web1tProvider.getFrequency("sätze"));
    }

    @Test
    public void web1TFormatTestWithMultiSlashedTypesAsFeaturePath(@TempDir File folder)
        throws Exception
    {
        var web1tProvider = prepareWeb1TFormatTest(folder, Token.class.getName() + "/lemma/value");

        assertEquals(2, web1tProvider.getFrequency("testsatz"));
        assertEquals(-1, web1tProvider.getFrequency("sätze"));
        assertEquals(2, web1tProvider.getFrequency("satz"));
    }

    @Test
    public void web1TFormatTest_randomFrequencies(@TempDir File folder) throws Exception
    {
        var web1tProvider = prepareWeb1TFormatTest(folder, Token.class.getName());

        assertEquals(4, web1tProvider.getFrequency("."));
        assertEquals(1, web1tProvider.getFrequency("Satz"));
        assertEquals(2, web1tProvider.getFrequency("Testsatz"));
        assertEquals(1, web1tProvider.getFrequency("geschrieben"));
    }

    @Test
    public void web1TFormatTest_exceptionForInvalidMinFrequency1(@TempDir File folder)
        throws Exception
    {
        assertThatExceptionOfType(ResourceInitializationException.class)
                .isThrownBy(() -> writeWeb1TFormat(folder, -1, Token.class.getName()));
    }

    @Test
    public void web1TFormatTest_exceptionForInvalidMinFrequency2(@TempDir File folder)
        throws Exception
    {
        assertThatExceptionOfType(ResourceInitializationException.class)
                .isThrownBy(() -> writeWeb1TFormat(folder, 0, Token.class.getName()));
    }

    private void writeWeb1TFormat(File aFolder, int minFreq, String... strings)
        throws UIMAException, IOException
    {
        var reader = createReader(TextReader.class, //
                TextReader.PARAM_LANGUAGE, "de", //
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/data", //
                TextReader.PARAM_PATTERNS, "**/*.txt");

        var segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        var tagger = createEngineDescription(OpenNlpPosTagger.class);

        var lemmatizer = createEngineDescription(OpenNlpLemmatizer.class);

        var ngramWriter = createEngineDescription( //
                Web1TWriter.class, //
                Web1TWriter.PARAM_TARGET_LOCATION, aFolder, //
                Web1TWriter.PARAM_INPUT_TYPES, strings, //
                Web1TWriter.PARAM_MIN_NGRAM_LENGTH, MIN_NGRAM, //
                Web1TWriter.PARAM_MAX_NGRAM_LENGTH, MAX_NGRAM, //
                Web1TWriter.PARAM_MIN_FREQUENCY, minFreq);

        runPipeline(reader, segmenter, tagger, lemmatizer, ngramWriter);
    }

    private Web1TFileAccessProvider prepareWeb1TFormatTest(File target, String... inputTypes)
        throws Exception
    {
        writeWeb1TFormat(target, inputTypes);

        return new Web1TFileAccessProvider("de", target, MIN_NGRAM, MAX_NGRAM);
    }

    private void writeWeb1TFormat(File target, String... inputPath) throws Exception
    {
        var reader = createReader( //
                TextReader.class, //
                TextReader.PARAM_LANGUAGE, "de", //
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/data", //
                TextReader.PARAM_PATTERNS, "**/*.txt");

        var segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        var tagger = createEngineDescription(OpenNlpPosTagger.class);

        var lemmatizer = createEngineDescription(OpenNlpLemmatizer.class);

        var ngramWriter = createEngineDescription( //
                Web1TWriter.class, //
                Web1TWriter.PARAM_TARGET_LOCATION, target, //
                Web1TWriter.PARAM_INPUT_TYPES, inputPath, //
                Web1TWriter.PARAM_MIN_NGRAM_LENGTH, MIN_NGRAM, //
                Web1TWriter.PARAM_MAX_NGRAM_LENGTH, MAX_NGRAM);

        runPipeline(reader, segmenter, tagger, lemmatizer, ngramWriter);
    }
}
