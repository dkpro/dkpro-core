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

import static de.tudarmstadt.ukp.dkpro.core.performance.PerformanceTestUtil.initRandomCas;
import static de.tudarmstadt.ukp.dkpro.core.performance.PerformanceTestUtil.measurePerformance;
import static de.tudarmstadt.ukp.dkpro.core.performance.PerformanceTestUtil.repeat;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class BinaryCasWriterReaderTest
{
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void test0()
        throws Exception
    {
        write("0");
        read(createTypeSystemDescription());
    }

    @Test
    public void test4()
        throws Exception
    {
        write("4");
        read(createTypeSystemDescription());
    }

    @Test
    public void test6()
        throws Exception
    {
        write("6");
        read(createTypeSystemDescription());
    }

    @Test
    public void test6plus0()
        throws Exception
    {
        write("6+");
        read(createTypeSystemDescription());
    }

    @Test
    public void test6plus1()
        throws Exception
    {
        write("6+");
        read(createTypeSystemDescription("desc.type.metadata"));
    }

    @Test
    public void testSerialized()
        throws Exception
    {
        writeSerialized();
        read(createTypeSystemDescription());
    }

    public void write(String aFormat)
        throws Exception
    {
        System.out.println("--- WRITING ---");
        CollectionReader textReader = CollectionReaderFactory.createReader(TextReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, "src/test/resources/texts",
                ResourceCollectionReaderBase.PARAM_PATTERNS, "[+]*.txt",
                ResourceCollectionReaderBase.PARAM_LANGUAGE, "latin");

        AnalysisEngine writer = createEngine(BinaryCasWriter.class, BinaryCasWriter.PARAM_FORMAT,
                aFormat, BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot().getPath());

        // AnalysisEngine dumper = createEngine(CASDumpWriter.class);

        runPipeline(textReader, /* dumper, */writer);

        assertTrue(new File(testFolder.getRoot(), "example1.txt.bin").exists());
    }

    public void writeSerialized()
        throws Exception
    {
        System.out.println("--- WRITING ---");
        CollectionReader textReader = CollectionReaderFactory.createReader(TextReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, "src/test/resources/texts",
                ResourceCollectionReaderBase.PARAM_PATTERNS, "[+]*.txt",
                ResourceCollectionReaderBase.PARAM_LANGUAGE, "latin");

        AnalysisEngine writer = createEngine(SerializedCasWriter.class,
                SerializedCasWriter.PARAM_FILENAME_SUFFIX, ".bin",
                SerializedCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot().getPath());

        // AnalysisEngine dumper = createEngine(CASDumpWriter.class);

        runPipeline(textReader, /* dumper, */writer);

        assertTrue(new File(testFolder.getRoot(), "example1.txt.bin").exists());
    }

    public void read(TypeSystemDescription aTSD)
        throws Exception
    {
        System.out.println("--- READING ---");
        CollectionReader reader = CollectionReaderFactory.createReader(BinaryCasReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, testFolder.getRoot().getPath(),
                ResourceCollectionReaderBase.PARAM_PATTERNS,
                new String[] { ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.bin" });

        CAS cas = CasCreationUtils.createCas(aTSD, null, null);
        reader.getNext(cas);

        // createEngine(CASDumpWriter.class).process(cas);

        String refText = readFileToString(new File("src/test/resources/texts/example1.txt"));

        assertEquals(refText, cas.getDocumentText());
        assertEquals("latin", cas.getDocumentLanguage());
    }

    @Test
    public void performanceTest()
        throws Exception
    {
        // Generate test data
        System.out.printf("Generating test data... ");
        JCas jcas = JCasFactory.createJCas();
        DocumentMetaData.create(jcas).setDocumentId("dummy");
        initRandomCas(jcas, 10000, 30000, 0);
        System.out.printf("done%n");
        
        System.out.printf("Data serialized to %s %n", testFolder.getRoot().getPath());
        
        // Set up configurations
        Map<String, AnalysisEngineDescription> configs = new LinkedHashMap<String, AnalysisEngineDescription>();
        configs.put(
                "Format 6+ - no compression",
                createEngineDescription(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_FORMAT, "6+", 
                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.NONE,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot().getPath()));
        configs.put(
                "Format 6+ - GZip compression",
                createEngineDescription(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_FORMAT, "6+", 
                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.GZIP,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot().getPath()));
        configs.put(
                "Format 6+ - BZIP2 compression",
                createEngineDescription(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_FORMAT, "6+", 
                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.BZIP2,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot().getPath()));
        configs.put(
                "Format 6+ - XZ compression",
                createEngineDescription(
                    BinaryCasWriter.class, 
                    BinaryCasWriter.PARAM_FORMAT, "6+", 
                    BinaryCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ,
                    BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot().getPath()));

        // Run tests
        for (Entry<String, AnalysisEngineDescription> cfg : configs.entrySet()) {
            System.out.printf("%s%n", cfg.getKey());
            
            for (File f : FileUtils.listFiles(testFolder.getRoot(), new PrefixFileFilter("dummy.bin"), null)) {
                f.delete();
            }
            
            SummaryStatistics stats = measurePerformance(cfg.getValue(), repeat(jcas, 1000));
            System.out.printf("  Repeat  %10d times%n", stats.getN());
            System.out.printf("  Total   %10.0f ms %n", stats.getSum());
            System.out.printf("  Mean    %10.0f ms %n", stats.getMean());
            System.out.printf("  Min     %10.0f ms %n", stats.getMin());
            System.out.printf("  Max     %10.0f ms %n", stats.getMax());
            
            for (File f : FileUtils.listFiles(testFolder.getRoot(), new PrefixFileFilter("dummy.bin"), null)) {
                System.out.printf("  Size    %10d bytes%n", f.length());
            }
        }
    }
}
