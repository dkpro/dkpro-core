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

package de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The simple dictionary reads a file in which each line is a new word.
 * 
 * This can be used to create your own dictionary from a corpus
 * 
 * @author Jens Haase <je.haase@googlemail.com>
 * 
 */
public class SimpleDictionary
	implements Dictionary
{

	private Set<String> words;

	/**
	 * Constructor for a simple dictionary
	 * 
	 * @param aDict
	 *            The file with all words
	 */
	public SimpleDictionary(File aDict)
	{
		try {
			words = readFileToSet(new BufferedReader(new FileReader(aDict)));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a simple dictionary from a list of string. This can be used for
	 * testing
	 * 
	 * @param aWords
	 *            A list of words
	 */
	public SimpleDictionary(String... aWords)
	{
		words = new HashSet<String>();
		for (String word : aWords) {
			words.add(word.toLowerCase());
		}
	}

	public SimpleDictionary(InputStream aDictStream)
	{
		try {
			words = readFileToSet(new BufferedReader(new InputStreamReader(aDictStream)));
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean contains(String aWord)
	{
		return words.contains(aWord);
	}

	/**
	 * Reads the dictionary to set
	 * 
	 * @return A set of words
	 * @throws IOException
	 */
	protected Set<String> readFileToSet(BufferedReader aReader)
		throws IOException
	{
		Set<String> words = new HashSet<String>();
		String line;
		while ((line = aReader.readLine()) != null) {
			words.add(line.toLowerCase());
		}

		return words;
	}

	public List<String> getAll()
	{
		return new ArrayList<String>(words);
	}

	public void setWords(Set<String> aWords)
	{
		words = aWords;
	}
}
