package de.tudarmstadt.ukp.dkpro.core.ldweb1t;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.frequency.resources.Web1TInMemoryFrequencyCountResource;
import de.tudarmstadt.ukp.dkpro.core.ldweb1t.LanguageDetectorWeb1T;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class LanguageDetectorWeb1TTest {

	@Ignore
	@Test
	public void web1tLanguageDetectorTeset() throws Exception {
		
		ExternalResourceDescription en = ExternalResourceFactory.createExternalResourceDescription(
				Web1TInMemoryFrequencyCountResource.class,
                Web1TInMemoryFrequencyCountResource.PARAM_MODEL_LOCATION, "src/test/resources/web1t/en/",
                Web1TInMemoryFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, "2");
		
		ExternalResourceDescription de = ExternalResourceFactory.createExternalResourceDescription(
				Web1TInMemoryFrequencyCountResource.class,
                Web1TInMemoryFrequencyCountResource.PARAM_MODEL_LOCATION, "src/test/resources/web1t/de/",
                Web1TInMemoryFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, "2");
		
		List<ExternalResourceDescription> resources = new ArrayList<ExternalResourceDescription>();
		resources.add(en);
		resources.add(de);
		
		AnalysisEngine engine = AnalysisEngineFactory.createEngine(
				AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
				AnalysisEngineFactory.createEngineDescription(
					LanguageDetectorWeb1T.class,
					LanguageDetectorWeb1T.PARAM_MAX_NGRAM_SIZE, 2,
					LanguageDetectorWeb1T.PARAM_FREQUENCY_PROVIDER_RESOURCES, resources.toArray()
		));
		
		JCas jcas = engine.newJCas();
		jcas.setDocumentText("This is an English example.");
		engine.process(jcas);
		
		assertEquals("en", jcas.getDocumentLanguage());
	}
}
