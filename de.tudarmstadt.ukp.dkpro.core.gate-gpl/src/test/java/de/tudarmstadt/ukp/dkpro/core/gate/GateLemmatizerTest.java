/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.gate;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class GateLemmatizerTest
{
	@Test
	public void testEnglish()
		throws Exception
	{
		JCas jcas = runTest("en", "We need a very complicated example sentence, which " +
			"contains as many constituents and dependencies as possible .");

		String[] lemmas = new String[] { "We", "need", "a", "very", "complicate", "example",
				"sentence", ",", "which", "contain", "as", "many", "constituent", "and",
				"dependency", "as", "possible", "." };

		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
	}

	@Test
	public void testEnglish2()
		throws Exception
	{
		JCas jcas = runTest("en", "Two cars went around corners .");

		String[] lemmas = new String[] { "Two", "car", "go", "around", "corner", "." };

		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
	}

	private JCas runTest(String aLanguage, String aText)
		throws Exception
	{
		AnalysisEngineDescription seg = createEngineDescription(OpenNlpSegmenter.class);
		AnalysisEngineDescription tagger = createEngineDescription(OpenNlpPosTagger.class);
		AnalysisEngineDescription lemma = createEngineDescription(GateLemmatizer.class);

		AnalysisEngineDescription aggregate = createEngineDescription(seg, tagger, lemma);

		AnalysisEngine engine = createEngine(aggregate);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage(aLanguage);
		jcas.setDocumentText(aText);
		engine.process(jcas);

		return jcas;
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		Runtime.getRuntime().gc();
		Runtime.getRuntime().gc();
		Runtime.getRuntime().gc();

		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
