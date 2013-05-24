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

package de.tudarmstadt.ukp.dkpro.core.decompounding.trie;

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;

/**
 * A trie datastructor which also stores the number of successor for each node
 * 
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class TrieStructure
{

	// Key: substring. Value: Successors
	private KeyValueNode<String, Integer> root = new KeyValueNode<String, Integer>(
			"", 0);

	/**
	 * Adds a word to the tree. Also increments the successor value for each
	 * node
	 * 
	 * @param aWord
	 */
	public void addWord(String aWord)
	{
		KeyValueNode<String, Integer> parent = root;

		for (int i = 0; i < aWord.length(); i++) {
			String subword = aWord.substring(0, i + 1);
			KeyValueNode<String, Integer> child = parent.getChild(subword);

			if (child != null) {
				if (!subword.equals(aWord)) {
					child.setValue(child.getValue() + 1);
				}
			}
			else {
				Integer value = 1;
				if (subword.equals(aWord)) {
					value = 0;
				}
				child = new KeyValueNode<String, Integer>(subword, value);
				parent.addChild(child);
			}

			parent = child;
		}
	}

	/**
	 * Finds a not with a given string. If not found NULL is returned.
	 * 
	 * @param aWord
	 * @return
	 */
	public KeyValueNode<String, Integer> findWord(String aWord)
	{
		aWord = aWord.toLowerCase();
		KeyValueNode<String, Integer> parent = root;
		int depth = 1;

		while (parent.hasChildren()) {
			String w = aWord.substring(0, depth);
			KeyValueNode<String, Integer> child = parent.getChild(w);

			if (w.equals(aWord)) {
				return child;
			}
			else if (child != null) {
				parent = child;
				depth++;
			}
			else {
				return null;
			}
		}

		return null;
	}

	/**
	 * Returns the number of successor for a node. If the node could not be
	 * found the return value is 0.
	 * 
	 * @param aWord
	 * @return
	 */
	public Integer getSuccessors(String aWord)
	{
		KeyValueNode<String, Integer> node = findWord(aWord);
		if (node != null) {
			return node.getValue();
		}

		return 0;
	}

	/**
	 * Creates a Trie object for a SimpleDictionary
	 * 
	 * @param aDict
	 * @return
	 */
	public static TrieStructure createForDict(Dictionary aDict)
	{
		TrieStructure t = new TrieStructure();

		for (String word : aDict.getAll()) {
			t.addWord(word);
		}

		return t;
	}

	/**
	 * Creates a Trie object for a SimpleDictionary with all words reversed
	 * 
	 * @param aDict
	 * @return
	 */
	public static TrieStructure createForDictReverse(Dictionary aDict)
	{
		TrieStructure t = new TrieStructure();

		for (String word : aDict.getAll()) {
			t.addWord(new StringBuffer(word).reverse().toString());
		}

		return t;
	}
}
