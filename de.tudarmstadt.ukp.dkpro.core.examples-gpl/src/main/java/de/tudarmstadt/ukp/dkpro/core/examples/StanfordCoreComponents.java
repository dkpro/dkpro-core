package de.tudarmstadt.ukp.dkpro.core.examples;

import java.io.IOException;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

public class StanfordCoreComponents {

	public static void main(String[] args) throws UIMAException, IOException {
		CollectionReader reader = createReader(
				TextReader.class,
				TextReader.PARAM_SOURCE_LOCATION, "src/main/resources",
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, new String[] { "[+]*.txt" });

		AnalysisEngineDescription seg = createEngineDescription(StanfordSegmenter.class);

		AnalysisEngineDescription ner = createEngineDescription(StanfordNamedEntityRecognizer.class);

		AnalysisEngineDescription parser = createEngineDescription(StanfordParser.class);

		AnalysisEngineDescription writer = createEngineDescription(NPNEWriter.class);

		SimplePipeline.runPipeline(reader, seg, ner, parser, writer);
	} /* main() */
} /* class */
