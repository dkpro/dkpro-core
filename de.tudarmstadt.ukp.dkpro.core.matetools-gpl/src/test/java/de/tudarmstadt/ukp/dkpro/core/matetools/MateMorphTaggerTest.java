/**
 * Copyright 2007-2014
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
package de.tudarmstadt.ukp.dkpro.core.matetools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class MateMorphTaggerTest
{
    @Test
    public void testGerman()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 1000000000);

        JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] lemmas = new String[] { "wir", "brauchen", "ein", "sehr", "kompliziert",
                "Beispiel", "--", "welcher", "möglichst", "vieler", "Konstituent", "und",
                "Dependenz", "beinhalten", "--" };

        String[] morphTagsExpected = { "pl|1|pres|ind", "acc|sg|neut", "_", "nom|sg|neut|pos",
                "nom|sg|neut", "_", "nom|sg|neut", "_", "nom|pl|*", "gen|pl|masc", "_",
                "gen|pl|masc", "sg|3|pres|ind", "_" };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        AssertAnnotations.assertMorpheme(morphTagsExpected, select(jcas, Morpheme.class));
    }

    @Ignore
    @Test
    public void testFrench()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 1000000000);

        JCas jcas = runTest("fr", "Nous avons besoin d'une phrase par exemple très "
                + "compliqué, qui contient des constituants que de nombreuses dépendances et que "
                + "possible .");

        String[] lemmas = new String[] { "il", "avoir", "besoin", "d'une", "phrase", "par",
                "exemple", "très", "compliqué,", "qui", "contenir", "de", "constituant", "que",
                "de", "nombreux", "dépendance", "et", "que", "possible", "." };

        String[] morphTagsExpected = { "nom|pl|masc|pos", "acc|sg|neut", "nom|pl|neut",
                "nom|pl|neut", "_", "acc|sg|neut", "_", "nom|pl|neut", "nom|pl|neut", "*|*|*",
                "nom|pl|neut", "nom|pl|neut", "dat|sg|masc", "acc|pl|*", "gen|sg|masc",
                "nom|pl|neut", "acc|sg|fem|sup", "nom|pl|neut", "nom|pl|neut", "nom|pl|neut" };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        AssertAnnotations.assertMorpheme(morphTagsExpected, select(jcas, Morpheme.class));
    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

        AnalysisEngineDescription lemma = createEngineDescription(MateLemmatizer.class);
        AnalysisEngineDescription morphTag = createEngineDescription(MateMorphTagger.class);

        AnalysisEngineDescription aggregate = createEngineDescription(lemma, morphTag);

        return TestRunner.runTest(aggregate, aLanguage, aText);
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
