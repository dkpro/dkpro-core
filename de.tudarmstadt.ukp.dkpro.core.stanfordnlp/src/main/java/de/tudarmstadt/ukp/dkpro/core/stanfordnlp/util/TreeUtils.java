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
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util;

import static org.uimafit.util.JCasUtil.selectFollowing;
import static org.uimafit.util.JCasUtil.selectPreceding;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.TreeReader;
import edu.stanford.nlp.util.IntPair;

/**
 * Utility class for the StanfordParser
 * 
 * @author Oliver Ferschke
 * 
 */
public class TreeUtils
{
	/**
	 * Recursively creates an edu.stanford.nlp.trees.Tree from a ROOT annotation It also saves the
	 * whitespaces before and after a token as <code>CoreAnnotation.BeforeAnnotation</code> and
	 * <code>CoreAnnotation.AfterAnnotation</code> in the respective label of the current node.
	 * 
	 * @author Oliver Ferschke
	 * 
	 * @param root
	 *            the ROOT annotation
	 * @return an edu.stanford.nlp.trees.Tree object representing the syntax structure of the
	 *         sentence
	 * @throws CASException
	 */
	public static Tree createStanfordTree(Annotation root)
	{
		return createStanfordTree(root, new LabeledScoredTreeFactory(CoreLabel.factory()));
	}

	public static Tree createStanfordTree(Annotation root, TreeFactory tFact)
	{
		JCas aJCas;
		try {
			aJCas = root.getCAS().getJCas();
		}
		catch (CASException e) {
			throw new IllegalStateException("Unable to get JCas from JCas wrapper");
		}

		// define the new (root) node
		Tree rootNode;

		// before we can create a node, we must check if we have any children (we have to know
		// whether to create a node or a leaf - not very dynamic)
		if (root instanceof Constituent && !isLeaf((Constituent) root)) {
			Constituent node = (Constituent) root;
			List<Tree> childNodes = new ArrayList<Tree>();

			// get childNodes from child annotations
			FSArray children = node.getChildren();
			for (int i = 0; i < children.size(); i++) {
				childNodes.add(createStanfordTree(node.getChildren(i), tFact));
			}

			// now create the node with its children
			rootNode = tFact.newTreeNode(node.getConstituentType(), childNodes);

		}
		else {
			// Handle leaf annotations
			// Leafs are always Token-annotations
			// We also have to insert a Preterminal node with the value of the
			// POS-Annotation on the token
			// because the POS is not directly stored within the treee
			Token wordAnnotation = (Token) root;

			// create leaf-node for the tree
			Tree wordNode = tFact.newLeaf(wordAnnotation.getCoveredText());

			// create information about preceding and trailing whitespaces in the leaf node
			StringBuilder preWhitespaces = new StringBuilder();
			StringBuilder trailWhitespaces = new StringBuilder();

			List<Token> precedingTokenList = selectPreceding(aJCas, Token.class, wordAnnotation, 1);
			List<Token> followingTokenList = selectFollowing(aJCas, Token.class, wordAnnotation, 1);

			if (precedingTokenList.size() > 0) {
				Token precedingToken = precedingTokenList.get(0);
				int precedingWhitespaces = wordAnnotation.getBegin() - precedingToken.getEnd();
				for (int i = 0; i < precedingWhitespaces; i++) {
					preWhitespaces.append(" ");
				}
			}
			if (followingTokenList.size() > 0) {
				Token followingToken = followingTokenList.get(0);
				int trailingWhitespaces = followingToken.getBegin() - wordAnnotation.getEnd();
				for (int i = 0; i < trailingWhitespaces; i++) {
					trailWhitespaces.append(" ");
				}
			}

			// write whitespace information as CoreAnnotation.BeforeAnnotation and
			// CoreAnnotation.AfterAnnotation to the node add annotation to list and write back to
			// node label
			((CoreLabel) wordNode.label()).set(CoreAnnotations.BeforeAnnotation.class,
					preWhitespaces.toString());
			((CoreLabel) wordNode.label()).set(CoreAnnotations.AfterAnnotation.class,
					trailWhitespaces.toString());

			// get POS-annotation
			// get the token that is covered by the POS
			List<POS> coveredPos = JCasUtil.selectCovered(aJCas, POS.class, wordAnnotation);
			// the POS should only cover one token
			assert coveredPos.size() == 1;
			POS pos = coveredPos.get(0);

			// create POS-Node in the tree and attach word-node to it
			rootNode = tFact.newTreeNode(pos.getPosValue(),
					Arrays.asList((new Tree[] { wordNode })));
		}

		return rootNode;
	}

	/**
	 * <p>
	 * Recreates a Stanford Tree from the StanfordParser annotations and saves all
	 * non-StanfordParser-Annotations within the scope of the sentence in the label of the best
	 * fitting node.
	 * </p>
	 * 
	 * <p>
	 * <strong>CAUTION: </strong><i>This method is intended for the use by CAS Multipliers, which
	 * create new CASes from this tree. The annotation-spans in the source-CAS will be changed!!!!!!
	 * You do NOT want to use the source CAS after this method has been called. The
	 * createStanfordTree()-method does not change the CAS, so use this instead, if the annotations
	 * do not have to be recovered or accessed in the tree.</i>
	 * </p>
	 * 
	 * <p>
	 * TODO: This behavior could be changed by making COPIES of the annotations and changing the
	 * copied instead of the originals. However, in order to being able to make copies, a dummy CAS
	 * must be introduced to which the annotations can be copied. When they are recovered, they will
	 * be copied to the new destination CAS anyway.
	 * </p>
	 * 
	 * @param root
	 *            the ROOT annotation
	 * @return an edu.stanford.nlp.trees.Tree object representing the syntax structure of the
	 *         sentence
	 * @throws CASException
	 */
	public static Tree createStanfordTreeWithAnnotations(Annotation root)
		throws CASException
	{
		JCas aJCas = root.getCAS().getJCas();

		// Create tree
		Tree tree = createStanfordTree(root);

		// Get all non-parser related annotations
		// and all tokens (needed for span-calculations later on)
		List<Annotation> nonParserAnnotations = new ArrayList<Annotation>();
		List<Token> tokens = new ArrayList<Token>();

		// Using getCoveredAnnotations instead of iterate, because subiterators did not work in all
		// cases
		List<Annotation> annosWithinRoot = JCasUtil.selectCovered(aJCas, Annotation.class, root);

		for (Annotation curAnno : annosWithinRoot) {
			if (!(curAnno instanceof POS) && !(curAnno instanceof Constituent)
					&& !(curAnno instanceof Dependency) && !(curAnno instanceof PennTree)
					&& !(curAnno instanceof Lemma) && !(curAnno instanceof Token)
					&& !(curAnno instanceof DocumentMetaData)) {
				nonParserAnnotations.add(curAnno);
			}
			else if (curAnno instanceof Token) {
				tokens.add((Token) curAnno);
			}

		}

		// create wrapper for tree and its tokens
		TreeWithTokens annoTree = new TreeWithTokens(tree, tokens);

		/*
		 * Add annotations to the best-fitting nodes. The best-fitting node for an annotation is the
		 * deepest node in the tree that still completely contains the annotation.
		 */
		for (Annotation curAnno : nonParserAnnotations) {
			// get best fitting node
			Tree bestFittingNode = annoTree.getBestFit(curAnno);

			// Add annotation to node
			if (bestFittingNode != null) {

				// translate annotation span to a value relative to the
				// node-span
				IntPair span = annoTree.getSpan(bestFittingNode);
				curAnno.setBegin(curAnno.getBegin() - span.getSource());
				curAnno.setEnd(curAnno.getEnd() - span.getSource());

				// get the collection from the label of the best-fitting node in which we store UIMA
				// annotations or create it, if it does not exist
				Collection<Annotation> annotations = ((CoreLabel) bestFittingNode.label())
						.get(UIMAAnnotations.class);
				if (annotations == null) {
					annotations = new ArrayList<Annotation>();
				}

				// add annotation + checksum of annotated text to list and write it back to node
				// label
				annotations.add(curAnno);

				((CoreLabel) bestFittingNode.label()).set(UIMAAnnotations.class, annotations);
			}
		}

		return tree;
	}

	private static boolean isLeaf(Constituent constituent)
	{
		return (constituent.getChildren() == null || constituent.getChildren().size() == 0);
	}

	/**
	 * Returns the sentence from its tree representation.
	 * 
	 * @param t
	 *            the tree representation of the sentence
	 * @return the sentence
	 */
	public static String pennString2Words(String penn)
		throws IOException
	{
		return tree2Words(pennString2Tree(penn));
	}

	/**
	 * Returns the sentence from its tree representation.
	 * 
	 * @param t
	 *            the tree representation of the sentence
	 * @return the sentence
	 */
	public static String tree2Words(Tree t)
	{
		StringBuilder buffer = new StringBuilder();

		List<Tree> leaves = t.getLeaves();
		for (Tree leaf : leaves) {
			String word = ((CoreLabel) leaf.label()).get(CoreAnnotations.ValueAnnotation.class);

			// TODO maybe double check preceding whitespaces, because transformations could have
			// resulted in the situation that the trailing
			// whitespaces of out last tokens is not the same as the preceding whitespaces of out
			// current token BUT: This has also to be done in getTokenListFromTree(...)

			// now add the trailing whitespaces
			String trailingWhitespaces = ((CoreLabel) leaf.label())
					.get(CoreAnnotations.AfterAnnotation.class);
			// if no whitespace-info is available, insert a whitespace this may happen for nodes
			// inserted by TSurgeon operations
			if (trailingWhitespaces == null) {
				trailingWhitespaces = " ";
			}

			buffer.append(word).append(trailingWhitespaces);
		}

		return buffer.toString();
	}

	/**
	 * Returns a list of Token annotations from a Tree-object
	 * 
	 * @param aJCas
	 * @param t
	 * @return
	 */
	public static List<Token> getTokenListFromTree(JCas aJCas, Tree t)
	{
		List<Token> tokenList = new ArrayList<Token>();
		int index = 0;
		for (Tree leaf : t.getLeaves()) {

			String word = ((CoreLabel) leaf.label()).get(CoreAnnotations.ValueAnnotation.class);

			tokenList.add(new Token(aJCas, index, index + word.length()));

			// get trailing whitespaces to calculate next index
			String whiteSpaces = ((CoreLabel) leaf.label())
					.get(CoreAnnotations.AfterAnnotation.class);
			if (whiteSpaces == null) {
				whiteSpaces = " ";
			}

			index += word.length() + whiteSpaces.length();
		}
		return tokenList;
	}

	/**
	 * Reimplementation of the indexLeaves() method of stanford tree objects. This method reindexes
	 * already indexed trees starting with index 1. The method expects trees with
	 * <code>CoreMap</code>-type labels.
	 * 
	 * @see edu.stanford.nlp.trees.Tree.indexLeaves()
	 * @param t
	 *            a tree with CoreLabel-type labels.
	 */
	public static void reIndexLeaves(Tree t)
	{
		reIndexLeaves(t, 1);
	}

	private static int reIndexLeaves(Tree t, int startIndex)
	{
		if (t.isLeaf()) {
			CoreLabel afl = (CoreLabel) t.label();
			afl.setIndex(startIndex);
			startIndex++;
		}
		else {
			for (Tree child : t.children()) {
				startIndex = reIndexLeaves(child, startIndex);
			}
		}
		return startIndex;
	}

	/**
	 * Reads in a Penn Treebank-style String and returns a tree.
	 * 
	 * @param pennString
	 *            A Penn Treebank-style String as produced by the StandfordParser
	 * @return a tree representation of the PennString (LabeledScoredTree)
	 */
	public static Tree pennString2Tree(String pennString)
		throws IOException
	{
		TreeReader tr = new PennTreeReader(new StringReader(pennString),
				new LabeledScoredTreeFactory());
		return tr.readTree();
	}

}
