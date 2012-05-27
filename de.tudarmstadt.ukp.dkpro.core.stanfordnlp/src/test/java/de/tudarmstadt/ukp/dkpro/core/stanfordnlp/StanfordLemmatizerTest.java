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
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class StanfordLemmatizerTest
{
	@Test
	public void testEnglish() throws Exception
	{
        runTest("en", "bidirectional-distsim-wsj-0-18", "This is a test .",
        		new String[] { "DT", "VBZ", "DT", "NN",  "."    },
        		new String[] { "this",  "be",  "a", "test", "." });

	}

	/**
	 * There seems to be a bug in the lemmatizer code causing an NPE when a token ends in a dash.
	 * Should this bug go away some day, this test will fail. When somebody uses the
	 * {@link StanfordLemmatizer}, a workaround is usually enabled, which we explicitly disable in
	 * this test case ({@link StanfordLemmatizer#PARAM_DASH_BUG_WORKAROUND});
	 */
	@Test(expected=AnalysisEngineProcessException.class)
	public void testEnglishEndingWithDash() throws Exception
	{
        runTest("en", "bidirectional-distsim-wsj-0-18", "b-",
        		null,
        		new String[] { "b-" });
	}

	private void runTest(String aLanguage, String aVariant, String testDocument, String[] tags,
			String[] lemmas)
		throws Exception
	{
		AnalysisEngineDescription posTagger = createPrimitiveDescription(StanfordPosTagger.class,
				StanfordPosTagger.PARAM_VARIANT, aVariant);

		AnalysisEngineDescription lemmatizer = createPrimitiveDescription(StanfordLemmatizer.class,
				StanfordLemmatizer.PARAM_DASH_BUG_WORKAROUND, false);

		JCas aJCas = TestRunner.runTest(createAggregateDescription(posTagger, lemmatizer),
				aLanguage, testDocument);
		
		AssertAnnotations.assertPOS(null, tags, select(aJCas, POS.class));
		AssertAnnotations.assertLemma(lemmas, select(aJCas, Lemma.class));
    }
}
