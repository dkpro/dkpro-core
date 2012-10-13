/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.ngrams.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Creates a NGram iterable from a list of tokens.
 * It does not detect any sentence boundaries.
 * Thus, one should make sure to only add lists that reflect a sentence or a phrase.
 * 
 * @author zesch
 *
 */
public class NGramStringListIterable implements Iterable<List<String>>
{
	List<List<String>> nGramList;

    /**
	 * @param tokens An iterable of tokens.
	 */
	public NGramStringListIterable(Iterable<String> tokens, int minN, int maxN)
	{
		this.nGramList = createNGramList(tokens, minN, maxN);
	}
	
	/**
     * @param tokens An array of tokens.
     */
    public NGramStringListIterable(String[] tokens, int minN, int maxN)
    {
        this.nGramList = createNGramList(Arrays.asList(tokens), minN, maxN);
    }

	
	@Override
	public Iterator<List<String>> iterator()
	{
		return nGramList.iterator();
	}

	private List<List<String>> createNGramList(Iterable<String> tokens, int minN, int maxN)
	{
        if (minN > maxN) {
            throw new IllegalArgumentException("minN needs to be smaller or equal than maxN.");
        }

		List<List<String>> nGrams = new ArrayList<List<String>>();

		// fill token list
		List<String> tokenList = new ArrayList<String>();
		for (String t : tokens) {
			tokenList.add(t);
		}

		for (int k = minN; k <= maxN; k++) {
			// if the number of tokens is less than k => break
			if (tokenList.size() < k) {
				break;
			}
			nGrams.addAll(getNGrams(tokenList, k));
		}

		return nGrams;
	}

	private List<List<String>> getNGrams(List<String> tokenList, int k)
	{
		List<List<String>> nGrams = new ArrayList<List<String>>();

		int size = tokenList.size();
		for (int i = 0; i < (size + 1 - k); i++) {
			nGrams.add(tokenList.subList(i, i + k));
		}

		return nGrams;
	}
}