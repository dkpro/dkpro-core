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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class StanfordLemmatizerTest
{

    @Test
    public void testUnderscore() throws Exception
    {
        AnalysisEngineDescription lemmatizer = createEngineDescription(StanfordLemmatizer.class);

        JCas aJCas = TestRunner.runTest(createEngineDescription(lemmatizer),
                "en", "foo _ bar");

        Lemma[] lemmas = JCasUtil.select(aJCas, Lemma.class).toArray(new Lemma[0]);
        assertEquals(3, lemmas.length);
        assertEquals("foo", lemmas[0].getValue());
        assertEquals("_", lemmas[1].getValue());
        assertEquals("bar", lemmas[2].getValue());
    }

	@Test
	public void testEnglish() throws Exception
	{
        runTest("en", null, "This is a test _ .",
        		new String[] { "DT", "VBZ", "DT", "NN",  "NN", "."    },
        		new String[] { "this",  "be",  "a", "test", "_", "." });

	}

	private void runTest(String aLanguage, String aVariant, String testDocument, String[] tags,
			String[] lemmas)
		throws Exception
	{
		AnalysisEngineDescription posTagger = createEngineDescription(StanfordPosTagger.class,
				StanfordPosTagger.PARAM_VARIANT, aVariant);

		AnalysisEngineDescription lemmatizer = createEngineDescription(StanfordLemmatizer.class);

		JCas aJCas = TestRunner.runTest(createEngineDescription(posTagger, lemmatizer),
				aLanguage, testDocument);

		AssertAnnotations.assertPOS(null, tags, select(aJCas, POS.class));
		AssertAnnotations.assertLemma(lemmas, select(aJCas, Lemma.class));
    }
}
