/**
 * Copyright 2007-2017
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.berkeleyparser;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertConstituents;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertPOS;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertPennTree;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertTagset;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertTagsetMapping;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class BerkeleyParserTest
{
	static final String documentEnglish = "We need a very complicated example sentence , which " +
			"contains as many constituents and dependencies as possible .";

	@Test
	public void testArabic()
		throws Exception
	{
        JCas jcas = runTest("ar",
                "نحتاج مثالا معقدا جدا ل جملة تحتوي على أكبر قدر ممكن من العناصر و الروابط .");

        String[] constituentMapped = { "ROOT 0,75", "X 0,75" };

        String[] constituentOriginal = { "ROOT 0,75", "X 0,75" };

        String[] dependencies = {};

        String pennTree = "(ROOT (ROOT (X (PUNC نحتاج) (PUNC مثالا) (NN معقدا) (NN جدا) (NN ل) (NN جملة) "
                + "(NN تحتوي) (NN على) (NN أكبر) (NN قدر) (NN ممكن) (NN من) (NN العناصر) (NN و) (NN الروابط) "
                + "(PUNC .))))";

        String[] posMapped = { "PUNCT", "PUNCT", "NOUN", "NOUN", "NOUN", "NOUN", "NOUN", "NOUN",
                "NOUN", "NOUN", "NOUN", "NOUN", "NOUN", "NOUN", "NOUN", "PUNCT" };

        String[] posOriginal = { "PUNC", "PUNC", "NN", "NN", "NN", "NN", "NN", "NN", "NN", "NN",
                "NN", "NN", "NN", "NN", "NN", "PUNC" };

        String[] posTags = { "CC", "CD", "DEM", "DT", "IN", "JJ", "NN", "NNP", "NNPS", "NNS",
                "NOFUNC", "NUMCOMMA", "PRP", "PRP$", "PUNC", "RB", "RP", "UH", "VB", "VBD", "VBN",
                "VBP", "VERB", "WP", "WRB" };

        String[] constituentTags = { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST", "NAC", "NP",
                "NX", "PP", "PRN", "PRT", "QP", "ROOT", "S", "SBAR", "SBARQ", "SINV", "SQ", "UCP",
                "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] unmappedPos = { "DEM", "NOFUNC", "NUMCOMMA", "PRP$", "VERB" };

        String[] unmappedConst = { "LST", "SINV" };
        
        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "atb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "atb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "atb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "atb", unmappedConst, jcas);
	}

	@Test
	public void testBulgarian()
		throws Exception
	{
		JCas jcas = runTest("bg", "Имаме нужда от един много сложен пример изречение , " +
				"което съдържа най-много съставки и зависимости, колкото е възможно .");

        String[] constituentMapped = { "ROOT 0,120", "X 0,118", "X 0,120", "X 0,5", "X 100,107",
                "X 100,118", "X 108,109", "X 108,118", "X 110,118", "X 12,118", "X 12,14",
                "X 15,118", "X 15,19", "X 15,39", "X 20,25", "X 20,32", "X 20,39", "X 26,32",
                "X 33,39", "X 40,118", "X 40,49", "X 40,84", "X 50,84", "X 52,57", "X 52,84",
                "X 58,65", "X 58,84", "X 6,11", "X 6,118", "X 66,75", "X 66,84", "X 76,84",
                "X 85,86", "X 87,118", "X 87,99" };

        String[] constituentOriginal = { "A 26,32", "APA 20,32", "Adv 100,107", "Adv 110,118",
                "Adv 20,25", "Adv 66,75", "Adv 87,99", "AdvPA 87,118", "C 85,86", "CL 100,118",
                "CLR 50,84", "Conj 85,86", "ConjArg 40,84", "ConjArg 87,118", "CoordP 40,118",
                "M 15,19", "N 33,39", "N 40,49", "N 6,11", "N 76,84", "NPA 15,118", "NPA 15,39",
                "NPA 20,39", "NPA 40,84", "NPA 6,118", "NPA 66,84", "PP 12,118", "Prep 12,14",
                "Pron 52,57", "ROOT 0,120", "S 0,120", "V 0,5", "V 108,109", "V 58,65",
                "VPA 100,118", "VPC 0,118", "VPC 108,118", "VPC 58,84", "VPS 52,84" };

        String[] posMapped = { "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS",
                "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS" };

        String[] posOriginal = { "Vpitf", "Ncfsi", "R", "Mcmsi", "Md", "Amsi", "Ncmsi", "Ncnsi",
                "pt", "Pre", "Vpitf", "Md", "Ncmpi", "Cp", "Dm", "Prq", "Vxitf", "Dd", "pt" };

		String pennTree = "(ROOT (ROOT (S (VPC (V (Vpitf Имаме)) (NPA (N (Ncfsi нужда)) (PP "
		        + "(Prep (R от)) (NPA (NPA (M (Mcmsi един)) (NPA (APA (Adv (Md много)) (A "
		        + "(Amsi сложен))) (N (Ncmsi пример)))) (CoordP (ConjArg (NPA (N "
		        + "(Ncnsi изречение)) (CLR (pt ,) (VPS (Pron (Pre което)) (VPC (V "
		        + "(Vpitf съдържа)) (NPA (Adv (Md най-много)) (N (Ncmpi съставки)))))))) "
		        + "(Conj (C (Cp и))) (ConjArg (AdvPA (Adv (Dm зависимости,)) (CL (VPA (Adv "
		        + "(Prq колкото)) (VPC (V (Vxitf е)) (Adv (Dd възможно)))))))))))) (pt .))))";
		
        String[] posTags = { "A", "Afsd", "Afsi", "Ams", "Amsf", "Amsh", "Amsi",
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

        String[] constituentTags = { "A", "APA", "APC", "Adv", "AdvPA", "AdvPC", "C",
                "CL", "CLCHE", "CLDA", "CLQ", "CLR", "CLZADA", "Conj", "ConjArg", "CoordP",
                "Gerund", "H", "M", "N", "NPA", "NPC", "PP", "Participle", "Prep", "Pron", "ROOT",
                "S", "T", "V", "VPA", "VPC", "VPF", "VPS", "Verbalised" };
        
        String[] unmappedConstituents = { "Conj", "ConjArg", "Verbalised" };

		assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        assertTagset(POS.class, "btb", posTags, jcas);
        // FIXME assertTagsetMapping(POS.class, "btb", new String[] {}, jcas);
        assertTagset(Constituent.class, "btb", constituentTags, jcas);
        assertTagsetMapping(Constituent.class, "btb", unmappedConstituents, jcas);
	}

	@Test
	public void testChinese()
		throws Exception
	{
		JCas jcas = runTest("zh", 
		        "我们 需要 一个 非常 复杂 的 句子 例如 其中 包含 许多 成分 和 尽可能 的 依赖 。");

        String[] constituentMapped = { "ADVP 20,22", "ADVP 9,11", "NP 0,2", "NP 17,19", "NP 23,25",
                "NP 23,34", "NP 32,34", "NP 37,40", "NP 37,45", "NP 43,45", "NP 6,34", "NP 6,45",
                "NP 6,8", "PARN 20,34", "QP 29,31", "ROOT 0,47", "VP 12,14", "VP 26,28", "VP 3,45",
                "VP 9,14", "X 0,47", "X 23,28", "X 37,42", "X 6,14", "X 6,16" };

        String[] constituentOriginal = { "ADVP 20,22", "ADVP 9,11", "CP 6,16", "DNP 37,42",
                "IP 0,47", "IP 23,28", "IP 6,14", "NP 0,2", "NP 17,19", "NP 23,25", "NP 23,34",
                "NP 32,34", "NP 37,40", "NP 37,45", "NP 43,45", "NP 6,34", "NP 6,45", "NP 6,8",
                "PRN 20,34", "QP 29,31", "ROOT 0,47", "VP 12,14", "VP 26,28", "VP 3,45", "VP 9,14" };

        String[] posMapped = { "PRON", "VERB", "NOUN", "ADJ", "VERB", "PART", "NOUN", "ADJ", "NOUN",
                "VERB", "NUM", "NOUN", "CONJ", "NOUN", "PART", "NOUN", "PUNCT" };

        String[] posOriginal = { "PN", "VV", "NN", "AD", "VA", "DEC", "NN", "AD", "NN", "VV", "CD",
                "NN", "CC", "NN", "DEG", "NN", "PU" };

		String pennTree = "(ROOT (IP (NP (PN 我们)) (VP (VV 需要) (NP (NP (CP (IP (NP (NN 一个)) "
		        + "(VP (ADVP (AD 非常)) (VP (VA 复杂)))) (DEC 的)) (NP (NN 句子)) (PRN (ADVP "
		        + "(AD 例如)) (NP (IP (NP (NN 其中)) (VP (VV 包含))) (QP (CD 许多)) (NP "
		        + "(NN 成分))))) (CC 和) (NP (DNP (NP (NN 尽可能)) (DEG 的)) (NP (NN 依赖))))) "
		        + "(PU 。)))";

        String[] posTags = { "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG", "DER", "DEV", "DT",
                "ETC", "FW", "IJ", "JJ", "LB", "LC", "M", "MSP", "NN", "NP", "NR", "NT", "OD", "P",
                "PN", "PU", "SB", "SP", "VA", "VC", "VE", "VP", "VV", "X" };

        String[] constituentTags = { "ADJP", "ADVP", "CLP", "CP", "DNP", "DP", "DVP", "FRAG",
                "INTJ", "IP", "LCP", "LST", "MSP", "NN", "NP", "PP", "PRN", "QP", "ROOT", "UCP",
                "VCD", "VCP", "VNV", "VP", "VPT", "VRD", "VSB" };

        String[] unmappedPos = { "NP", "VP" };
        
		assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		List<PennTree> trees = new ArrayList<PennTree>(select(jcas, PennTree.class));
		assertPennTree(pennTree, trees.get(0));
		assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        assertTagset(POS.class, "ctb", posTags, jcas);
        assertTagsetMapping(POS.class, "ctb", unmappedPos, jcas);
        assertTagset(Constituent.class, "ctb", constituentTags, jcas);
        // FIXME assertTagsetMapping(Constituent.class, "ctb", new String[] {}, jcas);
	}

	@Test
	public void testEnglish()
		throws Exception
	{
		JCas jcas = runTest("en", documentEnglish);

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "ADJP 61,68", "NP 0,2",
                "NP 61,98", "NP 8,110", "NP 8,43", "PP 99,110", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "ADJP 61,68", "NP 0,2",
                "NP 61,98", "NP 8,110", "NP 8,43", "PP 99,110", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "ADJ", "NOUN", "NOUN", "PUNCT", "DET",
                "VERB", "ADP", "ADJ", "NOUN", "CONJ", "NOUN", "ADP", "ADJ", "PUNCT" };

		String[] posOriginal = { "PRP", "VBP", "DT", "RB", "JJ", "NN", "NN", ",",
				"WDT", "VBZ", "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

		String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) " +
				"(JJ complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) (S " +
				"(VP (VBZ contains) (NP (ADJP (IN as) (JJ many)) (NNS constituents) (CC and) " +
				"(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] posTags = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC",
                "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS",
                "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH",
                "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST",
                "NAC", "NP", "NX", "PP", "PRN", "PRT", "PRT|ADVP", "QP", "ROOT", "RRC", "S", "SBAR",
                "SBARQ", "SINV", "SQ", "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] unmappedPos = {};

        String[] unmappedConst = {};

		assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        assertTagset(POS.class, "ptb", posTags, jcas);
        assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        // FIXME assertTagsetMapping(Constituent.class, "ptb", unmappedConst, jcas);
	}

    @Test
    public void testEnglishPreTagged()
        throws Exception
    {
        JCas jcas = runTest("en", null, documentEnglish, true);

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,110",
                "NP 64,98", "NP 8,110", "NP 8,43", "PP 61,110", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,110",
                "NP 64,98", "NP 8,110", "NP 8,43", "PP 61,110", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "ADJ", "NOUN", "NOUN", "PUNCT", "DET",
                "VERB", "ADP", "ADJ", "NOUN", "CONJ", "NOUN", "ADP", "ADJ", "PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "JJ", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP "
                + "(RB very) (JJ complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP "
                + "(WDT which)) (S (VP (VBZ contains) (PP (IN as) (NP (NP (JJ many) "
                + "(NNS constituents) (CC and) (NNS dependencies)) (PP (IN as) (ADJP "
                + "(JJ possible)))))))))) (. .)))";

        String[] posTags = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC", "CD", "DT",
                "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB",
                "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST", "NAC", "NP",
                "NX", "PP", "PRN", "PRT", "PRT|ADVP", "QP", "ROOT", "RRC", "S", "SBAR", "SBARQ",
                "SINV", "SQ", "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] unmappedPos = {};

        String[] unmappedConst = {};

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertTagset(POS.class, "ptb", posTags, jcas);
        assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        // FIXME assertTagsetMapping(Constituent.class, "ptb", unmappedConst,
        // jcas);
    }

	@Test
	public void testGerman()
		throws Exception
	{
		JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches " +
				"möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] constituentMapped = { "ADJP 17,35", "Constituent 0,113", "NP 13,111", "NP 55,100",
                "NP 71,100", "ROOT 0,113", "S 0,111", "S 47,111" };

        String[] constituentOriginal = { "AP 17,35", "CNP 71,100", "NP 13,111", "NP 55,100",
                "PSEUDO 0,113", "ROOT 0,113", "S 0,111", "S 47,111" };

        String[] posOriginal = { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,", "PRELS", "ADV",
                "PIDAT", "NN", "KON", "NN", "VVFIN", "$." };

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "ADJ", "NOUN", "PUNCT", "PRON", "ADV",
                "PRON", "NOUN", "CONJ", "NOUN", "VERB", "PUNCT" };

		String pennTree = "(ROOT (PSEUDO (S (PPER Wir) (VVFIN brauchen) (NP (ART ein) (AP " +
				"(ADV sehr) (ADJA kompliziertes)) (NN Beispiel) ($, ,) (S (PRELS welches) (NP " +
				"(ADV möglichst) (PIDAT viele) (CNP (NN Konstituenten) (KON und) " +
				"(NN Dependenzen))) (VVFIN beinhaltet)))) ($. .)))";

        String[] posTags = { "$*LRB*", "$,", "$.", "*T1*", "*T2*", "*T3*", "*T4*",
                "*T5*", "*T6*", "*T7*", "*T8*", "--", "ADJA", "ADJD", "ADV", "APPO", "APPR",
                "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI", "KOUS",
                "NE", "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER", "PPOSAT", "PPOSS",
                "PRELAT", "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU",
                "PWAT", "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN",
                "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] constituentTags = { "---CJ", "AA", "AP", "AVP", "CAC", "CAP", "CAVP",
                "CCP", "CH", "CNP", "CO", "CPP", "CS", "CVP", "CVZ", "DL", "ISU", "MPN", "MTA",
                "NM", "NP", "PP", "PSEUDO", "QL", "ROOT", "S", "VP", "VZ" };

        String[] unmappedPos = { "$*LRB*", "*T1*", "*T2*", "*T3*", "*T4*", "*T5*",
                "*T6*", "*T7*", "*T8*", "--" };
        
        String[] unmappedConst = { "---CJ", "PSEUDO" };
        
		assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        assertTagset(POS.class, "stts", posTags, jcas);
        assertTagsetMapping(POS.class, "stts", unmappedPos, jcas);
        assertTagset(Constituent.class, "negra", constituentTags, jcas);
        assertTagsetMapping(Constituent.class, "negra", unmappedConst, jcas);
	}

	@Test
	public void testFrench()
		throws Exception
	{
		JCas jcas = runTest("fr", "Nous avons besoin d' une phrase par exemple très " +
				"compliqué , qui contient des constituants que de nombreuses dépendances et que " +
				"possible .");

        String[] constituentMapped = { "ADJP 44,58", "NP 21,90", "NP 36,43", "NP 61,64",
                "NP 74,90", "NP 95,120", "PP 18,90", "PP 32,43", "ROOT 0,138", "S 0,138",
                "SBAR 124,136", "SBAR 61,90", "SBAR 91,120", "VP 0,17", "VP 65,73" };

        String[] constituentOriginal = { "AP 44,58", "NP 21,90", "NP 36,43", "NP 61,64",
                "NP 74,90", "NP 95,120", "PP 18,90", "PP 32,43", "ROOT 0,138", "SENT 0,138",
                "Srel 61,90", "Ssub 124,136", "Ssub 91,120", "VN 0,17", "VN 65,73" };

        String[] posMapped = { "PRON", "VERB", "VERB", "ADP", "DET", "NOUN", "ADP", "NOUN", "ADV",
                "ADJ", "PUNCT", "PRON", "VERB", "DET", "NOUN", "CONJ", "DET", "ADJ", "NOUN", "CONJ",
                "CONJ", "ADJ", "PUNCT" };

        String[] posOriginal = { "CL", "V", "V", "P", "D", "N", "P", "N", "ADV", "A",
                ",", "PRO", "V", "D", "N", "C", "D", "A", "N", "C", "C", "A", "." };

		String pennTree = "(ROOT (ROOT (SENT (VN (CL Nous) (V avons) (V besoin)) (PP (P d') (NP "
		        + "(D une) (N phrase) (PP (P par) (NP (N exemple))) (AP (ADV très) (A compliqué)) "
		        + "(, ,) (Srel (NP (PRO qui)) (VN (V contient)) (NP (D des) (N constituants))))) "
		        + "(Ssub (C que) (NP (D de) (A nombreuses) (N dépendances))) (C et) (Ssub (C que) "
		        + "(A possible)) (. .))))";

        String[] posTags = { "\"", ",", "-LRB-", "-RRB-", ".", ":", "A", "ADV",
                "ADVP", "Afs", "C", "CC", "CL", "CS", "D", "Dmp", "ET", "I", "N", "ND", "P", "PC",
                "PREF", "PRO", "S", "V", "X", "_unknown_", "p", "près" };

        String[] constituentTags = { "AP", "AdP", "NP", "PP", "ROOT", "SENT", "Sint",
                "Srel", "Ssub", "VN", "VPinf", "VPpart" };

        String[] unmappedPos = { "\"", "-LRB-", "-RRB-", "ADVP", "Afs", "CC",
                "CS", "Dmp", "ND", "PC", "S", "X", "_unknown_", "p", "près" };

		assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        assertTagset(POS.class, "ftb", posTags, jcas);
        assertTagsetMapping(POS.class, "ftb", unmappedPos, jcas);
        assertTagset(Constituent.class, "ftb", constituentTags, jcas);
        assertTagsetMapping(Constituent.class, "ftb", new String[] {}, jcas);
	}

	/**
     * Setup CAS to test parser for the English language (is only called once if
     * an English test is run)
     */
    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        return runTest(aLanguage, null, aText, false);
    }
    
    
    private JCas runTest(String aLanguage, String aVariant, String aText, boolean aGoldPos,
            Object... aExtraParams)
        throws Exception
    {
        AggregateBuilder aggregate = new AggregateBuilder();

        if (aGoldPos) {
            aggregate.add(createEngineDescription(OpenNlpPosTagger.class));
        }
        
        Object[] params = new Object[] {
                BerkeleyParser.PARAM_VARIANT, aVariant,
                BerkeleyParser.PARAM_PRINT_TAGSET, true,
                BerkeleyParser.PARAM_WRITE_PENN_TREE, true,
                BerkeleyParser.PARAM_WRITE_POS, !aGoldPos,
                BerkeleyParser.PARAM_READ_POS, aGoldPos};
        params = ArrayUtils.addAll(params, aExtraParams);
        aggregate.add(createEngineDescription(BerkeleyParser.class, params));

        return TestRunner.runTest(aggregate.createAggregateDescription(), aLanguage, aText);
    }
        
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
