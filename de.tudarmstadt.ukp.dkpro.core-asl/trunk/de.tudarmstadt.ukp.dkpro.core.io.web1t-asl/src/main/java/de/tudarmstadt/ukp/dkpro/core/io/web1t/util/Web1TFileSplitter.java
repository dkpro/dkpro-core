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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class Web1TFileSplitter
{

	private final File inputFile;
	private final File outputFolder;
	private final String fileEncoding;
	private final FrequencyDistribution<String> letterFD;
	private final double threshold;
	private int fileNumber;

	private HashMap<String, BufferedWriter> writerMap;
	private LinkedList<File> splittedFiles = new LinkedList<File>();

	public Web1TFileSplitter(File inputFile, File outputFolder,
			String fileEncoding, FrequencyDistribution<String> letterFD,
			double threshold, int startingFileNumber)
	{
		this.inputFile = inputFile;
		this.outputFolder = outputFolder;
		this.fileEncoding = fileEncoding;
		this.letterFD = letterFD;
		this.threshold = threshold;
		this.fileNumber = startingFileNumber;
	}

	public void split()
		throws IOException
	{
		createMappingsAndFileList();
		distributeInputFileToSplitFiles();
	}

	public LinkedList<File> getFiles()
	{
		return new LinkedList<File>(splittedFiles);
	}

	private void distributeInputFileToSplitFiles()
		throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputFile), fileEncoding));

		String TAB = "\t";
		String LF = "\n";

		String readLine = null;
		while ((readLine = reader.readLine()) != null) {

			int indexOfTab = readLine.indexOf(TAB);

			if (indexOfTab == -1) {
			    System.err.println("No tab found in line: " + readLine);
				continue;
			}

			String key = Web1TUtil.getStartingLetters(readLine, indexOfTab);

			BufferedWriter writer = writerMap.get(key);

			if (writer == null) {
			    System.err.println("No writer found for key: " + key);
				key = key.substring(0, 1);
				writer = writerMap.get(key);
				if (writer == null) {
				    System.err.println("No writer for key: " + key);
					continue;
				}
			}

			writer.write(readLine);
			writer.write(LF);
			writer.flush();
		}
		reader.close();
	}

	private void createMappingsAndFileList()
		throws UnsupportedEncodingException, FileNotFoundException
	{
		HashMap<String, String> letterToFileNameMap = null;
		HashMap<String, File> fileMap = null;
		HashMap<File, BufferedWriter> fileHandleToBufferdWriterMap = null;

		letterToFileNameMap = mapStartingLettersToFilenames();
		fileMap = mapFileNamesToFileHandels(letterToFileNameMap);
		fileHandleToBufferdWriterMap = mapFileHandelsToWriterHandels(fileMap);
		writerMap = mapFileNamesToWriterHandels(fileMap,
				fileHandleToBufferdWriterMap);

		splittedFiles = generateListOfUniqueFiles(fileMap);
	}

	private HashMap<File, BufferedWriter> mapFileHandelsToWriterHandels(
			HashMap<String, File> fileMap)
		throws UnsupportedEncodingException, FileNotFoundException
	{

		HashMap<File, BufferedWriter> fileHandleToBufferdWriterMap = new HashMap<File, BufferedWriter>();

		for (String key : fileMap.keySet()) {
			File file = fileMap.get(key);
			if (fileHandleToBufferdWriterMap.get(file) == null) {
				fileHandleToBufferdWriterMap.put(file, new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(file), fileEncoding)));
			}

		}
		return fileHandleToBufferdWriterMap;
	}

	private HashMap<String, File> mapFileNamesToFileHandels(
			HashMap<String, String> letterToFileNameMap)
	{

		HashMap<String, File> fileMap = new HashMap<String, File>();

		for (String key : letterToFileNameMap.keySet()) {
			fileMap.put(key, new File(outputFolder + "/" + letterToFileNameMap.get(key)
							+ "_unsorted"));
		}
		return fileMap;
	}

	public int getNextUnusedFileNumber()
	{
		return fileNumber;
	}

	private HashMap<String, String> mapStartingLettersToFilenames()
	{

		HashMap<String, String> letterToFileNameMap = new HashMap<String, String>();

		LinkedList<String> keyList = new LinkedList<String>(letterFD.getKeys());
		Collections.sort(keyList);
		for (String key : keyList) {

			Long freq = letterFD.getCount(key);
			Long total = letterFD.getN();

			double percentage = (double) freq / total * 100;
			if ((threshold > 0.0) && (percentage >= threshold)) {
				String filename = new Formatter().format("%08d", fileNumber++).toString();
				letterToFileNameMap.put(key, filename);
			}
			else {
				letterToFileNameMap.put(key, "99999999");
			}
		}

		return letterToFileNameMap;
	}

	private HashMap<String, BufferedWriter> mapFileNamesToWriterHandels(
			HashMap<String, File> fileMap,
			HashMap<File, BufferedWriter> fileHandleToBufferdWriterMap)
		throws UnsupportedEncodingException, FileNotFoundException
	{
		HashMap<String, BufferedWriter> nameToWriterMap = new HashMap<String, BufferedWriter>();
		for (String key : fileMap.keySet()) {
			File file = fileMap.get(key);
			BufferedWriter writer = fileHandleToBufferdWriterMap.get(file);
			nameToWriterMap.put(key, writer);
		}

		return nameToWriterMap;
	}

	private LinkedList<File> generateListOfUniqueFiles(HashMap<String, File> fileMap)
	{
		// Generate unique Filelist
		HashMap<String, String> uniqeFiles = new HashMap<String, String>();
		for (File file : fileMap.values()) {
			String absPath = file.getAbsolutePath();
			if (uniqeFiles.get(absPath) == null) {
				uniqeFiles.put(absPath, "");
			}
		}

		LinkedList<File> listOfUniqueFiles = new LinkedList<File>();
		for (String path : uniqeFiles.keySet()) {
			listOfUniqueFiles.add(new File(path));
		}
		return listOfUniqueFiles;
	}

	public void cleanUp()
	{
		for (File file : splittedFiles) {
			file.delete();
		}
		splittedFiles = new LinkedList<File>();
	}
}
