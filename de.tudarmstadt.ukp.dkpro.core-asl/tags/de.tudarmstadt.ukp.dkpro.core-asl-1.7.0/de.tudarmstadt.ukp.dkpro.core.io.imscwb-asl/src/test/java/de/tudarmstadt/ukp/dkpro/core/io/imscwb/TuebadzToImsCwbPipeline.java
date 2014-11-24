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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;

/**
 *
 * @author Erik-Lân Do Dinh
 */
@Ignore("This is to convert the actual corpus!")
public class TuebadzToImsCwbPipeline
{
	private static final String inputFile = "src/main/resources/tuebadz.export";
	private static final String outputFile = "target/tuebadz.ims.xml";

	@Test
	public void convert()
		throws Exception
	{
		CollectionReader ner = createReader(
				NegraExportReader.class,
				NegraExportReader.PARAM_SOURCE_LOCATION, inputFile,
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "ISO-8859-1");

		AnalysisEngineDescription tag = createEngineDescription(
				OpenNlpPosTagger.class);

		AnalysisEngineDescription tw = createEngineDescription(
				ImsCwbWriter.class,
				ImsCwbWriter.PARAM_TARGET_LOCATION, outputFile,
				ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8");

		runPipeline(ner, tag, tw);
	}
}
