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

package de.tudarmstadt.ukp.dkpro.core.io.rtf;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.EOLUtils;

/**
 * Test cases for {@link RTFReader}.
 *
 *
 */
public class RTFReaderTest
{
    /**
     * Simple test for reader / CASDumpWriter output.
     *
     * @throws UIMAException
     * @throws IOException
     */
    @Test
    public void test()
        throws UIMAException, IOException
    {
        File testFile = new File("src/test/resources/testfile.rtf");
        File output = new File("target/output.dump");
        output.deleteOnExit();
        File testDump = new File("src/test/resources/testfile.dump");

        CollectionReaderDescription reader = createReaderDescription(RTFReader.class,
                RTFReader.PARAM_SOURCE_LOCATION, testFile,
                RTFReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, output);

        SimplePipeline.runPipeline(reader, writer);
        String reference = FileUtils.readFileToString(testDump, "UTF-8").trim();
        String actual = FileUtils.readFileToString(output, "UTF-8").trim();
        reference = EOLUtils.normalizeLineEndings(reference);
        actual = EOLUtils.normalizeLineEndings(actual);
        assertEquals(reference, actual);
    }

    /**
     * Test for reading multiple (two) files.
     *
     * @throws UIMAException
     * @throws IOException
     */
    @Test
    public void testTwoFiles()
        throws UIMAException, IOException
    {
        String testFiles = "src/test/resources/*.rtf";
        File output = new File("target/output2.dump");
        output.deleteOnExit();
        File testDump = new File("src/test/resources/testfiles.dump");

        CollectionReaderDescription reader = createReaderDescription(RTFReader.class,
                RTFReader.PARAM_SOURCE_LOCATION, testFiles,
                RTFReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, output);

        SimplePipeline.runPipeline(reader, writer);
        
        String reference = FileUtils.readFileToString(testDump, "UTF-8").trim();
        String actual = FileUtils.readFileToString(output, "UTF-8").trim();
        reference = EOLUtils.normalizeLineEndings(reference);
        actual = EOLUtils.normalizeLineEndings(actual);
        assertEquals(reference, actual);
    }
}
