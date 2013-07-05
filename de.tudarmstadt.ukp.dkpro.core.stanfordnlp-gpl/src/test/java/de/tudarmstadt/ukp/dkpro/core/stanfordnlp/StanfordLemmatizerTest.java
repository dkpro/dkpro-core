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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
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
        runTest("en", "wsj-0-18-bidirectional-distsim", "This is a test .",
        		new String[] { "DT", "VBZ", "DT", "NN",  "."    },
        		new String[] { "this",  "be",  "a", "test", "." });

	}

	private void runTest(String aLanguage, String aVariant, String testDocument, String[] tags,
			String[] lemmas)
		throws Exception
	{
		AnalysisEngineDescription posTagger = createPrimitiveDescription(StanfordPosTagger.class,
				StanfordPosTagger.PARAM_VARIANT, aVariant);

		AnalysisEngineDescription lemmatizer = createPrimitiveDescription(StanfordLemmatizer.class);

		JCas aJCas = TestRunner.runTest(createAggregateDescription(posTagger, lemmatizer),
				aLanguage, testDocument);

		AssertAnnotations.assertPOS(null, tags, select(aJCas, POS.class));
		AssertAnnotations.assertLemma(lemmas, select(aJCas, Lemma.class));
    }
}
