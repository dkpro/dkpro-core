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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Scans recursively a directory. Successive calls to the
 * <code>next</code> method return successive arrays of 
 * objects <code>File</code>.
 * <p>
 * The following code fragment, in which <code>root</code> is
 * the starging directory, illustrates how to use a folder
 * scanner.
 * <p>
 * <pre>
 *		FolderScanner fs = new FolderScanner(root);
 *		while (fs.hasNext())
 *		{	
	 *			Object[] files = fs.next();
	 *
		 *			for (int i=0;i&lt;files.length;i++)
			 *				System.out.println((File) files[i]);
	 *		}
 * </pre>
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 2.0
 */
final class FolderScanner
{	
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>FolderScanner</code>.
	 */
	static Logger logger = Logger.getLogger(FolderScanner.class.getName()); 
	
	/** the name root path */
	private final File root;
	
	/** the name root path */
	private final Stack stack;
	
	/** the name root path */
	private FileFilter filter;
		
	/**
	 * Create a folder scanner.
	 *
	 * @param root the root directory.
	 */
	public FolderScanner(File root)
	{
		//System.out.println("FolderScanner.FolderScanner");
		this.root = root;
		
		stack = new Stack();
		stack.push(root);
		
	} // end constructor
	
	/**
		* Sets a file filter for this scanner
	 * @param filter a file filter.
	 */
	public void setFilter(FileFilter filter)
	{
		//System.out.println("FolderScanner.setFilter");
		this.filter = filter;
	} // end setFilter
		
	/**
		* Returns <code>true</code> if the scanner has more
	 * directories. (In other words, returns <code>true</code>
									 * if <code>next</code> would return an array of files
									 * rather than return <code>null</code>.) 
	 *
	 * @return <code>true</code> if the scanner has more elements.
	 */ 
	public boolean hasNext()
	{
		//System.out.println("FolderScanner.hasNext");
		
		if (!stack.empty())	
			return true;
		
		return false;
	} // end hasNext
	
	/**
		* Returns the next array of files in the iteration. 
	 *
	 * @return the next array of files in the iteration. 
	 */
	public Object[] next()
	{
		//System.out.println("FolderScanner.next");
		File dir = null;
		
		if (!stack.empty())	
		{
			List res = new ArrayList();
			try
			{
				dir = (File) stack.pop();
				
				File[] ls = null;
				if (filter == null)
					ls = dir.listFiles();
				else
					ls = dir.listFiles(filter);
				
				for(int i=0;i<ls.length;i++)
				{
					if (ls[i].isFile())
					{
						res.add(ls[i]);
					}
					else if (ls[i].isDirectory())
					{
						stack.push(ls[i]);
					}
				} // end for
			}
			catch (Exception e)
			{
			    e.printStackTrace();
				System.err.println("Exception thrown \"FolderScanner.next\" " + e.toString());
			}
			
			return res.toArray();
		} // end if
		
		return null;
	} // end next
	
	//
	public static void main(String[] args) throws Exception
	{
		String logConfig = System.getProperty("log-config");
		if (logConfig == null)
			logConfig = "log-config.txt";
		
		PropertyConfigurator.configure(logConfig);
		
		
		//java org.FBK.irst.tcc.corpora.util.FolderScanner dir sense-file example-dir
		FolderScanner fs = new FolderScanner(new File(args[0]));
		
		long size = 0;
		int count = 0;
		while (fs.hasNext())
		{	
			Object[] files = fs.next();
			System.out.println((count++) + " : " + files.length);
			for (int i=0;i<files.length;i++)
			{
				long begin = System.currentTimeMillis();
				File file = (File) files[i];
				
				
				long end = System.currentTimeMillis();
				System.out.println(files[i] + " read in " + (end - begin) + " ms");
				
			} // end for i
			
			
			
			logger.info("size done " + size + " byte");
		} // end while
		
		
	} // end main
} // end class FolderScanner