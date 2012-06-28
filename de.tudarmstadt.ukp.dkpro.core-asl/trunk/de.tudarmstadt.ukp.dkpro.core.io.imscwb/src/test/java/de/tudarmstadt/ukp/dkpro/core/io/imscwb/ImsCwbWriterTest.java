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
 ******************************************************************************/package de.tudarmstadt.ukp.dkpro.core.io.imscwb;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static org.uimafit.pipeline.SimplePipeline.runPipeline;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.component.xwriter.CASDumpWriter;

import de.tudarmstadt.ukp.dkpro.core.io.bnc.BncReader;
import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;

/**
 *
 * @author Erik-Lân Do Dinh
 *
 */
public class ImsCwbWriterTest
{
	private static final String outputFile = "target/corpus-sample.ims";

	@Test
	public void test1()
		throws Exception
	{
		CollectionReader ner = createCollectionReader(
				NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, "src/test/resources/corpus-sample.export",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "UTF-8");

		AnalysisEngineDescription tag = createPrimitiveDescription(
				OpenNlpPosTagger.class);

		AnalysisEngineDescription tw = createPrimitiveDescription(
				ImsCwbWriter.class,
				ImsCwbWriter.PARAM_TARGET_LOCATION, outputFile,
				ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8");

		AnalysisEngineDescription cdw = createPrimitiveDescription(
				CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, "target/dump.txt");

		runPipeline(ner, tag, tw, cdw);

		String reference = FileUtils.readFileToString(
				new File("src/test/resources/reference/corpus-sample.ims"), "UTF-8");
		String actual = FileUtils.readFileToString(
				new File(outputFile), "UTF-8");
		assertEquals(reference, actual);
	}

	@Ignore("FX8 is a file from the BNC. While available online for download, we currently do "
			+ "ship it due to licensing issues.")
	@Test
	public void test1a()
		throws Exception
	{
		CollectionReader ner = createCollectionReader(
				BncReader.class,
				BncReader.PARAM_PATH, "src/test/resources",
				BncReader.PARAM_PATTERNS, new String[] { "[+]FX8.xml" },
				BncReader.PARAM_LANGUAGE, "en");

		AnalysisEngineDescription tw = createPrimitiveDescription(
				ImsCwbWriter.class,
				ImsCwbWriter.PARAM_TARGET_LOCATION, outputFile,
				ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8");

		AnalysisEngineDescription cdw = createPrimitiveDescription(
				CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, "target/dump.txt");

		runPipeline(ner, tw, cdw);

		String reference = FileUtils.readFileToString(
				new File("src/test/resources/reference/bnc-sample.ims"), "UTF-8");
		String actual = FileUtils.readFileToString(
				new File(outputFile), "UTF-8");
		assertEquals(reference, actual);
	}

	@Ignore("This test cannot work (yet) because we do not ship the cwb-encode and cwb-makeall binaries")
	@Test
	public void test2()
		throws Exception
	{
		CollectionReader ner = createCollectionReader(
				NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, "src/test/resources/corpus-sample.export",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "UTF-8");

		AnalysisEngineDescription tag = createPrimitiveDescription(
				OpenNlpPosTagger.class);

		AnalysisEngineDescription tw = createPrimitiveDescription(
				ImsCwbWriter.class,
				ImsCwbWriter.PARAM_TARGET_LOCATION, "target/cqbformat",
				ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8",
				ImsCwbWriter.PARAM_CQP_HOME, "/Users/bluefire/bin/cwb-2.2.b99");

		runPipeline(ner, tag, tw);
	}
}
