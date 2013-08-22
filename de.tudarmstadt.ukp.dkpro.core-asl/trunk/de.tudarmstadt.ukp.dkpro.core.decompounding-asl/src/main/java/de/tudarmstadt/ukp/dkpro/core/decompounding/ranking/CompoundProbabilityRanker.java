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

package de.tudarmstadt.ukp.dkpro.core.decompounding.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundedWord;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.Fragment;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundingTree;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;
import de.tudarmstadt.ukp.dkpro.core.decompounding.web1t.Finder;

/**
 * Probability based ranking method
 *
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class CompoundProbabilityRanker
	extends AbstractRanker
	implements RankerList
{
	/**
	 * Empty constructor
	 *
	 * Use {@link #setFinder(Finder)} before using this class
	 */
	public CompoundProbabilityRanker() {
	}

	/**
	 * Constructor
	 */
	public CompoundProbabilityRanker(Finder aFinder)
	{
		super(aFinder);
	}

	@Override
	public DecompoundedWord highestRank(List<DecompoundedWord> aSplits)
	{
		return rank(aSplits).get(0);
	}

	@Override
	public List<DecompoundedWord> rank(List<DecompoundedWord> aSplits)
	{
		for (DecompoundedWord split : aSplits) {
			split.setWeight(calcRank(split));
		}

		List<DecompoundedWord> result = filterAndSort(aSplits);
		Collections.sort(result, Collections.reverseOrder());
		
		return result;
	}

	/**
	 * Calculates the weight for a split
	 */
	private float calcRank(DecompoundedWord aSplit)
	{
		float result = 0;

		for (Fragment elem : aSplit.getSplits()) {
			result += -1 * Math.log(freq(elem).doubleValue() / getFinder().getUnigramCount().doubleValue());
		}

		return result;
	}

	@Override
	public DecompoundedWord highestRank(DecompoundingTree aTree)
	{
		return highestRank(aTree.getRoot(), null);
	}

	@Override
	public List<DecompoundedWord> bestPath(DecompoundingTree aTree)
	{
		List<DecompoundedWord> path = new ArrayList<DecompoundedWord>();
		highestRank(aTree.getRoot(), path);
		return path;
	}

	/**
	 * Searches a a path throw the tree
	 */
	private DecompoundedWord highestRank(ValueNode<DecompoundedWord> aParent, List<DecompoundedWord> aPath)
	{
		if (aPath != null) {
			aPath.add(aParent.getValue());
		}
		
		List<DecompoundedWord> children = aParent.getChildrenValues();
		if (children.size() == 0) {
			return aParent.getValue();
		}

		children.add(aParent.getValue());
		List<DecompoundedWord> result = rank(children);
		DecompoundedWord best = result.get(0);

		if (best.equals(aParent.getValue())) {
			// None of the children get a better score than the parent
			return aParent.getValue();
		}
		else {
			// Find the child node that ranked best and recurse
			for (ValueNode<DecompoundedWord> split : aParent.getChildren()) {
				if (best.equals(split.getValue())) {
					return highestRank(split, aPath);
				}
			}
		}

		return null;
	}
}
