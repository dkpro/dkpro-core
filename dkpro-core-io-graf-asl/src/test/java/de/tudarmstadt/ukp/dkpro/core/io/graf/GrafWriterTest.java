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
package de.tudarmstadt.ukp.dkpro.core.io.graf;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Rule;
import org.junit.Test;
import org.xml.sax.InputSource;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.DocumentMetaDataStripper;

public class GrafWriterTest
{
	@Test
	public void test() throws Exception
	{
		write();
		//read();
	}

	public void write() throws Exception
	{
	    File targetFolder = testContext.getTestOutputFolder();
	    
		CollectionReaderDescription textReader = createReaderDescription(
				TextReader.class,
				TextReader.PARAM_LANGUAGE, "en",
				ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, "src/test/resources/texts",
				ResourceCollectionReaderBase.PARAM_PATTERNS, new String [] {
					ResourceCollectionReaderBase.INCLUDE_PREFIX+"*.txt"
				});

		AnalysisEngineDescription segmenter = createEngineDescription(
				OpenNlpSegmenter.class);

		AnalysisEngineDescription posTagger = createEngineDescription(
				OpenNlpPosTagger.class);

		AnalysisEngineDescription stripper = createEngineDescription(
				DocumentMetaDataStripper.class);

		AnalysisEngineDescription grafWriter = createEngineDescription(
				GrafWriter.class,
				GrafWriter.PARAM_TARGET_LOCATION, targetFolder);

		runPipeline(textReader, segmenter, posTagger, stripper, grafWriter);

		File output = new File(targetFolder, "example1.txt.xml");
		assertTrue(output.exists());

        Diff myDiff = new Diff(
                new InputSource("src/test/resources/reference/example1.txt.xml"),
                new InputSource(output.getPath()));
        myDiff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual(myDiff, true);     
	}

//	public void read() throws Exception
//	{
//		CollectionReader xmiReader = CollectionReaderFactory.createReader(
//				XmiReader.class,
//				ResourceCollectionReaderBase.PARAM_PATH, testFolder.getRoot().getPath(),
//				ResourceCollectionReaderBase.PARAM_PATTERNS, new String [] {
//					ResourceCollectionReaderBase.INCLUDE_PREFIX+"*.xmi"
//				});
//
//		CAS cas = CasCreationUtils.createCas(createTypeSystemDescription(), null, null);
//		xmiReader.getNext(cas);
//
//		String refText = readFileToString(new File("src/test/resources/texts/example1.txt"));
//		assertEquals(refText, cas.getDocumentText());
//		assertEquals("latin", cas.getDocumentLanguage());
//	}
	
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
