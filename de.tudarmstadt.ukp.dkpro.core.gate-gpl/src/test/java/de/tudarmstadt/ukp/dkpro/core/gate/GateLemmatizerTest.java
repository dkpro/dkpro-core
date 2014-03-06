/**
 * Copyright 2007-2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
