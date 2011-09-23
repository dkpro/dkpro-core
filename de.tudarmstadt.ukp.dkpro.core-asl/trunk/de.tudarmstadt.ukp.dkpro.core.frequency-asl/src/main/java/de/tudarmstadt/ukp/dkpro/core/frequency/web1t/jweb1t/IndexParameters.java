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


public class IndexParameters
{
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>IndexParameters</code>.
	 */
	static Logger logger = Logger.getLogger(IndexParameters.class.getName()); 
	
		//
		private FileMap map;
	
		//
		private Integer ngram;
		
		//
		private String fileName;
		
		/*
		public IndexParameters()
		{
			//
			logger.info("IndexParameters");

		} // end constructor
		*/
		
		//
		public void setLength(String len)
		{
			//logger.debug("setLength: " + len);
			this.ngram = new Integer(len);
		} // end setLength
		
		//	
		public void setFileName(String fileName)
		{
			//logger.debug("setFileName: " + fileName);
			this.fileName = fileName;
		} // end setClassName
		
		//
		public FileMap getFileMap() throws IOException
		{
			return new FileMap(new File(fileName));
		} // end getFileMap
		
		//
		public Integer getNgram()
		{
			return ngram;
		} // end getNgram
		
		//
		@Override
        public String toString()
		{
			return "IndexParameters(" + ngram + ", " + fileName + ")";
		}
} // end class IndexParameters