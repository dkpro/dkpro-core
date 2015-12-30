/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.languagetool;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class LanguageToolLemmatizerTest
{
	@Test
	public void testGerman()
		throws Exception
	{
        runTest("de", "Das ist ein Test .",
        		new String[] { "der",   "sein",  "ein", "Test", "." });

        runTest("de", "besitzt",
                new String[] { "besitzen" });
	}

    @Test
    public void testGerman2()
        throws Exception
    {
        JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] lemmas = new String[] { "ich", "brauchen", "ein", "sehr", "kompliziert",
                "Beispiel", ",", "welch", "möglichst", "viel", "Konstituente", "und",
                "Dependenzen", "beinhalten", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

	@Test
	public void testEnglish()
		throws Exception
	{
        runTest("en", "This is a test .",
				new String[] { "this", "be",  "a",   "test", "."    });

        runTest("en", "A neural net .",
        		new String[] { "a",   "neural", "net", "."    });

        runTest("en", "John is purchasing oranges .",
        		new String[] { "John", "be",  "purchase", "orange", "."    });
    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        AnalysisEngineDescription lemma = createEngineDescription(LanguageToolLemmatizer.class);

        return TestRunner.runTest(lemma, aLanguage, aText);
    }

	private void runTest(String language, String testDocument, String[] aLemma)
		throws Exception
	{
		AnalysisEngineDescription engine = createEngineDescription(
		        createEngineDescription(OpenNlpPosTagger.class),
		        createEngineDescription(LanguageToolLemmatizer.class));

		JCas jcas = TestRunner.runTest(engine, language, testDocument);

		AssertAnnotations.assertLemma(aLemma, select(jcas, Lemma.class));
	}

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
