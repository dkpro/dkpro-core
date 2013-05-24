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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.igerman98.Affix;

/**
 * The igerman98 dictionary from www.j3e.de/ispell/igerman98
 *
 * A current version of the german dictionary de_DE can be found in
 * /src/main/resources/de_DE.dic
 *
 * This class can also be used to read other ispell/hunspell dictionaries.
 *
 * @author Jens Haase <je.haase@googlemail.com>
 *
 */
public class German98Dictionary
	extends SimpleDictionary
{

	private static final String PREFIX_KEY = "PFX";
	private static final String SUFFIX_KEY = "SFX";

	private Map<Character, List<Affix>> affixes = new HashMap<Character, List<Affix>>();

	public German98Dictionary(File aDict, File aAffix)
	{
		try {
			readAffixFile(new BufferedReader(new FileReader(aAffix)));
			setWords(readFileToSet(new BufferedReader(new FileReader(aDict))));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public German98Dictionary(InputStream aDictStream, InputStream aAffixStream)
	{
		try {
			readAffixFile(new BufferedReader(new InputStreamReader(aAffixStream)));
			setWords(readFileToSet(new BufferedReader(new InputStreamReader(aDictStream))));
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected Set<String> readFileToSet(BufferedReader aReader)
		throws IOException
	{
		Set<String> words = new HashSet<String>();

		// First line contains number of entries -> skip
		String line = aReader.readLine();
		while ((line = aReader.readLine()) != null) {
			if (line.equals("") || line.substring(0, 1).equals("#")
					|| line.substring(0, 1).equals("\t")) {
				// Ignore lines starting with hash of tab (comments)
				continue;
			}
			String[] split = line.split("/");
			String word = split[0].toLowerCase();
			char[] flags = {};

			if (split.length > 1) {
				flags = split[1].toCharArray();
			}

			if (word.length() > 2) {
				words.add(word);
			}

			if (flags.length > 0) {
				words.addAll(buildWords(word, flags));
			}
		}

		return words;
	}

	/**
	 * Reads the affix file and processes the data
	 */
	protected void readAffixFile(BufferedReader aReader)
	{
		try {
			String line;
			while ((line = aReader.readLine()) != null) {
				if (line.startsWith(PREFIX_KEY) || line.startsWith(SUFFIX_KEY)) {
					parseAffix(line, aReader);
				}
			}
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Parse a affix in the affix file
	 *
	 * @param aHeader
	 *            The header of the affix
	 * @param aReader
	 *            The file reader to read the rest of the affix
	 * @throws IOException
	 */
	private void parseAffix(String aHeader, BufferedReader aReader)
		throws IOException
	{
		String args[] = aHeader.split("\\s+");

		boolean crossProduct = args[2].equals("Y");
		int numLines = Integer.parseInt(args[3]);

		for (int i = 0; i < numLines; i++) {
			String line = aReader.readLine();
			if (line == null) {
				throw new IOException("Unexpected end of file after reading [" + i +
						"] lines. Expected were [" + numLines + "] lines.");
			}
			String ruleArgs[] = line.split("\\s+");
			Character flag = ruleArgs[1].toCharArray()[0];

			Affix a = new Affix(args[0]);
			a.setCrossProduct(crossProduct);
			a.setFlag(flag);
			a.setStripping(ruleArgs[2]);
			a.setAffix(ruleArgs[3]);
			a.setCondition(ruleArgs[4]);

			List<Affix> list = affixes.get(flag);
			if (list == null) {
				list = new ArrayList<Affix>();
				affixes.put(flag, list);
			}
			list.add(a);
		}
	}

	/**
	 * Uses affixes to build new words
	 *
	 * @param aWord
	 * @param aFlags
	 * @return
	 */
	protected List<String> buildWords(String aWord, char[] aFlags)
	{
		List<String> words = new ArrayList<String>();
		for (char c : aFlags) {
			List<Affix> aff = affixes.get(c);
			if (aff == null) {
				continue;
			}
			for (Affix affix : aff) {
				String w = affix.handleWord(aWord);
				if (w != null && w.length() > 2) {
					words.add(w);
				}
			}
		}

		return words;
	}
}
