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
package org.dkpro.core.io.annis;

import static org.apache.commons.io.FileUtils.contentEqualsIgnoreEOL;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.io.negra.NegraExportReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RelAnnisWriterTest
{
    @Test
    public void tuebaTest(@TempDir File workspace)
        throws Exception
    {
        // create NegraExportReader output
        CollectionReaderDescription reader = createReaderDescription(NegraExportReader.class,
                NegraExportReader.PARAM_SOURCE_LOCATION, "src/test/resources/tueba/input/tueba-sample.export",
                NegraExportReader.PARAM_LANGUAGE, "de",
//                NegraExportReader.PARAM_READ_PENN_TREE, false,
                NegraExportReader.PARAM_SOURCE_ENCODING, "UTF-8");

        AnalysisEngineDescription writer = createEngineDescription(RelAnnisWriter.class,
                RelAnnisWriter.PARAM_PATH, workspace.getPath());

        SimplePipeline.runPipeline(reader, writer);

        // Check if the output matches the reference output
        for (File f : workspace.listFiles()) {
            System.out.print("Checking [" + f.getName() + "]... ");
            if (readFileToString(new File("src/test/resources/tueba/reference", f.getName()),
                    "UTF-8").equals(readFileToString(f, "UTF-8"))) {
                System.out.println("ok.");
            }
            else {
                System.out.println("FAIL.");
            }
        }

        // Check if the output matches the reference output
        for (File f : workspace.listFiles()) {
            assertTrue(contentEqualsIgnoreEOL(
                    new File("src/test/resources/tueba/reference", f.getName()), f, "UTF-8"));
        }
    }
}
