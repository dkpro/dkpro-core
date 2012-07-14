package de.tudarmstadt.ukp.dkpro.core.dictionaryannotator;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.selectSingle;
import org.apache.uima.analysis_engine.AnalysisEngine;
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
				DictionaryAnnotator.PARAM_PHRASE_FILE, "src/test/resources/persons.txt");
		
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
				DictionaryAnnotator.PARAM_PHRASE_FILE, "src/test/resources/persons.txt");
		
		JCas jcas = JCasFactory.createJCas();
		TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);
		tb.buildTokens(jcas, "I am John Silver 's ghost .");
		
		ae.process(jcas);
		
		NamedEntity ne = selectSingle(jcas, NamedEntity.class);
		assertEquals("PERSON", ne.getValue());
		assertEquals("John Silver", ne.getCoveredText());
	}
}
