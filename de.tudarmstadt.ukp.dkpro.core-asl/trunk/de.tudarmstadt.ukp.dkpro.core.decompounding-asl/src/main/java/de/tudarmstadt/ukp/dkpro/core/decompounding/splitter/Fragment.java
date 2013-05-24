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

/**
 * Data container for a split element. A split element contains a word and
 * optional a linking morpheme
 *
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class Fragment
{

	private String word;
	private String morpheme;
	private boolean splitAgain = false;

	/**
	 * Creates a empty split element
	 */
	public Fragment()
	{
		this(null, null);
	}

	/**
	 * Creates a split element with a word but no linking morpheme
	 *
	 * @param aWord
	 */
	public Fragment(String aWord)
	{
		this(aWord, null);
	}

	/**
	 * Creates a split element with a word and a linking morpheme
	 *
	 * @param aWord
	 * @param aMorpheme
	 */
	public Fragment(String aWord, String aMorpheme)
	{
		word = aWord;
		morpheme = aMorpheme;
	}

	/**
	 * Creates a split element from string. String format:
	 *
	 * word(morpheme)
	 *
	 * Example: "auto" or "auto(s)"
	 *
	 * @param aElement
	 * @return
	 */
	public static Fragment createFromString(String aElement)
	{
		Fragment e = new Fragment();

		String[] splits = aElement.split("\\(");
		e.setWord(splits[0]);
		if (splits.length == 2 && splits[1].endsWith(")")) {
			e.setMorpheme(splits[1].substring(0, splits[1].length() - 1));
		}

		return e;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((morpheme == null) ? 0 : morpheme.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) {
			return false;
		}

		return toString().equals(obj.toString());
	}

	@Override
	public String toString()
	{
		String s = word;

		if (morpheme != null) {
			s += "(" + morpheme + ")";
		}

		return s;
	}

	/**
	 * Returns the word of the split element
	 *
	 * @return
	 */
	public String getWord()
	{
		return word;
	}

	public String getWordWithMorpheme()
	{
		return word + ((morpheme != null) ? morpheme : "");
	}

	/**
	 * Sets the word of the split element
	 *
	 * @param aWord
	 */
	public void setWord(String aWord)
	{
		word = aWord;
	}

	/**
	 * Returns the linking morpheme of the split element
	 *
	 * @return
	 */
	public String getMorpheme()
	{
		return morpheme;
	}

	/**
	 * Checks if this element has an morpheme
	 *
	 * @return
	 */
	public boolean hasMorpheme()
	{
		return morpheme != null;
	}

	/**
	 * Sets the linking morpheme of the split element
	 *
	 * @param aMorpheme
	 */
	public void setMorpheme(String aMorpheme)
	{
		morpheme = aMorpheme;
	}

	/**
	 * Similar to the toString method, but combines morpheme and word
	 *
	 * @return
	 */
	public String toStringWithoutMorpheme()
	{
		String s = word;

		if (morpheme != null) {
			s += morpheme;
		}

		return s;
	}

	/**
	 * Checks if this split element should be split again by the algorithm.
	 * Can be used for recursive splitting
	 *
	 * @return
	 */
	public boolean shouldSplitAgain()
	{
		return splitAgain;
	}

	/**
	 * Set the splitAgain variable
	 *
	 * @param aSplitAgain
	 */
	public void setSplitAgain(boolean aSplitAgain)
	{
		splitAgain = aSplitAgain;
	}
}
