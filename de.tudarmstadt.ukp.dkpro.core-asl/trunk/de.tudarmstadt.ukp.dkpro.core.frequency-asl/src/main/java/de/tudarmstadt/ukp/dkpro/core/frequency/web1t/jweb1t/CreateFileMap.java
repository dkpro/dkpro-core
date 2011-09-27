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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * TO DO
 *
 * @author 	Claudio Giuliano
 */
public class CreateFileMap
{
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>CreateFileMap</code>.
	 */
    static Logger logger = Logger.getLogger(CreateFileMap.class.getName()); 
	
	private final Map<String,List<File>> map;
	
	private final File baseDir;
	
	public CreateFileMap(File baseDir, String n)
	    throws IOException
	{
		map = new HashMap<String,List<File>>();
		
        this.baseDir = baseDir;

        File ngramFile = new File(baseDir + "/" + n + "gms/");
        File indexFile = new File(baseDir + "/" + "index-" + n + "gms");
        
        if (ngramFile.isFile()) {
			read(ngramFile);
		}
		else {
			FolderScanner fs = new FolderScanner(ngramFile);
			fs.setFilter(new IndexFilter());
			
			int count = 0;
			while (fs.hasNext()) {	
				Object[] files = fs.next();
				logger.info((count++) + " : " + files.length);
				for (int i=0;i<files.length;i++) {
					long begin = System.currentTimeMillis();
					File f = (File) files[i];
					
					System.out.println(f.getAbsolutePath());
					
					read(f);
					
					long end = System.currentTimeMillis();
					logger.info(count + ", " + files[i] + " read in " + (end - begin) + " ms");
					count++;
				}
			}
		}
		
		write(indexFile);
	}
	
	private void write(File outputFile) throws IOException
	{
		PrintWriter pw = new PrintWriter(new FileWriter(outputFile));

		for (String ch : map.keySet()) {
			List<File> fileList = map.get(ch);
			pw.print(ch);
			for (File file : fileList) {
			    // store only the path relative to the index file
			    String relative = baseDir.toURI().relativize(file.toURI()).getPath();
			    pw.print("\t" + relative);
			}
			pw.print("\n");
		}
		
		pw.flush();
		pw.close();
	}
	
	private void read(File f) throws IOException
	{
		
		LineNumberReader lr = new LineNumberReader(new FileReader(f));
		String line = null;
		
		String ch = null;
		
		// first line
		if ((line = lr.readLine()) != null) {
			ch = line.substring(0, 2).trim();
		}
						
		String old = ch;
		
		// second line
		while ((line = lr.readLine()) != null) {
			ch = line.substring(0, 2).trim();
			
			if (!ch.equals(old)) {
				put(old, f);
				old = ch;
			}
		}
		
		put(old, f);
		lr.close();
	}
	
	private void put(String ch, File f)	{
		List<File> fileList = map.get(ch);
		
		if (fileList == null) {
			fileList = new ArrayList<File>();
			fileList.add(f);
			map.put(ch, fileList);
			return;
		}
		
		fileList.add(f);
	}

	public static void main(String args[])
	    throws Exception
	{
		
		if (args.length != 2) {
			System.out.print(getHelp());
			System.exit(-1);
		}
		
		new CreateFileMap(
		        new File(args[0]),
		        args[1]
		);
	}
	
	/**
	 * Returns a command-line help.
	 *
	 * return a command-line help.
	 */
	private static String getHelp()
	{
		StringBuffer sb = new StringBuffer();
		
		// License
		sb.append(License.get());
		
		// Usage
		sb.append("Usage: " + CreateFileMap.class.getName() + " n-gram-dir index-file\n\n");
		
		// Arguments
		sb.append("Arguments:\n");
		sb.append("\tn-gram-dir\t-> directory containing the n-gram files\n");
		sb.append("\tindex-file\t-> file in which to store resulting index\n");

		return sb.toString();
	}	
}