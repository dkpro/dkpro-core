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

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_PATH;
import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_PATTERNS;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createCollectionReader;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.xwriter.CASDumpWriter;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
				PARAM_PATH, "classpath:/wsdl",
				PARAM_PATTERNS, new String[] { "[+]**/*.wsdl" });
		AnalysisEngine writer = createPrimitive(CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, tmpFile.getPath());
		SimplePipeline.runPipeline(reader, writer);

		String reference = readFileToString(new File("src/test/resources/reference/operations-new.txt")).trim();
		String output = readFileToString(tmpFile).trim();
		assertEquals(reference, output);
	}
}
