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
import static org.uimafit.factory.AnalysisEngineFactory.createAggregate;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.iterate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeUtils;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;

/**
 * @author Oliver Ferschke
 * @author Niklas Jakob
 *
 */
public class StanfordParserTest
{

	static JCas englishCas = null;
	static JCas germanCas = null;

	static final String documentEnglish = "We need a very complicated example sentence, which contains as many constituents and dependencies as possible.";
	static final String documentGerman = "Wir brauchen ein sehr kompliziertes Beispiel, welches möglichst viele Konstituenten und Dependenzen beinhaltet .";

	// TODO Maybe test link to parents (not tested by syntax tree recreation)

	@Test
	@Ignore("German Test currently not working.")
	public void testGermanConstituents()
		throws Exception
	{
		if (germanCas == null) {
			setupGerman();
		}

		// TODO gold constituents have to be changed to SPAN instead of token
		// offset
		HashSet<String> constituentGold = new HashSet<String>();
		constituentGold.add("NUR 1,15");
		constituentGold.add("ROOT 1,15");
		constituentGold.add("S 1,14");
		constituentGold.add("NP 3,14");
		constituentGold.add("NP 9,13");
		constituentGold.add("AP 4,5");
		constituentGold.add("S 8,14");
		constituentGold.add("CNP 11,13");
		constituentGold.add("AP 9,10");

		// assertTrue("Constituents count mismatch", constituentIndex.size() ==
		// constituentsGold.size());
		boolean okCons = true;
		System.out.println("Checking constituents...");
		for (Constituent currConst : iterate(germanCas,
				Constituent.class)) {
			String constType = currConst.getConstituentType();

			// get covered tokens without using subiterators - subiterators did
			// not work all the time
			List<Token> coveredTokens = JCasUtil.selectCovered(
					germanCas, Token.class, currConst);
			Token firstToken = coveredTokens.get(0); // get first token
			Token lastToken = coveredTokens.get(coveredTokens.size() - 1); // get
																			// last
																			// token

			int firstTokenOffset = firstToken.getBegin();
			int lastTokenOffset = lastToken.getEnd();
			String toFind = constType.toUpperCase() + " " + firstTokenOffset
					+ "," + lastTokenOffset;
			System.out.println("CONST: "
					+ toFind
					+ " ["
					+ germanCas.getDocumentText().substring(
							firstToken.getBegin(), lastToken.getEnd()) + "] - "
					+ constituentGold.contains(toFind));
			okCons &= constituentGold.contains(toFind);
			constituentGold.remove(toFind);
		}

		if (constituentGold.size() > 0) {
			okCons = false;
		}

		assertTrue(
				"Constituents did not match the gold standard. Gold constituents that were not found: "
						+ constituentGold, okCons);

	}

	@Test
	public void testEnglishConstituents()
		throws Exception
	{
		if (englishCas == null) {
			setupEnglish();
		}

		Set<String> constituentsGold = new HashSet<String>();
		constituentsGold.add("ROOT 0,110");
		constituentsGold.add("S 0,110");
		constituentsGold.add("NP 0,2");
		constituentsGold.add("VP 3,109");
		constituentsGold.add("NP 8,109");
		constituentsGold.add("NP 8,43");
		constituentsGold.add("ADJP 10,26");
		constituentsGold.add("SBAR 45,109");
		constituentsGold.add("WHNP 45,50");
		constituentsGold.add("VP 51,109");
		constituentsGold.add("PP 60,109");
		constituentsGold.add("NP 63,109");
		constituentsGold.add("NP 63,97");
		constituentsGold.add("S 51,109");
		constituentsGold.add("ADJP 98,109");

		// assertTrue("Constituents count mismatch", constituentIndex.size() ==
		// constituentsGold.size());
		boolean okCons = true;
		System.out.println("Checking constituents...");
		for (Constituent currConst : iterate(englishCas,
				Constituent.class)) {
			String constType = currConst.getConstituentType();

			// get covered tokens without using subiterators - subiterators did
			// not work all the time
			List<Token> coveredTokens = JCasUtil.selectCovered(
					englishCas, Token.class, currConst);
			Token firstToken = coveredTokens.get(0); // get first token
			Token lastToken = coveredTokens.get(coveredTokens.size() - 1); // get
																			// last
																			// token

			int firstTokenOffset = firstToken.getBegin();
			int lastTokenOffset = lastToken.getEnd();
			String toFind = constType.toUpperCase() + " " + firstTokenOffset
					+ "," + lastTokenOffset;
			System.out.println("CONST: "
					+ toFind
					+ " ["
					+ englishCas.getDocumentText().substring(
							firstToken.getBegin(), lastToken.getEnd()) + "] - "
					+ constituentsGold.contains(toFind));
			okCons &= constituentsGold.contains(toFind);
			constituentsGold.remove(toFind);
		}

		if (constituentsGold.size() > 0) {
			okCons = false;
		}

		assertTrue(
				"Constituents did not match the gold standard. Gold constituents that were not found: "
						+ constituentsGold, okCons);
	}

	@Test
	public void testEnglishDependencies()
		throws Exception
	{
		if (englishCas == null) {
			setupEnglish();
		}

		Set<String> dependenciesGold = new HashSet<String>();
		dependenciesGold.add("CC 68,80,81,84");
		dependenciesGold.add("PREP 51,59,60,62");
		dependenciesGold.add("RCMOD 35,43,51,59");
		dependenciesGold.add("DET 35,43,8,9");
		dependenciesGold.add("POBJ 60,62,68,80");
		dependenciesGold.add("ADVMOD 15,26,10,14");
		dependenciesGold.add("ADVMOD 101,109,98,100");
		dependenciesGold.add("AMOD 35,43,15,26");
		dependenciesGold.add("AMOD 68,80,101,109");
		dependenciesGold.add("AMOD 68,80,63,67");
		dependenciesGold.add("CONJ 68,80,85,97");
		dependenciesGold.add("NN 35,43,27,34");
		dependenciesGold.add("DOBJ 3,7,35,43");
		dependenciesGold.add("NSUBJ 3,7,0,2");
		dependenciesGold.add("NSUBJ 51,59,45,50");

		boolean okDep = true;
		if (dependenciesGold != null) {
			FSIndex<Annotation> dependencyIndex = englishCas
					.getAnnotationIndex(Dependency.type);
			// assertTrue("Dependency count mismatch", dependencyIndex.size() ==
			// dependenciesGold.size());

			// Just to see what has been parsed.
			System.out.println("Checking dependencies...");
			System.out
					.println("# gold dependencies " + dependenciesGold.size());
			System.out
					.println("# found dependencies " + dependencyIndex.size());
			for (Dependency currDep : iterate(englishCas,
					Dependency.class)) {
				String depType = currDep.getDependencyType().toUpperCase();
				int governorBegin = currDep.getGovernor().getBegin();
				int governorEnd = currDep.getGovernor().getEnd();
				int dependentBegin = currDep.getDependent().getBegin();
				int dependentEnd = currDep.getDependent().getEnd();

				String toFind = depType + " " + governorBegin + ","
						+ governorEnd + "," + dependentBegin + ","
						+ dependentEnd;
				System.out.println("DEP: " + toFind + " - "
						+ dependenciesGold.contains(toFind));
				okDep &= dependenciesGold.contains(toFind);
				dependenciesGold.remove(toFind);
			}

			if (dependenciesGold.size() > 0) {
				okDep = false;
			}

		}
		assertTrue(
				"Dependencies did not match the gold standard. Gold dependencies that were not found: "
						+ dependenciesGold, okDep);
	}

	@Test
	public void testEnglishPos()
		throws Exception
	{
		if (englishCas == null) {
			setupEnglish();
		}

		Set<String> posGold = new HashSet<String>();
		posGold.add("PRP 0,2");
		posGold.add("VBP 3,7");
		posGold.add("DT 8,9");
		posGold.add("RB 10,14");
		posGold.add("VBN 15,26");
		posGold.add("NN 27,34");
		posGold.add("NN 35,43");
		posGold.add(", 43,44");
		posGold.add("WDT 45,50");
		posGold.add("VBZ 51,59");
		posGold.add("IN 60,62");
		posGold.add("JJ 63,67");
		posGold.add("NNS 68,80");
		posGold.add("CC 81,84");
		posGold.add("NNS 85,97");
		posGold.add("RB 98,100");
		posGold.add("JJ 101,109");
		posGold.add(". 109,110");

		boolean okPos = true;
		if (posGold != null) {
			FSIndex<Annotation> posIndex = englishCas
					.getAnnotationIndex(POS.type);

			System.out.println("Checking POS tags...");
			System.out.println("# gold POS tags " + posGold.size());
			System.out.println("# found pos tags " + posIndex.size());
			for (POS curPos : iterate(englishCas, POS.class)) {
				String posType = curPos.getPosValue();
				int posBegin = curPos.getBegin();
				int posEnd = curPos.getEnd();

				String toFind = posType + " " + posBegin + "," + posEnd;
				System.out.println("POS: " + toFind + " - "
						+ posGold.contains(toFind));
				okPos &= posGold.contains(toFind);
				posGold.remove(toFind);
			}

			if (posGold.size() > 0) {
				okPos = false;
			}

		}
		assertTrue(
				"POS tags did not match the gold standard. Gold POS tags that were not found: "
						+ posGold, okPos);
	}

	@Test
	public void testEnglishLemmatizer()
		throws Exception
	{
		if (englishCas == null) {
			setupEnglish();
		}

		Set<String> lemmaGold = new HashSet<String>();
		lemmaGold.add("we 0,2");
		lemmaGold.add("need 3,7");
		lemmaGold.add("a 8,9");
		lemmaGold.add("very 10,14");
		lemmaGold.add("complicate 15,26");
		lemmaGold.add("example 27,34");
		lemmaGold.add("sentence 35,43");
		lemmaGold.add(", 43,44");
		lemmaGold.add("which 45,50");
		lemmaGold.add("contain 51,59");
		lemmaGold.add("as 60,62");
		lemmaGold.add("many 63,67");
		lemmaGold.add("constituent 68,80");
		lemmaGold.add("and 81,84");
		lemmaGold.add("dependency 85,97");
		lemmaGold.add("as 98,100");
		lemmaGold.add("possible 101,109");
		lemmaGold.add(". 109,110");
		boolean okLemma = true;
		if (lemmaGold != null) {
			FSIndex<Annotation> posIndex = englishCas
					.getAnnotationIndex(POS.type);

			System.out.println("Checking Lemma tags...");
			System.out.println("# gold lemma tags " + lemmaGold.size());
			System.out.println("# found lemma tags " + posIndex.size());
			for (Lemma curLemma : iterate(englishCas, Lemma.class)) {
				String lemma = curLemma.getValue();
				int lemmaBegin = curLemma.getBegin();
				int lemmaEnd = curLemma.getEnd();

				String toFind = lemma + " " + lemmaBegin + "," + lemmaEnd;
				System.out.println("Lemma: " + toFind + " - "
						+ lemmaGold.contains(toFind));
				okLemma &= lemmaGold.contains(toFind);
				lemmaGold.remove(toFind);
			}

			if (lemmaGold.size() > 0) {
				okLemma = false;
			}

		}
		assertTrue(
				"Lemmas did not match the gold standard. Gold lemmas that were not found: "
						+ lemmaGold, okLemma);
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
	public void testEnglishSytaxTreeReconstruction()
		throws Exception
	{
		if (englishCas == null) {
			setupEnglish();
		}

		String pennOriginal = "";
		String pennFromRecreatedTree = "";

		// As we only have one input sentence, each loop only runs once!

		for (PennTree curPenn : iterate(englishCas, PennTree.class)) {
			// get original penn representation of syntax tree
			pennOriginal = curPenn.getPennTree();
		}

		for (ROOT curRoot : iterate(englishCas, ROOT.class)) {
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
	private void setupEnglish()
		throws Exception
	{
		checkModel("/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/lexparser-en-pcfg.ser.gz");

		AnalysisEngineDescription segmenter = createPrimitiveDescription(StanfordSegmenter.class);

		// setup English
		AnalysisEngineDescription parser = createPrimitiveDescription(StanfordParser.class,
				StanfordParser.PARAM_MODEL,
				"classpath:/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/lexparser-en-pcfg.ser.gz",
				StanfordParser.PARAM_LANGUAGE_PACK, PennTreebankLanguagePack.class.getName(),
				StanfordParser.PARAM_CREATE_CONSTITUENT_TAGS, true,
				StanfordParser.PARAM_CREATE_DEPENDENCY_ANNOTATION_ON_TOKEN, true,
				StanfordParser.PARAM_CREATE_DEPENDENCY_TAGS, true,
				StanfordParser.PARAM_CREATE_PENN_TREE_STRING, true,
				StanfordParser.PARAM_CREATE_POS_TAGS, true);

		AnalysisEngineDescription aggregate = createAggregateDescription(segmenter, parser);

		AnalysisEngine engine = createAggregate(aggregate);
		englishCas = engine.newJCas();
		englishCas.setDocumentText(documentEnglish);
		englishCas.setDocumentLanguage("en");

		engine.process(englishCas);
	}

	/**
	 * Setup CAS to test parser for the German language (is only called once if
	 * a German test is run)
	 */
	private void setupGerman()
		throws Exception
	{
		AnalysisEngineDescription segmenter = createPrimitiveDescription(StanfordSegmenter.class);

		// setup German
		AnalysisEngineDescription parser = createPrimitiveDescription(StanfordParser.class,
				StanfordParser.PARAM_MODEL,
				"classpath:/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/lexparser-de-factored.ser.gz",
				StanfordParser.PARAM_LANGUAGE_PACK, PennTreebankLanguagePack.class.getName(),
				StanfordParser.PARAM_CREATE_CONSTITUENT_TAGS, true,
				StanfordParser.PARAM_CREATE_DEPENDENCY_ANNOTATION_ON_TOKEN, true,
				StanfordParser.PARAM_CREATE_DEPENDENCY_TAGS, true);

		AnalysisEngineDescription aggregate = createAggregateDescription(segmenter, parser);

		AnalysisEngine engine = createAggregate(aggregate);
		germanCas = engine.newJCas();
		germanCas.setDocumentText(documentGerman);
		germanCas.setDocumentLanguage("de");

		engine.process(germanCas);
	}

    private
    void checkModel(String aModel)
    {
		Assume.assumeTrue(getClass().getResource(aModel) != null);
    }
}
