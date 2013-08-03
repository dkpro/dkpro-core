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

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
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
                ResourceCollectionReaderBase.PARAM_PATH, "src/test/resources/texts",
                ResourceCollectionReaderBase.PARAM_PATTERNS, "[+]*.txt",
                ResourceCollectionReaderBase.PARAM_LANGUAGE, "latin");

        AnalysisEngine xmiWriter = AnalysisEngineFactory.createEngine(BinaryCasWriter.class,
                BinaryCasWriter.PARAM_FORMAT, aFormat, 
                BinaryCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot().getPath());

//        AnalysisEngine dumper = createEngine(CASDumpWriter.class);

        runPipeline(textReader, /*dumper,*/ xmiWriter);

        assertTrue(new File(testFolder.getRoot(), "example1.txt.bin").exists());
    }

    public void writeSerialized()
            throws Exception
        {
            System.out.println("--- WRITING ---");
            CollectionReader textReader = CollectionReaderFactory.createReader(TextReader.class,
                    ResourceCollectionReaderBase.PARAM_PATH, "src/test/resources/texts",
                    ResourceCollectionReaderBase.PARAM_PATTERNS, "[+]*.txt",
                    ResourceCollectionReaderBase.PARAM_LANGUAGE, "latin");

            AnalysisEngine xmiWriter = AnalysisEngineFactory.createEngine(SerializedCasWriter.class,
                    SerializedCasWriter.PARAM_EXTENSION, ".bin",
                    SerializedCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot().getPath());

//            AnalysisEngine dumper = createEngine(CASDumpWriter.class);

            runPipeline(textReader, /*dumper,*/ xmiWriter);

            assertTrue(new File(testFolder.getRoot(), "example1.txt.bin").exists());
        }


    public void read(TypeSystemDescription aTSD)
        throws Exception
    {
        System.out.println("--- READING ---");
        CollectionReader reader = CollectionReaderFactory.createReader(BinaryCasReader.class,
                ResourceCollectionReaderBase.PARAM_PATH, testFolder.getRoot().getPath(),
                ResourceCollectionReaderBase.PARAM_PATTERNS,
                new String[] { ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.bin" });

        CAS cas = CasCreationUtils.createCas(aTSD, null, null);
        reader.getNext(cas);

//        createEngine(CASDumpWriter.class).process(cas);

        String refText = readFileToString(new File("src/test/resources/texts/example1.txt"));
        
        assertEquals(refText, cas.getDocumentText());
        assertEquals("latin", cas.getDocumentLanguage());
    }
}
