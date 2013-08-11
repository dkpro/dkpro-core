/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.tokit;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;



/**
 * @author Judith Eckle-Kohler
 *
 */
public class GermanSeparatedParticleAnnotatorTest
{



	@Test
	public void testGermanSeparatedParticles()
		throws Exception
	{
        runTest("de", "Wir schlagen ein Treffen vor .",
        		new String[] { "wir", "schlagen", "eine", "Treffen", "vor", "."    },
        		new String[] { "PPER", "VVFIN", "ART", "NN", "PTKVZ", "$."    },
        		new String[] { "wir", "vorschlagen", "eine", "Treffen", "vor", "."    });


        runTest("de", "Fangen wir jetzt an ?",
        		new String[] { "fangen", "wir", "jetzt", "an", "?"    },
				new String[] { "VVFIN", "PPER", "ADV", "PTKVZ", "$."    },
				new String[] { "anfangen", "wir", "jetzt", "an", "?"    });


	}

	private void runTest(String language, String testDocument, String[] documentTreeTaggerLemmas,
			String[] documentPosTags, String[] lemmatizedDocument) throws UIMAException
	{

		AnalysisEngineDescription processor = createEngineDescription(

				createEngineDescription(GermanSeparatedParticleAnnotator.class)
		);

		AnalysisEngine engine = createPrimitive(processor);
		JCas aJCas = engine.newJCas();
		aJCas.setDocumentLanguage(language);

		TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class,
				Sentence.class);
		tb.buildTokens(aJCas, testDocument);


		int offset = 0;
		for (Token token : JCasUtil.select(aJCas, Token.class)) {
			POS pos = new POS(aJCas, token.getBegin(), token.getEnd());
			pos.setPosValue(documentPosTags[offset]);
			pos.addToIndexes();

			token.setPos(pos);

			Lemma lemma = new Lemma(aJCas, token.getBegin(), token.getEnd());
			lemma.setValue(documentTreeTaggerLemmas[offset]);
			lemma.addToIndexes();

			token.setLemma(lemma);


			offset++;
		}
		engine.process(aJCas);

		AssertAnnotations.assertLemma(lemmatizedDocument, select(aJCas, Lemma.class));



	}


	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}


}
