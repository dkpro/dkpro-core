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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class MatePosTaggerTest
{
    @Test
    public void testGerman()
        throws Exception
    {
        JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] posOriginal = new String[] { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,",
                "PRELS", "ADV", "PIAT", "NN", "KON", "NN", "VVFIN", "$." };

        String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "ADJ", "NN", "PUNC", "PR",
                "ADV", "PR", "NN", "CONJ", "NN", "V", "PUNC" };

        String[] posTags = new String[] { "$(", "$,", "$.", "<None>", "<root-POS>", "ADJA", "ADJD",
                "ADV", "APPO", "APPR", "APPRART", "APZR", "ART", "CARD", "END", "FM", "ITJ",
                "KOKOM", "KON", "KOUI", "KOUS", "MID", "NE", "NN", "NNE", "PDAT", "PDS", "PIAT",
                "PIS", "PPER", "PPOSAT", "PPOSS", "PRELAT", "PRELS", "PRF", "PROAV", "PTKA",
                "PTKANT", "PTKNEG", "PTKVZ", "PTKZU", "PWAT", "PWAV", "PWS", "STPOS", "STR",
                "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN", "VMINF", "VMPP", "VVFIN",
                "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertTagset(POS.class, "stts", posTags, jcas);
    }

    @Test
    public void testEnglish()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

        JCas jcas = runTest("en", "We need a very complicated example sentence , which " +
                "contains as many constituents and dependencies as possible .");

        String[] posOriginal = new String[] { "PRP", "VBP", "DT", "RB", "JJ", "NN", "NN", ",",
                "WDT", "VBZ", "IN", "DT", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "ADJ", "NN", "NN", "PUNC",
                "ART", "V", "PP", "ART", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

        String[] posTags = new String[] { "#", "$", "''", "(", ")", ",", ".", ":", "<None>",
                "<root-POS>", "CC", "CD", "DT", "END", "EX", "FW", "HYPH", "IN", "JJ", "JJR",
                "JJS", "LS", "MD", "MID", "NIL", "NN", "NNP", "NNPS", "NNS", "PDT", "POS", "PRF",
                "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "STPOS", "STR", "SYM", "TO", "UH", "VB",
                "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        AnalysisEngineDescription posTag = createEngineDescription(MatePosTagger.class);

        return TestRunner.runTest(posTag, aLanguage, aText);
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
