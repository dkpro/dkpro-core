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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stopwordremover;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

/**
 * Used for storing stop words in a HashSet. Can be used as resource object in UIMA. Terms in stop
 * word files are converted to lower case.
 */
public class StopWordSet
	implements SharedResourceObject
{
	private HashSet<String> data;

	public StopWordSet()
	{
		super();
	}

	public StopWordSet(String[] fileNames)
		throws IOException
	{
		super();
		for (String fileName : fileNames) {
			addStopWordListFile(fileName);
		}
	}

	/**
	 * Loads a text file (UTF-8 encoding!) containing stop words. Only first
	 * word in each line will be taken into account. Everything after "|" will
	 * be treated as comment.
	 */
	public void addStopWordListFile(String fileName)
		throws IOException
	{
		Reader reader = null;
		try {
			reader = new FileReader(fileName);
			load(new FileReader(fileName));
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
	}

	@Override
	public void load(DataResource dataRes)
		throws ResourceInitializationException
	{
		InputStream is = null;
		try {
			is = dataRes.getInputStream();
			load(is);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}

	public void load(InputStream aIs)
		throws IOException
	{
		load(aIs, "UTF-8");
	}

    public void load(InputStream aIs, String aEncoding)
        throws IOException
    {
        load(new InputStreamReader(aIs, aEncoding));
    }

	public void load(Reader aReader)
		throws IOException
	{
		data = new HashSet<String>();
		String line = null;
		BufferedReader br = new BufferedReader(aReader);
		while ((line = br.readLine()) != null) {
			String[] words = line.trim().split("\\s|\\|");
			if (words.length > 0 && words[0].trim().length() > 0) {
				data.add(words[0].toLowerCase());
			}
		}
	}

	public boolean contains(String aWord)
	{
		return data.contains(aWord);
	}
}
