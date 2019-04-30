/*
 * Copyright 2007-2018
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.matetools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.matetools.MateLemmatizer;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

public class MateLemmatizerTest
{
    @Test
    public void testGerman()
        throws Exception
    {
        JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .", true);

        String[] lemmas = { "wir", "brauchen", "ein", "sehr", "kompliziert",
                "Beispiel", "--", "welcher", "möglichst", "vieler", "Konstituent", "und",
                "Dependenz", "beinhalten", "--" };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .", false);

        String[] lemmas = { "we", "need", "a", "very", "complicate", "example",
                "sentence", ",", "which", "contain", "as", "many", "constituent", "and",
                "dependency", "as", "possible", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    @Test
    public void testFrench()
        throws Exception
    {
        JCas jcas = runTest("fr", "Nous avons besoin d'une phrase par exemple très "
                + "compliqué, qui contient des constituants que de nombreuses dépendances et que "
                + "possible .", false);

        String[] lemmas = { "il", "avoir", "besoin", "d'une", "phrase", "par",
                "exemple", "très", "compliqué,", "qui", "contenir", "de", "constituant", "que",
                "de", "nombreux", "dépendance", "et", "que", "possible", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    private JCas runTest(String aLanguage, String aText, boolean aUppercase)
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

        AssumeResource.assumeResource(MateLemmatizer.class, "lemmatizer", aLanguage, null);

        AnalysisEngineDescription lemma = createEngineDescription(MateLemmatizer.class,
                MateLemmatizer.PARAM_UPPERCASE, aUppercase);

        return TestRunner.runTest(lemma, aLanguage, aText);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
