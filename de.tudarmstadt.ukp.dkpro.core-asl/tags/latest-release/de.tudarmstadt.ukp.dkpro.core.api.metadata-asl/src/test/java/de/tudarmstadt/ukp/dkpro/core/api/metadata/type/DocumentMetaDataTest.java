/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.api.metadata.type;

import static org.uimafit.util.JCasUtil.selectSingle;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.junit.Test;
import org.uimafit.factory.JCasFactory;
import static org.junit.Assert.*;

public class DocumentMetaDataTest
{
	/**
	 * Check that the properties are correct if {@link DocumentMetaData} is added after text and
	 * language have already been set.
	 */
	@Test
	public void doubleInitTest() throws Exception
	{
		String documentText = "initialized";
		String documentLanguage = "en";
		
		JCas jcas = JCasFactory.createJCas();

		// Let UIMA create a DocumentAnnotation
		jcas.setDocumentText(documentText);
		jcas.setDocumentLanguage(documentLanguage);
		
		// Check it's there and correctly initialized
		DocumentAnnotation documentAnnotation = selectSingle(jcas, DocumentAnnotation.class);
		assertEquals(0, documentAnnotation.getBegin());
		assertEquals(documentText.length(), documentAnnotation.getEnd());
		assertEquals(documentLanguage, documentAnnotation.getLanguage());
		
		// Now create a DKPro DocumentMetaData which is also a DocumentAnnotation
		DocumentMetaData meta = DocumentMetaData.create(jcas);
		assertTrue(meta == jcas.getDocumentAnnotationFs());
		assertEquals(0, meta.getBegin());
		assertEquals(documentText.length(), meta.getEnd());
		assertEquals(documentLanguage, meta.getLanguage());

		// Make sure we still only have one
		documentAnnotation = selectSingle(jcas, DocumentAnnotation.class);
	}

	/**
	 * Check that offsets are correct text is set after {@link DocumentMetaData} has already been
	 * added.
	 */
	@Test
	public void offsetTest() throws Exception
	{
		String documentText = "initialized";
		
		JCas jcas = JCasFactory.createJCas();

		// Create a DKPro DocumentMetaData which is also a DocumentAnnotation
		DocumentMetaData meta = DocumentMetaData.create(jcas);
		assertEquals(0, meta.getBegin());
		assertEquals(0, meta.getEnd());
		assertTrue(meta == jcas.getDocumentAnnotationFs());

		// Make sure we still have a DocumentMetaData as DocumentAnnotation
		jcas.setDocumentText(documentText);
		
		DocumentMetaData.get(jcas);

		// Make sure we still only have one
		assertEquals(0, meta.getBegin());
		assertEquals(documentText.length(), meta.getEnd());
	}
	
	/**
	 * Check that {@link DocumentMetaData#get} fails if no {@link DocumentMetaData} in the CAS.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void noAnnotationTest() throws Exception
	{
		JCas jcas = JCasFactory.createJCas();
		DocumentMetaData.get(jcas);
	}
	
	/**
	 * Check that {@link DocumentMetaData#get} fails if there is more than one
	 * {@link DocumentMetaData} in the CAS.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void tooManyAnnotationsTest1() throws Exception
	{
		JCas jcas = JCasFactory.createJCas();
		DocumentMetaData meta = DocumentMetaData.create(jcas);
		meta.addToIndexes();
		DocumentMetaData.get(jcas);
	}
	
	/**
	 * Check that {@link DocumentMetaData#get} fails if there is more than one
	 * {@link DocumentAnnotation} in the CAS.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void tooManyAnnotationsTest2() throws Exception
	{
		JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText("initialized");
		jcas.getDocumentAnnotationFs().addToIndexes();
		DocumentMetaData.get(jcas);
	}
}
