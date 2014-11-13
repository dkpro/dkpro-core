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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Johannes Hoffart
 */
public class PhraseTreeElement
{
	private String word;

	private boolean endElement;

	private Map<String, PhraseTreeElement> children;

	public PhraseTreeElement(String aWord)
	{
		word = aWord;

		children = new HashMap<String, PhraseTreeElement>();
	}

	public String getWord()
	{
		return word;
	}

	public PhraseTreeElement addChild(String aWord)
	{
		// do not add if it exists
		PhraseTreeElement child = getChild(aWord);

		if (child == null) {
			child = new PhraseTreeElement(aWord);
			children.put(aWord, child);
		}

		return child;
	}

	public PhraseTreeElement getChild(String aWord)
	{
		return children.get(aWord);
	}

	public boolean isEndElement()
	{
		return endElement;
	}

	public void setEndElement(boolean aEndElement)
	{
		endElement = aEndElement;
	}
}
