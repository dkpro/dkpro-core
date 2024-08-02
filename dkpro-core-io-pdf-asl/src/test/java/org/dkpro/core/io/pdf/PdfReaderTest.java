/*
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
 */
package org.dkpro.core.io.pdf;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.assertj.core.api.Assertions.contentOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.apache.uima.cas.impl.FeatureStructureImplC;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.testing.EOLUtils;
import org.dkpro.core.testing.dumper.CasDumpWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PdfReaderTest
{
    @BeforeAll
    static void setupClass() {
        // V2 FS toString needed for CasDumpWriter. Also see comment in the root-level pom.xml
        // file where this property is globally set for all surefire runs
        System.setProperty(FeatureStructureImplC.V2_PRETTY_PRINT, "true");
    }
    
    @Test
    public void test(@TempDir File tempDir) throws Exception
    {
        var outputFile = new File(tempDir, "dump-output.txt");

        var reader = createReader(PdfReader.class, //
                PdfReader.PARAM_SOURCE_LOCATION, "src/test/resources/data", //
                PdfReader.PARAM_PATTERNS, "[+]**/*.pdf");

        var writer = createEngine(CasDumpWriter.class, //
                CasDumpWriter.PARAM_TARGET_LOCATION, outputFile);

        SimplePipeline.runPipeline(reader, writer);

        var reference = contentOf(new File("src/test/resources/reference/test.dump"), UTF_8).trim();
        var actual = contentOf(outputFile, UTF_8).trim();

        actual = EOLUtils.normalizeLineEndings(actual);
        reference = EOLUtils.normalizeLineEndings(reference);

        assertEquals(reference, actual);
    }
}
