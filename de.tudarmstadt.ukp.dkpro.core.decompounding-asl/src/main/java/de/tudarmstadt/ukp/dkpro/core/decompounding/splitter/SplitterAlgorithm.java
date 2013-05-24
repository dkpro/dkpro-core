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

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;


/**
 * Interface for all splitting algorithms
 * 
 * @author Jens Haase <je.haase@googlemail.com>
 */
public interface SplitterAlgorithm
{

	/**
	 * Returns all possible splits for a given word.
	 * 
	 * @param aWord
	 *            The word to split
	 * @return
	 */
	public DecompoundingTree split(String aWord);
	
	/**
	 * Sets the dictionary for the algorithm
	 * 
	 * @param aDict The dictionary
	 */
	public void setDictionary(Dictionary aDict);
	
	/**
	 * Sets the linking morphemes for the algorithm
	 * 
	 * @param aMorphemes
	 */
	public void setLinkingMorphemes(LinkingMorphemes aMorphemes);
	
	/**
	 * Set the maximal tree depth. Default: Integer.MaxValue
	 * 
	 * @param aDepth
	 */
	public void setMaximalTreeDepth(int aDepth);
}
