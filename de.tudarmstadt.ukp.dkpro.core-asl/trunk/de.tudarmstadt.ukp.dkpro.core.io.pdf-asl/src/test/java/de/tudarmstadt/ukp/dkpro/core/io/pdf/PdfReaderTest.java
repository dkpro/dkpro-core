/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.io.pdf;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.xwriter.CasDumpWriter;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Richard Eckart de Castilho
 */
public class PdfReaderTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test()
        throws Exception
    {
        File outputFile = new File(folder.getRoot(), "dump-output.txt");

        CollectionReader reader = createReader(PdfReader.class, 
                PdfReader.PARAM_SOURCE_LOCATION, "src/test/resources/data", 
                PdfReader.PARAM_PATTERNS, "[+]**/*.pdf");

        AnalysisEngine writer = createEngine(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, outputFile);

        SimplePipeline.runPipeline(reader, writer);

        String reference = readFileToString(new File("src/test/resources/reference/test.dump"),
                "UTF-8").trim();
        String actual = readFileToString(outputFile, "UTF-8").trim();

        assertEquals(reference, actual);
    }
}
