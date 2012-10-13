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

import java.io.File;

import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

public class Web1TUtil
{

	public static void writeToLog(Logger logger, String desc, String entry)
	{
		if (logger != null) {
			logger.log(Level.WARNING, desc + " " + entry);
		}
	}

	public static String cutOffUnderscoredSuffixFromFileName(File file)
	{

		String path = file.getAbsolutePath();

		return path.substring(0, path.lastIndexOf("_"));
	}

	public static String getStartingLetters(String readLine, int indexOfTab)
	{
		String line = readLine.substring(0, indexOfTab);

		String key = null;
		if (line.length() > 1) {
			key = readLine.substring(0, 2);
		}
		else {
			key = readLine.substring(0, 1);
		}
		key = key.toLowerCase();
		return key;
	}
}
