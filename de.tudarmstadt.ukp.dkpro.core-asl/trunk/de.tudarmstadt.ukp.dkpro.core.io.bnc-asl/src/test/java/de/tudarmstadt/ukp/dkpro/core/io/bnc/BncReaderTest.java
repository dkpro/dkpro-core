/*******************************************************************************
 * Copyright 2011
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit√§t Darmstadt
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
package de.tudarmstadt.ukp.dkpro.core.io.bnc;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.xwriter.CasDumpWriter;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Test;

public class BncReaderTest
{
	@Test
	public void test() throws Exception
	{
		CollectionReaderDescription reader = createReaderDescription(BncReader.class,
				BncReader.PARAM_PATH, "src/test/resources",
				BncReader.PARAM_PATTERNS, new String[] { "[+]FX8.xml" },
				BncReader.PARAM_LANGUAGE, "en");

		AnalysisEngineDescription casDumper = createEngineDescription(CasDumpWriter.class,
				CasDumpWriter.PARAM_OUTPUT_FILE, "target/test-output/FX8.dump");

		SimplePipeline.runPipeline(reader, casDumper);

		String reference = FileUtils.readFileToString(
				new File("src/test/resources/reference/FX8.dump"), "UTF-8");
		String actual = FileUtils.readFileToString(
				new File("target/test-output/FX8.dump"), "UTF-8");
		assertEquals(reference, actual);
	}
}
