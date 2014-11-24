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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Creates a character NGram iterable from a list of tokens.
 * 
 * @author zesch
 *
 */
public class CharacterNGramStringIterable implements Iterable<String>
{
	List<String> nGramList;

	/**
	 * @param token A token
	 */
	public CharacterNGramStringIterable(String token, int minN, int maxN)
	{
		this.nGramList = createNGramList(token, minN, maxN);
	}
	
	@Override
	public Iterator<String> iterator()
	{
		return nGramList.iterator();
	}

	private List<String> createNGramList(String token, int minN, int maxN)
	{
        if (minN > maxN) {
            throw new IllegalArgumentException("minN needs to be smaller or equal than maxN.");
        }

		List<String> nGrams = new ArrayList<String>();

		// fill character list
		List<String> charList = new ArrayList<String>();
		for (char c : token.toCharArray()) {
			charList.add(Character.toString(c));
		}

		for (int k = minN; k <= maxN; k++) {
			// if the number of tokens is less than k => break
			if (charList.size() < k) {
				break;
			}
			nGrams.addAll(getNGrams(charList, k));
		}

		return nGrams;
	}

	private List<String> getNGrams(List<String> tokenList, int k)
	{
		List<String> nGrams = new ArrayList<String>();

		int size = tokenList.size();
		for (int i = 0; i < (size + 1 - k); i++) {
			nGrams.add(
					StringUtils.join(tokenList.subList(i, i + k), "")
			);
		}

		return nGrams;
	}
}