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
package de.tudarmstadt.ukp.dkpro.core.dictionaryannotator;


import java.util.Arrays;

/**
 * Stores phrases in a tree structure to
 * allow efficient access. Used by the 
 * DictionaryAnnotator.
 * 
 * @author hoffart
 *
 */
public class PhraseTree
{
	private PhraseTreeElement root;
	
	public PhraseTree() {
		root = new PhraseTreeElement(null);
	}
	
	/**
	 * Add a phrase, each array entry is a phrase token
	 * 
	 * @param phraseParts
	 */
	public void addPhrase(String[] phraseParts) {
		PhraseTreeElement current = root;
		
		for (String phrasePart : phraseParts) {
			PhraseTreeElement child = current.addChild(phrasePart);
			current = child;
		}
		
		current.setEndElement(true);
	}

	/**
	 * Checks if the phrase is contained in the tree
	 * 
	 * @param phraseParts	Phrase as token array
	 * @return				true if contained, false otherwise
	 */
	public boolean contains(String[] phraseParts) {
		int i=0;
		
		PhraseTreeElement match = root.getChild(phraseParts[i]);
		
		while (match != null && i < phraseParts.length-1) {
			i++;
			
			String childToken = phraseParts[i];
			PhraseTreeElement child = match.getChild(childToken);
			match = child;
		}
		
		if (match != null) {
			// make sure that the phrase is only matched fully
			return match.isEndElement();
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the longest matching phrase in the tree, beginning
	 * with the first array entry in matchText.
	 * 
	 * @param matchText	Text to match against, pre-tokenized
	 * @return			Longest matching phrase stored in the tree, as token-array
	 */
	public String[] getLongestMatch(String[] matchText) {
		int i=0;
		
		PhraseTreeElement match = root.getChild(matchText[i]);
		
		boolean isEndElement = false;
		
		while (match != null && i < matchText.length-1) {
			isEndElement = match.isEndElement();
		
			i++;
			
			String childToken = matchText[i];
			PhraseTreeElement child = match.getChild(childToken);
			match = child;
		}
			
		String[] matchedString = null;
		
		if (isEndElement) {
			matchedString = Arrays.copyOfRange(matchText, 0, i);
		}
		
		return matchedString;
	}
}
