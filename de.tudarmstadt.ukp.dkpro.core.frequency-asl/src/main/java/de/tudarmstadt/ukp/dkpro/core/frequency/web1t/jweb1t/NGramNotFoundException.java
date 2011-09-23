/*
 * Copyright 2005 ITC-irst (http://www.itc.it/)
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

/**
 * Thrown to indicate that the ngram
 * cannot be found.
 *
 * @author 	Claudio Giuliano
 * @version 1.0
 * @since		1.0
 */
public class NGramNotFoundException extends Exception
{

	/**
	 * Constructs a <code>NGramNotFoundException</code>
	 * with no detail message.
	 */
	public NGramNotFoundException()
	{
		super();
	} // end constructor


} // end class NGramNotFoundException