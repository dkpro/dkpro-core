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

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
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
                "beispiel", "--", "welcher", "möglichst", "vieler", "konstituent", "und",
                "dependenz", "beinhalten", "--" };

        String[] morphTagsExpected = { "case=nom|number=pl|gender=*|person=1",
                "number=pl|person=1|tense=pres|mood=ind", "case=acc|number=sg|gender=neut", "_",
                "case=acc|number=sg|gender=neut|degree=pos", "case=acc|number=sg|gender=neut", "_",
                "case=acc|number=sg|gender=neut", "_", "case=acc|number=pl|gender=*",
                "case=acc|number=pl|gender=*", "_", "case=acc|number=pl|gender=fem",
                "number=sg|person=3|tense=pres|mood=ind", "_" };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        AssertAnnotations.assertMorpheme(morphTagsExpected, select(jcas, Morpheme.class));
    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

        AnalysisEngineDescription lemma = createPrimitiveDescription(MateLemmatizer.class);
        AnalysisEngineDescription morphTag = createPrimitiveDescription(MateMorphTagger.class);

        AnalysisEngineDescription aggregate = createAggregateDescription(lemma, morphTag);

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
