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
                "Dependency(--)[112,113] D(.)[112,113] G(beinhaltet)[101,111]",
                "Dependency(--)[45,46] D(,)[45,46] G(Beispiel)[36,44]",
                "Dependency(CD)[85,88] D(und)[85,88] G(Konstituenten)[71,84]",
                "Dependency(CJ)[89,100] D(Dependenzen)[89,100] G(und)[85,88]",
                "Dependency(MO)[17,21] D(sehr)[17,21] G(kompliziertes)[22,35]",
                "Dependency(MO)[55,64] D(möglichst)[55,64] G(viele)[65,70]",
                "Dependency(NK)[13,16] D(ein)[13,16] G(Beispiel)[36,44]",
                "Dependency(NK)[22,35] D(kompliziertes)[22,35] G(Beispiel)[36,44]",
                "Dependency(NK)[65,70] D(viele)[65,70] G(Konstituenten)[71,84]",
                "Dependency(OA)[36,44] D(Beispiel)[36,44] G(brauchen)[4,12]",
                "Dependency(OA)[71,84] D(Konstituenten)[71,84] G(beinhaltet)[101,111]",
                "Dependency(RC)[101,111] D(beinhaltet)[101,111] G(Beispiel)[36,44]",
                "Dependency(SB)[0,3] D(Wir)[0,3] G(brauchen)[4,12]",
                "Dependency(SB)[47,54] D(welches)[47,54] G(beinhaltet)[101,111]" };

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
                "Dependency(AMOD)[10,14] D(very)[10,14] G(complicated)[15,26]",
                "Dependency(AMOD)[61,63] D(as)[61,63] G(many)[64,68]",
                "Dependency(CONJ)[86,98] D(dependencies)[86,98] G(and)[82,85]",
                "Dependency(COORD)[82,85] D(and)[82,85] G(constituents)[69,81]",
                "Dependency(NMOD)[15,26] D(complicated)[15,26] G(sentence)[35,43]",
                "Dependency(NMOD)[27,34] D(example)[27,34] G(sentence)[35,43]",
                "Dependency(NMOD)[52,60] D(contains)[52,60] G(sentence)[35,43]",
                "Dependency(NMOD)[64,68] D(many)[64,68] G(constituents)[69,81]",
                "Dependency(NMOD)[8,9] D(a)[8,9] G(sentence)[35,43]",
                "Dependency(NMOD)[99,101] D(as)[99,101] G(constituents)[69,81]",
                "Dependency(OBJ)[35,43] D(sentence)[35,43] G(need)[3,7]",
                "Dependency(OBJ)[69,81] D(constituents)[69,81] G(contains)[52,60]",
                "Dependency(P)[111,112] D(.)[111,112] G(need)[3,7]",
                "Dependency(P)[44,45] D(,)[44,45] G(sentence)[35,43]",
                "Dependency(PMOD)[102,110] D(possible)[102,110] G(as)[99,101]",
                "Dependency(SBJ)[0,2] D(We)[0,2] G(need)[3,7]",
                "Dependency(SBJ)[46,51] D(which)[46,51] G(contains)[52,60]" };

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
