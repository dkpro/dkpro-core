package de.tudarmstadt.ukp.dkpro.core.io.bnc;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.junit.Test;
import org.uimafit.component.xwriter.CASDumpWriter;
import org.uimafit.pipeline.SimplePipeline;

public class BncReaderTest
{
	@Test
	public void test() throws Exception
	{
		CollectionReaderDescription reader = createDescription(BncReader.class, 
				BncReader.PARAM_PATH, "src/test/resources",
				BncReader.PARAM_PATTERNS, new String[] { "[+]*.xml" },
				BncReader.PARAM_LANGUAGE, "en");
		
		AnalysisEngineDescription casDumper = createPrimitiveDescription(CASDumpWriter.class, 
				CASDumpWriter.PARAM_OUTPUT_FILE, "target/test-output/dump.txt");
		
		SimplePipeline.runPipeline(reader, casDumper);
	}
}
