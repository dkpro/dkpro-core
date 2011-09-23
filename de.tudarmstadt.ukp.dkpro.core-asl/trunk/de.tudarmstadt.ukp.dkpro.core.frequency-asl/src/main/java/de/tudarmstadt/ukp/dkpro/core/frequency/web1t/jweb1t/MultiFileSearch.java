/*
 * Copyright 2007 FBK-irst http://www.itc.it/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.dkpro.core.frequency.web1t.jweb1t;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * TO DO
 *
 * @author 	Claudio Giuliano
 * @version %I%, %G%
 * @since		1.0
 */
public class MultiFileSearch implements Search
{
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>MultiFileSearch</code>.
	 */
	static Logger logger = Logger.getLogger(MultiFileSearch.class.getName()); 
	
	//
	private final FileMap map;
	
	//
	public MultiFileSearch(File f) throws IOException
	{
		map = new FileMap(f);
	} // end constructor

	//
	public long getFreq(String t) throws IOException
	{
		////String ch = t.substring(0, 1);
		String ch = t.substring(0, 2);
		
		String[] file = map.get(ch);

		if (file == null)
		{
			logger.error(file + " is null for " + ch);
			return 0;
		}

		for (int i=0;i<file.length;i++)
		{
			logger.debug(i + ":" + file[i]);
			FileSearch fs = new FileSearch(new File(file[i]));
			long f = fs.getFreq(t);
			if (f != 0)
			{
				//logger.debug(file[i] + ", f('" + t + "') = " + f);
				fs.close();
				return f;
			}
			
			fs.close();
		} // end for i
		
		return 0;
	} // end getFreq

	//
	public static void main(String args[]) throws Exception
	{
		String logConfig = System.getProperty("log-config");
		if (logConfig == null)
			logConfig = "log-config.txt";
		
		PropertyConfigurator.configure(logConfig);
		
		if (args.length < 2)
		{
			logger.info("java de.tudarmstadt.ukp.parzonka.bbi.web1t.MultiFileSearch index-file n-gram+");
			System.exit(-1);
		}
		
		File path = new File(args[0]);
		MultiFileSearch mfs = new MultiFileSearch(path);
		
		for (int i=1;i<args.length;i++)
		{
			long f = mfs.getFreq(args[i]);
			logger.info("f('" + args[i] + "') = " + f);
		} // end for
	} // end main
	
} // end class MultiFileSearch
