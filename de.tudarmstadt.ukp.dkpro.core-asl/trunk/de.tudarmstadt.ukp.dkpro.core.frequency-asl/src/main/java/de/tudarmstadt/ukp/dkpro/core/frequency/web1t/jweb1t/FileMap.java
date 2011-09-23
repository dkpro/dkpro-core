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
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * TO DO
 *
 * @author 	Claudio Giuliano
 * @version %I%, %G%
 * @since		1.0
 */
public class FileMap
{
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>FileMap</code>.
	 */
	static Logger logger = Logger.getLogger(FileMap.class.getName()); 
	
    private static final String LF = System.getProperty("line.separator"); 
	
	private final Map<String,String[]> map;
	
	private final File indexFile;
	
	public FileMap(File f) throws IOException {
		this.indexFile = f;
		map = new HashMap<String,String[]>();
		read(f);
	} 
	
	protected void read(File f)
	    throws IOException
	{
		LineNumberReader lr = new LineNumberReader(new FileReader(f));
		String line = null;
		while ((line = lr.readLine()) != null) {
			if (line.length() > 0) {
				String[] s = line.split("\t");
				String[] t = new String[s.length - 1];
				System.arraycopy(s, 1, t, 0, t.length);

				// get absolute path for stored relative path
				for (int i=0; i<t.length; i++) {
				    t[i] = new File(indexFile.getParent(), t[i]).getAbsolutePath();
				}

				map.put(s[0], t);
			}
		}
		
		lr.close();
	}
	
	public String[] get(String ch)
	{
		return map.get(ch);
	}
	
	public static void main(String args[]) throws Exception
	{
		String logConfig = System.getProperty("log-config");
		if (logConfig == null)
			logConfig = "log-config.txt";
		
		PropertyConfigurator.configure(logConfig);
		
		if (args.length != 2)
		{
			logger.info("java PACKAGE.FileMap index-file char");
			System.exit(-1);
		}
		
		File f = new File(args[0]);
		String ch = args[1].substring(0, 1);
		
		FileMap map = new FileMap(f);
		String[] s = map.get(ch);
		
		System.out.print("\"" + ch + "\"\t");
		
		for (int i=0;i<s.length;i++)
		{
			if (i > 0)
				System.out.print(" ");
			
			System.out.print(s[i]);

		}
		
		System.out.print("\n");
	} 

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            sb.append(key);
            sb.append(" - ");
            sb.append(StringUtils.join(map.get(key), ","));
            sb.append(LF);
        }
        sb.append(LF);
        
        return sb.toString();
    }
}