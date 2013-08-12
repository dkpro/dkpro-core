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

        String[] dependencies = new String[] { 
                "[  0,  3]Dependency(SB) D[0,3](Wir) G[4,12](brauchen)",
                "[ 13, 16]Dependency(NK) D[13,16](ein) G[36,44](Beispiel)",
                "[ 17, 21]Dependency(MO) D[17,21](sehr) G[22,35](kompliziertes)",
                "[ 22, 35]Dependency(NK) D[22,35](kompliziertes) G[36,44](Beispiel)",
                "[ 36, 44]Dependency(OA) D[36,44](Beispiel) G[4,12](brauchen)",
                "[ 45, 46]Dependency(--) D[45,46](,) G[36,44](Beispiel)",
                "[ 47, 54]Dependency(SB) D[47,54](welches) G[101,111](beinhaltet)",
                "[ 55, 64]Dependency(MO) D[55,64](möglichst) G[65,70](viele)",
                "[ 65, 70]Dependency(NK) D[65,70](viele) G[71,84](Konstituenten)",
                "[ 71, 84]Dependency(OA) D[71,84](Konstituenten) G[101,111](beinhaltet)",
                "[ 85, 88]Dependency(CD) D[85,88](und) G[71,84](Konstituenten)",
                "[ 89,100]Dependency(CJ) D[89,100](Dependenzen) G[85,88](und)",
                "[101,111]Dependency(RC) D[101,111](beinhaltet) G[36,44](Beispiel)",
                "[112,113]Dependency(--) D[112,113](.) G[101,111](beinhaltet)" };

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

        String[] dependencies = new String[] { 
                "[  0,  2]Dependency(SBJ) D[0,2](We) G[3,7](need)",
                "[  8,  9]Dependency(NMOD) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(AMOD) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(NMOD) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(NMOD) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(OBJ) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(P) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(SBJ) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(NMOD) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]Dependency(AMOD) D[61,63](as) G[64,68](many)",
                "[ 64, 68]Dependency(NMOD) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(OBJ) D[69,81](constituents) G[52,60](contains)",
                "[ 82, 85]Dependency(COORD) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]Dependency(CONJ) D[86,98](dependencies) G[82,85](and)",
                "[ 99,101]Dependency(NMOD) D[99,101](as) G[69,81](constituents)",
                "[102,110]Dependency(PMOD) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(P) D[111,112](.) G[3,7](need)" };

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
