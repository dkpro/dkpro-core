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

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.uimafit.component.xwriter.CASDumpWriter;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.DictionaryAnnotator;
import de.tudarmstadt.ukp.dkpro.core.examples.type.Name;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class NameAnnotationPipeline
{
	public static void main(String[] args)
		throws Exception
	{
		CollectionReaderDescription reader = createDescription(
				TextReader.class,
				TextReader.PARAM_PATH, "src/test/resources/text",
				TextReader.PARAM_PATTERNS, new String[] { INCLUDE_PREFIX + "*.txt" },
				TextReader.PARAM_LANGUAGE, "en");

		AnalysisEngineDescription tokenizer = createPrimitiveDescription(
				BreakIteratorSegmenter.class);

		AnalysisEngineDescription nameFinder = createPrimitiveDescription(
				DictionaryAnnotator.class,
				DictionaryAnnotator.PARAM_PHRASE_FILE, "src/test/resources/dictionaries/names.txt",
				DictionaryAnnotator.PARAM_ANNOTATION_TYPE, Name.class.getName());

		AnalysisEngineDescription writer = createPrimitiveDescription(
				CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, "target/output.txt");

		SimplePipeline.runPipeline(reader, tokenizer, nameFinder, writer);
	}
}
