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
import java.util.List;

/**
 * Linking morphemes container.
 *
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class LinkingMorphemes
{

	List<String> morphemes;

	/**
	 * Create a linking morphemes object from a array of morphemes
	 *
	 * @param aMorphemes
	 */
	public LinkingMorphemes(String... aMorphemes)
	{
		morphemes = new ArrayList<String>();

		for (String string : aMorphemes) {
			morphemes.add(string);
		}
	}

	/**
	 * Create a linking morphemes object from a list of morphemes
	 *
	 * @param aMorphemes
	 */
	public LinkingMorphemes(List<String> aMorphemes)
	{
		morphemes = aMorphemes;
	}

	/**
	 * Create a linking morphemes object from a input stream
	 *
	 * Each line in the file must contain one morpheme. Use # in front of a line
	 * for comments.
	 *
	 * @param aStream
	 */
	public LinkingMorphemes(InputStream aStream)  throws IOException
	{
		this(new BufferedReader(new InputStreamReader(aStream)));
	}

	/**
	 * Create a linking morphemes object from a file.
	 *
	 * Each line in the file must contain one morpheme. Use # in front of a line
	 * for comments.
	 *
	 * @param aMorphemesTextFile
	 * @throws IOException
	 */
	public LinkingMorphemes(File aMorphemesTextFile) throws IOException
	{
		this(new BufferedReader(new FileReader(aMorphemesTextFile)));
	}

	/**
	 * Create a linking morphemes object from a reader object.
	 *
	 * Each line in the file must contain one morpheme. Use # in front of a line
	 * for comments.
	 *
	 * @param aReader
	 */
	public LinkingMorphemes(BufferedReader aReader) throws IOException
	{
		morphemes = new ArrayList<String>();
		String line;

		while ((line = aReader.readLine()) != null) {
			if (line.length() > 0 && line.charAt(0) != '#') {
				morphemes.add(line);
			}
		}
	}

	/**
	 * Returns all morphemes.
	 *
	 * @return
	 */
	public List<String> getAll()
	{
		return morphemes;
	}

	/**
	 * Checks if the given word starts with a morpheme
	 *
	 * @param aWord
	 * @return The length of the morpheme or -1 if it do not start with a
	 *         morpheme
	 */
	public int startsWith(String aWord)
	{
		for (String m : getAll()) {
			if (aWord.startsWith(m)) {
				return m.length();
			}
		}

		return -1;
	}
}
