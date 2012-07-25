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
package de.tudarmstadt.ukp.dkpro.core.io.pdf;

import static de.tudarmstadt.ukp.dkpro.core.api.io.FileSetCollectionReaderBase.PARAM_PATH;
import static de.tudarmstadt.ukp.dkpro.core.api.io.FileSetCollectionReaderBase.PARAM_PATTERNS;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.uimafit.component.xwriter.CASDumpWriter;
import org.uimafit.pipeline.SimplePipeline;

/**
 * @author Richard Eckart de Castilho
 */
public class PdfReaderTest
{
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void test() throws Exception
	{
		File outputFile = new File(folder.getRoot(), "dump-output.txt");

		CollectionReader reader = createCollectionReader(PdfReader.class,
				PARAM_PATH, "src/test/resources/data",
				PARAM_PATTERNS, new String[] { "[+]**/*.pdf"  });

		AnalysisEngine writer = createPrimitive(CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, outputFile);

		SimplePipeline.runPipeline(reader, writer);

		String reference = readFileToString(new File("src/test/resources/reference/test.dump"), "UTF-8").trim();
		String actual = readFileToString(outputFile, "UTF-8").trim();

		assertEquals(reference, actual);
	}
}
