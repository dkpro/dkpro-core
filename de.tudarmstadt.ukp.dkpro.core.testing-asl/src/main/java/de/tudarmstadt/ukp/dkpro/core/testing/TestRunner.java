package de.tudarmstadt.ukp.dkpro.core.testing;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TestRunner
{
	/**
	 * Run an analysis engine using a document. The document is automatically split into tokens
	 * and sentenced based on spaces and dots. Make sure the dots are surrounded by spaces.
	 * 
	 * @param aEngine
	 * @param aLanguage
	 * @param aDocument
	 * @throws UIMAException 
	 * @see {@link TokenBuilder}
	 */
	public static JCas runTest(AnalysisEngineDescription aEngine, String aLanguage, String aDocument) throws UIMAException
	{
		return runTest(createPrimitive(aEngine), aLanguage, aDocument);
	}
	
	/**
	 * Run an analysis engine using a document. The document is automatically split into tokens
	 * and sentenced based on spaces and dots. Make sure the dots are surrounded by spaces.
	 * 
	 * @param aEngine
	 * @param aLanguage
	 * @param aDocument
	 * @throws UIMAException 
	 * @see {@link TokenBuilder}
	 */
	public static JCas runTest(AnalysisEngine aEngine, String aLanguage, String aDocument) throws UIMAException
	{
		JCas aJCas = aEngine.newJCas();
		aJCas.setDocumentLanguage(aLanguage);

		TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class,
				Sentence.class);
		tb.buildTokens(aJCas, aDocument);

		aEngine.process(aJCas);

		return aJCas;
	}
}
