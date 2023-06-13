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
package org.dkpro.core.io.xmi;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.FSUtil.getFeature;
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.uima.fit.factory.JCasFactory;
import org.junit.jupiter.api.Test;

public class XmiReaderWriterTest
{
    @Test
    public void testRoundtrip() throws Exception
    {
        testRoundTrip( //
                createReaderDescription(XmiReader.class), //
                createEngineDescription(XmiWriter.class),  //
                "xmi/english.xmi");
    }

    @Test
    public void testReadingFileWithDocumentMetaData() throws Exception
    {
        var reader = createReader( //
                XmiReader.class,  //
                XmiReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/xmi",  //
                XmiReader.PARAM_PATTERNS, "english.xmi");

        var doc = JCasFactory.createJCas();
        reader.getNext(doc.getCas());

        // System.out.println(doc.getDocumentAnnotationFs());

        var dmd = doc.getDocumentAnnotationFs();
        assertEquals((Integer) 0, getFeature(dmd, "begin", Integer.class));
        assertEquals((Integer) 230, getFeature(dmd, "end", Integer.class));
        assertEquals("en", getFeature(dmd, "language", String.class));
        assertEquals("english.txt", getFeature(dmd, "documentTitle", String.class));
        assertEquals("english.txt", getFeature(dmd, "documentId", String.class));
        assertEquals(null, getFeature(dmd, "documentUri", String.class));
        assertEquals(null, getFeature(dmd, "collectionId", String.class));
        assertEquals(null, getFeature(dmd, "documentBaseUri", String.class));
        assertEquals(false, getFeature(dmd, "isLastSegment", Boolean.class));
    }

    @Test
    public void testReadingFileWithoutDocumentMetaData() throws Exception
    {
        var reader = createReader( //
                XmiReader.class, //
                XmiReader.PARAM_SOURCE_LOCATION, "src/test/resources/xmi", //
                XmiReader.PARAM_PATTERNS, "english2.xmi");

        var doc = JCasFactory.createJCas();
        reader.getNext(doc.getCas());

        // System.out.println(doc.getDocumentAnnotationFs());

        var dmd = doc.getDocumentAnnotationFs();
        assertEquals((Integer) 0, getFeature(dmd, "begin", Integer.class));
        assertEquals((Integer) 230, getFeature(dmd, "end", Integer.class));
        assertEquals("x-unspecified", getFeature(dmd, "language", String.class));
        assertEquals("english2.xmi", getFeature(dmd, "documentTitle", String.class));
        assertEquals("english2.xmi", getFeature(dmd, "documentId", String.class));
        assertTrue(getFeature(dmd, "documentUri", String.class)
                .endsWith("/dkpro-core-io-xmi-asl/src/test/resources/xmi/english2.xmi"));
        assertTrue(getFeature(dmd, "collectionId", String.class)
                .endsWith("/dkpro-core-io-xmi-asl/src/test/resources/xmi/"));
        assertTrue(getFeature(dmd, "documentBaseUri", String.class)
                .endsWith("/dkpro-core-io-xmi-asl/src/test/resources/xmi/"));
        assertEquals(false, getFeature(dmd, "isLastSegment", Boolean.class));
    }

    @Test
    public void testReadingFileOverridingDocumentMetaData() throws Exception
    {
        var reader = createReader( //
                XmiReader.class, //
                XmiReader.PARAM_SOURCE_LOCATION, "src/test/resources/xmi", //
                XmiReader.PARAM_PATTERNS, "english.xmi", //
                XmiReader.PARAM_OVERRIDE_DOCUMENT_METADATA, true);

        var doc = JCasFactory.createJCas();
        reader.getNext(doc.getCas());

        // System.out.println(doc.getDocumentAnnotationFs());

        var dmd = doc.getDocumentAnnotationFs();
        assertEquals((Integer) 0, getFeature(dmd, "begin", Integer.class));
        assertEquals((Integer) 230, getFeature(dmd, "end", Integer.class));
        assertEquals("en", getFeature(dmd, "language", String.class));
        assertEquals("english.xmi", getFeature(dmd, "documentTitle", String.class));
        assertEquals("english.xmi", getFeature(dmd, "documentId", String.class));
        assertTrue(getFeature(dmd, "documentUri", String.class)
                .endsWith("/dkpro-core-io-xmi-asl/src/test/resources/xmi/english.xmi"));
        assertTrue(getFeature(dmd, "collectionId", String.class)
                .endsWith("/dkpro-core-io-xmi-asl/src/test/resources/xmi/"));
        assertTrue(getFeature(dmd, "documentBaseUri", String.class)
                .endsWith("/dkpro-core-io-xmi-asl/src/test/resources/xmi/"));
        assertEquals(false, getFeature(dmd, "isLastSegment", Boolean.class));
    }

    // @Test
    // public void generate()
    // throws Exception
    // {
    // SimplePipeline.runPipeline(
    // createReaderDescription(TextReader.class,
    // TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts/english.txt",
    // TextReader.PARAM_LANGUAGE, "en"),
    // createEngineDescription(StanfordSegmenter.class),
    // createEngineDescription(StanfordPosTagger.class),
    // createEngineDescription(StanfordLemmatizer.class),
    // createEngineDescription(StanfordParser.class,
    // StanfordParser.PARAM_WRITE_CONSTITUENT, false,
    // StanfordParser.PARAM_WRITE_DEPENDENCY, true),
    // createEngineDescription(StanfordNamedEntityRecognizer.class),
    // createEngineDescription(ClearNlpSemanticRoleLabeler.class),
    // createEngineDescription(TagsetDescriptionStripper.class),
    // createEngineDescription(XmiWriter.class,
    // XmiWriter.PARAM_STRIP_EXTENSION, true,
    // XmiWriter.PARAM_TARGET_LOCATION, "target/test-output/"+
    // testContext.getTestOutputFolderName()));
    // }
}
