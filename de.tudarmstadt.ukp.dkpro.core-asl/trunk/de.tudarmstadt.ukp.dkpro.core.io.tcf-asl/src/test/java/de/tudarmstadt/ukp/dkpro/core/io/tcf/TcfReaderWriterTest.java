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
package de.tudarmstadt.ukp.dkpro.core.io.tcf;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.xwriter.CasDumpWriter;
import org.junit.Test;

import eu.clarin.weblicht.wlfxb.io.WLDObjector;
import eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusStored;
import eu.clarin.weblicht.wlfxb.xb.WLData;

public class TcfReaderWriterTest
{
    @Test
    // @Ignore("The TCF library generates different xml namespaces and assertEquals fails on Jenkins ")
    public void test()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(TcfReader.class, 
                TcfReader.PARAM_PATH, new File("src/test/resources/").getAbsolutePath(), 
                TcfReader.PARAM_PATTERNS, new String[] { "[+]tcf-after.xml" });

        AnalysisEngineDescription writer = createEngineDescription(
                TcfWriter.class,
                TcfWriter.PARAM_TARGET_LOCATION, "target/test-output", 
                TcfWriter.PARAM_STRIP_EXTENSION, true);

        AnalysisEngineDescription dumper = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/test-output/dump.txt");

        runPipeline(reader, writer, dumper);

        InputStream isReference = new FileInputStream(new File(
                "src/test/resources/tcf-after-expected.xml"));

        InputStream isActual = new FileInputStream(new File("target/test-output/tcf-after.tcf"));

        WLData wLDataReference = WLDObjector.read(isReference);
        TextCorpusStored aCorpusDataReference = wLDataReference.getTextCorpus();

        WLData wLDataActual = WLDObjector.read(isActual);
        TextCorpusStored aCorpusDataActual = wLDataActual.getTextCorpus();

        // check if layers maintained
        assertEquals(aCorpusDataReference.getLayers().size(), aCorpusDataActual.getLayers().size());

        // Check if every layers have the same number of annotations
        for (int i = 0; i < aCorpusDataReference.getLayers().size(); i++) {
            assertEquals(aCorpusDataReference.getLayers().get(i).size(), aCorpusDataActual
                    .getLayers().get(i).size());
        }

        //
        // String reference = FileUtils.readFileToString(new File(
        // "src/test/resources/tcf-after-expected.xml"), "UTF-8");
        // String actual = FileUtils.readFileToString(
        // new File("target/test-output/tcf-after.tcf"), "UTF-8");
        // assertEquals(reference, actual);
    }
}
