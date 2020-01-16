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
package org.dkpro.core.io.bincas;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.uima.cas.SerialFormat.COMPRESSED_FILTERED;
import static org.apache.uima.cas.impl.Serialization.deserializeCASComplete;
import static org.apache.uima.cas.impl.Serialization.serializeCASComplete;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.FSUtil.getFeature;
import static org.dkpro.core.performance.PerformanceTestUtil.initRandomCas;
import static org.dkpro.core.performance.PerformanceTestUtil.measureReadPerformance;
import static org.dkpro.core.performance.PerformanceTestUtil.measureWritePerformance;
import static org.dkpro.core.performance.PerformanceTestUtil.repeat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.SerialFormat;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.CasIOUtils;
import org.dkpro.core.api.io.ResourceCollectionReaderBase;
import org.dkpro.core.api.resources.CompressionMethod;
import org.dkpro.core.io.bincas.BinaryCasReader;
import org.dkpro.core.io.bincas.BinaryCasWriter;
import org.dkpro.core.io.bincas.SerializedCasWriter;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class BinaryCasWriterReaderTest
{
    private static final int NONE = 1;
    private static final int METADATA = 2;
    private static final int ALL = 3;
    
    private File testFolder;
    
    @Before
    public void setup() throws IOException 
    {
        testFolder = testContext.getTestOutputFolder();
    }
    
    @Test
    public void testSReinitialize()
        throws Exception
    {
        write(testFolder.getPath(), SerialFormat.SERIALIZED.toString(), true);
        // Type system is reinitialized from the persisted type system
        read(testFolder.getPath(), NONE, true, false); 
        read(testFolder.getPath(), NONE, true, true);
    }

    @Test
    public void testSReinitializeInZIP()
        throws Exception
    {
        write("jar:" + testFolder.toURI().toURL() + "/archive.zip", "S", true);
        // Type system is reinitialized from the persisted type system
        read("jar:" + testFolder.toURI().toURL() + "/archive.zip", NONE, true, false); 
        read("jar:" + testFolder.toURI().toURL() + "/archive.zip", NONE, true, true);
    }

    @Test
    public void testSPreinitialized()
        throws Exception
    {
        write(testFolder.getPath(), "S", false);
        read(testFolder.getPath(), ALL, false, false);
        read(testFolder.getPath(), ALL, false, true);
    }

    @Test
    public void testSplusReinitialize()
        throws Exception
    {
        write(testFolder.getPath(), "S+", false);
     // Type system is reinitialized from the persisted CAS
        read(testFolder.getPath(), NONE, false, false); 
        read(testFolder.getPath(), NONE, false, true);
    }

    @Test
    public void test0Preinitialized()
        throws Exception
    {
        write(testFolder.getPath(), SerialFormat.BINARY.toString(), false);
        read(testFolder.getPath(), ALL, false, false);
        read(testFolder.getPath(), ALL, false, true);    
    }

    @Test
    public void test4Preinitialized()
        throws Exception
    {
        write(testFolder.getPath(), "4", false);
        read(testFolder.getPath(), ALL, false, false);
        read(testFolder.getPath(), ALL, false, true);
    }
    
    /**
     * The type system in the CAS is different from the one in the file. To do lenient loading with
     * format 6, we need to know the type system that was used to originally store the CAS.
     */
    @Test
    public void test6Lenient()
        throws Exception
    {
        write(testFolder.getPath(), SerialFormat.COMPRESSED_FILTERED.toString(), true);
        read(testFolder.getPath(), METADATA, true, false);
        read(testFolder.getPath(), METADATA, true, true);
    }
    
    @Test
    public void test6LenientPlainUima() throws Exception
    {
//      TypeSystemDescription tsd = new TypeSystemDescription_impl();
//      TypeDescription td = tsd.addType("DocumentMetaData", "", CAS.TYPE_NAME_DOCUMENT_ANNOTATION);
//      td.addFeature("feat", "", CAS.TYPE_NAME_STRING);
//        
//      CAS source = CasCreationUtils.createCas(tsd, null, null, null);
//      CAS target = CasCreationUtils.createCas(tsd, null, null, null);
//      source.getJCas();
//      target.getJCas();
//
//        AnnotationFS dmd = source
//                .createAnnotation(source.getTypeSystem().getType("DocumentMetaData"), 0, 0);
//        source.addFsToIndexes(dmd);
//        assertEquals("DocumentMetaData", source.getDocumentAnnotation().getType().getName());
        
        CAS source = JCasFactory.createJCas().getCas();
        CAS target = JCasFactory.createJCas().getCas();

        new DocumentMetaData(source.getJCas(), 0, 0).addToIndexes();

//        source.setDocumentText("This is a test.");
//        source.setDocumentLanguage("en");
        
        @SuppressWarnings("resource")
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CasIOUtils.save(source, bos, COMPRESSED_FILTERED);
        bos.close();
        
        CasIOUtils.load(new ByteArrayInputStream(bos.toByteArray()), target);
    }

    @Test
    public void test6Preinitialized()
        throws Exception
    {
        write(testFolder.getPath(), "6", false);
        read(testFolder.getPath(), ALL, false, false);
        read(testFolder.getPath(), ALL, false, true);
    }

    @Test
    public void test_COMPRESSED_FILTERED_TSI_preinitialized()
        throws Exception
    {
        write(testFolder.getPath(), SerialFormat.COMPRESSED_FILTERED_TSI.toString(), false);
        read(testFolder.getPath(), ALL, false, false);
        read(testFolder.getPath(), ALL, false, true);
    }

    @Test
    public void test_COMPRESSED_FILTERED_TSI_lenient()
        throws Exception
    {
        write(testFolder.getPath(), SerialFormat.COMPRESSED_FILTERED_TSI.toString(), false);
        read(testFolder.getPath(), METADATA, false, false);
        read(testFolder.getPath(), METADATA, false, true);
    }

    @Test
    public void test6plusPreinitialized()
        throws Exception
    {
        write(testFolder.getPath(), "6+", false);
        read(testFolder.getPath(), ALL, false, false);
        read(testFolder.getPath(), ALL, false, true);
    }

    @Test
    public void test6plusLenient()
        throws Exception
    {
        write(testFolder.getPath(), "6+", false);
        read(testFolder.getPath(), METADATA, false, false);
        read(testFolder.getPath(), METADATA, false, true);
    }

    @Test
    public void testSerializedEmbeddedTypeSystem()
        throws Exception
    {
        writeSerialized(testFolder.getPath(), false);
        // Type system is reinitialized from the persisted CAS
        read(testFolder.getPath(), NONE, false, false); 
        read(testFolder.getPath(), NONE, false, true); 
    }

    @Test
    public void testSerializedSeparateTypeSystem()
        throws Exception
    {
        writeSerialized(testFolder.getPath(), true);
        // Type system is reinitialized from the persisted CAS
        read(testFolder.getPath(), NONE, true, false); 
        read(testFolder.getPath(), NONE, true, true);
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
                BinaryCasWriter.PARAM_OVERWRITE, true,
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
        assertEquals(DocumentMetaData.get(out).getDocumentId(),
                DocumentMetaData.get(in).getDocumentId());
    }
    
    @Test
    public void testReadingFileWithDocumentMetaData() throws Exception {
        JCas prep = JCasFactory.createText("This is a test.", "en");
        DocumentMetaData origDmd = DocumentMetaData.create(prep);
        origDmd.setDocumentId("data.txt");
        origDmd.setDocumentTitle("data.txt");
        AnalysisEngine writer = createEngine(
                BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION, new File(testFolder, "out.bin"),
                BinaryCasWriter.PARAM_SINGULAR_TARGET, true);
        writer.process(prep);
        
        CollectionReader reader = createReader(
                BinaryCasReader.class,
                BinaryCasReader.PARAM_SOURCE_LOCATION, testFolder,
                BinaryCasReader.PARAM_PATTERNS, "out.bin");
        
        JCas doc = JCasFactory.createJCas();
        reader.getNext(doc.getCas());
        
        // System.out.println(doc.getDocumentAnnotationFs());
        
        TOP dmd = doc.getDocumentAnnotationFs();
        assertEquals((Integer) 0, getFeature(dmd, "begin", Integer.class));
        assertEquals((Integer) 15, getFeature(dmd, "end", Integer.class));
        assertEquals("en", getFeature(dmd, "language", String.class));
        assertEquals("data.txt", getFeature(dmd, "documentTitle", String.class));
        assertEquals("data.txt", getFeature(dmd, "documentId", String.class));
        assertEquals(null, getFeature(dmd, "documentUri", String.class));
        assertEquals(null, getFeature(dmd, "collectionId", String.class));
        assertEquals(null, getFeature(dmd, "documentBaseUri", String.class));
        assertEquals(false, getFeature(dmd, "isLastSegment", Boolean.class));
    }
    
    @Test
    public void testReadingFileWithoutDocumentMetaData() throws Exception {
        JCas prep = JCasFactory.createText("This is a test.");
        AnalysisEngine writer = createEngine(
                BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION, new File(testFolder, "out.bin"),
                BinaryCasWriter.PARAM_SINGULAR_TARGET, true);
        writer.process(prep);
        
        CollectionReader reader = createReader(
                BinaryCasReader.class,
                BinaryCasReader.PARAM_SOURCE_LOCATION, testFolder,
                BinaryCasReader.PARAM_PATTERNS, "out.bin");
                
        JCas doc = JCasFactory.createJCas();
        reader.getNext(doc.getCas());
        
        // System.out.println(doc.getDocumentAnnotationFs());
        
        TOP dmd = doc.getDocumentAnnotationFs();
        assertEquals((Integer) 0, getFeature(dmd, "begin", Integer.class));
        assertEquals((Integer) 15, getFeature(dmd, "end", Integer.class));
        assertEquals("x-unspecified", getFeature(dmd, "language", String.class));
        assertEquals("out.bin", getFeature(dmd, "documentTitle", String.class));
        assertEquals("out.bin", getFeature(dmd, "documentId", String.class));
        assertTrue(separatorsToUnix(getFeature(dmd, "documentUri", String.class))
                .endsWith("/target/test-output/BinaryCasWriterReaderTest-testReadingFileWithoutDocumentMetaData/out.bin"));
        assertTrue(separatorsToUnix(getFeature(dmd, "collectionId", String.class))
                .endsWith("/target/test-output/BinaryCasWriterReaderTest-testReadingFileWithoutDocumentMetaData/"));
        assertTrue(separatorsToUnix(getFeature(dmd, "documentBaseUri", String.class))
                .endsWith("/target/test-output/BinaryCasWriterReaderTest-testReadingFileWithoutDocumentMetaData/"));
        assertEquals(false, getFeature(dmd, "isLastSegment", Boolean.class));
    }
    
    @Test
    public void testReadingFileOverridingDocumentMetaData() throws Exception {
        JCas prep = JCasFactory.createText("This is a test.", "en");
        DocumentMetaData origDmd = DocumentMetaData.create(prep);
        origDmd.setDocumentId("data.txt");
        origDmd.setDocumentTitle("data.txt");
        AnalysisEngine writer = createEngine(
                BinaryCasWriter.class,
                BinaryCasWriter.PARAM_TARGET_LOCATION, new File(testFolder, "out.bin"),
                BinaryCasWriter.PARAM_SINGULAR_TARGET, true);
        writer.process(prep);
        
        CollectionReader reader = createReader(
                BinaryCasReader.class,
                BinaryCasReader.PARAM_SOURCE_LOCATION, testFolder,
                BinaryCasReader.PARAM_PATTERNS, "out.bin",
                BinaryCasReader.PARAM_OVERRIDE_DOCUMENT_METADATA, true);
        
        JCas doc = JCasFactory.createJCas();
        reader.getNext(doc.getCas());
        
        // System.out.println(doc.getDocumentAnnotationFs());
        
        TOP dmd = doc.getDocumentAnnotationFs();
        assertEquals((Integer) 0, getFeature(dmd, "begin", Integer.class));
        assertEquals((Integer) 15, getFeature(dmd, "end", Integer.class));
        assertEquals("en", getFeature(dmd, "language", String.class));
        assertEquals("out.bin", getFeature(dmd, "documentTitle", String.class));
        assertEquals("out.bin", getFeature(dmd, "documentId", String.class));
        assertTrue(separatorsToUnix(getFeature(dmd, "documentUri", String.class))
                .endsWith("/target/test-output/BinaryCasWriterReaderTest-testReadingFileOverridingDocumentMetaData/out.bin"));
        assertTrue(separatorsToUnix(getFeature(dmd, "collectionId", String.class))
                .endsWith("/target/test-output/BinaryCasWriterReaderTest-testReadingFileOverridingDocumentMetaData/"));
        assertTrue(separatorsToUnix(getFeature(dmd, "documentBaseUri", String.class))
                .endsWith("/target/test-output/BinaryCasWriterReaderTest-testReadingFileOverridingDocumentMetaData/"));
        assertEquals(false, getFeature(dmd, "isLastSegment", Boolean.class));
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
                    BinaryCasWriter.PARAM_FILENAME_EXTENSION, ".bin",
                    BinaryCasWriter.PARAM_TYPE_SYSTEM_LOCATION, 
                            aWriteTypeSystem ? new File(aLocation, "typesystem.bin") : null);
        }
        else {
            writer = createEngine(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_FORMAT, aFormat, 
                    BinaryCasWriter.PARAM_TARGET_LOCATION, aLocation,
                    BinaryCasWriter.PARAM_FILENAME_EXTENSION, ".bin",
                    BinaryCasWriter.PARAM_TYPE_SYSTEM_LOCATION, 
                            aWriteTypeSystem ? "typesystem.bin" : null);
        }

        // AnalysisEngine dumper = createEngine(CASDumpWriter.class);

        runPipeline(textReader, /* dumper, */writer);

        if (aLocation.startsWith("jar:")) {
            assertTrue(new File(testFolder, "archive.zip").exists());
        }
        else {
            assertTrue(new File(testFolder, "example1.txt.bin").exists());
            assertTrue(new File(testFolder, "example2.txt.bin").exists());
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
                    SerializedCasWriter.PARAM_TARGET_LOCATION, testFolder,
                    SerializedCasWriter.PARAM_FILENAME_EXTENSION, ".bin",
                    SerializedCasWriter.PARAM_TYPE_SYSTEM_LOCATION, 
                            aWriteTypeSystem ? "typesystem.bin" : null);
        }

        runPipeline(reader, writer);

        assertTrue(new File(testFolder, "example1.txt.bin").exists());
    }

    public void read(String aLocation, int aMode, boolean aLoadExternal, boolean aMergeTS)
        throws Exception
    {
        TypeSystemDescription tsd;
        switch (aMode) {
        case NONE:
            tsd = null;
            break;
        case METADATA:
            tsd = createTypeSystemDescription("desc.type.metadata", "desc.type.metadata_customized");
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
                    BinaryCasReader.PARAM_MERGE_TYPE_SYSTEM, aMergeTS,
                    // Allow loading only if TSD is not specified
                    BinaryCasReader.PARAM_TYPE_SYSTEM_LOCATION, 
                            aLoadExternal ? "typesystem.bin" : null);
        }

        // Test reading into CAS
        CAS cas = CasCreationUtils.createCas(tsd, null, null);
        reader.typeSystemInit(cas.getTypeSystem());
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
    
    @Test
    public void measureSerializedCas()
        throws UIMAException, IOException
    {
        File file = new File(testFolder, "dummy.bin");
        
        Iterable<JCas> data = repeat(generateRandomCas(), 100);
        
        System.out.printf("= write%n");
        SummaryStatistics statsWrite = measureWriteSerializedCas(data, file);
        
        System.out.printf("= read%n");
        SummaryStatistics statsRead = measureReadSerializedCas(file, 100);
        printStats(statsWrite, statsRead);
    }
    
    private static SummaryStatistics measureWriteSerializedCas(Iterable<JCas> aTestData, File aFile)
        throws IOException
    {
        SummaryStatistics stats = new SummaryStatistics();
        
        for (JCas jcas : aTestData) {
            long begin = System.currentTimeMillis();
            writeSerializedCas(jcas, aFile);
            stats.addValue(System.currentTimeMillis() - begin);
        }

        return stats;
    }

    private static SummaryStatistics measureReadSerializedCas(File aFile, int aRepeat)
        throws IOException, UIMAException
    {
        SummaryStatistics stats = new SummaryStatistics();

        JCas jcas = JCasFactory.createJCas();

        for (int n = 0; n < aRepeat; n++) {
            long begin = System.currentTimeMillis();
            readSerializedCas(jcas, aFile);
            stats.addValue(System.currentTimeMillis() - begin);
        }

        return stats;
    }
    
    @Test
    public void measureCasCreation()
        throws UIMAException
    {
        SummaryStatistics statsRead = measureCasCreation(100);
        printStats("CREATE", statsRead);
    }
    
    private static SummaryStatistics measureCasCreation(int aRepeat)
        throws UIMAException
    {
        SummaryStatistics stats = new SummaryStatistics();
        
        for (int n = 0; n < aRepeat; n++) {
            long begin = System.currentTimeMillis();
//            JCas jcas = JCasFactory.createJCas();
            JCas jcas = CasCreationUtils.createCas((TypeSystemDescription) null, null, null)
                    .getJCas();
            stats.addValue(System.currentTimeMillis() - begin);
        }
        
        return stats;
    }

    private static void writeSerializedCas(JCas aJCas, File aFile)
        throws IOException
    {
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(aFile))) {
            CASCompleteSerializer serializer = serializeCASComplete(aJCas.getCasImpl());
            os.writeObject(serializer);
        }
    }

    private static void readSerializedCas(JCas aJCas, File aFile)
            throws IOException
    {
        try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(aFile))) {
            CASCompleteSerializer serializer = (CASCompleteSerializer) is.readObject();
            deserializeCASComplete(serializer, aJCas.getCasImpl());
            
//          // Initialize the JCas sub-system which is the most often used API in DKPro Core 
//          // components
//          try {
//              aJCas.getCas().getJCas();
//          }
//          catch (CASException e) {
//              throw new IOException(e);
//          }
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    private JCas generateRandomCas()
        throws UIMAException
    {
        // Generate test data
        System.out.printf("Generating test data... ");
        JCas jcas = JCasFactory.createJCas();
        DocumentMetaData.create(jcas).setDocumentId("dummy");
        initRandomCas(jcas, 10000, 30000, 0);
        System.out.printf("done%n");
        return jcas;
    }
    
    @Ignore("Run this only when you want to compare performance")
    @Test
    public void performanceTest()
        throws Exception
    {
        int REPEATS = 100;
        
        // Generate test data
        Iterable<JCas> testdata = repeat(generateRandomCas(), REPEATS);
        
        System.out.printf("Data serialized to %s %n", testFolder);
        
        // Set up configurations
        Map<String, AnalysisEngineDescription> configs = new LinkedHashMap<>();
        configs.put(
                "Format S - no compression",
                createEngineDescription(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_OVERWRITE, true,
                    BinaryCasWriter.PARAM_FORMAT, "S", 
                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.NONE,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder));
        configs.put(
                "Format S+ - no compression",
                createEngineDescription(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_OVERWRITE, true,
                    BinaryCasWriter.PARAM_FORMAT, "S+", 
                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.NONE,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder));
        configs.put(
                "Format 0 - no compression",
                createEngineDescription(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_OVERWRITE, true,
                    BinaryCasWriter.PARAM_FORMAT, "0", 
                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.NONE,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder));
        configs.put(
                "Format 4 - no compression",
                createEngineDescription(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_OVERWRITE, true,
                    BinaryCasWriter.PARAM_FORMAT, "4", 
                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.NONE,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder));
        configs.put(
                "Format 6 - no compression",
                createEngineDescription(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_OVERWRITE, true,
                    BinaryCasWriter.PARAM_FORMAT, "6", 
                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.NONE,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder));
        configs.put(
                "Format 6+ - no compression",
                createEngineDescription(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_OVERWRITE, true,
                    BinaryCasWriter.PARAM_FORMAT, "6+", 
                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.NONE,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder));
//        configs.put(
//                "Format 6+ - GZip compression",
//                createEngineDescription(
//                    BinaryCasWriter.class, 
//                    BinaryCasWriter.PARAM_FORMAT, "6+", 
//                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.GZIP,
//                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder));
//        configs.put(
//                "Format 6+ - BZIP2 compression",
//                createEngineDescription(
//                    BinaryCasWriter.class, 
//                    BinaryCasWriter.PARAM_FORMAT, "6+", 
//                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.BZIP2,
//                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder));
//        configs.put(
//                "Format 6+ - XZ compression",
//                createEngineDescription(
//                    BinaryCasWriter.class, 
//                    BinaryCasWriter.PARAM_FORMAT, "6+", 
//                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ,
//                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder));

        // Run tests
        System.out.printf("--------------------------------------------%n");
        for (Entry<String, AnalysisEngineDescription> cfg : configs.entrySet()) {
            System.out.printf("%s%n", cfg.getKey());
            System.out.printf("  Measuring WRITE%n");
            
            for (File f : FileUtils.listFiles(testFolder, new PrefixFileFilter("dummy.bin"),
                    null)) {
                f.delete();
            }
            
            SummaryStatistics writeStats = measureWritePerformance(cfg.getValue(), testdata);

            Collection<File> files = FileUtils.listFiles(testFolder,
                    new PrefixFileFilter("dummy.bin"), null);
            assertEquals(1, files.size());
            File f = files.iterator().next();
            
            // For some readers, we may need a CAS with is already initialized with the proper 
            // type system, so we create one here
            JCas jcas = JCasFactory.createJCas();
            System.out.printf("  Measuring READ%n");
            CollectionReaderDescription reader = createReaderDescription(
                    BinaryCasReader.class,
                    BinaryCasReader.PARAM_SOURCE_LOCATION, f);
            
            SummaryStatistics readStats = measureReadPerformance(reader, jcas, REPEATS);
            
            printStats(writeStats, readStats);
            System.out.printf("  Size    %10d bytes%n", f.length());
            System.out.printf("--------------------------------------------%n");
        }
        
        measureWriteSerializedCas(testdata, new File(testFolder, "dummy.bin"));
    }
    
    private static void printStats(String aTitle, SummaryStatistics aStats)
    {
        System.out.printf("          %10s%n", aTitle, "READ");
        System.out.printf("  Repeat  %10d times%n", aStats.getN());
        System.out.printf("  Total   %10.0f ms%n", aStats.getSum());
        System.out.printf("  Mean    %10.0f ms%n", aStats.getMean());
        System.out.printf("  Min     %10.0f ms%n", aStats.getMin());
        System.out.printf("  Max     %10.0f ms%n", aStats.getMax());
    }
    
    private static void printStats(SummaryStatistics aWrite, SummaryStatistics aRead)
    {
        System.out.printf("          %10s         %10s%n", "WRITE", "READ");
        System.out.printf("  Repeat  %10d times %10d times%n", aWrite.getN(), aRead.getN());
        System.out.printf("  Total   %10.0f ms    %10.0f ms%n", aWrite.getSum(), aRead.getSum());
        System.out.printf("  Mean    %10.0f ms    %10.0f ms%n", aWrite.getMean(), aRead.getMean());
        System.out.printf("  Min     %10.0f ms    %10.0f ms%n", aWrite.getMin(), aRead.getMin());
        System.out.printf("  Max     %10.0f ms    %10.0f ms%n", aWrite.getMax(), aRead.getMax());
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
