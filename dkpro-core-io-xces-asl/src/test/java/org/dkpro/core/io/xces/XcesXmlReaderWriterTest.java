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
package org.dkpro.core.io.xces;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.xmlunit.builder.Input.fromFile;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xmlunit.assertj3.XmlAssert;

public class XcesXmlReaderWriterTest
{
    private @TempDir File targetFolder;

    @Test
    public void testComplexReaderWriter() throws Exception
    {
        var reader = createReaderDescription(//
                XcesXmlReader.class, //
                XcesXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/", //
                XcesXmlReader.PARAM_PATTERNS, "[+]xces-complex.xml", //
                XcesXmlReader.PARAM_LANGUAGE, "el");

        var writer = createEngineDescription(//
                XcesXmlWriter.class, //
                XcesXmlWriter.PARAM_STRIP_EXTENSION, true, //
                XcesXmlWriter.PARAM_TARGET_LOCATION, targetFolder);

        runPipeline(reader, writer);

        XmlAssert.assertThat(fromFile(new File(targetFolder, "xces-complex.xml")))
                .and(fromFile("src/test/resources/xces-complex.xml")).areSimilar();
    }

    @Test
    public void testBasicReaderWriter() throws Exception
    {
        var reader = createReaderDescription( //
                XcesBasicXmlReader.class, //
                XcesBasicXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/", //
                XcesBasicXmlReader.PARAM_PATTERNS, "[+]xces-basic.xml", //
                XcesBasicXmlReader.PARAM_LANGUAGE, "el");

        var writer = createEngineDescription(//
                XcesBasicXmlWriter.class, //
                XcesBasicXmlWriter.PARAM_STRIP_EXTENSION, true, //
                XcesBasicXmlWriter.PARAM_TARGET_LOCATION, targetFolder);

        runPipeline(reader, writer);

        XmlAssert.assertThat(fromFile(new File(targetFolder, "xces-basic.xml")))
                .and(fromFile("src/test/resources/xces-basic.xml")).areSimilar();
    }
}
