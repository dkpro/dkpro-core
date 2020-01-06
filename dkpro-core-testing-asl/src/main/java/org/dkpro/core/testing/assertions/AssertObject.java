package org.dkpro.core.testing.assertions;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssertObject {
	
	public static void assertDeepEquals(
			String message, Object expObject, Object gotObject) throws IOException {
		Set<String> emptyIgnoreFields = new HashSet<String>();
		assertDeepEquals(message, expObject, gotObject, emptyIgnoreFields, null);
	}
	
	public static void assertDeepEquals(
			String message, Object expObject, Object gotObject, String[] ignoreFields) throws IOException {
		Set<String> ignoreFieldsSet = new HashSet<String>();
		for (String aFieldName: ignoreFields) ignoreFieldsSet.add(aFieldName);
		assertDeepEquals(message, expObject, gotObject, ignoreFieldsSet, null);
	}

	public static void assertDeepEquals(
			String message, Object expObject, Object gotObject,
			Set<String> ignoreFields, Integer decimalsTolerance) throws IOException {
		String expObjectJsonString = PrettyPrinter.print(expObject, ignoreFields, decimalsTolerance);
		assertEqualsJsonCompare(message, expObjectJsonString, gotObject, ignoreFields, true, decimalsTolerance);		
	}

	public static void assertDeepEquals(
			String message, Object expObject, Object gotObject,
			Integer decimalsTolerance) throws IOException {
		Set<String> ignoreFields = new HashSet<String>();
		String expObjectJsonString = PrettyPrinter.print(expObject, ignoreFields, decimalsTolerance);
		assertEqualsJsonCompare(message, expObjectJsonString, gotObject, ignoreFields, true, decimalsTolerance);		
	}
	
	public static void assertDeepNotEqual(String message, Object expObject, Object gotObject) {
		try {
			
			assertDeepEquals("", expObject, gotObject);
			
			// NOTE: If the two objects are not equal, then the above assertion should
			//  fail. Therefore, if we make it to here, it means that the two 
			//  objects are equal, and that we should raise an exception (since
			//  we are trying to assert that the two objects are NOT equal).
			
			Assert.assertTrue(message+"\nThe two objects should NOT have been equal. But they were both equal to:\n"+PrettyPrinter.print(expObject), 
					false);
			
		} catch (AssertionError | IOException e) {
			// Nothing to do. We actually WANT the above deepEquals to fail (i.e. we WANT
			// the two objects to differ ins SOME respect). 
		}
		
	}

	public static void assertEqualsJsonCompare(String expJsonString, Object gotObject) throws  IOException {
		assertEqualsJsonCompare("", expJsonString, gotObject);
	}

	public static void assertEqualsJsonCompare(
			String message, String expJsonString, Object gotObject) throws  IOException {
		HashSet<String> emptySetOfFieldsToIgnore = new HashSet<String>();
		assertEqualsJsonCompare(message, expJsonString, gotObject, emptySetOfFieldsToIgnore);
	}

	public static void assertEqualsJsonCompare(String expJsonString, Object gotObject,
			HashSet<String> ignoreFields) throws  IOException {
		assertEqualsJsonCompare("", expJsonString, gotObject, ignoreFields);
	}

	public static void assertEqualsJsonCompare( 
			String expJsonString, Object gotObject,
			String[] ignoreFields) throws  IOException {
		assertEqualsJsonCompare("", expJsonString, gotObject, ignoreFields);
	}

		public static void assertEqualsJsonCompare(String message, 
			String expJsonString, Object gotObject,
			String[] ignoreFields) throws  IOException {
		Set<String> ignoreFieldsSet = new HashSet<String>();
		for (String aField: ignoreFields) {
			ignoreFieldsSet.add(aField);
		}
		assertEqualsJsonCompare(message, expJsonString, gotObject, ignoreFieldsSet);
	}
	
	
	public static void assertEqualsJsonCompare(String message, 
			String expJsonString, Object gotObject,
			Set<String> ignoreFields) throws  IOException {
		assertEqualsJsonCompare(message, expJsonString, gotObject, ignoreFields, false, null);
	}
		
	public static void assertEqualsJsonCompare(String message, 
			String expJsonString, Object gotObject,
			Set<String> ignoreFields, boolean expJsonIsAlreadyPretty, Integer decimalsTolerance) throws  IOException {
		/*
		 * Algorithm is as follows:
		 * 
		 * - Transform the gotObject into a json string where the keys of 
		 *    all dictionaries are guaranteed to be sorted alphabetically
		 *    
		 *  - if expJsonIsAlreadyPretty is false, then "prettify" it by
		 *    - Deserializing it into an Object
		 *    - PrettyPrinting it to a json string.
		 *    
		 *  - Compare the two strings using our own string comparison 
		 *    which gives a better view of the difference
		 *    
		 * Note: For generating a json string with all dictionary keys
		 *   sorted, we use our own serialization method jsonNodeToString().
		 *   We could have used the writeValueAsString with 
		 *   ORDER_MAP_ENTRIES_BY_KEYS set to true, but this does not
		 *   seem to work for JsonNode objects that have been read from string.
		 */
		
		/*
		 * Transform the gotObject into a json string where the keys of 
		 *  all dictionaries are garanteed to be sorted alphabetically.
		 *  - First we transform the object into a JsonNode
		 *  - Then we print that JsonNode into a string with keys sorted 
		 *     alphabetically  
		*/
		String gotJsonStrKeysSorted = PrettyPrinter.print(gotObject, ignoreFields, decimalsTolerance);
		
		/*
		 *  Possibly "prettify" the expected json string
		 */
		String expJsonPrettyPrint = expJsonString;
		if (! expJsonIsAlreadyPretty) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode expJsonObj = mapper.readTree(expJsonString);
			expJsonPrettyPrint = PrettyPrinter.print(expJsonObj);
		}

		// Ignore differences in \n versus \r\n
		//  TODO: This should probably be an option
		expJsonPrettyPrint = expJsonPrettyPrint.replaceAll("\r\n", "\n");
		gotJsonStrKeysSorted = gotJsonStrKeysSorted.replaceAll("\r\n", "\n");
		
		AssertString.assertStringEquals(
				message+"\nThe objects was not as expected.\nBelow is a diff of a JSON serialization of the gotten and expected objects.\n",
				expJsonPrettyPrint, gotJsonStrKeysSorted);		
	}	

}
