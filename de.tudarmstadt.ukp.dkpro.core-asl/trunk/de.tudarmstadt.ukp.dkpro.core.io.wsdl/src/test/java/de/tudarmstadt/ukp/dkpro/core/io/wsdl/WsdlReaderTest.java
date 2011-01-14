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
package de.tudarmstadt.ukp.dkpro.core.io.wsdl;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.*;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.uimafit.component.xwriter.CASDumpWriter;
import org.uimafit.pipeline.SimplePipeline;

public class WsdlReaderTest
{
	@Rule
	public TemporaryFolder workdir = new TemporaryFolder();

	@Test
	public void testServices()
		throws Exception
	{
		File tmpFile = workdir.newFile("services-new.txt");

		CollectionReader reader = createCollectionReader(WsdlReader.class,
				PARAM_PATH, "src/test/resources/wsdl",
				PARAM_PATTERNS, new String[] { "[+]**/*.wsdl" });
		AnalysisEngine writer = createPrimitive(CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, tmpFile.getPath());
		SimplePipeline.runPipeline(reader, writer);

		String reference = readFileToString(new File("src/test/resources/reference/services-new.txt")).trim();
		String output = readFileToString(tmpFile).trim();
		assertEquals(reference, output);
	}

	@Test
	public void testOperations()
		throws Exception
	{
		File tmpFile = workdir.newFile("operations-new.txt");

		CollectionReader reader = createCollectionReader(WsdlReader.class,
				WsdlReader.PARAM_OPERATION_AS_DOCID, true,
				PARAM_PATH, "src/test/resources/wsdl",
				PARAM_PATTERNS, new String[] { "[+]**/*.wsdl" });
		AnalysisEngine writer = createPrimitive(CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, tmpFile.getPath());
		SimplePipeline.runPipeline(reader, writer);

		String reference = readFileToString(new File("src/test/resources/reference/operations-new.txt")).trim();
		String output = readFileToString(tmpFile).trim();
		assertEquals(reference, output);
	}
}
