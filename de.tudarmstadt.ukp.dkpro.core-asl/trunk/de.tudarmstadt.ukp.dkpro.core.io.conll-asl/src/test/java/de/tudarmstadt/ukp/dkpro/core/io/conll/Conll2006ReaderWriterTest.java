/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.junit.Test;

public class Conll2006ReaderWriterTest
{
    @Test
    public void test()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                Conll2006Reader.class, 
                Conll2006Reader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/2006/", 
                Conll2006Reader.PARAM_PATTERNS, "[+]fk003_2006_08_ZH1.conll10");

        AnalysisEngineDescription writer = createEngineDescription(
                Conll2006Writer.class,
                Conll2006Writer.PARAM_TARGET_LOCATION, "target/test-output", 
                Conll2006Writer.PARAM_STRIP_EXTENSION, true);

        AnalysisEngineDescription dumper = createEngineDescription(
                CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/test-output/dump.txt");

        runPipeline(reader, writer, dumper);

        String reference = FileUtils.readFileToString(new File(
                "src/test/resources/conll/2006/fk003_2006_08_ZH1.conll10"), "UTF-8");
        String actual = FileUtils.readFileToString(
                new File("target/test-output/fk003_2006_08_ZH1.conll"), "UTF-8");
        assertEquals(reference, actual);
    }
    
    @Test
    public void testFinnTreeBank()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                Conll2006Reader.class, 
                Conll2006Reader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/2006/", 
                Conll2006Reader.PARAM_PATTERNS, "[+]fi-orig.conll");

        AnalysisEngineDescription writer = createEngineDescription(
                Conll2006Writer.class,
                Conll2006Writer.PARAM_TARGET_LOCATION, "target/test-output", 
                Conll2006Writer.PARAM_STRIP_EXTENSION, true);

        AnalysisEngineDescription dumper = createEngineDescription(
                CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/test-output/dump.txt");

        runPipeline(reader, writer, dumper);

        String reference = FileUtils.readFileToString(new File(
                "src/test/resources/conll/2006/fi-ref.conll"), "UTF-8").trim();
        String actual = FileUtils.readFileToString(
                new File("target/test-output/fi-orig.conll"), "UTF-8").trim();
        assertEquals(reference, actual);
    }
}
