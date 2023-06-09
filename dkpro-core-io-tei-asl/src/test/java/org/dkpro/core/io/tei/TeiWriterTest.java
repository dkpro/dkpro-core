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
package org.dkpro.core.io.tei;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.opennlp.OpenNlpNamedEntityRecognizer;
import org.dkpro.core.opennlp.OpenNlpParser;
import org.dkpro.core.opennlp.OpenNlpPosTagger;
import org.dkpro.core.opennlp.OpenNlpSegmenter;
import org.dkpro.core.testing.dumper.CasDumpWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TeiWriterTest
{
    @Test
    public void test(@TempDir File targetFolder)
        throws Exception
    {
        CollectionReaderDescription textReader = createReaderDescription(
                TextReader.class,
                TextReader.PARAM_LANGUAGE, "en",
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts",
                TextReader.PARAM_PATTERNS, "*.txt");

        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);

        AnalysisEngineDescription posTagger = createEngineDescription(OpenNlpPosTagger.class);

        AnalysisEngineDescription parser = createEngineDescription(OpenNlpParser.class);

        AnalysisEngineDescription ner = createEngineDescription(OpenNlpNamedEntityRecognizer.class);

        AnalysisEngineDescription dump = createEngineDescription(CasDumpWriter.class);

        AnalysisEngineDescription teiWriter = createEngineDescription(
                TeiWriter.class,
                TeiWriter.PARAM_TARGET_LOCATION, targetFolder,
                TeiWriter.PARAM_WRITE_CONSTITUENT, true);

        runPipeline(textReader, segmenter, posTagger, parser, ner, dump, teiWriter);

        File output = new File(targetFolder, "example1.txt.xml");
        assertTrue(output.exists());

//        Diff myDiff = new Diff(
//                new InputSource("src/test/resources/reference/example1.txt.xml"),
//                new InputSource(output.getPath()));
//        myDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
//        XMLAssert.assertXMLEqual(myDiff, true);     
    }
}
