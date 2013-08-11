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
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class MateParserTest
{
	@Test
	public void testGerman()
		throws Exception
	{
		JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
				+ "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] dependencies = new String[] { "-- 101,111,112,113", "-- 36,44,45,46",
                "CD 71,84,85,88", "CJ 85,88,89,100", "MO 22,35,17,21", "MO 65,70,55,64",
                "NK 36,44,13,16", "NK 36,44,22,35", "NK 71,84,65,70", "OA 101,111,71,84",
                "OA 4,12,36,44", "RC 36,44,101,111", "SB 101,111,47,54", "SB 4,12,0,3" };

        String[] posTags = new String[] { "$(", "$,", "$.", "<None>", "<root-POS>", "ADJA", "ADJD",
                "ADV", "APPO", "APPR", "APPRART", "APZR", "ART", "CARD", "END", "FM", "ITJ",
                "KOKOM", "KON", "KOUI", "KOUS", "MID", "NE", "NN", "NNE", "PDAT", "PDS", "PIAT",
                "PIS", "PPER", "PPOSAT", "PPOSS", "PRELAT", "PRELS", "PRF", "PROAV", "PTKA",
                "PTKANT", "PTKNEG", "PTKVZ", "PTKZU", "PWAT", "PWAV", "PWS", "STPOS", "STR",
                "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN", "VMINF", "VMPP", "VVFIN",
                "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] dependencyTags = new String[] { "--", "<None>", "<no-type>", "<root-type>", "AC",
                "ADC", "AG", "AMS", "APP", "AVC", "CC", "CD", "CJ", "CM", "CP", "CVC", "DA", "DM",
                "END", "EP", "JU", "MNR", "MO", "NG", "NK", "NMC", "OA", "OA2", "OC", "OG", "OP",
                "PAR", "PD", "PG", "PH", "PM", "PNC", "RC", "RE", "RS", "SB", "SBP", "SP", "SVP",
                "UC", "VO" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "stts", posTags, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "negra", dependencyTags, jcas);
	}

    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", "We need a very complicated example sentence , which " +
                "contains as many constituents and dependencies as possible .");

        String[] dependencies = new String[] { "AMOD 15,26,10,14", "AMOD 64,68,61,63",
                "CONJ 82,85,86,98", "COORD 69,81,82,85", "NMOD 35,43,15,26", "NMOD 35,43,27,34",
                "NMOD 35,43,52,60", "NMOD 35,43,8,9", "NMOD 69,81,64,68", "NMOD 69,81,99,101",
                "OBJ 3,7,35,43", "OBJ 52,60,69,81", "P 3,7,111,112", "P 35,43,44,45",
                "PMOD 99,101,102,110", "SBJ 3,7,0,2", "SBJ 52,60,46,51" };

        String[] posTags = new String[] { "#", "$", "''", "(", ")", ",", ".", ":", "<None>",
                "<root-POS>", "CC", "CD", "DT", "END", "EX", "FW", "HYPH", "IN", "JJ", "JJR",
                "JJS", "LS", "MD", "MID", "NIL", "NN", "NNP", "NNPS", "NNS", "PDT", "POS", "PRF",
                "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "STPOS", "STR", "SYM", "TO", "UH", "VB",
                "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] dependencyTags = new String[] { "<None>", "<no-type>", "<root-type>", "ADV",
                "ADV-GAP", "AMOD", "AMOD-GAP", "APPO", "BNF", "CONJ", "COORD", "DEP", "DEP-GAP",
                "DIR", "DIR-GAP", "DIR-OPRD", "DIR-PRD", "DTV", "DTV-GAP", "END", "EXT", "EXT-GAP",
                "EXTR", "EXTR-GAP", "GAP-LGS", "GAP-LOC", "GAP-LOC-PRD", "GAP-MNR", "GAP-NMOD",
                "GAP-OBJ", "GAP-OPRD", "GAP-PMOD", "GAP-PRD", "GAP-PRP", "GAP-PUT", "GAP-SBJ",
                "GAP-SUB", "GAP-TMP", "GAP-VC", "HMOD", "HYPH", "IM", "LGS", "LOC", "LOC-MNR",
                "LOC-OPRD", "LOC-PRD", "LOC-TMP", "MNR", "MNR-PRD", "MNR-TMP", "NAME", "NMOD",
                "OBJ", "OPRD", "P", "PMOD", "POSTHON", "PRD", "PRD-PRP", "PRD-TMP", "PRN", "PRP",
                "PRT", "PUT", "ROOT", "SBJ", "SUB", "SUFFIX", "TITLE", "TMP", "VC", "VOC" };

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "conll2009", dependencyTags, jcas);
    }

	private JCas runTest(String aLanguage, String aText)
		throws Exception
	{
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

		AnalysisEngineDescription aggregate = createEngineDescription(
				createEngineDescription(MatePosTagger.class),
				createEngineDescription(MateParser.class));

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
