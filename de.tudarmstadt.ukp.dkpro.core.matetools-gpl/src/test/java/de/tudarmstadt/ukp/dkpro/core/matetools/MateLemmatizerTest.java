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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class MateLemmatizerTest
{
	@Test
	public void testGerman()
		throws Exception
	{
		JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel, welches " +
				"möglichst viele Konstituenten und Dependenzen beinhaltet.");

		String[] lemmas = new String[] { "wir", "brauchen", "ein", "sehr", "kompliziert",
				"beispiel", "_", "welcher", "möglichst", "vieler", "konstituent", "und",
				"dependenz", "beinhalten", "_" };

		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
	}
	
	private JCas runTest(String aLanguage, String aText)
		throws Exception
	{
		AnalysisEngineDescription seg = createPrimitiveDescription(BreakIteratorSegmenter.class);
		AnalysisEngineDescription lemma = createPrimitiveDescription(MateLemmatizer.class);

		AnalysisEngineDescription aggregate = createAggregateDescription(seg, lemma);
		
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
