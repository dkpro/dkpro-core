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
package de.tudarmstadt.ukp.dkpro.core.examples.example1;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.DictionaryAnnotator;
import de.tudarmstadt.ukp.dkpro.core.examples.type.Name;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class NameAnnotationPipeline
{
	public static void main(String[] args)
		throws Exception
	{
		CollectionReaderDescription reader = createReaderDescription(
				TextReader.class,
				TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/text",
				TextReader.PARAM_PATTERNS, "[+]*.txt",
				TextReader.PARAM_LANGUAGE, "en");

		AnalysisEngineDescription tokenizer = createEngineDescription(
				BreakIteratorSegmenter.class);

		AnalysisEngineDescription nameFinder = createEngineDescription(
				DictionaryAnnotator.class,
				DictionaryAnnotator.PARAM_MODEL_LOCATION, "src/test/resources/dictionaries/names.txt",
				DictionaryAnnotator.PARAM_ANNOTATION_TYPE, Name.class);

		AnalysisEngineDescription writer = createEngineDescription(
				CasDumpWriter.class,
				CasDumpWriter.PARAM_OUTPUT_FILE, "target/output.txt");

		SimplePipeline.runPipeline(reader, tokenizer, nameFinder, writer);
	}
}
