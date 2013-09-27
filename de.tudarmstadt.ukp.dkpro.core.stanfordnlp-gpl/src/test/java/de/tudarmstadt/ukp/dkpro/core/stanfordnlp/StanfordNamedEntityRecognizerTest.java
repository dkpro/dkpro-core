/**
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Person;

/**
 * @author Oliver Ferschke
 */
public class StanfordNamedEntityRecognizerTest
{
	@Test
	public void testEnglish()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

		Iterator<NamedEntity> iter = runNER("SAP where John works is in Germany.", "en");

		assertNE(iter.next(), Organization.class, 0, 3);
		assertNE(iter.next(), Person.class, 10, 14);
		assertNE(iter.next(), Location.class, 27, 34);
		assertFalse(iter.hasNext());
	}

	@Test
	public void testGerman()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

		/*
		 * Note: The FaruquiPado-classifiers need at least 2 GiG of Heap Space
		 */
		Iterator<NamedEntity> iter = runNER(
				"Markus arbeitet seit 10 Jahren bei SAP in Deutschland.", "de");

		assertNE(iter.next(), Person.class, 0, 6);
		assertNE(iter.next(), Organization.class, 35, 38);
		assertNE(iter.next(), Location.class, 42, 53);
		assertFalse(iter.hasNext());
	}

	/**
	 * Helper method to check a single NE-annotation.
	 *
	 * @param actual
	 *            the actual NamedEntity from the CAS
	 * @param expected
	 *            the expected NE Type
	 * @param begin
	 *            the begin of the NE span
	 * @param end
	 *            the end of the ne span
	 * @throws Exception
	 *             if assertion was false, i.e. wrong NE-Type or wrong span
	 */
	private void assertNE(NamedEntity actual,
			Class<? extends NamedEntity> expected, int begin, int end)
		throws Exception
	{
		assertTrue("must be a " + expected.getName() + " but was " + actual.getClass().getName(), actual.getClass()
				.getCanonicalName().equals(expected.getCanonicalName()));
		assertEquals("begin index must be " + begin, begin, actual.getBegin());
		assertEquals("end index must be " + end, end, actual.getEnd());
	}

	/**
	 * Performs the named entity recognition with the specified model.
	 *
	 * @param testDocument
	 *            the document to be ne-tagged
	 * @return an iterator over all created NamedEntity-annotations
	 */
	private Iterator<NamedEntity> runNER(String testDocument, String language)
		throws Exception
	{
		AnalysisEngineDescription desc = createEngineDescription(
				StanfordNamedEntityRecognizer.class,
				StanfordNamedEntityRecognizer.PARAM_PRINT_TAGSET, true);

		AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(desc);
		JCas testCas = ae.newJCas();
		testCas.setDocumentLanguage(language);
		testCas.setDocumentText(testDocument);
		ae.process(testCas);

		// Return iterator for the named entities
		return JCasUtil.iterator(testCas, NamedEntity.class);
	}

	@Test(expected = AnalysisEngineProcessException.class)
	public void testMissingModel() throws Exception
	{
		AnalysisEngineDescription desc = createEngineDescription(
				StanfordNamedEntityRecognizer.class,
				StanfordNamedEntityRecognizer.PARAM_PRINT_TAGSET, true);

		AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(desc);
		JCas testCas = ae.newJCas();
		testCas.setDocumentLanguage("xx");
		testCas.setDocumentText("Xec xena Xeo.");
		ae.process(testCas);
	}
}
