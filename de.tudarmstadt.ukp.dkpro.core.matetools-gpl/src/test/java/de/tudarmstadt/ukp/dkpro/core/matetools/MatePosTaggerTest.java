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
package de.tudarmstadt.ukp.dkpro.core.matetools;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class MatePosTaggerTest
{
	@Test
	public void testGerman()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 1000000000);

		JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel, welches "
				+ "möglichst viele Konstituenten und Dependenzen beinhaltet.");

		String[] lemmas = new String[] { "wir", "brauchen", "ein", "sehr", "kompliziert",
				"Beispiel", "--", "welcher", "möglichst", "vieler", "Konstituent", "und",
				"Dependenz", "beinhalten", "--" };

		String[] posOriginal = new String[] { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,",
				"PRELS", "ADV", "PIAT", "NN", "KON", "NN", "VVFIN", "$." };

		String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "ADJ", "NN", "PUNC", "PR",
				"ADV", "PR", "NN", "CONJ", "NN", "V", "PUNC" };

		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
	}

	private JCas runTest(String aLanguage, String aText)
		throws Exception
	{
		AnalysisEngineDescription seg = createPrimitiveDescription(BreakIteratorSegmenter.class);
		AnalysisEngineDescription lemma = createPrimitiveDescription(MateLemmatizer.class);
		AnalysisEngineDescription posTag = createPrimitiveDescription(MatePosTagger.class);

		AnalysisEngineDescription aggregate = createAggregateDescription(seg, lemma, posTag);

		AnalysisEngine engine = createPrimitive(aggregate);
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
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
