/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.junit.Assert.assertTrue;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeUtils;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.Tree;

/**
 * @author Oliver Ferschke
 * @author Niklas Jakob
 *
 */
public class StanfordParserTest
{
	static final String documentEnglish = "We need a very complicated example sentence, which " +
			"contains as many constituents and dependencies as possible.";
	
	// TODO Maybe test link to parents (not tested by syntax tree recreation)

	@Test
	public void testGermanPcfg()
		throws Exception
	{
		JCas jcas = runTest("de", "pcfg", "Wir brauchen ein sehr kompliziertes Beispiel, welches " +
				"möglichst viele Konstituenten und Dependenzen beinhaltet.");

		String[] constituentMapped = new String[] { "ROOT 0,111", "S 0,111", "S 46,110",
				"X 13,110", "X 17,35", "X 64,99", "X 70,99" };

		String[] constituentOriginal = new String[] { "AP 17,35", "CNP 70,99", "NP-OA 13,110",
				"NP-SB 64,99", "ROOT 0,111", "S 0,111", "S 46,110" };

		String[] lemmas = new String[] { /** No lemmatization for German */ };

		String[] posOriginal = new String[] { "PPER-SB", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,",
				"PRELS-SB", "ADV", "PIDAT", "NN", "KON", "NN", "VVFIN", "$." };

		String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "ADJ", "NN", "PUNC", "PR",
				"ADV", "PR", "NN", "CONJ", "NN", "V", "PUNC" };

		String[] dependencies = new String[] { /** No dependencies for German */ };

		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	@Test
	public void testGermanFactored()
		throws Exception
	{
		JCas jcas = runTest("de", "factored", "Wir brauchen ein sehr kompliziertes Beispiel, welches " +
				"möglichst viele Konstituenten und Dependenzen beinhaltet.");

		String[] constituentMapped = new String[] { "ROOT 0,111", "S 0,111", "S 46,110",
				"X 13,110", "X 17,35", "X 54,69", "X 54,99", "X 70,99" };

		String[] constituentOriginal = new String[] { "AP 17,35", "AP 54,69", "CNP 70,99",
				"NP-DA 54,99", "NP-OA 13,110", "ROOT 0,111", "S 0,111", "S 46,110" };

		String[] lemmas = new String[] { /** No lemmatization for German */ };

		String[] posOriginal = new String[] { "PPER-SB", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,",
				"PRELS-SB", "ADV", "PIDAT", "NN", "KON", "NN", "VVFIN", "$." };

		String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "ADJ", "NN", "PUNC", "PR",
				"ADV", "PR", "NN", "CONJ", "NN", "V", "PUNC" };

		String[] dependencies = new String[] { /** No dependencies for German */ };
		
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	@Test
	public void testEnglishPcfg()
		throws Exception
	{
		JCas jcas = runTest("en", "pcfg", documentEnglish);
		
		String[] constituentMapped = new String[] { "ROOT 0,110", "S 0,110", "NP 0,2", "VP 3,109", 
				"NP 8,109", "NP 8,43", "ADJP 10,26", "SBAR 45,109", "WHNP 45,50", "VP 51,109", 
				"S 51,109", "PP 60,97", "NP 63,97", "PP 98,109", "ADJP 101,109" };

		String[] constituentOriginal = new String[] { "ROOT 0,110", "S 0,110", "NP 0,2", "VP 3,109", 
				"NP 8,109", "NP 8,43", "ADJP 10,26", "SBAR 45,109", "WHNP 45,50", "VP 51,109", 
				"S 51,109", "PP 60,97", "NP 63,97", "PP 98,109", "ADJP 101,109" };

		String[] dependencies = new String[] { "ADVMOD 15,26,10,14", "RCMOD 35,43,51,59", 
				"DET 35,43,8,9", "POBJ 60,62,68,80", "POBJ 98,100,101,109", "DOBJ 3,7,35,43", 
				"AMOD 35,43,15,26", "AMOD 68,80,63,67", "NSUBJ 3,7,0,2", "NSUBJ 51,59,45,50",
				"PREP 51,59,60,62", "PREP 51,59,98,100", "CC 68,80,81,84", "NN 35,43,27,34",
				"CONJ 68,80,85,97" };

		String[] lemma = new String[] { "we", "need", "a", "very", "complicate", "example", 
				"sentence", ",", "which", "contain", "as", "many", "constituent", "and",
				"dependency", "as", "possible", "." };

		String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "V", "NN", "NN", "PUNC",
				"ART", "V", "PP", "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

		String[] posOriginal = new String[] { "PRP", "VBP", "DT", "RB", "VBN", "NN", 
				"NN", ",", "WDT", "VBZ", "IN", "JJ", "NNS", "CC",
				"NNS", "IN", "JJ", "." };
		
		AssertAnnotations.assertLemma(lemma, select(jcas, Lemma.class));
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}
	
	@Test
	public void testEnglishFactored()
		throws Exception
	{
		JCas jcas = runTest("en", "factored", documentEnglish);
		
		String[] constituentMapped = new String[] { "ADJP 10,26", "ADJP 101,109", "ADJP 60,67",
				"NP 0,2", "NP 60,97", "NP 8,109", "NP 8,43", "PP 98,109", "ROOT 0,110", "S 0,110",
				"S 51,109", "SBAR 45,109", "VP 3,109", "VP 51,109", "WHNP 45,50" };

		String[] constituentOriginal = new String[] { "ADJP 10,26", "ADJP 101,109", "ADJP 60,67",
				"NP 0,2", "NP 60,97", "NP 8,109", "NP 8,43", "PP 98,109", "ROOT 0,110", "S 0,110",
				"S 51,109", "SBAR 45,109", "VP 3,109", "VP 51,109", "WHNP 45,50" };

		String[] dependencies = new String[] { "ADVMOD 15,26,10,14", "ADVMOD 63,67,60,62",
				"AMOD 35,43,15,26", "AMOD 68,80,63,67", "CC 68,80,81,84", "CONJ 68,80,85,97",
				"DET 35,43,8,9", "DOBJ 3,7,35,43", "DOBJ 51,59,68,80", "NN 35,43,27,34",
				"NSUBJ 3,7,0,2", "NSUBJ 51,59,45,50", "POBJ 98,100,101,109", "PREP 51,59,98,100",
				"RCMOD 35,43,51,59" };

		String[] lemma = new String[] { "we", "need", "a", "very", "complicate", "example", 
				"sentence", ",", "which", "contain", "as", "many", "constituent", "and",
				"dependency", "as", "possible", "." };

		String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "V", "NN", "NN", "PUNC",
				"ART", "V", "ADV", "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

		String[] posOriginal = new String[] { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",",
				"WDT", "VBZ", "RB", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };
		
		AssertAnnotations.assertLemma(lemma, select(jcas, Lemma.class));
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}
	
	@Test
	public void testFrenchFactored()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);
		
		JCas jcas = runTest("fr", "factored", "Nous avons besoin d'une phrase par exemple très " +
				"compliqué, qui contient des constituants que de nombreuses dépendances et que " +
				"possible.");
		
		String[] constituentMapped = new String[] { "NP 11,17", "NP 18,42", "NP 59,62", "NP 72,88",
				"NP 93,118", "ROOT 0,135", "X 0,10", "X 0,135", "X 119,134", "X 122,134",
				"X 126,134", "X 18,57", "X 31,42", "X 43,57", "X 59,88", "X 63,71", "X 89,134" };

		String[] constituentOriginal = new String[] { "AP 126,134", "COORD 119,134", "MWADV 31,42",
				"NP 11,17", "NP 18,42", "NP 59,62", "NP 72,88", "NP 93,118", "ROOT 0,135",
				"SENT 0,135", "Srel 18,57", "Srel 59,88", "Ssub 122,134", "Ssub 89,134", "VN 0,10",
				"VN 43,57", "VN 63,71" };

		String[] dependencies = new String[] { /** No dependencies for French */ };

		String[] lemmas = new String[] { /** No lemmatization for French */ };

		String[] posMapped = new String[] { "PR", "V", "N", "ART", "N", "PP", "N", "ADV", "V",
				"PUNC", "PR", "V", "ART", "N", "CONJ", "ART", "ADJ", "N", "CONJ", "CONJ", "ADJ",
				"PUNC" };

		String[] posOriginal = new String[] { "CL", "V", "N", "D", "N", "P", "N", "ADV", "V",
				"PUNC", "PRO", "V", "D", "N", "C", "D", "A", "N", "C", "C", "A", "PUNC" };
		
		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}
	
//	@Ignore("This requires a class from the parser package that do currently not ship with " +
//			"CoreNLP (ChineseUnknownWordModel). Even if we just add it to DKPro Core and test, " +
//			"we get no proper results. Somebody needs to investigate this. Maybe a tokenizer " +
//			"issue?")
	@Test
	public void testChineseFactored()
		throws Exception
	{
		JCas jcas = runTest("zh", "factored", "我们需要一个非常复杂的句子，例如其中包含许多成分和尽可能的依赖。");
		
		String[] constituentMapped = new String[] { "QP 0,13", "ROOT 0,32", "VP 14,31", "X 0,32" };

		String[] constituentOriginal = new String[] { "IP 0,32", "QP 0,13", "ROOT 0,32", "VP 14,31" };

		String[] dependencies = new String[] { "NSUBJ 14,31,0,13" };

		String[] lemmas = new String[] {  };

		String[] posMapped = new String[] { "CARD", "PUNC", "V", "PUNC" };

		String[] posOriginal = new String[] { "CD", "PU", "VV", "PU" };
		
		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}
	
//	@Ignore("This requires two classes from the parser package that do currently not ship with " +
//			"CoreNLP (ArabicUnknownWordModel and ArabicUnknownWordSignatures). Even if we " +
//			"just add them to DKPro Core and test, we get no proper results. Somebody needs to " +
//			"investigate this. Maybe a tokenizer issue?")
	@Test
	public void testArabicFactored()
		throws Exception
	{
		JCas jcas = runTest("ar", "factored", "نحن بحاجة إلى مثال على جملة معقدة جدا، والتي تحتوي على مكونات مثل العديد من والتبعيات وقت ممكن.");
		
		String[] constituentMapped = new String[] { "NP 0,1", "ROOT 0,1" };

		String[] constituentOriginal = new String[] { "NP 0,1", "ROOT 0,1" };

		String[] dependencies = new String[] {  };

		String[] lemmas = new String[] {  };

		String[] posMapped = new String[] { "POS", "POS" };

		String[] posOriginal = new String[] { "NN", "NN" };
		
		AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
		AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}
	/**
	 * This tests whether a complete syntax tree can be recreated from the
	 * annotations without any loss. Consequently, all links to children should
	 * be correct. (This makes no assertions about the parent-links, because
	 * they are not used for the recreation)
	 *
	 * @throws Exception
	 */
	@Test
	public void testEnglishSyntaxTreeReconstruction()
		throws Exception
	{
		JCas jcas = runTest("en", "factored", documentEnglish);

		String pennOriginal = "";
		String pennFromRecreatedTree = "";

		// As we only have one input sentence, each loop only runs once!

		for (PennTree curPenn : select(jcas, PennTree.class)) {
			// get original penn representation of syntax tree
			pennOriginal = curPenn.getPennTree();
		}

		for (ROOT curRoot : select(jcas, ROOT.class)) {
			// recreate syntax tree
			Tree recreation = TreeUtils.createStanfordTree(curRoot);

			// make a tree with simple string-labels
			recreation = recreation.deepCopy(recreation.treeFactory(), StringLabel.factory());

			pennFromRecreatedTree = recreation.pennString();
		}

		assertTrue(
				"The recreated syntax-tree did not match the input syntax-tree.",
				pennOriginal.equals(pennFromRecreatedTree));
	}

	/**
	 * Setup CAS to test parser for the English language (is only called once if
	 * an English test is run)
	 */
	private JCas runTest(String aLanguage, String aVariant, String aText)
		throws Exception
	{
		AnalysisEngineDescription segmenter = createPrimitiveDescription(StanfordSegmenter.class);

		// setup English
		AnalysisEngineDescription parser = createPrimitiveDescription(StanfordParser.class,
				StanfordParser.PARAM_VARIANT, aVariant,
				StanfordParser.PARAM_PRINT_TAGSET, true,
				StanfordParser.PARAM_CREATE_CONSTITUENT_TAGS, true,
				StanfordParser.PARAM_CREATE_DEPENDENCY_TAGS, true,
				StanfordParser.PARAM_CREATE_PENN_TREE_STRING, true,
				StanfordParser.PARAM_CREATE_POS_TAGS, true);

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
