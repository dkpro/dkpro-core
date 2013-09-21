/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
public class ClearNlpLemmatizerTest
{
	@Test
	public void testEnglish()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1200000000l);

		JCas jcas = runTest("en", "We need a very complicated example sentence, which " +
			"contains as many constituents and dependencies as possible.");

		String[] lemmas = new String[] { "we", "need", "a", "very", "complicated", "example",
				"sentence", ",", "which", "contain", "as", "many", "constituent", "and",
				"dependency", "as", "possible", "." };

		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
	}

	private JCas runTest(String aLanguage, String aText)
		throws Exception
	{
		AnalysisEngineDescription seg = createEngineDescription(ClearNlpSegmenter.class);
		AnalysisEngineDescription tagger = createEngineDescription(ClearNlpPosTagger.class);
		AnalysisEngineDescription lemma = createEngineDescription(ClearNlpLemmatizer.class);

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
