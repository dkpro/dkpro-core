/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.rftagger;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class RfTaggerTest {

	@Test
	public void testGerman() throws Exception {
		JCas runTest = runTest("de", "Er nahm meine Fackel und schlug sie dem Bär ins Gesicht.",
				new String[] { "Er", "nahm", "meine", "Fackel", "und", "schlug", "sie", "dem", "Bär", "ins", "Gesicht",
						"." },
				new String[] { "PRO", "VFIN", "PRO", "N", "CONJ", "VFIN", "PRO", "ART", "N", "APPRART", "N", "SYM" },
				new String[] { "PR", "V", "PR", "NN", "CONJ", "V", "PR", "ART", "NN", "PP", "NN", "PUNC" });

		verifyMorphologicalAnnotation(runTest);
	}

	private void verifyMorphologicalAnnotation(JCas runTest) {
		List<Token> tokens = new ArrayList<Token>(JCasUtil.select(runTest, Token.class));

		// 1st token "Er"
		Token token1 = tokens.get(0);
		MorphologicalFeatures morph = token1.getMorph();
		assertTrue(morph != null);
		assertEquals("3", morph.getPerson());
		assertEquals("Masc", morph.getGender());
		assertEquals("Nom", morph.getCase());
		assertEquals("Sing", morph.getNumber());
		assertEquals(null, morph.getDefiniteness());
		assertEquals("Prs", morph.getPronType());

		// 4th token "Fackel"
		Token token4 = tokens.get(3);
		morph = token4.getMorph();
		assertTrue(morph != null);
		assertEquals(null, morph.getPerson());
		assertEquals("Fem", morph.getGender());
		assertEquals("Acc", morph.getCase());
		assertEquals("Sing", morph.getNumber());
		assertEquals(null, morph.getDefiniteness());
		assertEquals(null, morph.getPronType());

		// 8th token "dem"
		Token token8 = tokens.get(7);
		morph = token8.getMorph();
		assertTrue(morph != null);
		assertEquals(null, morph.getPerson());
		assertEquals("Masc", morph.getGender());
		assertEquals("Dat", morph.getCase());
		assertEquals("Sing", morph.getNumber());
		assertEquals("Def", morph.getDefiniteness());
		assertEquals(null, morph.getPronType());
	}

	private JCas runTest(String language, String testDocument, String[] tokens, String[] tags, String[] tagClasses)
			throws Exception {
		AnalysisEngine tokenizer = createEngine(BreakIteratorSegmenter.class);

		AnalysisEngine tagger = createEngine(RfTagger.class, RfTagger.PARAM_LANGUAGE,language, RfTagger.PARAM_VARIANT,
				"tiger2treebank",
				RfTagger.PARAM_POS_MAPPING_LOCATION,
				               "classpath:de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/de-tiger2treebank-pos.map",
				               RfTagger.PARAM_MORPH_MAPPING_LOCATION,
				               "classpath:de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/de-tiger-morph.map");

		JCas aJCas = tagger.newJCas();
		aJCas.setDocumentLanguage(language);
		aJCas.setDocumentText(testDocument);

		tokenizer.process(aJCas);
		tagger.process(aJCas);

		// test tokens
		checkTokens(tokens, select(aJCas, Token.class));

		// test POS annotations
		if (tagClasses != null && tags != null) {
			checkTags(tagClasses, tags, select(aJCas, POS.class));
		}

		return aJCas;
	}

	private void checkTokens(String[] expected, Collection<Token> actual) {
		int i = 0;
		for (Token tokenAnnotation : actual) {
			assertEquals("In position " + i, expected[i], tokenAnnotation.getCoveredText());
			i++;
		}
	}

	private void checkTags(String[] tagClasses, String[] tags, Collection<POS> actual) {
		int i = 0;
		for (POS posAnnotation : actual) {
			assertEquals("In position " + i, tagClasses[i], posAnnotation.getType().getShortName());
			assertEquals("In position " + i, tags[i], posAnnotation.getPosValue());
			i++;
		}
	}
}
