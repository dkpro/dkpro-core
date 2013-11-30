/**
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
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
        runTest("en", null, "This is a test .",
        		new String[] { "DT", "VBZ", "DT", "NN",  "."    },
        		new String[] { "this",  "be",  "a", "test", "." });

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
