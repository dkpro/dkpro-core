package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;

public class ClearNlpSegmenterTest
{
	@Test
	public void run() throws Throwable
	{
		AnalysisEngineDescription aed = createPrimitiveDescription(ClearNlpSegmenter.class);
		
		SegmenterHarness.run(aed, "de.1", "de.2", "de.3", "de.4", "en.1", "en.7", "en.8", "en.9",
				"ar.1", "zh.1", "zh.2");
	}
}
