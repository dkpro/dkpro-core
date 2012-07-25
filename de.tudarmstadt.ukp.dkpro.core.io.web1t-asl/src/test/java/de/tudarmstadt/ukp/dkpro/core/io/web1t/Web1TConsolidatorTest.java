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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class Web1TConsolidatorTest
{
	LinkedList<File> filesToSort;
	Comparator<String> comparator;

	@Test
	public void testConsolidation()
		throws IOException
	{
		Web1TFileConsolidator consolidator = new Web1TFileConsolidator(
				filesToSort, comparator, "UTF-8", 1, null);

		consolidator.consolidate();

		LinkedList<File> consolidatedFiles = consolidator
				.getConsolidatedFiles();

		String TAB = "\t";

		assertEquals(2, consolidatedFiles.size());

		// File #1
		File file = consolidatedFiles.pop();
		String[] lines = getLines(file);

		assertEquals(3, lines.length);
		assertEquals("a" + TAB + "5", lines[0]);
		assertEquals("although" + TAB + "16", lines[1]);
		assertEquals("annoying" + TAB + "5", lines[2]);

		// File #2
		file = consolidatedFiles.pop();
		lines = getLines(file);

		assertEquals(4, lines.length);
		assertEquals("often" + TAB + "5", lines[0]);
		assertEquals("oil" + TAB + "32", lines[1]);
		assertEquals("out" + TAB + "2", lines[2]);
		assertEquals("out-of-order" + TAB + "9", lines[3]);
		// Clean up calls
		consolidator.cleanUp();
		consolidatedFiles = consolidator.getConsolidatedFiles();
		assertEquals(0, consolidatedFiles.size());

	}

	@Test
	public void testConsolidationWithFreq10()
		throws IOException
	{
		Web1TFileConsolidator consolidator = new Web1TFileConsolidator(
				filesToSort, comparator, "UTF-8", 10, null);

		consolidator.consolidate();

		LinkedList<File> consolidatedFiles = consolidator
				.getConsolidatedFiles();

		String TAB = "\t";

		assertEquals(2, consolidatedFiles.size());

		// File #1
		File file = consolidatedFiles.pop();
		String[] lines = getLines(file);

		assertEquals(1, lines.length);
		assertEquals("although" + TAB + "16", lines[0]);

		// File #2
		file = consolidatedFiles.pop();
		lines = getLines(file);

		assertEquals(1, lines.length);
		assertEquals("oil" + TAB + "32", lines[0]);
		// Clean up calls
		consolidator.cleanUp();
		consolidatedFiles = consolidator.getConsolidatedFiles();
		assertEquals(0, consolidatedFiles.size());

	}

	@Before
	public void setUp()
	{
		setUpFileList();
		setUpComparator();

	}

	private void setUpComparator()
	{
		comparator = new Comparator<String>()
		{
			public int compare(String r1, String r2)
			{
				return r1.compareTo(r2);
			}
		};
	}

	private void setUpFileList()
	{
		filesToSort = new LinkedList<File>();

		File file_1 = new File(
				"src/test/resources/Web1TConsolidator/00000000_sorted");
		File file_2 = new File(
				"src/test/resources/Web1TConsolidator/00000001_sorted");

		filesToSort.add(file_1);
		filesToSort.add(file_2);

	}

	private String[] getLines(File file)
		throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF-8"));

		LinkedList<String> lines = new LinkedList<String>();

		String line = "";
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}

		return lines.toArray(new String[0]);
	}
}
