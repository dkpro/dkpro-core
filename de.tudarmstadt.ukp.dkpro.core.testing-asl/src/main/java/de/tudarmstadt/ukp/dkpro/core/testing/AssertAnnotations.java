/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.testing;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.uimafit.util.JCasUtil.toText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class AssertAnnotations
{
	public static void assertToken(String[] aExpected, Collection<Token> aActual)
	{
		if (aExpected == null) {
			return;
		}
		
		List<String> expected = asList(aExpected);
		List<String> actual = toText(aActual);
		
		System.out.printf("%-20s - Expected: %s%n", "Tokens", asCopyableString(expected));
		System.out.printf("%-20s - Actual  : %s%n", "Tokens", asCopyableString(actual));

		assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
	}
	
	public static void assertSentence(String[] aExpected, Collection<Sentence> aActual)
	{
		if (aExpected == null) {
			return;
		}
		
		List<String> expected = asList(aExpected);
		List<String> actual = toText(aActual);
		
		System.out.printf("%-20s - Expected: %s%n", "Sentences", asCopyableString(expected));
		System.out.printf("%-20s - Actual  : %s%n", "Sentences", asCopyableString(actual));

		assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
	}
	
	public static void assertPOS(String[] aExpectedMapped, String[] aExpectedOriginal,
			Collection<POS> actual)
	{
		List<String> expectedOriginal = aExpectedOriginal != null ? asList(aExpectedOriginal) : null;
		List<String> expectedMapped = aExpectedMapped != null ? asList(aExpectedMapped) : null;
		List<String> actualOriginal = new ArrayList<String>();
		List<String> actualMapped = new ArrayList<String>();

		for (POS posAnnotation : actual) {
			actualOriginal.add(posAnnotation.getPosValue());
			actualMapped.add(posAnnotation.getType().getShortName());
		}

		
		if (aExpectedOriginal != null) {
			System.out.printf("%-20s - Expected: %s%n", "POS (original)", asCopyableString(expectedOriginal));
			System.out.printf("%-20s - Actual  : %s%n", "POS (original)", asCopyableString(actualOriginal));
		}
		
		if (aExpectedMapped != null) {
			System.out.printf("%-20s - Expected: %s%n", "POS (mapped)", asCopyableString(expectedMapped));
			System.out.printf("%-20s - Actual  : %s%n", "POS (mapped)", asCopyableString(actualMapped));
		}

		if (aExpectedOriginal != null) {
			assertEquals(asCopyableString(expectedOriginal, true), asCopyableString(actualOriginal, true));
		}
		if (aExpectedMapped != null) {
			assertEquals(asCopyableString(expectedMapped, true), asCopyableString(actualMapped, true));
		}
	}

	public static void assertLemma(String[] aExpected, Collection<Lemma> aActual)
	{
		if (aExpected == null) {
			return;
		}

		List<String> expected = asList(aExpected);
		List<String> actual = new ArrayList<String>();

		for (Lemma a : aActual) {
			actual.add(a.getValue());
		}

		System.out.printf("%-20s - Expected: %s%n", "Lemmas", asCopyableString(expected));
		System.out.printf("%-20s - Actual  : %s%n", "Lemmas", asCopyableString(actual));

		assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
	}

	public static void assertConstituents(String[] aExpectedMapped, String[] aExpectedOriginal,
			Collection<Constituent> aActual)
	{
		String[] actualTags = new String[aActual.size()];
		String[] actualClasses = new String[aActual.size()];

		int i = 0;
		for (Constituent a : aActual) {
			actualTags[i] = String.format("%s %d,%d", a.getConstituentType(), a.getBegin(), a.getEnd());
			actualClasses[i] = String.format("%s %d,%d", a.getType().getShortName(), a.getBegin(), a.getEnd());
			i++;
		}

		List<String> sortedExpectedOriginal = deduplicateAndSort(asList(aExpectedOriginal));
		List<String> sortedExpectedMapped = deduplicateAndSort(asList(aExpectedMapped));
		List<String> sortedActualOriginal = deduplicateAndSort(asList(actualTags));
		List<String> sortedActualMapped = deduplicateAndSort(asList(actualClasses));
		
		if (aExpectedOriginal != null) {
			System.out.printf("%-20s - Expected: %s%n", "Constituents (original)", asCopyableString(sortedExpectedOriginal));
			System.out.printf("%-20s - Actual  : %s%n", "Constituents (original)", asCopyableString(sortedActualOriginal));
		}
		
		if (aExpectedMapped != null) {
			System.out.printf("%-20s - Expected: %s%n", "Constituents (mapped)", asCopyableString(sortedExpectedMapped));
			System.out.printf("%-20s - Actual  : %s%n", "Constituents (mapped)", asCopyableString(sortedActualMapped));
		}

		if (aExpectedOriginal != null) {
			assertEquals(asCopyableString(sortedExpectedOriginal, true), asCopyableString(sortedActualOriginal, true));
		}
		if (aExpectedMapped != null) {
			assertEquals(asCopyableString(sortedExpectedMapped, true), asCopyableString(sortedActualMapped, true));
		}
	}
	
	public static <T extends Comparable<T>> List<T> deduplicateAndSort(Collection<T> aCollection)
	{
		if (aCollection == null) {
			return null;
		}
		else {
			List<T> result = new ArrayList<T>(new HashSet<T>(aCollection));
			Collections.sort(result);
			return result;
		}
	}

	public static void assertDependencies(String[] aExpected, Collection<Dependency> aActual)
	{
		List<String> expected = new ArrayList<String>(asList(aExpected));
		List<String> actual = new ArrayList<String>();

		for (Dependency a : aActual) {
			actual.add(String.format("%s %d,%d,%d,%d", a.getDependencyType().toUpperCase(), a
					.getGovernor().getBegin(), a.getGovernor().getEnd(), a.getDependent()
					.getBegin(), a.getDependent().getEnd()));
		}

		Collections.sort(actual);
		Collections.sort(expected);
		
		System.out.printf("%-20s - Expected: %s%n", "Dependencies", asCopyableString(expected));
		System.out.printf("%-20s - Actual  : %s%n", "Dependencies", asCopyableString(actual));

		assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
	}
	
	public static String asCopyableString(Collection<String> aCollection, boolean aLinebreak)
	{
		if (aCollection.isEmpty()) {
			return "{}";
		}
		else {
			if (aLinebreak) {
				return "{\n\"" + StringUtils.join(aCollection, "\",\n\"") + "\"\n}";
			}
			else {
				return "{ \"" + StringUtils.join(aCollection, "\", \"") + "\" }";
			}
		}
	}

	private static String asCopyableString(Collection<String> aCollection)
	{
		return asCopyableString(aCollection, false);
	}

}
