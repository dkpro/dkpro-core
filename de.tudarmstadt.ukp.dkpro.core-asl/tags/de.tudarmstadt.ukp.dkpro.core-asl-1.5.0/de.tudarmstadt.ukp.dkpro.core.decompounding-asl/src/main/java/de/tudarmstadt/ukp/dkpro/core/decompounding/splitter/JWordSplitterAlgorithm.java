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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.abelssoft.wordtools.jWordSplitter.AbstractWordSplitter;
import de.abelssoft.wordtools.jWordSplitter.impl.GermanWordSplitter;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;

/**
 * Wrapper for the JWordSplitter algorithm.
 *
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class JWordSplitterAlgorithm
	implements SplitterAlgorithm
{
	private AbstractWordSplitter splitterHiddenLinking;
	private AbstractWordSplitter splitter;
	private Dictionary dict;

	@Override
	public DecompoundingTree split(String aWord)
	{
		if (splitter == null) {
			try {
				splitterHiddenLinking = new InternalGermanWordSplitter(true);
				splitter = new InternalGermanWordSplitter(false);
			}
			catch (IOException e) {
				throw new IllegalStateException("Unable to access dictionary", e);
			}
		}
		
		DecompoundingTree t = new DecompoundingTree(aWord);

		// Just append on child to the tree
		String[] splits = splitter.splitWord(aWord).toArray(new String[0]);
		String[] splitsNoLink = splitterHiddenLinking.splitWord(aWord).toArray(new String[0]);
		
		if (splits.length != splitsNoLink.length) {
			throw new IllegalStateException(
					"Something is fishy - more must have happened than just hiding the links");
		}
		
		if (splits.length > 1) {
			StringBuilder splitStringMorph = new StringBuilder();
			for (int i = 0; i < splits.length; i++) {
				String base = splitsNoLink[i];
				String full = splits[i];
				
				if (!full.startsWith(base)) {
					throw new IllegalStateException(
							"Something is fishy - links should be at the end");
				}
				String link = full.substring(base.length());
				
				// Split with linking morphemes
				splitStringMorph.append(base);
				if (link.length() > 0) {
					splitStringMorph.append("(").append(link).append(")");
				}
				splitStringMorph.append("+");
			}
			
			String splitStringMorphStr = splitStringMorph.toString();
			t.getRoot().addChild(new ValueNode<DecompoundedWord>(DecompoundedWord.createFromString(splitStringMorphStr)));
		}

		return t;
	}
	
	@Override
	public void setDictionary(Dictionary aDict)
	{
		dict = aDict;
		splitter = null;
		splitterHiddenLinking = null;
	}

	@Override
	public void setLinkingMorphemes(LinkingMorphemes aMorphemes)
	{
		// Not needed for this algorithm
	}

	@Override
	public void setMaximalTreeDepth(int aDepth)
	{
		// Not needed for this algorithm
	}
	
	private class InternalGermanWordSplitter extends GermanWordSplitter
	{
		public InternalGermanWordSplitter(boolean aHideConnectingCharacters)
			throws IOException
		{
			super(aHideConnectingCharacters);
		}
		
		@Override
		protected Set<String> getWordList()
			throws IOException
		{
			if (dict == null) {
				return super.getWordList();
			}
			else {
				return new HashSet<String>(dict.getAll());
			}
		}
	}
}
