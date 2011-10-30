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

import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;
import static org.uimafit.util.JCasUtil.select;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.WordToSentenceProcessor;
import edu.stanford.nlp.trees.international.arabic.ArabicTokenizer;

/**
 * @author Richard Eckart de Castilho
 * @author Oliver Ferschke
 */
public
class StanfordSegmenter
extends SegmenterBase
{
    public static final String PARAM_FALLBACK = "fallbackLanguage";
    private final String fallbackLanguage = "en";

    private static final Map<String, InternalTokenizerFactory> tokenizerFactories;
//    private static final Map<String, TreebankLanguagePack> languagePacks;

    static {
    	tokenizerFactories = new HashMap<String, InternalTokenizerFactory>();
    	tokenizerFactories.put("en", new InternalPTBTokenizerFactory());
    	tokenizerFactories.put("ar", new InternalArabicTokenizerFactory());
    	// The Negra tokenizer is not really a full tokenizer.
//    	tokenizerFactories.put("de", new InternalNegraPennTokenizerFactory());
    	// Not sure if those really work - don't know how to test
//    	tokenizerFactories.put("zh", new InternalCHTBTokenizerFactory());

//    	languagePacks = new HashMap<String, TreebankLanguagePack>();
//    	languagePacks.put("en", new PennTreebankLanguagePack());
//    	languagePacks.put("zh", new ChineseTreebankLanguagePack());
//    	languagePacks.put("en", new ArabicTreebankLanguagePack());
//    	languagePacks.put("de", new NegraPennLanguagePack());
    }

    @Override
	protected void process(JCas aJCas, String aText, int aZoneBegin)
		throws AnalysisEngineProcessException
    {
		final String text = aText;
    	final Tokenizer<?> tokenizer = getTokenizer(aJCas.getDocumentLanguage(), aText);
		final int offsetInDocument = aZoneBegin;
		int offsetInSentence = 0;

		List<?> tokens = tokenizer.tokenize();
    	outer: for (int i = 0; i < tokens.size(); i++) {
    		final Object token = tokens.get(i);
    		//System.out.println("Token class: "+token.getClass());
    		String t = null;
    		if (token instanceof String) {
    			t = (String) token;
    		}
    		if (token instanceof CoreLabel) {
    			CoreLabel l = (CoreLabel) token;
    			t = l.word();
    			int begin = l.get(CharacterOffsetBeginAnnotation.class);
    			int end = l.get(CharacterOffsetEndAnnotation.class);

				createToken(aJCas, offsetInDocument + begin, offsetInDocument
						+ end, i);
    			offsetInSentence = end;
    			continue;
    		}
    		if (token instanceof Word) {
    			Word w = (Word) token;
    			t = w.word();
    		}

    		if (t == null) {
    			throw new AnalysisEngineProcessException(new IllegalStateException(
    					"Unknown token type: "+token.getClass()));
    		}

    		// Skip whitespace
			while (isWhitespace(text.charAt(offsetInSentence))) {
				offsetInSentence++;
				if (offsetInSentence >= text.length()) {
    				break outer;
    			}
    		}

    		// Match
			if (text.startsWith(t, offsetInSentence)) {
				createToken(aJCas, offsetInDocument + offsetInSentence,
						offsetInDocument + offsetInSentence + t.length(), i);
				offsetInSentence = offsetInSentence + t.length();
    		}
    		else {
				System.out.println(aText);
    			throw new AnalysisEngineProcessException(
    					new IllegalStateException("Text mismatch. Tokenizer: ["
    							+ t +"] CAS: [" + text.substring(
    										offsetInSentence, min(offsetInSentence+t.length(),text.length()))));
			}
		}

		// Prepare the tokens for processing by WordToSentenceProcessor
		List<CoreLabel> tokensInDocument = new ArrayList<CoreLabel>();
		for (Token token : select(aJCas, Token.class)) {
			CoreLabel l = new CoreLabel();
			l.set(CharacterOffsetBeginAnnotation.class, token.getBegin());
			l.set(CharacterOffsetEndAnnotation.class, token.getEnd());
			l.setWord(token.getCoveredText());
			tokensInDocument.add(l);
		}

		PTBEscapingProcessor escaper = new PTBEscapingProcessor();
		escaper.apply(tokensInDocument);

		// Apply the WordToSentenceProcessor to find the sentence boundaries
		WordToSentenceProcessor<CoreLabel> proc =
				new WordToSentenceProcessor<CoreLabel>();
		List<List<CoreLabel>> sentencesInDocument = proc.process(tokensInDocument);
		for (List<CoreLabel> sentence : sentencesInDocument) {
			int begin = sentence.get(0).get(CharacterOffsetBeginAnnotation.class);
			int end = sentence.get(sentence.size()-1).get(CharacterOffsetEndAnnotation.class);

			// Bugfix JC: somehow, the above code saves sentences multiple times for subsequent zones (strictZoning)
			// given a document with n zones, A, B, C, ... e.g. these are headlines:
			// A
			// sent1
			// sent2
			// B
			// sent3
			// sent4
			// C
			// ...
			// the list of sentences should contain: sent1, sent2, sent 3, ...
			/// but it contains: sent1, sent2, ->sent1<- (again), sent2, sent3, sent4, sent1, sent2, ...

			// easy hack solution:
			if (begin >= aZoneBegin)
			{
				//System.out.println("outside create sentence: " + begin + "\t" + end);
				createSentence(aJCas, begin, end);
			}

			// instead of just
			// createSentence(aJCas, begin, end);
			// which results in multiple sentence annotations per original sentence from the document

			// if someone has time to fix the above code, this would probably improve time & space efficiency
		}
//		for(Sentence currSentence : JCasUtil.iterate(aJCas, Sentence.class)){
//			if(isCreateIndexedTokens()){
//				int tokenIndex = 1;
//				for(TokenWithIndex t : JCasUtil.selectCovered(aJCas, TokenWithIndex.class, currSentence)){
//					t.setTokenIndex(tokenIndex);
//					tokenIndex++;
//				}
//			}
//		}
    }

	private
    Tokenizer getTokenizer(
    		final String aLanguage,
    		final String aText)
    {
//    	TreebankLanguagePack tlp = languagePacks.get(aLanguage);
//    	if (tlp == null) {
//    		tlp = languagePacks.get(fallbackLanguage);
//    	}

        InternalTokenizerFactory tk = tokenizerFactories.get(aLanguage);
        if (tk == null) {
        	tk = tokenizerFactories.get(fallbackLanguage);
        }
    	return tk.create(aText);
    }

    private static
    interface InternalTokenizerFactory
    {
    	Tokenizer<?> create(String s);
    }

    private static
    class InternalPTBTokenizerFactory
    implements InternalTokenizerFactory
    {
    	@Override
    	public
    	Tokenizer<?> create(
    			final String s)
    	{

//    		TokenizerFactory<Word> f = PTBTokenizer.factory(false, true, false);


//    		TokenizerFactory<CoreLabel> f = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible,ptb3Escaping=false");
    		return new PTBTokenizer<CoreLabel>(new StringReader(s),new CoreLabelTokenFactory(),"invertible");

    	}
    }

	// The InternalNegraPennTokenizer is not meant for German text. It
	// is for parsing a particular corpus format.
//    private static
//    class InternalNegraPennTokenizerFactory
//    implements InternalTokenizerFactory
//    {
//    	@Override
//    	public
//    	Tokenizer<?> create(
//    			final String s)
//    	{
//    		return new NegraPennTokenizer(new StringReader(s));
//    	}
//    }

    private static
    class InternalArabicTokenizerFactory
    implements InternalTokenizerFactory
    {
    	@Override
    	public
    	Tokenizer<?> create(
    			final String s)
    	{
    		return new ArabicTokenizer(new StringReader(s), false);
    	}
    }

    // While the stanford parser should come with a proper tokenizer for
    // Chinese (because it can parse chinese text), this does not seem to be
    // the right one or I am using it wrong. The associated test cases do not
    // work.
//    private static
//    class InternalCHTBTokenizerFactory
//    implements InternalTokenizerFactory
//    {
//    	@Override
//    	public
//    	Tokenizer<?> create(
//    			final String s)
//    	{
//    		return new CHTBTokenizer(new StringReader(s));
//    	}
//    }
}
