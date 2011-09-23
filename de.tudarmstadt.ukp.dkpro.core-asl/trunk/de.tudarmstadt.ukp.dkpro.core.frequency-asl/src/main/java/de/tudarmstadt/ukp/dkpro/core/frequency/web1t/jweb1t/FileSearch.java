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
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * TO DO
 *
 * @author 	Claudio Giuliano
 * @version %I%, %G%
 * @since		1.0
 */
class FileSearch implements Search
{
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>FileSearch</code>.
	 */
	static Logger logger = Logger.getLogger(FileSearch.class.getName()); 
	
	//
	private final RandomAccessFile raf;
	
	//
	private final File file;
	
	//
	public FileSearch(File file) throws IOException
	{
		this.file = file;
		//logger.info(f.getName());
		raf = new RandomAccessFile(file, "r");
		
	} // end constructor

	//
	public void close() throws IOException
	{
		raf.close();
	} // end close
	
	//
	public long getFreq(String t) throws IOException
	{
		//logger.info("searching '" + t + "' in " + file.getName());
		long s = 0;
		long m = raf.length();
		long e = raf.length();
		int loop = 0;
		while (e > (s + 1)) 
		{
			++loop;
			//logger.info("loop: " + loop);
			m = s + ((e - s) / 2);
			//logger.debug(s + ", [" + m + "], " + e);
			NGram n = read(m);
			if (n == null)
			{
				//logger.info("loop: " + loop);
				return 0;
			}

			int c = t.compareTo(n.s);
			if (c == 0)
			{
				//logger.debug(t + " == " + n.s + " (" + c + ")");
				//logger.info("loops: " + loop);
				return n.f;
			}
			else if (c > 0)
			{
				//logger.debug(t + " > " + n.s + " (" + c + ")");
				s = m;
			}
			else
			{
				//logger.debug(t + " < " + n.s + " (" + c + ")");
				e = m;
			}
			
		} // end while
		
		//logger.info("loops: " + loop);
		return 0;
 	} // end getFreq
	
	//
	public NGram read(long m) throws IOException
	{
		NGram n = null;
		long s = m - 50;
		if (s < 0)
			s = 0;
		long e = m + 50;
		if (e > raf.length())
			e = raf.length();
		
		int len = (int) (e - s);
		int nm = (int) (m - s);
		
		//logger.debug("nm = " + nm);
		//logger.debug("len = " + len);
		
		raf.seek(s);
		byte[] array = new byte[len];
		int l = raf.read(array);
		char ch = 0;
		int i = nm;
		while ((i >= 0) && ((ch = (char) array[i]) != '\n'))
		{
			//logger.debug("before: " + i + " : " + (char) array[i] + " : " + array[i]);
			i--;
		}
		
		int ns = i + 1;
		
		i = nm + 1;
		while ((i < array.length) && ((ch = (char) array[i]) != '\n'))
		{
			//logger.debug("after: " + i + " : " + (char) array[i] + " : " + array[i]);
			i++;
		}
		
		int ne = i;
					
		StringBuffer sb = new StringBuffer();
		for (int j=ns;j<ne;j++)
		{
			sb.append((char) array[j]);
			//logger.debug(j + " : " + (char) array[j] + " : " + array[j]);
		}
		
		if (sb.length() == 0)
			return null;
		
		return new NGram(sb.toString());
	} // end put
	
	//
	public static void main(String args[]) throws Exception
	{
		String logConfig = System.getProperty("log-config");
		if (logConfig == null)
			logConfig = "log-config.txt";
		
		PropertyConfigurator.configure(logConfig);
		
		if (args.length < 2)
		{
			logger.info("java de.tudarmstadt.ukp.parzonka.bbi.web1t.FileSearch [n-gram-file|n-gram-dir] n-gram+");
			System.exit(-1);
		}
		
		File path = new File(args[0]);
		
		if (path.isFile())
		{
			FileSearch h = new FileSearch(path);
			for (int i=1;i<args.length;i++)
			{
				long f = h.getFreq(args[i]);
				logger.info("f(" + args[i] + ") = " + f);			
			} // end for i
			h.close();
			
		}
		else
		{
			FolderScanner fs = new FolderScanner(path);
			fs.setFilter(new IndexFilter());
			
			long size = 0;
			int count = 0;
			while (fs.hasNext())
			{	
				Object[] files = fs.next();
				//System.out.println((count++) + " : " + files.length);
				for (int j=0;j<files.length;j++)
				{
					//long begin = System.currentTimeMillis();
					File file = (File) files[j];
					boolean found = false;
					FileSearch h = new FileSearch(file);
					for (int i=1;i<args.length;i++)
					{
						long f = h.getFreq(args[i]);
						if (f != 0)
						{
							logger.info(file + ".f(" + args[i] + ") = " + f);
							
							found = true;
							System.exit(0);
						}
							
					}			
					h.close();
					
					//long end = System.currentTimeMillis();
					//System.out.println(files[i] + " read in " + (end - begin) + " ms");
					
				} // end for i
				
				logger.info("size done " + size + " byte");
			} // end while
			
			
		}
	} // end main
	
} // end class FileSearch