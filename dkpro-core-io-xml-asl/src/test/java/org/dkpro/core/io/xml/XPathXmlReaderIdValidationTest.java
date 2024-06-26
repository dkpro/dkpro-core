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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.CasDumpWriter;
import org.junit.jupiter.api.Test;

public class XPathXmlReaderIdValidationTest
{
    private static final String VALID_DOCS_ROOT = "src/test/resources/input/valid_docs";
    private static final String INVALID_DOCS_ROOT = "src/test/resources/input/invalid_docs";

    // Valid docs

    @Test
    public void idValidationTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]**/abbr*.xml" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top", XmlXPathReader.PARAM_LANGUAGE,
                "en", XmlXPathReader.PARAM_DOC_ID_TAG, "num");

        // Should find two files
        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/id_validation.txt");

        runPipeline(reader, writer);
    }

    @Test
    public void heteroFormatsIdValidationTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]full*.xml", "[+]abbr*.xml" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/topic | /topics/top",
                XmlXPathReader.PARAM_LANGUAGE, "en", XmlXPathReader.PARAM_DOC_ID_TAG,
                "identifier | num");

        // Should find two files
        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/hetero_formats_id_validation.txt");

        runPipeline(reader, writer);
    }

    @Test
    public void attributeIdTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]attribute_id.xml" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top",
                XmlXPathReader.PARAM_DOC_ID_TAG, "@num");

        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/attribute_id.txt");

        runPipeline(reader, writer);
    }

    @Test
    public void deepTagIdTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]deep_tag_id.xml" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top",
                XmlXPathReader.PARAM_DOC_ID_TAG, "EN-title/num");

        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/deep_tag_id.txt");

        runPipeline(reader, writer);
    }

    @Test
    public void deepAttributeIdTest() throws UIMAException, IOException
    {
        CollectionReader reader = createReader(XmlXPathReader.class,
                XmlXPathReader.PARAM_SOURCE_LOCATION, VALID_DOCS_ROOT,
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]deep_attribute_id.xml" },
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top",
                XmlXPathReader.PARAM_DOC_ID_TAG, "EN-title/@num");

        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/deep_attribute_id.txt");

        runPipeline(reader, writer);
    }

    // Invalid docs

    @Test
    public void invalidSubstitutionParameterTest() throws Exception
    {
        assertThatExceptionOfType(IllegalArgumentException.class)//
                .isThrownBy(() -> createReader(//
                        XmlXPathReader.class, //
                        XmlXPathReader.PARAM_SOURCE_LOCATION, INVALID_DOCS_ROOT, //
                        XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]*.*" }, //
                        XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top", //
                        // User should provide even number parameters
                        XmlXPathReader.PARAM_SUBSTITUTE_TAGS, new String[] { "EN-title" }, //
                        XmlXPathReader.PARAM_LANGUAGE, "en"));
    }

    @Test
    public void emptyIdTest() throws Exception
    {
        // Doc contains ID tag but no value is provided within the tag.
        // E.g. <num></num>
        CollectionReader reader = createReader( //
                XmlXPathReader.class, //
                XmlXPathReader.PARAM_SOURCE_LOCATION, INVALID_DOCS_ROOT, //
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]empty_id.xml" }, //
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top", //
                XmlXPathReader.PARAM_DOC_ID_TAG, "num", //
                XmlXPathReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription writer = createEngineDescription(//
                CasDumpWriter.class, //
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/empty_id.txt");

        assertThatExceptionOfType(IllegalStateException.class)//
                .isThrownBy(() -> runPipeline(reader, writer));
    }

    @Test
    public void noIdTagTest() throws UIMAException, IOException
    {
        // Doc doesn't contain ID tag at all
        CollectionReader reader = createReader(//
                XmlXPathReader.class, //
                XmlXPathReader.PARAM_SOURCE_LOCATION, INVALID_DOCS_ROOT, //
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]no_id_tag.xml" }, //
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top", //
                XmlXPathReader.PARAM_DOC_ID_TAG, "num", //
                XmlXPathReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription writer = createEngineDescription(//
                CasDumpWriter.class, //
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/no_id_tag.txt");

        assertThatExceptionOfType(IllegalStateException.class)//
                .isThrownBy(() -> runPipeline(reader, writer));
    }

    @Test
    public void nonUniqueIdTagTest() throws UIMAException, IOException
    {
        // A single doc contains ID tag twice
        // E.g. <top>
        // <num>01</num>
        // <num>01</num>
        // <title>.....
        // ...
        // </top>
        CollectionReader reader = createReader(//
                XmlXPathReader.class, //
                XmlXPathReader.PARAM_SOURCE_LOCATION, INVALID_DOCS_ROOT, //
                XmlXPathReader.PARAM_PATTERNS, new String[] { "[+]duplicated_id_tags.xml" }, //
                XmlXPathReader.PARAM_XPATH_EXPRESSION, "/topics/top", //
                XmlXPathReader.PARAM_DOC_ID_TAG, "num", //
                XmlXPathReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription writer = createEngineDescription(//
                CasDumpWriter.class, //
                CasDumpWriter.PARAM_OUTPUT_FILE, "target/output/duplicated_id_tags.txt");

        assertThatExceptionOfType(IllegalStateException.class)//
                .isThrownBy(() -> runPipeline(reader, writer));
    }
}
