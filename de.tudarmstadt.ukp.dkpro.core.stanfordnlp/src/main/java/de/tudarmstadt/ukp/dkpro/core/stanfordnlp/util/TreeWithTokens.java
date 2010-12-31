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

import java.util.Iterator;
import java.util.List;

import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.IntPair;

/**
 * A wrapper object that manages a tree object together with the respective
 * Token annotations for the leafs of the tree. This is needed for being able to
 * map the leaves of the tree to words in a CAS.
 *
 * Trees in TreeWithToken-object are always converted to trees with
 * CoreLabel-type labels.
 *
 * @author Oliver Ferschke
 *
 */
public class TreeWithTokens
{
	private Tree tree;
	private List<Token> tokens;

	public TreeWithTokens(Tree tree, List<Token> tokens)
	{
		setTree(tree);
		setTokens(tokens);
	}

	public void setTree(Tree tree)
	{
		if (!(tree.label() instanceof CoreLabel)) {
			tree = tree.deepCopy(tree.treeFactory(), CoreLabel.factory());
		}

		tree.indexLeaves();

		this.tree = tree;
	}

	public Tree getTree()
	{
		return tree;
	}

	public void setTokens(List<Token> tokens)
	{
		this.tokens = tokens;
	}

	public List<Token> getTokens()
	{
		return tokens;
	}

	/**
	 * Returns the span of the documentText that is covered by this
	 * TreeWithTokens.
	 *
	 * @return an IntPair describing the span of the documentText that is
	 *         covered by this tree
	 */
	public IntPair getSpan()
	{
		return getSpan(getTree());
	}

	/**
	 * Returns the span of the documentText that is covered by a given subtree,
	 * that has to be taken directly from the original tree.<br/>
	 *
	 * NOTE: Possibly we could make this more general to also support general
	 * trees that are contained in the original tree, but are not directly taken
	 * from it (i.e. with different leaf-numbering). In order to do so, we would
	 * have to make a Tregex-Matching of the given subtree in the original tree
	 * to identify the positition of the given subtree.<br/>
	 * This could be achieved by translating the subtree into a Tregex pattern
	 * and then matching this pattern against the original tree.
	 *
	 * @param subtree
	 *            a subtree of this TreeWithTokens (it has to be a real
	 *            subtree(!), because index numbering of subtree has to fit to
	 *            the numbering of the original tree)
	 * @return an IntPair describing the span of the documentText that is
	 *         covered by this tree
	 */
	public IntPair getSpan(Tree subtree)
	{
		// TODO check if subtree is a real subtree of tokenTree.getTree()

		int nodeIndexLeft = ((CoreLabel) getLeftmostLeaf(subtree).label())
				.index();
		int nodeIndexRight = ((CoreLabel) getRightmostLeaf(subtree).label())
				.index();
		int a = tokens.get(nodeIndexLeft - 1).getBegin();
		int b = tokens.get(nodeIndexRight - 1).getEnd();

		return new IntPair(a, b);
	}

	private Tree getLeftmostLeaf(Tree t)
	{
		if (t.isLeaf()) {
			return t;
		}
		else {
			return getLeftmostLeaf(t.firstChild());
		}
	}

	private Tree getRightmostLeaf(Tree t)
	{
		if (t.isLeaf()) {
			return t;
		}
		else {
			return getRightmostLeaf(t.lastChild());
		}
	}

	/**
	 * Finds the best-fitting node in the tree for a given annotation.
	 *
	 * The best-fitting node for an annotation is the deepest node in the tree
	 * that still completely contains the span of the given annotation.
	 *
	 * TODO Could be done more efficiently, I think. In a recursive method, for
	 * example, recursion could be stopped as soon as overlap becomes -1
	 *
	 * @param anno
	 *            the annotation to find a best fit for
	 *
	 * @return the node of the tree that is the best fit for <code>anno</code>
	 */
	public Tree getBestFit(Annotation anno)
	{
		Tree curBestFit = null;
		int curBestOverlap = Integer.MAX_VALUE;

		Iterator<Tree> treeIterator = getTree().iterator();
		while (treeIterator.hasNext()) {
			Tree curTree = treeIterator.next();
			IntPair span = getSpan(curTree);

			// calc overlap: if annotation not completely contained in span of
			// subtree, overlap will be -1, otherwise it will be >0
			// Our goal is to find the node with minimal positive overlap
			int overlap = -1;
			int leftBorder = anno.getBegin() - span.getSource();
			int rightBorder = span.getTarget() - anno.getEnd();
			if (!(leftBorder < 0) && !(rightBorder < 0)) {
				overlap = leftBorder + rightBorder;
			}

			// determine whether node is better than the temporary best fit
			if ((overlap > -1) && overlap < curBestOverlap) {
				curBestFit = curTree;
				curBestOverlap = overlap;
			}
		}

		return curBestFit;
	}

}
