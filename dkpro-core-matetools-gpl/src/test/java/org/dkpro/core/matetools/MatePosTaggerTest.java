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
import org.dkpro.core.matetools.MatePosTagger;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class MatePosTaggerTest
{
    @Test
    public void testGerman()
        throws Exception
    {
        JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] posOriginal = { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,", "PRELS", "ADV",
                "PIAT", "NN", "KON", "NN", "VVFIN", "$." };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_ADJ", "POS_NOUN", "POS_PUNCT", "POS_PRON", "POS_ADV",
                "POS_PRON", "POS_NOUN", "POS_CONJ", "POS_NOUN", "POS_VERB", "POS_PUNCT" };

        String[] posTags = { "$(", "$,", "$.", "ADJA", "ADJD", "ADV", "APPO", "APPR", "APPRART",
                "APZR", "ART", "CARD", "END", "FM", "ITJ", "KOKOM", "KON", "KOUI", "KOUS", "MID",
                "NE", "NN", "NNE", "PDAT", "PDS", "PIAT", "PIS", "PPER", "PPOSAT", "PPOSS",
                "PRELAT", "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU",
                "PWAT", "PWAV", "PWS", "STPOS", "STR", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP",
                "VMFIN", "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

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

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "JJ", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "DT", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_ADJ", "POS_NOUN",
                "POS_NOUN", "POS_PUNCT", "POS_DET", "POS_VERB", "POS_ADP", "POS_DET", "POS_NOUN",
                "POS_CONJ", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_PUNCT" };

        String[] posTags = { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD", "DT", "END",
                "EX", "FW", "HYPH", "IN", "JJ", "JJR", "JJS", "LS", "MD", "MID", "NIL", "NN",
                "NNP", "NNPS", "NNS", "PDT", "POS", "PRF", "PRP", "PRP$", "RB", "RBR", "RBS", "RP",
                "STPOS", "STR", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT",
                "WP", "WP$", "WRB", "``" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
    }

    @Test
    public void testFrench()
        throws Exception
    {
        JCas jcas = runTest("fr", "Nous avons besoin d'une phrase par exemple très "
                + "compliqué, qui contient des constituants que de nombreuses dépendances et que "
                + "possible .");

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_ADP",
                "POS_NOUN", "POS_ADV", "POS_ADJ", "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN",
                "POS_CONJ", "POS_DET", "POS_ADJ", "POS_NOUN", "POS_CONJ", "POS_CONJ", "POS_ADJ",
                "POS_PUNCT" };

        String[] posOriginal = { "CLS", "V", "NC", "P", "NC", "P", "NC", "ADV", "ADJ", "PROREL",
                "V", "DET", "NC", "CS", "DET", "ADJ", "NC", "CC", "CS", "ADJ", "PONCT" };

        String[] posTags = { "ADJ", "ADJWH", "ADV", "ADVWH", "CC", "CLO", "CLR", "CLS", "CS",
                "DET", "DETWH", "END", "ET", "I", "MID", "NC", "NPP", "P", "P+D", "P+PRO", "PONCT",
                "PREF", "PRO", "PROREL", "PROWH", "STPOS", "STR", "V", "VIMP", "VINF", "VPP",
                "VPR", "VS" };

        String[] unmappedPos = { "END", "MID", "STPOS", "STR" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));

        AssertAnnotations.assertTagset(POS.class, "melt", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "melt", unmappedPos, jcas);
    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        AssumeResource.assumeResource(MatePosTagger.class, "tagger", aLanguage, null);
        
        AnalysisEngineDescription posTag = createEngineDescription(MatePosTagger.class);

        return TestRunner.runTest(posTag, aLanguage, aText);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
