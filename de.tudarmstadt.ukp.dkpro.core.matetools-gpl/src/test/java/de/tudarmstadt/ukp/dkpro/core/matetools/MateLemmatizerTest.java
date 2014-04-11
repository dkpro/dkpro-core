/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.matetools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class MateLemmatizerTest
{
    @Test
    public void testGerman()
        throws Exception
    {
        JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] lemmas = new String[] { "wir", "brauchen", "ein", "sehr", "kompliziert",
                "beispiel", "--", "welcher", "möglichst", "vieler", "konstituent", "und",
                "dependenz", "beinhalten", "--" };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] lemmas = new String[] { "we", "need", "a", "very", "complicate", "example",
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
                + "possible .");

        String[] lemmas = new String[] { "il", "avoir", "besoin", "d'une", "phrase", "par",
                "exemple", "très", "compliqué,", "qui", "contenir", "de", "constituant", "que",
                "de", "nombreux", "dépendance", "et", "que", "possible", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

        AnalysisEngineDescription lemma = createEngineDescription(MateLemmatizer.class);

        return TestRunner.runTest(lemma, aLanguage, aText);
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
