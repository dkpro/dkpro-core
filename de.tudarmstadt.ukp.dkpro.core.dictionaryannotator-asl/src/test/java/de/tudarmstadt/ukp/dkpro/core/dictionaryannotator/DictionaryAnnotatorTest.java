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
package de.tudarmstadt.ukp.dkpro.core.dictionaryannotator;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.selectSingle;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.factory.JCasFactory;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import static org.junit.Assert.*;

public class DictionaryAnnotatorTest
{
	@Test
	public void test() throws Exception
	{
		AnalysisEngine ae = createPrimitive(DictionaryAnnotator.class,
				DictionaryAnnotator.PARAM_ANNOTATION_TYPE, NamedEntity.class,
				DictionaryAnnotator.PARAM_MODEL_LOCATION, "src/test/resources/persons.txt");
		
		JCas jcas = JCasFactory.createJCas();
		TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);
		tb.buildTokens(jcas, "I am John Silver 's ghost .");
		
		ae.process(jcas);

		NamedEntity ne = selectSingle(jcas, NamedEntity.class);
		assertEquals("John Silver", ne.getCoveredText());
	}
	
	@Test
	public void testWithValue() throws Exception
	{
		AnalysisEngine ae = createPrimitive(DictionaryAnnotator.class,
				DictionaryAnnotator.PARAM_ANNOTATION_TYPE, NamedEntity.class,
				DictionaryAnnotator.PARAM_VALUE, "PERSON",
				DictionaryAnnotator.PARAM_MODEL_LOCATION, "src/test/resources/persons.txt");
		
		JCas jcas = JCasFactory.createJCas();
		TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);
		tb.buildTokens(jcas, "I am John Silver 's ghost .");
		
		ae.process(jcas);
		
		NamedEntity ne = selectSingle(jcas, NamedEntity.class);
		assertEquals("PERSON", ne.getValue());
		assertEquals("John Silver", ne.getCoveredText());
	}

	@Test
	public void testWithWrongType() throws Exception
	{
		try {
			AnalysisEngine ae = createPrimitive(DictionaryAnnotator.class,
					DictionaryAnnotator.PARAM_ANNOTATION_TYPE, "lala",
					DictionaryAnnotator.PARAM_VALUE, "PERSON",
					DictionaryAnnotator.PARAM_MODEL_LOCATION, "src/test/resources/persons.txt");
			
			JCas jcas = JCasFactory.createJCas();
			TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);
			tb.buildTokens(jcas, "I am John Silver 's ghost .");
			
			ae.process(jcas);
			fail("An exception for an undeclared type should have been thrown");
		}
		catch (AnalysisEngineProcessException e) {
			assertTrue(ExceptionUtils.getRootCauseMessage(e).contains("Undeclared type"));
		}
	}

	@Test
	public void testWithWrongValueFeature() throws Exception
	{
		try {
			AnalysisEngine ae = createPrimitive(DictionaryAnnotator.class,
					DictionaryAnnotator.PARAM_ANNOTATION_TYPE, NamedEntity.class,
					DictionaryAnnotator.PARAM_VALUE_FEATURE, "lala",
					DictionaryAnnotator.PARAM_VALUE, "PERSON",
					DictionaryAnnotator.PARAM_MODEL_LOCATION, "src/test/resources/persons.txt");
			
			JCas jcas = JCasFactory.createJCas();
			TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);
			tb.buildTokens(jcas, "I am John Silver 's ghost .");
			
			ae.process(jcas);
			fail("An exception for an undeclared type should have been thrown");
		}
		catch (AnalysisEngineProcessException e) {
			assertTrue(ExceptionUtils.getRootCauseMessage(e).contains("Undeclared feature"));
		}
	}
}
