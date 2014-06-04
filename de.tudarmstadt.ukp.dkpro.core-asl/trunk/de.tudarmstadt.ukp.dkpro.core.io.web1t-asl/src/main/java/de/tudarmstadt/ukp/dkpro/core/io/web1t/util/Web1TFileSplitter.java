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
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class Web1TFileSplitter
{
    private final Log log = LogFactory.getLog(getClass());

	private final File inputFile;
	private final File outputFolder;
	private final String fileEncoding;
	private final FrequencyDistribution<String> letterFD;
	private final double threshold;
	private int fileNumber;

	private List<File> splittedFiles = new LinkedList<File>();

	public Web1TFileSplitter(File aInputFile, File aOutputFolder,
			String aFileEncoding, FrequencyDistribution<String> aLetterFD,
			double aThreshold, int aStartingFileNumber)
	{
		inputFile = aInputFile;
		outputFolder = aOutputFolder;
		fileEncoding = aFileEncoding;
		letterFD = aLetterFD;
		threshold = aThreshold;
		fileNumber = aStartingFileNumber;
	}

	public List<File> getFiles()
	{
		return new LinkedList<File>(splittedFiles);
	}

    public void split()
            throws IOException
	{
        Map<String, String> letterToFileNameMap = mapStartingLettersToFilenames();
        Map<String, File> fileMap = mapFileNamesToFileHandels(letterToFileNameMap);
        Map<File, BufferedWriter> fileHandleToBufferdWriterMap = mapFileHandelsToWriterHandels(fileMap);
        Map<String, BufferedWriter> writerMap = mapFileNamesToWriterHandels(fileMap,
                fileHandleToBufferdWriterMap);

        splittedFiles = generateListOfUniqueFiles(fileMap);
	    
		BufferedReader reader = null;
		try {
    		reader = new BufferedReader(new InputStreamReader(
    				new FileInputStream(inputFile), fileEncoding));
    
    		String TAB = "\t";
    		String LF = "\n";
    
    		String readLine = null;
    		while ((readLine = reader.readLine()) != null) {
    
    			int indexOfTab = readLine.indexOf(TAB);
    
    			if (indexOfTab == -1) {
    			    log.warn("No tab found in line: " + readLine);
    				continue;
    			}
    
    			String key = Web1TUtil.getStartingLetters(readLine, indexOfTab);
    
    			Writer writer = writerMap.get(key);
    			if (writer == null) {
    			    log.warn("No writer found for key: " + key);
    				key = key.substring(0, 1);
    				writer = writerMap.get(key);
    				if (writer == null) {
    				    log.warn("No writer for key: " + key);
    					continue;
    				}
    			}
    
    			writer.write(readLine);
    			writer.write(LF);
    			writer.flush();
    		}
		}
		finally {
		    // Close reader
		    IOUtils.closeQuietly(reader);
		    // Close all writers
		    for (Writer writer : writerMap.values()) {
		        IOUtils.closeQuietly(writer);
		    }
		}
	}

	private Map<File, BufferedWriter> mapFileHandelsToWriterHandels(
			Map<String, File> fileMap)
		throws UnsupportedEncodingException, FileNotFoundException
	{

		Map<File, BufferedWriter> fileHandleToBufferdWriterMap = new HashMap<File, BufferedWriter>();

		for (String key : fileMap.keySet()) {
			File file = fileMap.get(key);
			if (fileHandleToBufferdWriterMap.get(file) == null) {
				fileHandleToBufferdWriterMap.put(file, new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(file), fileEncoding)));
			}

		}
		return fileHandleToBufferdWriterMap;
	}

	private Map<String, File> mapFileNamesToFileHandels(
			Map<String, String> letterToFileNameMap)
	{
		Map<String, File> fileMap = new HashMap<String, File>();

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

	private Map<String, String> mapStartingLettersToFilenames()
	{

		Map<String, String> letterToFileNameMap = new HashMap<String, String>();

		List<String> keyList = new LinkedList<String>(letterFD.getKeys());
		Collections.sort(keyList);
		for (String key : keyList) {

			Long freq = letterFD.getCount(key);
			Long total = letterFD.getN();

			double percentage = (double) freq / total * 100;
			if ((threshold > 0.0) && (percentage >= threshold)) {
				String filename = String.format("%08d", fileNumber++);
				letterToFileNameMap.put(key, filename);
			}
			else {
				letterToFileNameMap.put(key, "99999999");
			}
		}

		return letterToFileNameMap;
	}

	private Map<String, BufferedWriter> mapFileNamesToWriterHandels(
			Map<String, File> fileMap,
			Map<File, BufferedWriter> fileHandleToBufferdWriterMap)
		throws UnsupportedEncodingException, FileNotFoundException
	{
		Map<String, BufferedWriter> nameToWriterMap = new HashMap<String, BufferedWriter>();
		for (String key : fileMap.keySet()) {
			File file = fileMap.get(key);
			BufferedWriter writer = fileHandleToBufferdWriterMap.get(file);
			nameToWriterMap.put(key, writer);
		}

		return nameToWriterMap;
	}

	private List<File> generateListOfUniqueFiles(Map<String, File> fileMap)
	{
		// Generate unique Filelist
		Map<String, String> uniqeFiles = new HashMap<String, String>();
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
