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

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

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

	private JCas runTest(String aLanguage, String aVariant, String testDocument, String[] tags,
			String[] lemmas)
		throws Exception
	{
		AnalysisEngine posTagger = createPrimitive(StanfordPosTagger.class,
				StanfordPosTagger.PARAM_VARIANT, aVariant);

		AnalysisEngine lemmatizer = createPrimitive(StanfordLemmatizer.class,
				StanfordLemmatizer.PARAM_DASH_BUG_WORKAROUND, false);

        JCas aJCas = posTagger.newJCas();
        aJCas.setDocumentLanguage(aLanguage);

        TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);
        tb.buildTokens(aJCas, testDocument);

        posTagger.process(aJCas);
        lemmatizer.process(aJCas);

        // test POS annotations
        if (tags != null && lemmas != null) {
        	checkTags(tags, lemmas, select(aJCas, Token.class));
        }

        return aJCas;
    }

	private void checkTags(String[] tags, String[] lemmas, Collection<Token> actual)
	{
		assertEquals("Number of tags " + actual.size(), lemmas.length,
				actual.size());
		int i = 0;
        for (Token token : actual) {
            assertEquals("In position "+i, tags[i], token.getPos().getPosValue());
            assertEquals("In position "+i, lemmas[i], token.getLemma().getValue());
            i++;
        }
	}
}
