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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.ngrams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationFS;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NGram;

public class NGramIterable<T extends AnnotationFS>
	implements Iterable<NGram>
{
	List<NGram> nGramList;

	private NGramIterable(Iterable<T> tokens, int n)
	{
		this.nGramList = createNGramList(tokens, n);
	}

	public static <T extends AnnotationFS> NGramIterable<T> create(Iterable<T> tokens, int n)
	{
		return new NGramIterable<T>(tokens, n);
	}

	@Override
	public Iterator<NGram> iterator()
	{
		return nGramList.iterator();
	}

	private List<NGram> createNGramList(Iterable<T> tokens, int n)
	{
		List<NGram> nGrams = new ArrayList<NGram>();

		// fill token list
		List<T> tokenList = new ArrayList<T>();
		for (T t : tokens) {
			tokenList.add(t);
		}

		// remove last element, if it contains a punctuation mark
		if (tokenList.size() > 0) {
			if (tokenList.get(tokenList.size() - 1).getCoveredText().length() == 1
					&& (tokenList.get(tokenList.size() - 1).getCoveredText().equals(".")
							|| tokenList.get(tokenList.size() - 1).getCoveredText().equals("!") || tokenList
							.get(tokenList.size() - 1).getCoveredText().equals("?"))) {
				tokenList.remove(tokenList.size() - 1);
			}
		}

		for (int k = 1; k <= n; k++) {
			// if the number of tokens is less than k => break
			if (tokenList.size() < k) {
				break;
			}
			nGrams.addAll(getNGrams(tokenList, k));
		}

		return nGrams;
	}

	private List<NGram> getNGrams(List<T> tokenList, int k)
	{
		List<NGram> nGrams = new ArrayList<NGram>();

		int size = tokenList.size();
		for (int i = 0; i < (size + 1 - k); i++) {
			try {
				NGram ngram = new NGram(tokenList.get(i).getCAS().getJCas(), tokenList.get(i)
						.getBegin(), tokenList.get(i + k - 1).getEnd());
				ngram.setText(getTokenText(tokenList, i, i + k - 1));
				nGrams.add(ngram);
			}
			catch (CASException e) {
				throw new IllegalStateException(e);
			}
		}

		return nGrams;
	}

	private String getTokenText(List<T> tokenList, int start, int end)
	{
		List<String> tokenTexts = new ArrayList<String>();
		for (int i = start; i <= end; i++) {
			tokenTexts.add(tokenList.get(i).getCoveredText());
		}
		return StringUtils.join(tokenTexts, " ");
	}
}
