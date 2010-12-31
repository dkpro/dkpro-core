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
package de.tudarmstadt.ukp.dkpro.core.norvig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toUpperCase;
import static java.lang.Double.parseDouble;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Spelling corrector based on Norvig's algorithm.
 *
 * @author <a href="eckartde@tk.informatik.tu-darmstadt.de">Richard Eckart de Castilho</a>
 * @see http://norvig.com/spell-correct.html
 */
public
class NorvigSpellingCorrector
{
	private final static Pattern WORD_PATTERN = Pattern.compile("\\w+");

	private final Map<String, AtomicInteger> nWords = new HashMap<String, AtomicInteger>();

	private Map<String, String> cachedCorrections = new HashMap<String, String>();
	private int bestScore = -1;
	private String bestCandidate;

	protected
	void resetScore()
	{
		bestScore = -1;
		bestCandidate = null;
	}

	public
	void reset()
	{
		resetScore();
		cachedCorrections = new HashMap<String, String>();
	}

	/**
	 * Read words from the given reader and count their occurrences.
	 *
	 * @param aReader the reader.
	 * @throws IOException if the words cannot be read.
	 */
	public
	void train(
			Reader aReader)
	throws IOException
	{
		BufferedReader in = new BufferedReader(aReader);

		String line = in.readLine();
		while (line != null) {
			Matcher m = WORD_PATTERN.matcher(line.toLowerCase());

			while (m.find()) {
				String word = m.group();
				AtomicInteger count = nWords.get(word);
				if (count == null) {
					count = new AtomicInteger(0);
					nWords.put(word, count);
				}
				count.incrementAndGet();
			}

			line = in.readLine();
		}
	}

	/**
	 * Read words from the given URL and count their occurrences.
	 *
	 * @param aReader the reader.
	 * @param aEncoding the encoding.
	 * @throws IOException if the words cannot be read.
	 */
	public
	void train(
			URL aUrl,
			String aEncoding)
	throws IOException
	{
		InputStream is = null;
		try {
			is = aUrl.openStream();
			train(new InputStreamReader(is, aEncoding));
		}
		finally {
			closeQuietly(is);
		}
	}

	/**
	 * Get a list for all possible variants of the given word containing an
	 * insertion, deletion, replacement or transposition.
	 *
	 * @param word the word.
	 * @return the list of variants.
	 */
	protected
	List<String> edits(
			String word)
	{
		List<String> candidates = new ArrayList<String>();

		for (int i = 0; i < word.length(); i++) {
			// deletes
			candidates.add(word.substring(0, i) + word.substring(i + 1));

			for (char c = 'a'; c <= 'z'; c++) {
				// replaces
				candidates.add(word.substring(0, i) + c	+ word.substring(i + 1));
				// inserts
				candidates.add(word.substring(0, i) + c	+ word.substring(i));
			}
		}

		// inserts at the end
		for (char c = 'a'; c <= 'z'; c++) {
			candidates.add(word + c);
		}

		// transposes
		for (int i = 0; i < word.length() - 1; i++) {
			candidates.add(word.substring(0, i) + word.substring(i + 1, i + 2)
					+ word.substring(i, i + 1) + word.substring(i + 2));
		}

		return candidates;
	}

	/**
	 * Try to find a correction for the given word. The word may contain up to
	 * two edits. If no better alternative is found, the word is returned
	 * verbatim. For performance reasons corrections are cached.
	 *
	 * @param word the word to correct (has to be lower-case)
	 * @return the possible correction.
	 */
	public
	String correct(
			String aWord)
	{
		// Too short words and numbers cannot be corrected.
		if ((aWord.length() < 2) || isNumber(aWord)) {
			return aWord;
		}

		// Remember case
		boolean isUpper = isUpperCase(aWord.charAt(0));

		// Correct if not cached
		String word = aWord.toLowerCase();
		String correction = cachedCorrections.get(word);
		if (correction == null) {
			correction = getBestCandidate(word);
			cachedCorrections.put(word, correction);
		}

		// Restore case
		char[] buffer = correction.toCharArray();
		if (isUpper) {
			buffer[0] = toUpperCase(buffer[0]);
		}

		return new String(buffer);
	}

	protected
	boolean isNumber(
			String aWord)
	{
		try {
			parseDouble(aWord);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	/**
	 * Try to find a correction for the given word. The word may contain up to
	 * two edits. If no better alternative is found, the word is returned
	 * verbatim.
	 *
	 * @param word the word to correct (has to be lower-case)
	 * @return the possible correction.
	 */
	protected
	String getBestCandidate(
			String word)
	{
		// If the word is in the dictionary, it is probably correct
		if (nWords.containsKey(word)) {
			return word;
		}

		// Reset score
		resetScore();

		// Look up the potential correct words in the dictionary
		List<String> candidates1 = edits(word);
		for (String candidate : candidates1) {
			consider(candidate);
		}

		// Found possible correction for one mistake
		if (bestScore != -1) {
			return bestCandidate;
		}

		// Repeat the process for a potential second mistake
		for (String candidate1 : candidates1) {
			List<String> candidates2 = edits(candidate1);
			for (String candidate2 : candidates2) {
				consider(candidate2);
			}
		}

		if (bestScore != -1) {
			return bestCandidate;
		}
		else {
			return word;
		}
	}

	/**
	 * Consider the given candidate. If it is better than a previously found
	 * candidate, then remember it, otherwise forget it.
	 *
	 * @param candidate the candidate to consider.
	 */
	protected
	void consider(
			String candidate)
	{
		AtomicInteger score = nWords.get(candidate);
		if (score != null) {
			if (score.get() > bestScore) {
				bestScore = score.get();
				bestCandidate = candidate;
			}
		}
	}
}
