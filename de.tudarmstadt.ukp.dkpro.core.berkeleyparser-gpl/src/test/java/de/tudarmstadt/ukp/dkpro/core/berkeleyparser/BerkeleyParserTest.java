/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.berkeleyparser;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectSingle;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Ignore;
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
	
	@Ignore("We don't seem to have a proper tokenizer for arabic...")
	@Test
	public void testArabic()
		throws Exception
	{
		JCas jcas = runTest("ar", "نحن بحاجة إلى مثال على جملة معقدة جدا، والتي تحتوي على مكونات مثل العديد من والتبعيات وقت ممكن.");
		
		String[] constituentMapped = new String[] { "NP 0,1", "ROOT 0,1" };

		String[] constituentOriginal = new String[] { "NP 0,1", "ROOT 0,1" };

		String[] posMapped = new String[] { "POS", "POS" };

		String[] posOriginal = new String[] { "PUNC", "PUNC" };

		String pennTree = "...";

		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
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
		
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
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

		String[] posMapped = new String[] { "PR", "V", "V", "ADJ", "ADJ", "O", "NN", "PUNC", "PR",
				"V", "V", "ADJ", "ADJ", "O", "NN", "CONJ", "NN", "NN", "PUNC" };

		String[] posOriginal = new String[] { "PN", "VV", "VV", "AD", "VA", "DEC", "NN", "PU",
				"PN", "VV", "VV", "AD", "VA", "DEC", "NN", "CC", "NN", "NN", "PU" };
		
		String[] pennTree = new String[] { "(ROOT (IP (NP (PN 我们)) (VP (VV 需要) (VP (VV 一个) " +
				"(NP (CP (IP (VP (ADVP (AD 非常)) (VP (VA 复杂)))) (DEC 的)) (NP (NN 例句))))) " +
				"(PU ，)))", "(ROOT (IP (NP (PN 它)) (VP (VV 要) (VP (VV 包含) (NP (CP (IP (VP " +
				"(ADVP (AD 尽可能)) (VP (VA 多)))) (DEC 的)) (NP (NN 句子成分) (CC 和) (NN 依存)) " +
				"(NP (NN 性))))) (PU 。)))" };
		
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		List<PennTree> trees = new ArrayList<PennTree>(select(jcas, PennTree.class));
		AssertAnnotations.assertPennTree(pennTree[0], trees.get(0));
		AssertAnnotations.assertPennTree(pennTree[1], trees.get(1));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
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

		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
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

		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
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
		
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
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
			segmenter = createPrimitiveDescription(LanguageToolSegmenter.class);
		}
		else {
			segmenter = createPrimitiveDescription(StanfordSegmenter.class);
		}

		// setup English
		AnalysisEngineDescription parser = createPrimitiveDescription(BerkeleyParser.class,
				BerkeleyParser.PARAM_PRINT_TAGSET, true,
				BerkeleyParser.PARAM_CREATE_PENN_TREE_STRING, true);

		AnalysisEngineDescription aggregate = createAggregateDescription(segmenter, parser);
		
		AnalysisEngine engine = createPrimitive(aggregate);
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
