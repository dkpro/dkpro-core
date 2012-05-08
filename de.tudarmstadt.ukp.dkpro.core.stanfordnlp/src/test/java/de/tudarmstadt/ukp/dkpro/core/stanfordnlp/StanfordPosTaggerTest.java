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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class StanfordPosTaggerTest
{
	@Test
	public void testEnglish()
		throws Exception
	{
        runTest("en", "This is a test . \n",
				new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
				new String[] { "ART",  "V",   "ART", "NN",   "PUNC" });

        runTest("en", "A neural net . \n",
        		new String[] { "DT",  "JJ",     "NN",  "." },
        		new String[] { "ART", "ADJ",    "NN",  "PUNC" });

        runTest("en", "John is purchasing oranges . \n",
        		new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
        		new String[] { "NP",   "V",   "V",        "NN",     "PUNC" });
    }
	
	@Test
	public void testGerman()
		throws Exception
    {
        runTest("de", "Das ist ein Test .",
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."    },
        		new String[] { "PR",  "V",     "ART", "NN",   "PUNC" });
    }

	private JCas runTest(String language, String testDocument, String[] tags, String[] tagClasses)
		throws Exception
	{
        AnalysisEngine engine = createPrimitive(StanfordPosTagger.class);

        JCas aJCas = engine.newJCas();
        aJCas.setDocumentLanguage(language);

        TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);
        tb.buildTokens(aJCas, testDocument);

        engine.process(aJCas);

        // test POS annotations
        if (tagClasses != null && tags != null) {
        	checkTags(tagClasses, tags, select(aJCas, POS.class));
        }

        return aJCas;
    }

	private void checkTags(String[] expectedClasses, String[] expectedTags, Collection<POS> actual)
	{
		String[] actualTags = new String[actual.size()];
		String[] actualClasses = new String[actual.size()];
		
        int i = 0;
        for (POS posAnnotation : actual) {
        	actualTags[i] = posAnnotation.getPosValue();
        	actualClasses[i] = posAnnotation.getType().getShortName();
            i++;
        }
        
        System.out.printf("Tags    - Expected: %s%n", asList(expectedTags));
        System.out.printf("Tags    - Actual  : %s%n", asList(actualTags));
        System.out.printf("Classes - Expected: %s%n", asList(expectedClasses));
        System.out.printf("Classes - Actual  : %s%n", asList(actualClasses));
        
        assertEquals(asList(expectedTags), asList(actualTags));
        assertEquals(asList(expectedClasses), asList(actualClasses));
	}
	
	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
