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

import static org.junit.Assert.assertArrayEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import java.util.LinkedList;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class MateMorphTaggerTest
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

		LinkedList<String> morphTagsExpected = new LinkedList<String>();
		morphTagsExpected.add("case=nom|number=pl|gender=*|person=1");
		morphTagsExpected.add("number=pl|person=1|tense=pres|mood=ind");
		morphTagsExpected.add("case=acc|number=sg|gender=neut");
		morphTagsExpected.add("_");
		morphTagsExpected.add("case=acc|number=sg|gender=neut|degree=pos");
		morphTagsExpected.add("case=acc|number=sg|gender=neut");
		morphTagsExpected.add("_");
		morphTagsExpected.add("case=acc|number=sg|gender=neut");
		morphTagsExpected.add("_");
		morphTagsExpected.add("case=acc|number=pl|gender=*");
		morphTagsExpected.add("case=acc|number=pl|gender=*");
		morphTagsExpected.add("_");
		morphTagsExpected.add("case=dat|number=pl|gender=fem");
		morphTagsExpected.add("_");
		morphTagsExpected.add("_");

		LinkedList<String> morphTagsActual = new LinkedList<String>();
		for (Morpheme morpheme : select(jcas, Morpheme.class)) {
			morphTagsActual.add(morpheme.getMorphTag());
		}

		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));

		System.out.printf("%-20s - Expected: %s%n", "MorphTags",
				AssertAnnotations.asCopyableString(morphTagsExpected, false));
		System.out.printf("%-20s - Actual  : %s%n", "MorphTags",
				AssertAnnotations.asCopyableString(morphTagsActual, false));
		assertArrayEquals(morphTagsExpected.toArray(new String[0]),
				morphTagsActual.toArray(new String[0]));

	}

	private JCas runTest(String aLanguage, String aText)
		throws Exception
	{
		AnalysisEngineDescription seg = createPrimitiveDescription(BreakIteratorSegmenter.class);
		AnalysisEngineDescription lemma = createPrimitiveDescription(MateLemmatizer.class);
		AnalysisEngineDescription morphTag = createPrimitiveDescription(MateMorphTagger.class);

		AnalysisEngineDescription aggregate = createAggregateDescription(seg, lemma, morphTag);

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
