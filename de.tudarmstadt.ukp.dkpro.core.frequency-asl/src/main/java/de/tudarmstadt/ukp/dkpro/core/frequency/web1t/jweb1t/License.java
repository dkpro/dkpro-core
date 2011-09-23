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

/**
 * TO DO
 *
 * @author 	Claudio Giuliano
 * @version %I%, %G%
 * @since		1.0
 */
public class License
{

	/**
	 * Returns a license message.
	 *
	 * return a license message.
	 */
	public static String get()
	{
		StringBuffer sb = new StringBuffer();
		
		// jWeb1T
		sb.append("\njWeb1T: Web 1T 5-gram Searcher V1.10\t 16.07.07\n");
		sb.append("developed by Claudio Giuliano (giuliano@itc.it)\n\n");
		
		// License
		sb.append("Copyright 2007 FBK-irst (http://www.itc.it/)\n");
		sb.append("\n");
		sb.append("Licensed under the Apache License, Version 2.0 (the \"License\");\n");
		sb.append("you may not use this file except in compliance with the License.\n");
		sb.append("You may obtain a copy of the License at\n");
		sb.append("\n");
		sb.append("    http://www.apache.org/licenses/LICENSE-2.0\n");
		sb.append("\n");
		sb.append("Unless required by applicable law or agreed to in writing, software\n");
		sb.append("distributed under the License is distributed on an \"AS IS\" BASIS,\n");
		sb.append("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n");
		sb.append("See the License for the specific language governing permissions and\n");
		sb.append("limitations under the License.\n\n");
		
		return sb.toString();
	} // end get

} // end class License
