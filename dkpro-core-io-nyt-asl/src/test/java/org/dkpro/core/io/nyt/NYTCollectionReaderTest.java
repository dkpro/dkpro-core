package org.dkpro.core.io.nyt;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.dkpro.core.io.nyt.NYTCollectionReader;
import org.junit.Test;


public class NYTCollectionReaderTest {

	@Test
	public void test() {
		
		final String DATA_PATH = "src/test/resources/data";
		
		try {

			final TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory
					.createTypeSystemDescription("desc.type.NYTArticleMetaData");

			CollectionReader articleReader = CollectionReaderFactory.createReader(NYTCollectionReader.class,
					typeSystemDescription, 
					NYTCollectionReader.PARAM_DATA_PATH, DATA_PATH,
					NYTCollectionReader.PARAM_OFFSET, 0,
					NYTCollectionReader.PARAM_LIMIT, 2);

			AnalysisEngine extractor = AnalysisEngineFactory.createEngine(CasDumpWriter.class,
					CasDumpWriter.PARAM_OUTPUT_FILE, "-");

			SimplePipeline.runPipeline(articleReader, extractor);

		} catch (IOException | ResourceInitializationException e) {
			e.printStackTrace();
			fail();
		} catch (UIMAException e) {
			e.printStackTrace();
			fail();
		}
	}

}