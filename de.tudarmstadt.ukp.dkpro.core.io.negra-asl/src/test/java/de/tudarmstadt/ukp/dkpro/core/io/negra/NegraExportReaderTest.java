/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.io.negra;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.xwriter.CasDumpWriter;
import org.junit.Test;

/**
 *
 * Sample is taken from
 * http://www.coli.uni-saarland.de/projects/sfb378/negra-corpus
 * /corpus-sample.export Only the second sentence is used.
 *
 * @author Erik-Lân Do Dinh
 *
 */
public class NegraExportReaderTest
{
	@Test
	public void negraTest()
		throws Exception
	{
		File testDump = new File("target/sentence.export.dump");
		File referenceDump = new File("src/test/resources/sentence.export.dump");

		// create NegraExportReader output
		CollectionReader ner = createReader(NegraExportReader.class,
				NegraExportReader.PARAM_SOURCE_LOCATION, new File("src/test/resources/sentence.export"),
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_READ_PENN_TREE, true);

		AnalysisEngineDescription cdw = createEngineDescription(CasDumpWriter.class,
				CasDumpWriter.PARAM_OUTPUT_FILE, testDump.getPath());

		runPipeline(ner, cdw);

		// compare both dumps
		String reference = readFileToString(referenceDump, "UTF-8").trim();
		String test = readFileToString(testDump, "UTF-8").trim();

		assertEquals(reference, test);
	}

	@Test
	public void negraTigerTest()
		throws Exception
	{
		File testDump = new File("target/tiger-sample.export.dump");
		File referenceDump = new File("src/test/resources/tiger-sample.export.dump");

		// create NegraExportReader output
		CollectionReader ner = createReader(NegraExportReader.class,
				NegraExportReader.PARAM_SOURCE_LOCATION, "src/test/resources/tiger-sample.export",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "ISO-8859-15",
                NegraExportReader.PARAM_READ_PENN_TREE, true);

		AnalysisEngineDescription cdw = createEngineDescription(CasDumpWriter.class,
				CasDumpWriter.PARAM_OUTPUT_FILE, testDump.getPath());

		runPipeline(ner, cdw);

		// compare both dumps
		String reference = readFileToString(referenceDump, "UTF-8").trim();
		String test = readFileToString(testDump, "UTF-8").trim();

		assertEquals(reference, test);
	}

	@Test
	public void tuebaTest()
		throws Exception
	{
		File testDump = new File("target/tueba-sample.export.dump");
		File referenceDump = new File("src/test/resources/tueba-sample.export.dump");

		// create NegraExportReader output
		CollectionReader ner = createReader(NegraExportReader.class,
				NegraExportReader.PARAM_SOURCE_LOCATION, "src/test/resources/tueba-sample.export",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "UTF-8",
                NegraExportReader.PARAM_READ_PENN_TREE, true);

		AnalysisEngineDescription cdw = createEngineDescription(CasDumpWriter.class,
				CasDumpWriter.PARAM_OUTPUT_FILE, testDump.getPath());

		runPipeline(ner, cdw);

		// compare both dumps
		String reference = readFileToString(referenceDump, "UTF-8").trim();
		String test = readFileToString(testDump, "UTF-8").trim();

		assertEquals(reference, test);
	}
}
