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
package de.tudarmstadt.ukp.dkpro.core.gate;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class HepplePosTaggerTest
{
	@Test
	public void testEnglish()
		throws Exception
	{
        runTest("en", null, "This is a test . \n",
				new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
				new String[] { "ART",  "V",   "ART", "NN",   "PUNC" });

        runTest("en", null, "A neural net . \n",
        		new String[] { "DT",  "NN", "JJ",  "." },
        		new String[] { "ART", "NN", "ADJ", "PUNC" });

        runTest("en", null, "John is purchasing oranges . \n",
        		new String[] { "NNP", "VBZ", "VBG", "NNS", "." },
        		new String[] { "NP",  "V",   "V",   "NN",  "PUNC" });
    }

	private void runTest(String language, String variant, String testDocument, String[] tags,
			String[] tagClasses)
		throws Exception
	{
		AnalysisEngine engine = createEngine(HepplePosTagger.class,
		        HepplePosTagger.PARAM_VARIANT, variant,
		        HepplePosTagger.PARAM_PRINT_TAGSET, true);

		JCas jcas = TestRunner.runTest(engine, language, testDocument);

		AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
