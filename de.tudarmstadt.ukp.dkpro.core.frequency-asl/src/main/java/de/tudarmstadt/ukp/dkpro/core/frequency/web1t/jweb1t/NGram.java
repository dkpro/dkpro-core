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

import org.apache.log4j.Logger;

/**
 * TO DO
 *
 * @author 	Claudio Giuliano
 * @version %I%, %G%
 * @since		1.0
 */
class NGram
{
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>NGram</code>.
	 */
	static Logger logger = Logger.getLogger(NGram.class.getName()); 
	
	//
	public String s;
	
	//
	public long f;
	
	//
	public NGram(String line)
	{
		try
		{
			String[] t = line.split("\t");
			s = t[0];
			f = Long.parseLong(t[1]);
		}
		catch (Exception e)
		{
			f = 0;
			logger.debug("error at line: '" + line + "'");
			logger.debug(e);
		}
	}
	
	public NGram(String s, long f) {
		this.s = s;
		this.f = f;
	}
}
