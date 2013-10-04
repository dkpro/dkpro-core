/**
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.berkeleyparser;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class BerkeleyParserTest
{
	static final String documentEnglish = "We need a very complicated example sentence, which " +
			"contains as many constituents and dependencies as possible.";

//	@Ignore("We don't seem to have a proper tokenizer for arabic...")
	@Test
	public void testArabic()
		throws Exception
	{
		JCas jcas = runTest("ar", "نحن بحاجة إلى مثال على جملة معقدة جدا، والتي تحتوي على مكونات مثل العديد من والتبعيات وقت ممكن.");

		String[] constituentMapped = new String[] { "Constituent 0,1" };

		String[] constituentOriginal = new String[] {"ROOT 0,1", "X 0,1" };

		String[] posMapped = new String[] { "POS", "POS" };

		String[] posOriginal = new String[] { "PUNC", "PUNC" };

		String pennTree = "(ROOT (ROOT (X (PUNC ن) (PUNC ن))))";

        String[] posTags = new String[] { "CC", "CD", "DEM", "DT", "IN", "JJ", "NN", "NNP", "NNPS",
                "NNS", "NOFUNC", "NUMCOMMA", "PRP", "PRP$", "PUNC", "RB", "RP", "UH", "VB", "VBD",
                "VBN", "VBP", "VERB", "WP", "WRB" };

        String[] constituentTags = new String[] { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST",
                "NAC", "NP", "NX", "PP", "PRN", "PRT", "QP", "ROOT", "S", "SBAR", "SBARQ", "SINV",
                "SQ", "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };
        
        String[] unmappedConst = new String[] { "LST", "SINV" };
		
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        AssertAnnotations.assertTagset(POS.class, "atb", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "atb", new String[] {}, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "atb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "atb", unmappedConst, jcas);
	}

	@Test
	public void testBulgarian()
		throws Exception
	{
		JCas jcas = runTest("bg", "Имаме нужда от един много сложен пример изречение, " +
				"което съдържа най-много съставки и зависимости, колкото е възможно.");

		String[] constituentMapped = new String[] { "ROOT 0,118", "X 0,117", "X 0,118", "X 0,5",
				"X 107,108", "X 107,117", "X 109,117", "X 12,117", "X 12,14", "X 15,117",
				"X 15,19", "X 15,39", "X 15,49", "X 20,25", "X 20,32", "X 20,39", "X 26,32",
				"X 33,39", "X 40,49", "X 49,117", "X 51,117", "X 51,56", "X 51,97", "X 57,64",
				"X 57,97", "X 6,11", "X 6,117", "X 65,74", "X 65,97", "X 75,83", "X 75,97",
				"X 84,85", "X 86,97", "X 97,117", "X 99,106", "X 99,117" };

		String[] constituentOriginal = new String[] { "A 26,32", "APA 20,32", "Adv 109,117",
				"Adv 20,25", "Adv 65,74", "Adv 99,106", "C 84,85", "CLR 49,117", "CLR 97,117",
				"Conj 84,85", "ConjArg 75,83", "ConjArg 86,97", "CoordP 75,97", "M 15,19",
				"N 33,39", "N 40,49", "N 6,11", "N 75,83", "N 86,97", "NPA 15,117", "NPA 15,39",
				"NPA 15,49", "NPA 20,39", "NPA 6,117", "NPA 65,97", "PP 12,117", "Prep 12,14",
				"Pron 51,56", "ROOT 0,118", "S 0,118", "V 0,5", "V 107,108", "V 57,64",
				"VPA 51,117", "VPA 99,117", "VPC 0,117", "VPC 107,117", "VPC 57,97", "VPS 51,97" };

		String[] posMapped = new String[] { "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS",
				"POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS" };

		String[] posOriginal = new String[] { "Vpitf", "Ncfsi", "R", "Mcmsi", "Md", "Amsi",
				"Ncmsi", "Ncnsi", "pt", "Pre", "Vpitf", "Md", "Ncmpi", "Cp", "Ncfpi", "pt", "Prq",
				"Vxitf", "Dd", "pt" };

		String pennTree = "(ROOT (ROOT (S (VPC (V (Vpitf Имаме)) (NPA (N (Ncfsi нужда)) (PP " +
				"(Prep (R от)) (NPA (NPA (NPA (M (Mcmsi един)) (NPA (APA (Adv (Md много)) (A " +
				"(Amsi сложен))) (N (Ncmsi пример)))) (N (Ncnsi изречение))) (CLR (pt ,) (VPA " +
				"(VPS (Pron (Pre което)) (VPC (V (Vpitf съдържа)) (NPA (Adv (Md най-много)) " +
				"(CoordP (ConjArg (N (Ncmpi съставки))) (Conj (C (Cp и))) (ConjArg (N " +
				"(Ncfpi зависимости))))))) (CLR (pt ,) (VPA (Adv (Prq колкото)) (VPC (V " +
				"(Vxitf е)) (Adv (Dd възможно))))))))))) (pt .))))";
		
        String[] posTags = new String[] { "A", "Afsd", "Afsi", "Ams", "Amsf", "Amsh", "Amsi",
                "Ansd", "Ansi", "Cc", "Cp", "Cr", "Cs", "Dd", "Dl", "Dm", "Dq", "Dt", "Hfsi",
                "Hmsf", "I", "Mc", "Mcf", "Mcfpd", "Mcfpi", "Mcfsd", "Mcfsi", "Mcm", "Mcmpd",
                "Mcmpi", "Mcmsf", "Mcmsi", "Mcn", "Mcnpd", "Mcnpi", "Mcnsd", "Mcnsi", "Md", "Mo",
                "Mofsd", "Mofsi", "Momsf", "Momsh", "Momsi", "Monsd", "Monsi", "My", "Nc", "Ncfpd",
                "Ncfpi", "Ncfs", "Ncfsd", "Ncfsi", "Ncmpd", "Ncmpi", "Ncms", "Ncmsd", "Ncmsf",
                "Ncmsh", "Ncmsi", "Ncmt", "Ncnpd", "Ncnpi", "Ncnsd", "Ncnsi", "Npfsi", "Npnsi",
                "Pca", "Pce", "Pcl", "Pcq", "Pct", "Pda", "Pde", "Pdl", "Pdm", "Pdq", "Pds", "Pdt",
                "Pfa", "Pfe", "Pfl", "Pfm", "Pfp", "Pfq", "Pft", "Pfy", "Pia", "Pic", "Pie", "Pil",
                "Pim", "Pip", "Piq", "Pit", "Pna", "Pne", "Pnl", "Pnm", "Pnp", "Pnt", "Ppe",
                "Ppelap1", "Ppelap2", "Ppelap3", "Ppelas1", "Ppelas2", "Ppelas3f", "Ppelas3m",
                "Ppelas3n", "Ppeldp1", "Ppelds1", "Ppelds2", "Ppelds3m", "Ppetap1", "Ppetap2",
                "Ppetap3", "Ppetas1", "Ppetas2", "Ppetas3f", "Ppetas3m", "Ppetas3n", "Ppetdp1",
                "Ppetdp2", "Ppetdp3", "Ppetds1", "Ppetds2", "Ppetds3f", "Ppetds3m", "Ppetds3n",
                "Ppetsp1", "Ppetsp2", "Ppetsp3", "Ppetss1", "Ppetss2", "Ppetss3f", "Ppetss3m",
                "Pph", "Pphlas2", "Pphtas2", "Pphtds2", "Pphtss2", "Ppxta", "Ppxtd", "Ppxts",
                "Pra", "Pre", "Prl", "Prm", "Prp", "Prq", "Prs", "Prt", "Pshl", "Psht", "Psol",
                "Psot", "Psxlop", "Psxlos", "Psxto", "Pszl", "Pszt", "R", "Ta", "Te", "Tg", "Ti",
                "Tm", "Tn", "Tv", "Tx", "Viitf", "Vniicam", "Vniicao", "Vniif", "Vnitcam",
                "Vnitcao", "Vnitf", "Vnpicao", "Vnpif", "Vnptcao", "Vnptf", "Vpiicam", "Vpiicao",
                "Vpiicar", "Vpiif", "Vpiig", "Vpiiz", "Vpitcam", "Vpitcao", "Vpitcar", "Vpitcv",
                "Vpitf", "Vpitg", "Vpitz", "Vppicam", "Vppicao", "Vppif", "Vppiz", "Vpptcam",
                "Vpptcao", "Vpptcv", "Vpptf", "Vpptz", "Vxitcat", "Vxitf", "Vxitu", "Vyptf",
                "Vyptz", "abbr", "foreign", "mw", "name", "pt", "w" };

        String[] constituentTags = new String[] { "A", "APA", "APC", "Adv", "AdvPA", "AdvPC", "C",
                "CL", "CLCHE", "CLDA", "CLQ", "CLR", "CLZADA", "Conj", "ConjArg", "CoordP",
                "Gerund", "H", "M", "N", "NPA", "NPC", "PP", "Participle", "Prep", "Pron", "ROOT",
                "S", "T", "V", "VPA", "VPC", "VPF", "VPS", "Verbalised" };
        
        String[] unmappedConstituents = new String[] { "Conj", "ConjArg", "NPC", "Verbalised" };

		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        AssertAnnotations.assertTagset(POS.class, "btb", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "btb", new String[] {}, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "btb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "btb", unmappedConstituents, jcas);
	}

	@Test
	public void testChinese()
		throws Exception
	{
		JCas jcas = runTest("zh", "我们需要一个非常复杂的例句，它要包含尽可能多的句子成分和依存性。");

		String[] constituentMapped = new String[] { "ADVP 18,21", "ADVP 6,8", "NP 0,2", "NP 11,13",
				"NP 14,15", "NP 18,31", "NP 23,30", "NP 30,31", "NP 6,13", "ROOT 0,14",
				"ROOT 14,32", "VP 15,31", "VP 16,31", "VP 18,22", "VP 2,13", "VP 21,22", "VP 4,13",
				"VP 6,10", "VP 8,10", "X 0,14", "X 14,32", "X 18,22", "X 18,23", "X 6,10", "X 6,11" };

		String[] constituentOriginal = new String[] { "ADVP 18,21", "ADVP 6,8", "CP 18,23",
				"CP 6,11", "IP 0,14", "IP 14,32", "IP 18,22", "IP 6,10", "NP 0,2", "NP 11,13",
				"NP 14,15", "NP 18,31", "NP 23,30", "NP 30,31", "NP 6,13", "ROOT 0,14",
				"ROOT 14,32", "VP 15,31", "VP 16,31", "VP 18,22", "VP 2,13", "VP 21,22", "VP 4,13",
				"VP 6,10", "VP 8,10" };

        String[] posMapped = new String[] { "PR", "V", "V", "ADJ", "V", "PRT", "NN", "PUNC", "PR",
                "V", "V", "ADJ", "V", "PRT", "NN", "CONJ", "NN", "NN", "PUNC" };

		String[] posOriginal = new String[] { "PN", "VV", "VV", "AD", "VA", "DEC", "NN", "PU",
				"PN", "VV", "VV", "AD", "VA", "DEC", "NN", "CC", "NN", "NN", "PU" };

		String[] pennTree = new String[] { "(ROOT (IP (NP (PN 我们)) (VP (VV 需要) (VP (VV 一个) " +
				"(NP (CP (IP (VP (ADVP (AD 非常)) (VP (VA 复杂)))) (DEC 的)) (NP (NN 例句))))) " +
				"(PU ，)))", "(ROOT (IP (NP (PN 它)) (VP (VV 要) (VP (VV 包含) (NP (CP (IP (VP " +
				"(ADVP (AD 尽可能)) (VP (VA 多)))) (DEC 的)) (NP (NN 句子成分) (CC 和) (NN 依存)) " +
				"(NP (NN 性))))) (PU 。)))" };

        String[] posTags = new String[] { "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG", "DER",
                "DEV", "DT", "ETC", "FW", "IJ", "JJ", "LB", "LC", "M", "MSP", "NN", "NP", "NR",
                "NT", "OD", "P", "PN", "PU", "SB", "SP", "VA", "VC", "VE", "VP", "VV", "X" };

        String[] constituentTags = new String[] { "ADJP", "ADVP", "CLP", "CP", "DNP", "DP", "DVP",
                "FRAG", "INTJ", "IP", "LCP", "LST", "MSP", "NN", "NP", "PP", "PRN", "QP", "ROOT",
                "UCP", "VCD", "VCP", "VNV", "VP", "VPT", "VRD", "VSB" };

        String[] unmappedPos = new String[] { "NP", "VP" };
        
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		List<PennTree> trees = new ArrayList<PennTree>(select(jcas, PennTree.class));
		AssertAnnotations.assertPennTree(pennTree[0], trees.get(0));
		AssertAnnotations.assertPennTree(pennTree[1], trees.get(1));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        AssertAnnotations.assertTagset(POS.class, "ctb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ctb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ctb", constituentTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Constituent.class, "ctb", new String[] {}, jcas);
	}

	@Test
	public void testEnglish()
		throws Exception
	{
		JCas jcas = runTest("en", documentEnglish);

		String[] constituentMapped = new String[] { "ADJP 10,26", "ADJP 101,109", "ADJP 60,67",
				"NP 0,2", "NP 60,97", "NP 8,109", "NP 8,43", "PP 98,109", "ROOT 0,110", "S 0,110",
				"S 51,109", "SBAR 45,109", "VP 3,109", "VP 51,109", "WHNP 45,50" };

		String[] constituentOriginal = new String[] { "ADJP 10,26", "ADJP 101,109", "ADJP 60,67",
				"NP 0,2", "NP 60,97", "NP 8,109", "NP 8,43", "PP 98,109", "ROOT 0,110", "S 0,110",
				"S 51,109", "SBAR 45,109", "VP 3,109", "VP 51,109", "WHNP 45,50" };

		String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "ADJ", "NN", "NN", "PUNC",
				"ART", "V", "PP", "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

		String[] posOriginal = new String[] { "PRP", "VBP", "DT", "RB", "JJ", "NN", "NN", ",",
				"WDT", "VBZ", "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

		String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) " +
				"(JJ complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) (S " +
				"(VP (VBZ contains) (NP (ADJP (IN as) (JJ many)) (NNS constituents) (CC and) " +
				"(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] posTags = new String[] { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC",
                "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS",
                "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH",
                "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = new String[] { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST",
                "NAC", "NP", "NX", "PP", "PRN", "PRT", "PRT|ADVP", "QP", "ROOT", "RRC", "S", "SBAR",
                "SBARQ", "SINV", "SQ", "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] unmappedPos = new String[] { "#", "$", "''", "-LRB-", "-RRB-", "``" };

        String[] unmappedConst = new String[] { };

		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", unmappedConst, jcas);
	}

	@Test
	public void testGerman()
		throws Exception
	{
		JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel, welches " +
				"möglichst viele Konstituenten und Dependenzen beinhaltet.");

		String[] constituentMapped = new String[] { "ADJP 17,35", "NP 13,110", "NP 54,99",
				"ROOT 0,111", "S 0,110", "S 46,110", "X 0,111", "X 70,99" };

		String[] constituentOriginal = new String[] { "AP 17,35", "CNP 70,99", "NP 13,110",
				"NP 54,99", "PSEUDO 0,111", "ROOT 0,111", "S 0,110", "S 46,110" };

		String[] posOriginal = new String[] { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,",
				"PRELS", "ADV", "PIDAT", "NN", "KON", "NN", "VVFIN", "$." };

		String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "ADJ", "NN", "PUNC", "PR",
				"ADV", "PR", "NN", "CONJ", "NN", "V", "PUNC" };

		String pennTree = "(ROOT (PSEUDO (S (PPER Wir) (VVFIN brauchen) (NP (ART ein) (AP " +
				"(ADV sehr) (ADJA kompliziertes)) (NN Beispiel) ($, ,) (S (PRELS welches) (NP " +
				"(ADV möglichst) (PIDAT viele) (CNP (NN Konstituenten) (KON und) " +
				"(NN Dependenzen))) (VVFIN beinhaltet)))) ($. .)))";

        String[] posTags = new String[] { "$*LRB*", "$,", "$.", "*T1*", "*T2*", "*T3*", "*T4*",
                "*T5*", "*T6*", "*T7*", "*T8*", "--", "ADJA", "ADJD", "ADV", "APPO", "APPR",
                "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI", "KOUS",
                "NE", "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER", "PPOSAT", "PPOSS",
                "PRELAT", "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU",
                "PWAT", "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN",
                "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] constituentTags = new String[] { "---CJ", "AA", "AP", "AVP", "CAC", "CAP", "CAVP",
                "CCP", "CH", "CNP", "CO", "CPP", "CS", "CVP", "CVZ", "DL", "ISU", "MPN", "MTA",
                "NM", "NP", "PP", "PSEUDO", "QL", "ROOT", "S", "VP", "VZ" };

        String[] unmappedPos = new String[] { "$*LRB*", "*T1*", "*T2*", "*T3*", "*T4*", "*T5*",
                "*T6*", "*T7*", "*T8*", "--" };
        
        String[] unmappedConst = new String[] { "---CJ", "PSEUDO" };
        
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        AssertAnnotations.assertTagset(POS.class, "stts", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "stts", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "negra", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "negra", unmappedConst, jcas);
	}

	@Test
	public void testFrench()
		throws Exception
	{
		JCas jcas = runTest("fr", "Nous avons besoin d'une phrase par exemple très " +
				"compliqué, qui contient des constituants que de nombreuses dépendances et que " +
				"possible.");

		String[] constituentMapped = new String[] { "NP 18,30", "NP 59,62", "NP 72,88",
				"NP 93,118", "ROOT 0,135", "S 0,135", "X 0,17", "X 122,134", "X 43,57", "X 59,88",
				"X 63,71", "X 89,118" };

		String[] constituentOriginal = new String[] { "AP 43,57", "NP 18,30", "NP 59,62",
				"NP 72,88", "NP 93,118", "ROOT 0,135", "SENT 0,135", "Srel 59,88", "Ssub 122,134",
				"Ssub 89,118", "VN 0,17", "VN 63,71" };

		String[] posMapped = new String[] { "PR", "V", "V", "N", "N", "PP", "N", "ADV", "ADJ",
				"PUNC", "PR", "V", "ART", "N", "CONJ", "ART", "ADJ", "N", "CONJ", "CONJ", "ADJ",
				"PUNC" };

		String[] posOriginal = new String[] { "CL", "V", "V", "N", "N", "P", "N", "ADV", "A", ",",
				"PRO", "V", "D", "N", "C", "D", "A", "N", "C", "C", "A", "." };

		String pennTree = "(ROOT (ROOT (SENT (VN (CL Nous) (V avons) (V besoin)) (NP (N d'une) " +
				"(N phrase)) (P par) (N exemple) (AP (ADV très) (A compliqué)) (, ,) (Srel (NP " +
				"(PRO qui)) (VN (V contient)) (NP (D des) (N constituants))) (Ssub (C que) (NP " +
				"(D de) (A nombreuses) (N dépendances))) (C et) (Ssub (C que) (A possible)) " +
				"(. .))))";

        String[] posTags = new String[] { "\"", ",", "-LRB-", "-RRB-", ".", ":", "A", "ADV",
                "ADVP", "Afs", "C", "CC", "CL", "CS", "D", "Dmp", "ET", "I", "N", "ND", "P", "PC",
                "PREF", "PRO", "S", "V", "X", "_unknown_", "p", "près" };

        String[] constituentTags = new String[] { "AP", "AdP", "NP", "PP", "ROOT", "SENT", "Sint",
                "Srel", "Ssub", "VN", "VPinf", "VPpart" };

        String[] unmappedPos = new String[] { "\"", "-LRB-", "-RRB-", ":", "ADVP", "Afs", "CC",
                "CS", "Dmp", "ND", "PC", "S", "X", "_unknown_", "p", "près" };

		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        AssertAnnotations.assertTagset(POS.class, "ftb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ftb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ftb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ftb", new String[] {}, jcas);
	}

	/**
	 * Setup CAS to test parser for the English language (is only called once if
	 * an English test is run)
	 */
	private JCas runTest(String aLanguage, String aText)
		throws Exception
	{
		AnalysisEngineDescription segmenter;

		if ("zh".equals(aLanguage)) {
			segmenter = createEngineDescription(LanguageToolSegmenter.class);
		}
		else {
			segmenter = createEngineDescription(StanfordSegmenter.class);
		}

		// setup English
		AnalysisEngineDescription parser = createEngineDescription(BerkeleyParser.class,
				BerkeleyParser.PARAM_PRINT_TAGSET, true,
				BerkeleyParser.PARAM_WRITE_PENN_TREE, true);

		AnalysisEngineDescription aggregate = createEngineDescription(segmenter, parser);

		AnalysisEngine engine = createEngine(aggregate);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage(aLanguage);
		jcas.setDocumentText(aText);
		engine.process(jcas);

		return jcas;
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
