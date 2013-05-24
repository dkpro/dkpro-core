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

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;

/**
 * Implements a simple left to right split algorithm.
 *
 * Goes from left to right to the word. If a word is found the right side is
 * evaluation from left to right. At the end we have a right balanced tree. All
 * leaves are the smallest word fractions.
 *
 * The leaves will combined to all possible splits.
 *
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class LeftToRightSplitterAlgorithm
	implements SplitterAlgorithm
{
	private Dictionary dict;
	private LinkingMorphemes morphemes;
	private int maxTreeDepth = Integer.MAX_VALUE;
	private int minWordLength = 1;
	private int minRestLength = 2;

	/**
	 * Empty constructor.
	 *
	 * Before you use this class set the dictionary and the
	 * linking morphemes with the setter methods
	 */
	public LeftToRightSplitterAlgorithm()
	{
		// Nothing to do
	}

	/**
	 * Create a instance of the algorithm
	 *
	 * @param aDict
	 *            A dictionary with all words
	 * @param aMorphemes
	 *            A LinkingMorphemes class
	 */
	public LeftToRightSplitterAlgorithm(Dictionary aDict, LinkingMorphemes aMorphemes)
	{
		setDictionary(aDict);
		setLinkingMorphemes(aMorphemes);
	}

	@Override
	public DecompoundingTree split(String aWord)
	{
		aWord = aWord.toLowerCase();

		DecompoundingTree t = new DecompoundingTree(aWord);
		t.getRoot().getValue().getSplits().get(0).setSplitAgain(true);

		ltrSplit(t.getRoot(),0);

		return t;
	}

	/**
	 * The basic split algorithm. Moves the word from left to right and checks
	 * for valid words.
	 *
	 * @param word
	 * @return
	 */
	protected void ltrSplit(ValueNode<DecompoundedWord> aParent, int aDepth)
	{
		if (aDepth > maxTreeDepth) {
			return;
		}

		for (int i = 0; i < aParent.getValue().getSplits().size(); i++) {
			Fragment element = aParent.getValue().getSplits().get(i);

			if (element.shouldSplitAgain()) {
				List<DecompoundedWord> results = makeSplit(element.getWord());

				for (DecompoundedWord result : results) {
					DecompoundedWord copy = aParent.getValue().createCopy();
					if (result.getSplits().size() > 1) {
						result.getSplits().get(1).setSplitAgain(true);
						copy.replaceSplitElement(i, result);
						ValueNode<DecompoundedWord> child = new ValueNode<DecompoundedWord>(copy);
						aParent.addChild(child);
						ltrSplit(child, aDepth+1);
					}
					else if (result.getSplits().size() == 1
							&& !result.equals(aParent.getValue())) {
						copy.replaceSplitElement(i, result);
						ValueNode<DecompoundedWord> child = new ValueNode<DecompoundedWord>(copy);
						aParent.addChild(child);
					}
				}
			}
		}
	}

	/**
	 * Splits a word in two word.
	 *
	 * @param aWord
	 * @return
	 */
	protected List<DecompoundedWord> makeSplit(String aWord)
	{
		List<DecompoundedWord> result = new ArrayList<DecompoundedWord>();

		for (int i = 0; i < aWord.length(); i++) {
			String leftWord = aWord.substring(0, i + 1);
			String rightWord = aWord.substring(i + 1);
			
			boolean leftGood = dict.contains(leftWord) && leftWord.length() >= minWordLength;
			boolean rightGood = rightWord.length() > minRestLength || rightWord.length() == 0;

			if (leftGood && rightGood) {
				// createFromString removes the trailing + if rightWord is empty.
				DecompoundedWord split = DecompoundedWord.createFromString(leftWord + "+" + rightWord); 
				split.setSplitPos(i);
				result.add(split);
			}
			
			// Check if left word contains linking morphemes
			for (String morpheme : morphemes.getAll()) {
				try {
					String leftWithoutMorpheme = leftWord.substring(0,
							leftWord.length() - morpheme.length());
					if (leftWord.endsWith(morpheme) && dict.contains(leftWithoutMorpheme)
							&& rightGood) {
						DecompoundedWord split = DecompoundedWord.createFromString(leftWithoutMorpheme
								+ "(" + morpheme + ")+" + rightWord); 
						split.setSplitPos(i);
						result.add(split);
					}
				}
				catch (StringIndexOutOfBoundsException e) {
					continue;
				}
			}
		}

		return result;
	}

	@Override
	public void setDictionary(Dictionary aDict)
	{
		dict = aDict;
	}
	
	public Dictionary getDictionary()
	{
		return dict;
	}

	@Override
	public void setLinkingMorphemes(LinkingMorphemes aLinkingMorphemes)
	{
		morphemes = aLinkingMorphemes;
	}
	
	public LinkingMorphemes getMorphemes()
	{
		return morphemes;
	}

	@Override
	public void setMaximalTreeDepth(int aDepth)
	{
		maxTreeDepth = aDepth;
	}
	
	public int getMaxTreeDepth()
	{
		return maxTreeDepth;
	}
	
	public void setMinWordLength(int aMinWordLength)
	{
		minWordLength = aMinWordLength;
	}
	
	public int getMinWordLength()
	{
		return minWordLength;
	}
	
	public void setMinRestLength(int aMinRestLength)
	{
		minRestLength = aMinRestLength;
	}
	
	public int getMinRestLength()
	{
		return minRestLength;
	}
}
