package de.tudarmstadt.ukp.dkpro.core.examples.example1;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
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
		CollectionReader reader = createCollectionReader(TextReader.class,
				TextReader.PARAM_PATH, "src/test/resources/text",
				TextReader.PARAM_PATTERNS, new String[] { INCLUDE_PREFIX + "*.txt" },
				TextReader.PARAM_LANGUAGE, "en");

		AnalysisEngine tokenizer = createPrimitive(BreakIteratorSegmenter.class);

		AnalysisEngine nameFinder = createPrimitive(DictionaryAnnotator.class,
				DictionaryAnnotator.PARAM_PHRASE_FILE, "src/test/resources/dictionaries/names.txt",
				DictionaryAnnotator.PARAM_ANNOTATION_TYPE, Name.class.getName());

		AnalysisEngine writer = createPrimitive(CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, "target/output.txt");

		SimplePipeline.runPipeline(reader, tokenizer, nameFinder, writer);
	}
}
