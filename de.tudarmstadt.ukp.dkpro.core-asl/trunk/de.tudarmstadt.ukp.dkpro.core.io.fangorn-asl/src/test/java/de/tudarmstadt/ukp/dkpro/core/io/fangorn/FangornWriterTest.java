package de.tudarmstadt.ukp.dkpro.core.io.fangorn;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.factory.JCasFactory;
import org.uimafit.pipeline.SimplePipeline;

import au.edu.unimelb.csse.queryParser.QueryBuilder;
import au.edu.unimelb.csse.search.SimpleHitCollector;
import au.edu.unimelb.csse.search.TreebankQuery;
import au.edu.unimelb.csse.search.complete.AllResults;
import au.edu.unimelb.csse.search.complete.Result;
import au.edu.unimelb.csse.search.join.TermJoinType;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class FangornWriterTest
{
	@Test
	public void test()
		throws Exception
	{
		File outputFile = new File("target/test-output");

		JCas jcas = JCasFactory.createJCas();

		jcas.setDocumentLanguage("en");
		jcas.setDocumentText("This is a test. I may work. Or it may not work.");

		DocumentMetaData meta = DocumentMetaData.create(jcas);
		meta.setCollectionId("dummyCollection");
		meta.setDocumentId("dummyId");
		
		AnalysisEngineDescription segmenter = createPrimitiveDescription(OpenNlpSegmenter.class);

		AnalysisEngineDescription parser = createPrimitiveDescription(OpenNlpParser.class,
				OpenNlpParser.PARAM_CREATE_PENN_TREE_STRING, true);

		AnalysisEngineDescription writer = createPrimitiveDescription(FangornWriter.class,
				FangornWriter.PARAM_TARGET_LOCATION, outputFile);

		SimplePipeline.runPipeline(jcas, segmenter, parser, writer);

		IndexSearcher searcher = new IndexSearcher(FSDirectory.getDirectory(outputFile));
		QueryBuilder builder = new QueryBuilder("//NP");
		TreebankQuery tq = builder.parse(TermJoinType.SIMPLE_WITH_FC, false);
		SimpleHitCollector hitCollector = new SimpleHitCollector(100);
		searcher.search(tq, hitCollector);
		AllResults allResults = new AllResults(hitCollector.hits, hitCollector.totalHits, tq);

		Result[] resultMeta = allResults.collect(searcher);

		String[] results = new String[hitCollector.totalHits];
		for (int i = 0; i < hitCollector.totalHits; i++) {
			results[i] = searcher.doc(hitCollector.hits[i]).get("sent").trim();
		}

		List<String> actual = new ArrayList<String>();
		
		for (int i = 0; i < hitCollector.totalHits; i++) {
			Document doc = searcher.doc(hitCollector.hits[i]);
			actual.add(String.format("%s %s %s %s %s",
					doc.get(FangornWriter.FIELD_COLLECTION_ID),
					doc.get(FangornWriter.FIELD_DOCUMENT_ID),
					doc.get(FangornWriter.FIELD_BEGIN),
					doc.get(FangornWriter.FIELD_END),
					resultMeta[i].asJSONString().replace('"', '\'')));
		}
		
		List<String> expected = asList(
				"dummyCollection dummyId 0 15 {'num':'2','ms':[{'m':[{'s':'','e':'1_0_2_8','o':'0','t':'0'}]},{'m':[{'s':'','e':'4_2_3_6','o':'0','t':'0'}]}]}",
				"dummyCollection dummyId 16 27 {'num':'1','ms':[{'m':[{'s':'','e':'1_0_2_7','o':'0','t':'0'}]}]}",
				"dummyCollection dummyId 28 47 {'num':'1','ms':[{'m':[{'s':'','e':'2_1_2_9','o':'0','t':'0'}]}]}");
		
		assertEquals(StringUtils.join(expected, "\n"), StringUtils.join(actual, "\n"));
	}
}
