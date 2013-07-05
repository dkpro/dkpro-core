/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.io.graf;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.xwriter.CASDumpWriter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.testing.DocumentMetaDataStripper;

public class GrafWriterTest
{
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void test() throws Exception
	{
		write();
		//read();
	}

	public void write() throws Exception
	{
		CollectionReaderDescription textReader = createDescription(
				TextReader.class,
				TextReader.PARAM_LANGUAGE, "en",
				ResourceCollectionReaderBase.PARAM_PATH, "src/test/resources/texts",
				ResourceCollectionReaderBase.PARAM_PATTERNS, new String [] {
					ResourceCollectionReaderBase.INCLUDE_PREFIX+"*.txt"
				});

		AnalysisEngineDescription segmenter = createPrimitiveDescription(
				OpenNlpSegmenter.class);

		AnalysisEngineDescription posTagger = createPrimitiveDescription(
				OpenNlpPosTagger.class);

		AnalysisEngineDescription stripper = createPrimitiveDescription(
				DocumentMetaDataStripper.class);

		AnalysisEngineDescription grafWriter = createPrimitiveDescription(
				GrafWriter.class,
				GrafWriter.PARAM_PATH, testFolder.getRoot().getPath());

		AnalysisEngineDescription dumpWriter = createPrimitiveDescription(
				CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, "-");

		runPipeline(textReader, segmenter, posTagger, stripper, grafWriter, dumpWriter);

		File output = new File(testFolder.getRoot(), "example1.txt.xml");
		assertTrue(output.exists());

		String expected = readFileToString(new File("src/test/resources/reference/example1.txt.xml"), "UTF-8");
		String actual = readFileToString(output, "UTF-8");

		System.out.println(actual);

		assertEquals(expected, actual);
	}

//	public void read() throws Exception
//	{
//		CollectionReader xmiReader = CollectionReaderFactory.createCollectionReader(
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
}
