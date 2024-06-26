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
package org.dkpro.core.io.xml;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.CasDumpWriter;
import org.junit.jupiter.api.Test;

public class XPathXmlReaderFeatureTest
{
    private static final String VALID_DOCS_ROOT = "src/test/resources/input/valid_docs";

    @Test
    public void abbreviatedFormatTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(
                XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]abbr*.xml" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top",
                XmlXPathReader.PARAM_LANGUAGE, "en"
        );

        // Should find one file
        AnalysisEngineDescription writer = createEngineDescription(
                CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/abbr_format_reading.txt"
        );

        runPipeline(reader, writer);
    }


    @Test
    public void fullFormatTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(
                XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]full*.xml" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/topic",
                XmlXPathReader.PARAM_LANGUAGE, "en"
        );

        // Should find one file
        AnalysisEngineDescription writer = createEngineDescription(
                CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/full_format_reading.txt"
        );

        runPipeline(reader, writer);
    }


    @Test
    public void heteroFormatsTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(
                XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]full*.xml", "[+]abbr*.xml" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/topic | /topics/top",
                XmlXPathReader.PARAM_LANGUAGE, "en"
        );

        // Should find one file
        AnalysisEngineDescription writer = createEngineDescription(
                CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/hetero_formats_reading.txt"
        );

        runPipeline(reader, writer);
    }


    @Test
    public void recursiveReadingTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(
                XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]**/abbr*.xml" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top",
                XmlXPathReader.PARAM_LANGUAGE, "en"
        );

        // Should find two files
        AnalysisEngineDescription writer = createEngineDescription(
                CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/recursive_reading.txt"
        );

        runPipeline(reader, writer);
    }


    @Test
    public void tagFilteringTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(
                XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]abbr*.*" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top",
                // read only num and EN-title tags
                XmlXPathReader.PARAM_INCLUDE_TAGS, new String[] { "EN-title", "num" }, 
                XmlXPathReader.PARAM_LANGUAGE, "en"
        );

        AnalysisEngineDescription writer = createEngineDescription(
                CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/tag_filtering.txt"
        );

        runPipeline(reader, writer);
    }


    @Test
    public void substitutionTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(
                XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]abbr*.*" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top",
                // Subtitute "EN-title" tag with "title" and "EN-narr" with "narration"
                XmlXPathReader.PARAM_SUBSTITUTE_TAGS, new String[] { 
                        "EN-title", "title", "EN-narr", "narration" },
                XmlXPathReader.PARAM_LANGUAGE, "en"
        );

        AnalysisEngineDescription writer = createEngineDescription(
                CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/substitution.txt"
        );

        runPipeline(reader, writer);
    }


}
