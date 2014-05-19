/*******************************************************************************
 * Copyright 2010
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
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class SerializedCasWriterReaderTest
{
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void testCasWithTypeSystemEmbedded() throws Exception
	{
		write(true);
		read();
	}

    @Test
    public void testCasWithTypeSystemSeparate() throws Exception
    {
        write(false);
        read();
    }

	public void write(boolean aIncludeTypeSystem) throws Exception
	{
		CollectionReader reader = CollectionReaderFactory.createReader(
				TextReader.class,
				TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts",
				TextReader.PARAM_PATTERNS, "*.txt",
				TextReader.PARAM_LANGUAGE, "latin");

		AnalysisEngine writer = AnalysisEngineFactory.createEngine(
				SerializedCasWriter.class,
				SerializedCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot(),
				SerializedCasWriter.PARAM_TYPE_SYSTEM_LOCATION, 
				        aIncludeTypeSystem ? null : testFolder.newFile("typesystem.ser"));

		runPipeline(reader, writer);

		assertTrue(new File(testFolder.getRoot(), "example1.txt.ser").exists());
	}

	public void read() throws Exception
	{
		CollectionReader reader = CollectionReaderFactory.createReader(
				SerializedCasReader.class,
				SerializedCasReader.PARAM_SOURCE_LOCATION, testFolder.getRoot(),
				SerializedCasReader.PARAM_PATTERNS, "*.ser",
				SerializedCasReader.PARAM_TYPE_SYSTEM_LOCATION, 
				        new File(testFolder.getRoot(), "typesystem.ser"));

		CAS cas = CasCreationUtils.createCas(createTypeSystemDescription(), null, null);
		reader.getNext(cas);

		String refText = readFileToString(new File("src/test/resources/texts/example1.txt"));
		assertEquals(refText, cas.getDocumentText());
		assertEquals("latin", cas.getDocumentLanguage());
	}
	
//	@Test
//	public void lenientTest() throws Exception
//	{
//        TypeSystemDescription tsdMeta = TypeSystemDescriptionFactory
//                .createTypeSystemDescription("desc.type.metadata");
//        
//        
//        // Create a CAS initialized with that type system and set the text
//        CAS casOut = createCas(tsdMeta, null, null);
//        casOut.setDocumentText("This is a test.");
//        DocumentMetaData meta = DocumentMetaData.create(casOut);
//        meta.setDocumentId("document");
//        
//        // Write out
//        AnalysisEngine writer = AnalysisEngineFactory.createEngine(
//                SerializedCasWriter.class, tsdMeta,
//                SerializedCasWriter.PARAM_TARGET_LOCATION, testFolder.getRoot().getPath());
//        writer.process(casOut);
//        
//        // Create a new type system from scratch
//        TypeSystemDescription tsd = new TypeSystemDescription_impl();
//        TypeDescription tokenTypeDesc = tsd.addType("Token", "", CAS.TYPE_NAME_ANNOTATION);
//        tokenTypeDesc.addFeature("length", "", CAS.TYPE_NAME_INTEGER);
//        tsd = CasCreationUtils.mergeTypeSystems(asList(tsd, tsdMeta));
//        
//        // Now read in to CAS with different type system
//        CollectionReader reader = CollectionReaderFactory.createReader(
//                SerializedCasReader.class,
//                SerializedCasReader.PARAM_SOURCE_LOCATION, testFolder.getRoot().getPath(),
//                SerializedCasReader.PARAM_PATTERNS, new String [] {
//                    SerializedCasReader.INCLUDE_PREFIX+"*.ser"
//                });
//
//        CAS casIn = CasCreationUtils.createCas(tsd, null, null);
//        reader.getNext(casIn);
//        
//        upgrade(casIn, tsd);
//        
//        // Try to create an annotation with the extra type
//        AnnotationFS fs = casOut.createAnnotation(casIn.getTypeSystem().getType("Token"), 0, 1);
//        casOut.addFsToIndexes(fs);
//	}
//	
//	private void upgrade(CAS aCas, TypeSystemDescription aTsd) throws Exception
//	{
//	    // Prepare template for new CAS
//	    CAS newCas = CasCreationUtils.createCas(aTsd, null, null);
//        CASCompleteSerializer serializer = Serialization.serializeCASComplete((CASImpl) newCas);
//	    
//        // Save old type system
//	    TypeSystem oldTypeSystem = aCas.getTypeSystem();
//	    
//	    // Save old CAS contents
//	    ByteArrayOutputStream os2 = new ByteArrayOutputStream();
//	    Serialization.serializeWithCompression(aCas, os2, oldTypeSystem);
//        
//        // Prepare CAS with new type system
//	    Serialization.deserializeCASComplete(serializer, (CASImpl) aCas);
//        
//        // Restore CAS data to new type system
//	    Serialization.deserializeCAS(aCas, new ByteArrayInputStream(os2.toByteArray()), oldTypeSystem, null);
//	}
//	
//    private void upgrade(CAS aCas) throws Exception
//    {
//        // Prepare template for new CAS
//        CAS newCas = JCasFactory.createJCas().getCas();
//        CASCompleteSerializer serializer = Serialization.serializeCASComplete((CASImpl) newCas);
//        
//        // Save old type system
//        TypeSystem oldTypeSystem = aCas.getTypeSystem();
//        
//        // Save old CAS contents
//        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
//        Serialization.serializeWithCompression(aCas, os2, oldTypeSystem);
//        
//        // Prepare CAS with new type system
//        Serialization.deserializeCASComplete(serializer, (CASImpl) aCas);
//        
//        // Restore CAS data to new type system
//        Serialization.deserializeCAS(aCas, new ByteArrayInputStream(os2.toByteArray()), oldTypeSystem, null);
//    }

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
