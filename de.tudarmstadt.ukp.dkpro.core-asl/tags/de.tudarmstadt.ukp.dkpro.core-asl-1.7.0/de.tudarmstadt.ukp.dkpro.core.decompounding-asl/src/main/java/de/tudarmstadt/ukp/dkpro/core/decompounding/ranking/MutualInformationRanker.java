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

import java.math.BigInteger;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundedWord;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.Fragment;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;
import de.tudarmstadt.ukp.dkpro.core.decompounding.web1t.Finder;

/**
 * Mutual informationen based ranking algorithm. See doc folder for more
 * information
 *
 * @author <a href="mailto:je.haase@googlemail.com">Jens Haase</a>
 */
public class MutualInformationRanker
	extends AbstractRanker
	implements RankerList
{
	/**
	 * Empty constructor
	 *
	 * Use {@link #setFinder(Finder)} before using this class
	 */
	public MutualInformationRanker() {
	}

	public MutualInformationRanker(Finder aFinder)
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
			double weight = calcRank(split);
			if (Double.isInfinite(split.getWeight()) || Double.isNaN(split.getWeight())) {
				weight = 0.0;
			}
			split.setWeight(weight);
		}

		return filterAndSort(aSplits);
	}

	/**
	 * Calculates the weight for a split
	 */
	private float calcRank(DecompoundedWord aSplit)
	{
		double total = 0;
		double count = 0;

		BigInteger unigramCount = getFinder().getUnigramCount();

		if (aSplit.getSplits().size() == 1) {
			// Entropy for single words
			Fragment w = aSplit.getSplits().get(0);
			double p = freq(w).doubleValue() / unigramCount.doubleValue();

			return (float) ((-1) * p * Math.log(p));
		}

		// Mutual Information for splits.
		for (int i = 1; i < aSplit.getSplits().size(); i++) {
			count++;

			Fragment w1 = aSplit.getSplits().get(i - 1);
			Fragment w2 = aSplit.getSplits().get(i);
			// Look up unigram frequencies first - this is fast and allows us to bail out early
			BigInteger w1f = freq(w1);
			if (w1f.equals(BigInteger.ZERO)) {
				continue;
			}

			BigInteger w2f = freq(w2);
			if (w2f.equals(BigInteger.ZERO)) {
				continue;
			}

			// This is a slow lookup that we only do if the unigram frequencies are greate than 0
			double a = freq(w1, w2).multiply(unigramCount).doubleValue();
			if (a == 0d) {
				continue;
			}

			// Finally calculate
			double b = w1f.multiply(w2f).doubleValue();
			total += Math.log(a / b);
		}

		return (float) (total / count);
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
