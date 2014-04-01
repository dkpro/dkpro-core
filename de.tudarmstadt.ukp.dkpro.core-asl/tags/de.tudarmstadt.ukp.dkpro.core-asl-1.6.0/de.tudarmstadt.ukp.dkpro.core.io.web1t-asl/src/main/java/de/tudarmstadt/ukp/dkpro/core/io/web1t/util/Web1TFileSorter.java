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
package de.tudarmstadt.ukp.dkpro.core.io.web1t.util;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;



public class Web1TFileSorter
{

	private final LinkedList<File> inputFiles;
	private LinkedList<File> sortedFiles = new LinkedList<File>();
	private final Comparator<String> comparator;

	public Web1TFileSorter(LinkedList<File> unsortedFiles,
			Comparator<String> comparator)
	{
		this.inputFiles = unsortedFiles;
		this.comparator = comparator;
	}

	public void sort()
		throws IOException
	{
		for (File file : inputFiles) {

			List<File> l = ExternalSort.sortInBatch(file, comparator);

			File sortedSplitFile = new File(
					Web1TUtil.cutOffUnderscoredSuffixFromFileName(file)
							+ "_sorted");
			sortedFiles.add(sortedSplitFile);
			ExternalSort.mergeSortedFiles(l, sortedSplitFile, comparator);
		}
	}

	public LinkedList<File> getSortedFiles()
	{
		return new LinkedList<File>(sortedFiles);
	}

	public void cleanUp()
	{
		for (File file : sortedFiles) {
			file.delete();
		}
		sortedFiles = new LinkedList<File>();
	}

}
