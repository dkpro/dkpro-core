/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.json;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.uima.fit.factory.JCasFactory;
import org.dkpro.core.io.conll.Conll2000Reader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class JsonCasReaderWriterTest
{
    @Test
    public void testWriteAndReadBack(@TempDir File tempDir) throws Exception
    {
        var reader = createReaderDescription(Conll2000Reader.class, "sourceLocation",
                "src/test/resources/conll/2000/chunk2000_test.conll");

        var writer = createEngineDescription(JsonCasWriter.class, "targetLocation", tempDir);

        runPipeline(reader, writer);

        var jsonFiles = tempDir.listFiles((dir, name) -> name.endsWith(".json"));
        assertThat(jsonFiles).isNotNull().isNotEmpty();

        var jsonReader = createReader(JsonCasReader.class, "sourceLocation",
                tempDir.getAbsolutePath(), "patterns", "*.json");

        var jcas = JCasFactory.createJCas();
        jsonReader.getNext(jcas.getCas());

        assertThat(jcas.getDocumentText()).isNotNull().isNotEmpty();
        assertThat(jcas.getCas().getAnnotationIndex()).isNotEmpty();
    }
}
