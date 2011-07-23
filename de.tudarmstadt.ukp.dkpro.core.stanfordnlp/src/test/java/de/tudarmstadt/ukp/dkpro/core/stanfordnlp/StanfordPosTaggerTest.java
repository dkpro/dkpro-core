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

import java.net.URL;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Test;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class StanfordPosTaggerTest
{
	@Test
	public void testGerman() throws Exception
	{
        runTest("de", "fast", "Das ist ein Test .",
        		new String[] { "PDS", "VAFIN", "ART", "NN",  "$."    },
        		new String[] { "POS",  "POS",  "POS", "POS", "POS" });

	}

	private JCas runTest(String aLanguage, String aVariant, String testDocument, String[] tags, String[] tagClasses)
		throws Exception
	{
		checkModelsAndBinary(aLanguage, aVariant);

		AnalysisEngine engine = createPrimitive(StanfordPosTagger.class,
				StanfordPosTagger.PARAM_MODEL_PATH,
				"classpath:/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/postagger-" + aLanguage
						+ "-" + aVariant + ".tagger");

        JCas aJCas = engine.newJCas();
        aJCas.setDocumentLanguage(aLanguage);

        TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);
        tb.buildTokens(aJCas, testDocument);

        engine.process(aJCas);

        // test POS annotations
        if (tagClasses != null && tags != null) {
        	checkTags(tagClasses, tags, select(aJCas, POS.class));
        }

        return aJCas;
    }

	private void checkTags(String[] tagClasses, String[] tags, Collection<POS> actual)
	{
		assertEquals("Number of tags " + actual.size(), tags.length,
				actual.size());
		int i = 0;
        for (POS posAnnotation : actual) {
            assertEquals("In position "+i, tagClasses[i], posAnnotation.getType().getShortName());
            assertEquals("In position "+i, tags[i], posAnnotation.getPosValue());
            i++;
        }
	}

	private void checkModelsAndBinary(String aLanguage, String aVariant)
	{
		String loc = "/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/postagger-" + aLanguage + "-"
				+ aVariant + ".tagger";
		URL url = getClass().getResource(loc);
		Assume.assumeTrue(url != null);
	}
}
