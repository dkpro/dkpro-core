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
package de.tudarmstadt.ukp.dkpro.core.io.annis;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;

public class RelAnnisWriterTest
{
	@Rule
	public TemporaryFolder workspace = new TemporaryFolder();
	
	@Test
	public void tuebaTest()
		throws Exception
	{
		// create NegraExportReader output
		CollectionReaderDescription reader = createDescription(NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, "src/test/resources/tueba/input/tueba-sample.export",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "UTF-8");
		
		AnalysisEngineDescription writer = createPrimitiveDescription(RelAnnisWriter.class, 
				RelAnnisWriter.PARAM_PATH, workspace.getRoot().getPath());

		SimplePipeline.runPipeline(reader, writer);
		
		// Check if the output matches the reference output
		for (File f : workspace.getRoot().listFiles()) {
			System.out.print("Checking ["+f.getName()+"]... ");
			assertEquals(
					readFileToString(new File("src/test/resources/tueba/reference", f.getName()), "UTF-8"), 
					readFileToString(f, "UTF-8"));
			System.out.println("ok.");
		}
	}
}
