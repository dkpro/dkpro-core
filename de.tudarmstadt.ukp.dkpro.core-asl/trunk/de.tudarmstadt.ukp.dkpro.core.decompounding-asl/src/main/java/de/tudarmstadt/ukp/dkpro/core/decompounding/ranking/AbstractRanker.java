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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ivy.util.cli.CommandLine;

import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundedWord;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundingTree;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.Fragment;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;
import de.tudarmstadt.ukp.dkpro.core.decompounding.web1t.Finder;
import de.tudarmstadt.ukp.dkpro.core.decompounding.web1t.NGramModel;

/**
 * Contains base method for the ranking algorithms
 *
 * @author Jens Haase <je.haase@googlemail.com>
 *
 */
public abstract class AbstractRanker implements Ranker
{

	private Finder finder;

	/**
	 * Empty constructor
	 *
	 * Use setFinder before using this class
	 */
	public AbstractRanker() {

	}

	public AbstractRanker(Finder aFinder)
	{
		finder = aFinder;
	}

	public Finder getFinder()
	{
		return finder;
	}

	/**
	 * Gets the frequency of a Split Element
	 */
	protected BigInteger freq(Fragment aWord)
	{
		return finder.freq(aWord.getWord());
	}

	/**
	 * Returns the frequency of n-grams that contain both split elements
	 */
	protected BigInteger freq(Fragment aWord1, Fragment aWord2)
	{
		return freq(new String[] { aWord1.getWord(), aWord2.getWord() });
	}

	/**
	 * Returns the frequency for a array of words
	 */
	protected BigInteger freq(String[] aWords)
	{
		BigInteger total = BigInteger.valueOf(0l);

		for (NGramModel gram : finder.find(aWords)) {
			total = total.add(BigInteger.valueOf(gram.getFreq()));
		}

		return total;
	}

	public final static String INDEX_OPTION = "luceneIndex";
	public final static String LIMIT_OPTION = "limit";

	public static int getLimitOption(CommandLine aCmd)
	{
		int i = Integer.MAX_VALUE;
		if (aCmd.hasOption(LIMIT_OPTION)) {
			i = Integer.valueOf(aCmd.getOptionValue(LIMIT_OPTION));
		}

		return i;
	}

	public static String getIndexPathOption(CommandLine aCmd)
	{
		return aCmd.getOptionValue(INDEX_OPTION);
	}

	@Override
	public void setFinder(Finder aFinder) {
		finder = aFinder;
	}

	/**
	 * Expects that the splits list contains at least one element and that this is the unsplit word.
	 */
	public static List<DecompoundedWord> filterAndSort(List<DecompoundedWord> aSplits) {
		List<DecompoundedWord> filtered = new ArrayList<DecompoundedWord>();
		for (DecompoundedWord s : aSplits) {
			if (!Double.isInfinite(s.getWeight()) && !Double.isInfinite(s.getWeight())
					&& (s.getWeight() > 0.0)) {
				filtered.add(s);
			}
		}
		Collections.sort(filtered);

		if (filtered.isEmpty()) {
			filtered.add(aSplits.get(0));
		}

		return filtered;
	}

    @Override
    public DecompoundedWord highestRank(DecompoundingTree aTree){
        return highestRank(aTree.getRoot(), null);
    }

    public abstract DecompoundedWord highestRank(ValueNode<DecompoundedWord> aParent,
            List<DecompoundedWord> aPath);

}
