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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


public class Web1TFileConsolidator
{

	private final List<File> inputFiles;
	private final Comparator<String> comparator;
	private LinkedList<File> consolidatedFiles = new LinkedList<File>();
	private final String fileEncoding;
	private final int minFreq;

	private final String TAB = "\t";
	private final String LF = "\n";

	public Web1TFileConsolidator(List<File> sortedInputFiles,
			Comparator<String> comparator, String fileEncoding, int minFreq)
	{
		this.inputFiles = sortedInputFiles;
		this.comparator = comparator;
		this.fileEncoding = fileEncoding;
		this.minFreq = minFreq;
	}

	public void consolidate()
		throws IOException
	{

		consolidatedFiles = new LinkedList<File>();
		// new temporary files for storing the sorted and consolidated data
		for (File file : inputFiles) {
			consolidatedFiles.add(new File(Web1TUtil
					.cutOffUnderscoredSuffixFromFileName(file) + "_cons"));
		}

		for (int i = 0; i < inputFiles.size(); i++) {

			File file_in = inputFiles.get(i);
			File file_out = consolidatedFiles.get(i);

			BufferedReader sortedSplitFileReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file_in),
							fileEncoding));

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file_out), fileEncoding));

			String prevEntry = null;
			String entry = null;
			Integer prevEntryFreq = null;
			Integer entryFreq = null;

			while ((entry = sortedSplitFileReader.readLine()) != null) {

				int tabPos = entry.indexOf(TAB);

				if (hasLineInvalidFormat(tabPos)) {
				    System.err.println("Wrong file format in line: " + entry);
					continue;
				}

				String entryWithoutFreq = extractTextValue(entry, tabPos);
				entryFreq = extractFreqValue(entry, tabPos);

				if (isFirstIteration(prevEntry, prevEntryFreq)) {
					prevEntry = entryWithoutFreq;
					prevEntryFreq = entryFreq;
				}
				else {

					// Entries are equal, add up frequency
					if (arePrevEntryAndCurrentEntryEqual(prevEntry,
							entryWithoutFreq, comparator)) {
						prevEntryFreq += entryFreq;
					}
					else { // Entry changed, write aggregated entry

						writeAggregatedEntryToFile(writer, prevEntry,
								prevEntryFreq);

						// Prepare next iteration
						prevEntry = entryWithoutFreq;
						prevEntryFreq = entryFreq;
					}
				}

			}
			writeAggregatedEntryToFile(writer, prevEntry, prevEntryFreq);
			writer.close();
			
			sortedSplitFileReader.close();
		}
	}

	private void writeAggregatedEntryToFile(BufferedWriter writer,
			String entry, Integer entryFrequency)
		throws IOException
	{

		if (entryFrequency < minFreq) {
			return;
		}

		writer.write(entry + TAB + entryFrequency + LF);
	}

	private boolean arePrevEntryAndCurrentEntryEqual(String prevEntry,
			String entryWithoutFreq, Comparator<String> comparator)
	{
		return comparator.compare(prevEntry, entryWithoutFreq) == 0;
	}

	private boolean isFirstIteration(String prevEntry, Integer prevEntryFreq)
	{
		return prevEntry == null || prevEntryFreq == null;
	}

	private boolean hasLineInvalidFormat(int tabPos)
	{
		return (tabPos < 0);
	}

	private Integer extractFreqValue(String entry, int tabPos)
	{
		String freqOfEntryAsString = entry.substring(tabPos + 1);
		Integer freqOfEntryAsInt = Integer.parseInt(freqOfEntryAsString);
		return freqOfEntryAsInt;
	}

	private String extractTextValue(String entry, int tabPos)
	{

		return entry.substring(0, tabPos);
	}

	public LinkedList<File> getConsolidatedFiles()
	{
		return new LinkedList<File>(consolidatedFiles);
	}

	public void cleanUp()
	{
		for (File file : consolidatedFiles) {
			file.delete();
		}
		consolidatedFiles = new LinkedList<File>();
	}
}
