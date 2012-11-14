/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-2.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.tgrep;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.uimafit.factory.JCasFactory;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

public class TGrepWriterTest
{
	private File outputPath = new File("src/test/resources/tgrep");

	@Test
	public void testTxt()
		throws Exception
	{
		String language = "en";
		String text = "This is a sample sentence. Followed by another one.";
		AnalysisEngineDescription seg = createPrimitiveDescription(StanfordSegmenter.class);

		AnalysisEngineDescription parse = createPrimitiveDescription(StanfordParser.class,
				StanfordParser.PARAM_CREATE_PENN_TREE_STRING, true,
				StanfordParser.PARAM_LANGUAGE, "en",
				StanfordParser.PARAM_VARIANT, "pcfg");

		AnalysisEngineDescription tgrep = createPrimitiveDescription(TGrepWriter.class,
				TGrepWriter.PARAM_TARGET_LOCATION, outputPath,
				TGrepWriter.PARAM_COMPRESSION, CompressionMethod.GZIP,
				TGrepWriter.PARAM_DROP_MALFORMED_TREES, true,
				TGrepWriter.PARAM_WRITE_COMMENTS, true,
				TGrepWriter.PARAM_WRITE_T2C, false);

		JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentLanguage(language);
		jcas.setDocumentText(text);
		DocumentMetaData meta = DocumentMetaData.create(jcas);
		meta.setCollectionId("testCollection");
		meta.setDocumentId("testDocument");

		SimplePipeline.runPipeline(jcas, seg, parse, tgrep);

		List<String> expected = new ArrayList<String>();
		expected.add("# testDocument,0,26");
		expected.add("(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (DT a) (NN sample) (NN sentence))) (. .)))");
		expected.add("# testDocument,27,51");
		expected.add("(ROOT (S (VP (VBN Followed) (PP (IN by) (NP (DT another) (NN one)))) (. .)))");
		List<String> actual = FileUtils.readLines(new File(outputPath, "testCollection.txt"), "UTF-8");

		Assert.assertEquals(expected.size(), actual.size());

		for (int i = 0; i < actual.size(); i++) {
			Assert.assertEquals(expected.get(i), actual.get(i));
		}
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
	
	@After
	public void cleanUp()
	{
		FileUtils.deleteQuietly(outputPath);
	}
}