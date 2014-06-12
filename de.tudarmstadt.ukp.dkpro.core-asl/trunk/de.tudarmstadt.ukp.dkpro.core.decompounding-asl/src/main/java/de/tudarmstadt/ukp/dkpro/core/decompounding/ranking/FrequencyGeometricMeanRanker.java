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

import java.util.List;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundedWord;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.Fragment;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;
import de.tudarmstadt.ukp.dkpro.core.decompounding.web1t.Finder;

/**
 * Frequency based ranking algorithm. See doc folder for more informations.
 *
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class FrequencyGeometricMeanRanker
	extends AbstractRanker
	implements RankerList
{
	/**
	 * Empty constructor
	 *
	 * Use {@link #setFinder(Finder)} before using this class
	 */
	public FrequencyGeometricMeanRanker() {

	}

	public FrequencyGeometricMeanRanker(Finder aFinder)
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

		return filterAndSort(aSplits);
	}

	/**
	 * Calculates the weight for a split
	 */
	private double calcRank(DecompoundedWord aSplit)
	{
		SummaryStatistics stats = new SummaryStatistics();
		for (Fragment elem : aSplit.getSplits()) {
			stats.addValue(freq(elem).doubleValue());
		}
		return stats.getGeometricMean();
	}

	/**
	 * Searches a a path throw the tree
	 */
	@Override
    public DecompoundedWord highestRank(ValueNode<DecompoundedWord> aParent,
            List<DecompoundedWord> aPath)
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
			// None of the childs get a better score than the parent
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
