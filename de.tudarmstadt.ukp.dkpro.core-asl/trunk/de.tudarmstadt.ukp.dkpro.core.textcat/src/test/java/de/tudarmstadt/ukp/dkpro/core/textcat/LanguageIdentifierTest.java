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
package de.tudarmstadt.ukp.dkpro.core.textcat;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public
class LanguageIdentifierTest
{
	@Test
	public
	void testEnglish()
	throws Exception
	{
		AnalysisEngine ae = createPrimitive(LanguageIdentifier.class, createTypeSystemDescription());
		JCas aJCas = ae.newJCas();
		aJCas.setDocumentText("This is an english file.");
		ae.process(aJCas);
		assertEquals("en", aJCas.getDocumentLanguage());
	}

	@Test
	public
	void testGerman()
	throws Exception
	{
		AnalysisEngine ae = createPrimitive(LanguageIdentifier.class, createTypeSystemDescription());
		JCas aJCas = ae.newJCas();
		aJCas.setDocumentText("Das ist ein deutsches Dokument.");
		ae.process(aJCas);
		assertEquals("de", aJCas.getDocumentLanguage());
	}
}
