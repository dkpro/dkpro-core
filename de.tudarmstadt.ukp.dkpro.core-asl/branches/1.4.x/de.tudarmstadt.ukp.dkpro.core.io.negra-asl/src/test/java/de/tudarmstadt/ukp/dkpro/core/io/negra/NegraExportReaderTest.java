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
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static org.uimafit.pipeline.SimplePipeline.runPipeline;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.junit.Test;
import org.uimafit.component.xwriter.CASDumpWriter;

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
		CollectionReader ner = createCollectionReader(NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, new File("src/test/resources/sentence.export"),
				NegraExportReader.PARAM_LANGUAGE, "de");

		AnalysisEngineDescription cdw = createPrimitiveDescription(CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, testDump.getPath());

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
		CollectionReader ner = createCollectionReader(NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, "src/test/resources/tiger-sample.export",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "ISO-8859-15");

		AnalysisEngineDescription cdw = createPrimitiveDescription(CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, testDump.getPath());

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
		CollectionReader ner = createCollectionReader(NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, "src/test/resources/tueba-sample.export",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "UTF-8"
		);

		AnalysisEngineDescription cdw = createPrimitiveDescription(CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, testDump.getPath());

		runPipeline(ner, cdw);

		// compare both dumps
		String reference = readFileToString(referenceDump, "UTF-8").trim();
		String test = readFileToString(testDump, "UTF-8").trim();

		assertEquals(reference, test);
	}
}
