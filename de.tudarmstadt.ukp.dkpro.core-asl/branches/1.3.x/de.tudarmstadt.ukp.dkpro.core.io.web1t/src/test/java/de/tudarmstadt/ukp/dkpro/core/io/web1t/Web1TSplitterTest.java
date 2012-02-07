/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.io.web1t;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class Web1TSplitterTest
{

	FrequencyDistribution<String> fdist;
	File input;
	File output;

	@Test
	public void testSplitter()
		throws IOException
	{
		Web1TFileSplitter splitter = new Web1TFileSplitter(input, output,
				"UTF-8", fdist, 0.1, 0, null);
		splitter.split();
		List<File> splits = splitter.getFiles();

		assertEquals(4, splitter.getNextUnusedFileNumber());

		//
		assertEquals(4, splits.size());
		assertEquals(12, countWordsInSplitFiles(splits));
		//
		splitter.cleanUp();
		splits = splitter.getFiles();
		assertEquals(0, splits.size());
	}

	private int countWordsInSplitFiles(List<File> splits)
		throws IOException
	{

		int words = 0;
		for (File file : splits) {

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			while (reader.readLine() != null) {
				words++;
			}

			reader.close();
		}
		return words;
	}

	@Before
	public void setUp()
		throws IOException
	{
		fdist = createTestInputFile();
		output = new File("src/test/resources/tmp." + this.getClass().getName());
		output.mkdir();
	}

	private FrequencyDistribution<String> createTestInputFile()
		throws IOException
	{
		input = new File("input.txt");

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(input), "UTF-8"));

		String[] words = new String[] { "Can", "you", "can", "a", "can", "as",
				"a", "canner", "can", "can", "a", "can" };

		FrequencyDistribution<String> fdist = new FrequencyDistribution<String>();
		for (String word : words) {
			writer.write(word + "\t" + "1" + "\n");

			if (word.length() > 1) {
				String subsKey = word.substring(0, 2);
				String subsKeyLowered = subsKey.toLowerCase();
				fdist.addSample(subsKeyLowered, 1);
			}
			else {
				String subsKey = word.substring(0, 1);
				String subsKeyLowered = subsKey.toLowerCase();
				fdist.addSample(subsKeyLowered, 1);
			}

		}

		writer.close();

		return fdist;
	}

	@After
	public void tearDown()
	{
		input.delete();

		File[] files = output.listFiles();

		for (File file : files) {
			file.delete();
		}

		output.delete();
	}

}
