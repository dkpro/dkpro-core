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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.gate;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class GateLemmatizerTest
{
	@Test
	public void testEnglish()
		throws Exception
	{
		JCas jcas = runTest("en", "We need a very complicated example sentence , which " +
			"contains as many constituents and dependencies as possible .");

        String[] lemmas = { "We", "need", "a", "very", "complicate", "example", "sentence", ",",
                "which", "contain", "as", "many", "constituent", "and", "dependency", "as",
                "possible", "." };

		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
	}

	@Test
	public void testEnglish2()
		throws Exception
	{
		JCas jcas = runTest("en", "Two cars went around corners .");

		String[] lemmas = { "Two", "car", "go", "around", "corner", "." };

		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
	}

	private JCas runTest(String aLanguage, String aText)
		throws Exception
	{
		AnalysisEngineDescription tagger = createEngineDescription(HepplePosTagger.class);
		AnalysisEngineDescription lemma = createEngineDescription(GateLemmatizer.class);

		AnalysisEngineDescription aggregate = createEngineDescription(tagger, lemma);

        JCas jcas = TestRunner.runTest(aggregate, aLanguage, aText);
		
		return jcas;
	}

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
