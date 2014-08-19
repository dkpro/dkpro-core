/*******************************************************************************
 * Copyright 2013
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.bincas;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class BinaryCasWriterReaderTest
{
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private static final int NONE = 1;
    private static final int METADATA = 2;
    private static final int ALL = 3;
    
    @Test
    public void testSReinitialize()
        throws Exception
    {
        write(testFolder.getRoot().getPath(), "S", true);
        read(testFolder.getRoot().getPath(), NONE, true); // Type system is reinitialized from the persisted type system
    }

    @Test
    public void testSReinitializeInZIP()
        throws Exception
    {
        write("jar:file:" + testFolder.getRoot().getPath() + "/archive.zip", "S", true);
        read("jar:file:" + testFolder.getRoot().getPath() + "/archive.zip", NONE, true); // Type system is reinitialized from the persisted type system
    }

    @Test
    public void testSPreinitialized()
        throws Exception
    {
        write(testFolder.getRoot().getPath(), "S", false);
        read(testFolder.getRoot().getPath(), ALL, false);
    }

    @Test
    public void testSplusReinitialize()
        throws Exception
    {
        write(testFolder.getRoot().getPath(), "S+", false);
        read(testFolder.getRoot().getPath(), NONE, false); // Type system is reinitialized from the persisted CAS
    }

    @Test
    public void test0Preinitialized()
        throws Exception
    {
        write(testFolder.getRoot().getPath(), "0", false);
        read(testFolder.getRoot().getPath(), ALL, false);
    }

    @Test
    public void test4Preinitialized()
        throws Exception
    {
        write(testFolder.getRoot().getPath(), "4", false);
        read(testFolder.getRoot().getPath(), ALL, false);
    }
    
    /**
     * The type system in the CAS is different from the one in the file. To do lenient loading with
     * format 6, we need to know the type system that was used to originally store the CAS.
     */
    @Test
    public void test6Lenient()
        throws Exception
    {
        write(testFolder.getRoot().getPath(), "6", true);
        read(testFolder.getRoot().getPath(), METADATA, true);
    }

    @Test
    public void test6Preinitialized()
        throws Exception
    {
        write(testFolder.getRoot().getPath(), "6", false);
        read(testFolder.getRoot().getPath(), ALL, false);
    }

    @Test
    public void test6plusPreinitialized()
        throws Exception
    {
        write(testFolder.getRoot().getPath(), "6+", false);
        read(testFolder.getRoot().getPath(), ALL, false);
    }

    @Test
    public void test6plusLenient()
        throws Exception
    {
        write(testFolder.getRoot().getPath(), "6+", false);
        read(testFolder.getRoot().getPath(), METADATA, false);
    }

    @Test
    public void testSerializedEmbeddedTypeSystem()
        throws Exception
    {
        writeSerialized(testFolder.getRoot().getPath(), false);
        read(testFolder.getRoot().getPath(), NONE, false); // Type system is reinitialized from the persisted CAS
    }

    @Test
    public void testSerializedSeparateTypeSystem()
        throws Exception
    {
        writeSerialized(testFolder.getRoot().getPath(), true);
        read(testFolder.getRoot().getPath(), NONE, true); // Type system is reinitialized from the persisted CAS
    }

    @Test
    public void readWriteZipMinimal()
            throws Exception
    {
        String targetZip = "jar:file:target/archive.zip";
        
        JCas out = JCasFactory.createJCas();
        out.setDocumentLanguage("en");
        out.setDocumentText("This is a test.");
        DocumentMetaData meta = DocumentMetaData.create(out);
        meta.setDocumentId("document");
        
        AnalysisEngine writer = createEngine(
                BinaryCasWriter.class, 
                BinaryCasWriter.PARAM_FORMAT, "6", 
                BinaryCasWriter.PARAM_TARGET_LOCATION, targetZip,
                BinaryCasWriter.PARAM_TYPE_SYSTEM_LOCATION, "typesystem.bin");
        
        writer.process(out);
        writer.collectionProcessComplete();
        
        CollectionReader reader = CollectionReaderFactory.createReader(
                BinaryCasReader.class,
                BinaryCasReader.PARAM_SOURCE_LOCATION, targetZip,
                BinaryCasReader.PARAM_PATTERNS, "*.bin",
                BinaryCasReader.PARAM_TYPE_SYSTEM_LOCATION, "typesystem.bin");
        
        JCas in = JCasFactory.createJCas();
        reader.getNext(in.getCas());
        
        assertEquals(out.getDocumentLanguage(), in.getDocumentLanguage());
        assertEquals(out.getDocumentText(), in.getDocumentText());
        assertEquals(DocumentMetaData.get(out).getDocumentId(), DocumentMetaData.get(in).getDocumentId());
    }
    
    public void write(String aLocation, String aFormat, boolean aWriteTypeSystem)
        throws Exception
    {
        System.out.println("--- WRITING ---");
        CollectionReader textReader = CollectionReaderFactory.createReader(TextReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, "src/test/resources/texts",
                ResourceCollectionReaderBase.PARAM_PATTERNS, "*.txt",
                ResourceCollectionReaderBase.PARAM_LANGUAGE, "latin");

        AnalysisEngine writer;
        if (false) {
            writer = createEngine(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_FORMAT, aFormat, 
                    BinaryCasWriter.PARAM_TARGET_LOCATION, aLocation,
                    BinaryCasWriter.PARAM_TYPE_SYSTEM_LOCATION, 
                            aWriteTypeSystem ? new File(aLocation, "typesystem.bin") : null);
        }
        else {
            writer = createEngine(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_FORMAT, aFormat, 
                    BinaryCasWriter.PARAM_TARGET_LOCATION, aLocation,
                    BinaryCasWriter.PARAM_TYPE_SYSTEM_LOCATION, 
                            aWriteTypeSystem ? "typesystem.bin" : null);
        }

        // AnalysisEngine dumper = createEngine(CASDumpWriter.class);

        runPipeline(textReader, /* dumper, */writer);

        if (aLocation.startsWith("jar:")) {
            assertTrue(new File(testFolder.getRoot(), "archive.zip").exists());
        }
        else {
            assertTrue(new File(testFolder.getRoot(), "example1.txt.bin").exists());
            assertTrue(new File(testFolder.getRoot(), "example2.txt.bin").exists());
        }
    }

    public void writeSerialized(String aLocation, boolean aWriteTypeSystem)
        throws Exception
    {
        System.out.println("--- WRITING ---");
        CollectionReader reader = CollectionReaderFactory.createReader(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts",
                TextReader.PARAM_PATTERNS, "*.txt",
                TextReader.PARAM_LANGUAGE, "latin");

        AnalysisEngine writer;
        if (false) {
            writer = AnalysisEngineFactory.createEngine(
                    SerializedCasWriter.class,
                    SerializedCasWriter.PARAM_TARGET_LOCATION, aLocation,
                    SerializedCasWriter.PARAM_FILENAME_EXTENSION, ".bin",
                    SerializedCasWriter.PARAM_TYPE_SYSTEM_LOCATION, 
                            aWriteTypeSystem ? new File(aLocation, "typesystem.bin") : null);
        }
        else {
            writer = AnalysisEngineFactory.createEngine(
                    SerializedCasWriter.class,
                    SerializedCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot(),
                    SerializedCasWriter.PARAM_FILENAME_EXTENSION, ".bin",
                    SerializedCasWriter.PARAM_TYPE_SYSTEM_LOCATION, 
                            aWriteTypeSystem ? "typesystem.bin" : null);
        }

        runPipeline(reader, writer);

        assertTrue(new File(testFolder.getRoot(), "example1.txt.bin").exists());
    }

    public void read(String aLocation, int aMode, boolean aLoadExternal)
        throws Exception
    {
        TypeSystemDescription tsd;
        switch (aMode) {
        case NONE:
            tsd = null;
            break;
        case METADATA:
            tsd = createTypeSystemDescription("desc.type.metadata");
            break;
        case ALL:
            tsd = createTypeSystemDescription();
            break;
        default:
            throw new IllegalArgumentException("Unknown mode");
        }
        
        System.out.println("--- READING ---");
        CollectionReader reader;
        if (false) {
                reader = CollectionReaderFactory.createReader(
                        BinaryCasReader.class,
                        BinaryCasReader.PARAM_SOURCE_LOCATION, aLocation,
                        BinaryCasReader.PARAM_PATTERNS, "*.bin",
                        // Allow loading only if TSD is not specified
                        BinaryCasReader.PARAM_TYPE_SYSTEM_LOCATION, 
                                aLoadExternal ? new File(aLocation, "typesystem.bin") : null);
        }
        else {
            reader = CollectionReaderFactory.createReader(
                    BinaryCasReader.class,
                    BinaryCasReader.PARAM_SOURCE_LOCATION, aLocation,
                    BinaryCasReader.PARAM_PATTERNS, "*.bin",
                    // Allow loading only if TSD is not specified
                    BinaryCasReader.PARAM_TYPE_SYSTEM_LOCATION, 
                            aLoadExternal ? "typesystem.bin" : null);
        }

        // Test reading into CAS
        CAS cas = CasCreationUtils.createCas(tsd, null, null);
        reader.getNext(cas);
        String refText1 = readFileToString(new File("src/test/resources/texts/example1.txt"));
        assertEquals(refText1, cas.getDocumentText());
        assertEquals("latin", cas.getDocumentLanguage());

        // Test reading into JCas
        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());
        assertEquals("latin", DocumentMetaData.get(jcas).getLanguage());
        String refText2 = readFileToString(new File("src/test/resources/texts/example2.txt"));
        assertEquals(refText2, jcas.getDocumentText());
        assertEquals("latin", jcas.getDocumentLanguage());
        
        assertFalse(reader.hasNext());
    }

    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
