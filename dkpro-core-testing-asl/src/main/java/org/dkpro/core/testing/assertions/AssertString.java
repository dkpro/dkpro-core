/*
 * Copyright 2020
 * National Research Council of Canada
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
 */
package org.dkpro.core.testing.assertions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

public class AssertString {
	
	public static void assertStringEquals(String message, String expString, String gotString) {
		message = message +
				"The two strings differred. Location of the first difference is highlighted below with tag <FIRST_DIFF>.\n";
		
		// Ignore differences in \n versus \r\n
		//  TODO: This should probably be an option
		if (expString != null) {
			expString = expString.replaceAll("\r\n", "\n");
		}
		if (gotString != null) {
			gotString = gotString.replaceAll("\r\n", "\n");
		}
		
		
		int firstDiffOffset = StringUtils.indexOfDifference(expString, gotString);
		
		if (expString == null || gotString == null) {
			Assert.assertEquals(message, expString, gotString);
		}

		if (firstDiffOffset >= 0) {
			String commonStart = expString.substring(0, firstDiffOffset);
			String expectedRest = expString.substring(firstDiffOffset);
			String gotRest = gotString.substring(firstDiffOffset);

			message = 
					message + 
					"== Expected:\n "+
					commonStart +
					"<<FIRST_DIFF>>" +
					expectedRest + "\n";
			message = 
					message + 
					"== Got         :\n "+
					commonStart +
					"<<FIRST_DIFF>>" +
					gotRest + "\n";
			
			Assert.fail(message);
		}
	}

	public static void assertStringEquals(String expString, String gotString) {
		assertStringEquals("", expString, gotString);
	}

	public static void assertStringContains(String gotString, String expSubstring) {
		assertStringContains(null, gotString, expSubstring, null, null);
	}

	public static void assertStringContains(String message,
			String gotString, String expSubstring) {
		boolean caseSensitive = true;
		assertStringContains(message, gotString, expSubstring, null, null);
	}

	public static void assertStringContains(String message,
			String gotString, String pattern, Boolean caseSensitive) {
		
		assertStringContains(message, gotString, pattern, caseSensitive, null);
	}
	
	public static void assertStringContains(String message, String gotString, 
			String pattern, Boolean caseSensitive, Boolean isRegexp) {
		
		if (caseSensitive == null) {
			caseSensitive = true;
		}
		
		if (isRegexp == null) {
			isRegexp = false;
		}
		
		if (!caseSensitive && !isRegexp) {
			gotString = gotString.toLowerCase();
			pattern = pattern.toLowerCase();
		}
		
		if (message == null) {
			message = "";
		} else {
			message = message + "\n";
		}
		
		String type = "substring";
		if (isRegexp) {type = "regexp";}
		
		message = message + 
				   "String did not contain an expected "+type+".\n"
						  + "== Expected "+type+": \n"+pattern+"\n\n"
						  + "== Got string : \n"+gotString+"\n\n";

		if (isRegexp) {
			Pattern patt = Pattern.compile(pattern);
			Matcher matcher = patt.matcher(gotString);
			Assert.assertTrue(message+"\nDid not find any occurence of regepx "+pattern, 
					matcher.find());
		} else {
			Assert.assertTrue(message+"\nDid not find any occurence of regepx "+pattern,
					gotString.contains(pattern));			
		}
	}	
	
	
	public static void assertStringDoesNotContain(String gotString, String unexpSubstring) {
		boolean caseSensitive = true;
		assertStringDoesNotContain("", gotString, unexpSubstring, caseSensitive);
	}
	
	public static void assertStringDoesNotContain(String message,
			String gotString, String unexpSubstring) {
		boolean caseSensitive = true;
		assertStringDoesNotContain(message, gotString, unexpSubstring, caseSensitive);
	}

	public static void assertStringDoesNotContain(String message, String gotString, 
			String unexpSubstring, Boolean caseSensitive) {
		assertStringDoesNotContain(message, gotString, unexpSubstring, caseSensitive, null);
	}

	
	public static void assertStringDoesNotContain(String message, String gotString, 
			String unexpSubstring, Boolean caseSensitive, Boolean isRegexp) {
		
		if (caseSensitive == null) {
			caseSensitive = false;
		}
		
		if (isRegexp == null) {
			isRegexp = false;
		}
		
		if (!caseSensitive && !isRegexp) {
			gotString = gotString.toLowerCase();
			unexpSubstring = unexpSubstring.toLowerCase();
		}
		
		if (message == null) {
			message = "";
		} else {
			message = message + "\n";
		}
		
		String type = "substring";
		if (isRegexp) {type = "regexp";}
		
		message = message + 
				   "String contained an UN-expected "+type+".\n"
						  + "== Un-expected "+type+": \n"+unexpSubstring+"\n\n"
						  + "== Got string : \n"+gotString+"\n\n";

		if (isRegexp) {
			Pattern patt = Pattern.compile(unexpSubstring);
			Matcher matcher = patt.matcher(gotString);
			Assert.assertFalse(message+"\nFound at least one occurence of regepx "+unexpSubstring, 
					matcher.find());
		} else {
			Assert.assertFalse(message, gotString.contains(unexpSubstring));			
		}
	}	

}
