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
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class MateParserTest
{
	@Test
	public void testGerman()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

		JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel, welches " +
				"möglichst viele Konstituenten und Dependenzen beinhaltet.");

		String[] lemmas = new String[] { "wir", "brauchen", "ein", "sehr", "kompliziert",
				"beispiel", "_", "welcher", "möglichst", "vieler", "konstituent", "und",
				"dependenz", "beinhalten", "_" };

		String[] posOriginal = new String[] { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,",
				"PRELS", "ADV", "PIAT", "NN", "KON", "NN", "VVFIN", "$." };

		String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "ADJ", "NN", "PUNC", "PR",
				"ADV", "PR", "NN", "CONJ", "NN", "V", "PUNC" };

		String[] dependencies = new String[] { "CD 70,83,84,87", "CJ 70,83,88,99",
				"MO 22,35,17,21", "MO 64,69,54,63", "NK 36,44,13,16", "NK 36,44,22,35",
				"NK 70,83,64,69", "OA 100,110,70,83", "OA 4,12,36,44", "PUNC 36,44,44,45",
				"PUNC 4,12,110,111", "RC 36,44,100,110", "SB 100,110,46,53", "SB 4,12,0,3" };

		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}
	
	private JCas runTest(String aLanguage, String aText)
		throws Exception
	{
		AnalysisEngineDescription aggregate = createAggregateDescription(
		        createPrimitiveDescription(BreakIteratorSegmenter.class),
		        createPrimitiveDescription(MateLemmatizer.class),
		        createPrimitiveDescription(MatePosTagger.class),
		        createPrimitiveDescription(MateParser.class)
		);

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
