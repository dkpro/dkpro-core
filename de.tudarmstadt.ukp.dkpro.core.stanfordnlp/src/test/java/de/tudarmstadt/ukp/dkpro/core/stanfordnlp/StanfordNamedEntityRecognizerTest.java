/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.util.Iterator;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Test;
import org.uimafit.util.JCasUtil;

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

		Iterator<NamedEntity> iter = runNER(
				"SAP where John works is in Germany.",
				"/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/ner-en-all.3class.distsim.crf.ser.gz");

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
				"Markus arbeitet seit 10 Jahren bei SAP in Deutschland.",
				"/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/ner-de-dewac_175m_600.crf.ser.gz");

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
	 * @param classifier
	 *            the NER-classifier that should be used
	 * @return an iterator over all created NamedEntity-annotations
	 * @throws Exception
	 */
	private Iterator<NamedEntity> runNER(String testDocument, String classifier)
		throws Exception
	{
		checkModel(classifier);

		AnalysisEngineDescription desc = createPrimitiveDescription(
				StanfordNamedEntityRecognizer.class,
				StanfordNamedEntityRecognizer.PARAM_MODEL, "classpath:"+classifier);

		AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(desc);
		JCas testCas = ae.newJCas();
		testCas.setDocumentText(testDocument);
		ae.process(testCas);

		// Return iterator for the named entities
		return JCasUtil.iterator(testCas, NamedEntity.class);
	}

    private
    void checkModel(String aModel)
    {
		Assume.assumeTrue(getClass().getResource(aModel) != null);
    }
}
