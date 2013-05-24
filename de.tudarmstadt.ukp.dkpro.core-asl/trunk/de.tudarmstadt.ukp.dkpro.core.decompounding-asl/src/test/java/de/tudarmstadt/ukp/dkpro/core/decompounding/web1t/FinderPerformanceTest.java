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

package de.tudarmstadt.ukp.dkpro.core.decompounding.web1t;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class FinderPerformanceTest
{

	/**
	 * Test the finder performace on the generated web1t index Time is printed
	 * on the console.
	 * @throws IOException
	 */
	@Test
	public void testPerformance1() throws IOException
	{
		File file = new File("/home/jens/Desktop/web1tIndex4");
		if (!file.exists()) {
			return;
		}
		File jWeb1T = new File("/home/likewise-open/UKP/santos/UKP/Library/DKPro/web1t/de");
		Finder f = new Finder(file, jWeb1T);
		String[] words = new String[] { "hallo welt", "wie geht es euch",
				"alpha", "zutun", "lasst uns nach hause gehen", "rennen" };
		long time = 0;

		for (String word : words) {
			long start = System.currentTimeMillis();
			Assert.assertTrue(f.find(word).size() > 0);
			long end = System.currentTimeMillis();
			time += end - start;
			System.out.println("Time for '" + word + "' (ms): "
					+ (end - start));
		}

		System.out.println("  -> Average time (ms): "
				+ ((float) time / (float) words.length));
	}

	@Test
	public void testPerformance2() throws IOException
	{
		File file = new File("/home/jens/Desktop/web1tIndex4");
		File jWeb1T = new File("/home/likewise-open/UKP/santos/UKP/Library/DKPro/web1t/de");
		if (!file.exists()) {
			return;
		}

		Finder f = new Finder(file, jWeb1T);

		String[] words = { "filmtauscher", "minimalanforderungen",
				"berufungsinstanz" };

		long time = 0;
		long count = 0;
		for (String word : words) {
			for (int i = 1; i < word.length(); i++) {
				String searchFor = word.substring(0, i);

				long start = System.currentTimeMillis();
				f.contains(searchFor);
				long end = System.currentTimeMillis();

				time += end - start;
				count++;

				System.out.println("Time for '" + searchFor + "' (ms): "
						+ (end - start));
			}
		}

		System.out.println("Average time (ms): "
				+ ((float) time / (float) count));
	}
}
