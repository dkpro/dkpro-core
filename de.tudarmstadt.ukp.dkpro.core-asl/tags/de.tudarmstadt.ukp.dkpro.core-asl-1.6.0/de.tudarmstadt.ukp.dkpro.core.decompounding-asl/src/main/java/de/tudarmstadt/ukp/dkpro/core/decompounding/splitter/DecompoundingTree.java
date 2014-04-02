/*******************************************************************************
 * Copyright 2010
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
 *******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.decompounding.splitter;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;

/**
 * A split tree. Holds all splits in a tree structure. This can help to see the
 * how the split algorithm works
 * 
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class DecompoundingTree
{

	private ValueNode<DecompoundedWord> root;

	public DecompoundingTree(String aWord)
	{
		root = new ValueNode<DecompoundedWord>(DecompoundedWord.createFromString(aWord));
	}

	public DecompoundingTree(DecompoundedWord aSplit)
	{
		root = new ValueNode<DecompoundedWord>(aSplit);
	}

	public ValueNode<DecompoundedWord> getRoot()
	{
		return root;
	}

	public void setRoot(ValueNode<DecompoundedWord> aRoot)
	{
		root = aRoot;
	}

	/**
	 * Converts the tree to a list.
	 */
	public List<DecompoundedWord> getAllSplits()
	{
		Set<DecompoundedWord> splits = new LinkedHashSet<DecompoundedWord>();
		getAllSplitsRecursive(splits, getRoot(), true);
		
		return new ArrayList<DecompoundedWord>(splits);
	}

	/**
	 * Converts the tree to a list. If there are splits, then the root node, which contains the
	 * unsplit word, is not returned.
	 */
	public List<DecompoundedWord> getSplits()
	{
		Set<DecompoundedWord> splits = new LinkedHashSet<DecompoundedWord>();
		getAllSplitsRecursive(splits, getRoot(), false);
		
		if (!splits.isEmpty()) {
			return new ArrayList<DecompoundedWord>(splits);
		}
		else {
			return asList(getRoot().getValue());
		}
	}

	protected void getAllSplitsRecursive(Set<DecompoundedWord> aSplits,
			ValueNode<DecompoundedWord> aNode, boolean aAddNode)
	{
		if (aAddNode) {
			aSplits.add(aNode.getValue());
		}
		if (aNode.hasChildren()) {
			for (ValueNode<DecompoundedWord> child : aNode.getChildren()) {
				getAllSplitsRecursive(aSplits, child, true);
			}
		}
	}
}
