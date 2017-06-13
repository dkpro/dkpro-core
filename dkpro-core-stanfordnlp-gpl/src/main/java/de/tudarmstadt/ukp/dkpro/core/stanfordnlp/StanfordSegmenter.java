/**
 * Copyright 2007-2017
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.Messages;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.international.arabic.process.ArabicTokenizer;
import edu.stanford.nlp.international.french.process.FrenchTokenizer;
import edu.stanford.nlp.international.spanish.process.SpanishTokenizer;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.WordToSentenceProcessor;
import edu.stanford.nlp.process.WordToSentenceProcessor.NewlineIsSentenceBreak;

/**
 * Stanford sentence splitter and tokenizer.
 */
@LanguageCapability({"en", "es", "fr"})
public class StanfordSegmenter
    extends SegmenterBase
{
    private static final Map<String, InternalTokenizerFactory> tokenizerFactories;
//    private static final Map<String, TreebankLanguagePack> languagePacks;

    static {
    	tokenizerFactories = new HashMap<String, InternalTokenizerFactory>();
//        tokenizerFactories.put("ar", new InternalArabicTokenizerFactory());
    	tokenizerFactories.put("en", new InternalPTBTokenizerFactory());
        tokenizerFactories.put("es", new InternalSpanishTokenizerFactory());
        tokenizerFactories.put("fr", new InternalFrenchTokenizerFactory());
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

    /**
     * If this component is not configured for a specific language and if the language stored in
     * the document metadata is not supported, use the given language as a fallback.
     */
    public static final String PARAM_LANGUAGE_FALLBACK = "languageFallback";
    @ConfigurationParameter(name = PARAM_LANGUAGE_FALLBACK, mandatory = false)
    private String languageFallback;

    /**
     * The set of boundary tokens. If null, use default.
     * 
     * @see WordToSentenceProcessor#WordToSentenceProcessor
     */
    public static final String PARAM_BOUNDARY_TOKEN_REGEX = "boundaryTokenRegex";
    @ConfigurationParameter(name = PARAM_BOUNDARY_TOKEN_REGEX, mandatory = false, defaultValue = WordToSentenceProcessor.DEFAULT_BOUNDARY_REGEX)
    private String boundaryTokenRegex;

    /**
     * This is a Set of String that are matched with .equals() which are allowed to be tacked onto
     * the end of a sentence after a sentence boundary token, for example ")".
     * 
     * @see WordToSentenceProcessor#DEFAULT_BOUNDARY_FOLLOWERS_REGEX
     */
    public static final String PARAM_BOUNDARY_FOLLOWERS_REGEX = "boundaryFollowersRegex";
    @ConfigurationParameter(name = PARAM_BOUNDARY_FOLLOWERS_REGEX, mandatory = false, defaultValue =
            WordToSentenceProcessor.DEFAULT_BOUNDARY_FOLLOWERS_REGEX)
    private String boundaryFollowersRegex;

    /**
     * These are elements like "p" or "sent", which will be wrapped into regex for approximate XML
     * matching. They will be deleted in the output, and will always trigger a sentence boundary.
     */
    public static final String PARAM_XML_BREAK_ELEMENTS_TO_DISCARD = "xmlBreakElementsToDiscard";
    @ConfigurationParameter(name = PARAM_XML_BREAK_ELEMENTS_TO_DISCARD, mandatory = false)
    private Set<String> xmlBreakElementsToDiscard;

    /**
     * The set of regex for sentence boundary tokens that should be discarded.
     * 
     * @see WordToSentenceProcessor#DEFAULT_SENTENCE_BOUNDARIES_TO_DISCARD
     */
    public static final String PARAM_BOUNDARIES_TO_DISCARD = "boundaryToDiscard";
    @ConfigurationParameter(name = PARAM_BOUNDARIES_TO_DISCARD, mandatory = false, defaultValue = {
            "\n", "*NL*" })
    private Set<String> boundariesToDiscard;

    /**
     * A regular expression for element names containing a sentence region. Only tokens in such
     * elements will be included in sentences. The start and end tags themselves are not included in
     * the sentence.
     */
    public static final String PARAM_REGION_ELEMENT_REGEX = "regionElementRegex";
    @ConfigurationParameter(name = PARAM_REGION_ELEMENT_REGEX, mandatory = false)
    private String regionElementRegex;

    /**
     * Strategy for treating newlines as paragraph breaks.
     */
    public static final String PARAM_NEWLINE_IS_SENTENCE_BREAK = "newlineIsSentenceBreak";
    @ConfigurationParameter(name = PARAM_NEWLINE_IS_SENTENCE_BREAK, mandatory = false, defaultValue = "TWO_CONSECUTIVE")
    private NewlineIsSentenceBreak newlineIsSentenceBreak;

    /**
     * The set of regex for sentence boundary tokens that should be discarded.
     */
    public static final String PARAM_TOKEN_REGEXES_TO_DISCARD = "tokenRegexesToDiscard";
    @ConfigurationParameter(name = PARAM_TOKEN_REGEXES_TO_DISCARD, mandatory = false, defaultValue = {})
    private Set<String> tokenRegexesToDiscard;

    /**
     * Whether to treat all input as one sentence.
     */
    public static final String PARAM_IS_ONE_SENTENCE = "isOneSentence";
    @ConfigurationParameter(name = PARAM_IS_ONE_SENTENCE, mandatory = true, defaultValue = "false")
    private boolean isOneSentence;

    /**
     * Whether to generate empty sentences.
     */
    public static final String PARAM_ALLOW_EMPTY_SENTENCES = "allowEmptySentences";
    @ConfigurationParameter(name = PARAM_ALLOW_EMPTY_SENTENCES, mandatory = true, defaultValue = "false")
    private boolean allowEmptySentences;

    /**
     * Additional options that should be passed to the tokenizers. The available options depend on
     * the language-specific tokenizer being used. 
     */
    private String[] additionalOptions;
    
    @Override
	protected void process(JCas aJCas, String aText, int aZoneBegin)
		throws AnalysisEngineProcessException
    {
        List<Token> casTokens = null;
        
        // Use value from language parameter, document language or fallback language - whatever
        // is available
        String language = getLanguage(aJCas);
        
        if (isWriteToken()) {
            casTokens = new ArrayList<Token>();
            final Tokenizer<?> tokenizer = getTokenizer(language, aText);

            List<?> tokens = tokenizer.tokenize();
            for (int i = 0; i < tokens.size(); i++) {
                final Object token = tokens.get(i);
                // System.out.println("Token class: "+token.getClass());
                CoreLabel l = (CoreLabel) token;
                String t = l.word();
                int begin = l.get(CharacterOffsetBeginAnnotation.class);
                int end = l.get(CharacterOffsetEndAnnotation.class);

                casTokens.add(createToken(aJCas, t, aZoneBegin + begin, aZoneBegin + end));
            }
        }

        if (isWriteSentence()) {
            if (casTokens == null) {
                casTokens = selectCovered(aJCas, Token.class, aZoneBegin,
                        aZoneBegin + aText.length());
            }
            
    		// Prepare the tokens for processing by WordToSentenceProcessor
    		List<CoreLabel> tokensInDocument = new ArrayList<CoreLabel>();
			Pattern nlPattern = Pattern.compile(".*(\r\n|\n|\r).*");
			Matcher nlMatcher = nlPattern.matcher("");
			int lastTokenEnd = 0;
    		for (Token token : casTokens) {
    		    if (!NewlineIsSentenceBreak.NEVER.equals(newlineIsSentenceBreak)) {
    				// add newline as token for newlineIsSentenceBreak parameter
    		        nlMatcher.reset(aJCas.getDocumentText().subSequence(lastTokenEnd, token.getBegin()));
    				if (nlMatcher.matches()) {
    					CoreLabel l = new CoreLabel();
    					l.set(CharacterOffsetBeginAnnotation.class, lastTokenEnd + nlMatcher.start(1));
    					l.set(CharacterOffsetEndAnnotation.class, lastTokenEnd + nlMatcher.end(1));
    					l.setWord("\n");
    					tokensInDocument.add(l);
    				}
    		    }
				lastTokenEnd = token.getEnd();
				// add regular token
    			CoreLabel l = new CoreLabel();
    			l.set(CharacterOffsetBeginAnnotation.class, token.getBegin());
    			l.set(CharacterOffsetEndAnnotation.class, token.getEnd());
    			l.setWord(token.getCoveredText());
    			tokensInDocument.add(l);
    		}

    		// The sentence splitter (probably) requires the escaped text, so we prepare it here
    		PTBEscapingProcessor escaper = new PTBEscapingProcessor();
    		escaper.apply(tokensInDocument);
    
            // Apply the WordToSentenceProcessor to find the sentence boundaries
            WordToSentenceProcessor<CoreLabel> proc = new WordToSentenceProcessor<CoreLabel>(
                    boundaryTokenRegex, boundaryFollowersRegex, boundariesToDiscard,
                    xmlBreakElementsToDiscard, regionElementRegex, newlineIsSentenceBreak, null,
                    tokenRegexesToDiscard, isOneSentence, allowEmptySentences);
    		
    		List<List<CoreLabel>> sentencesInDocument = proc.process(tokensInDocument);
    		for (List<CoreLabel> sentence : sentencesInDocument) {
    			int begin = sentence.get(0).get(CharacterOffsetBeginAnnotation.class);
    			int end = sentence.get(sentence.size()-1).get(CharacterOffsetEndAnnotation.class);
    
    			createSentence(aJCas, begin, end);
    		}
		}
    }

	private
    Tokenizer getTokenizer(
    		final String aLanguage,
    		final String aText) throws AnalysisEngineProcessException
    {
        InternalTokenizerFactory tk = tokenizerFactories.get(aLanguage);
        if (tk == null) {
            if (languageFallback == null) {
                throw new AnalysisEngineProcessException(Messages.BUNDLE,
                        Messages.ERR_UNSUPPORTED_LANGUAGE, new String[] { aLanguage });
            }
            else {
                tk = tokenizerFactories.get(languageFallback);
                if (tk == null) {
                    throw new AnalysisEngineProcessException(Messages.BUNDLE,
                            Messages.ERR_UNSUPPORTED_LANGUAGE, new String[] { languageFallback });
                }
            }
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
    		return ArabicTokenizer.newArabicTokenizer(new StringReader(s), new Properties());
    	}
    }

    private static
    class InternalFrenchTokenizerFactory
    implements InternalTokenizerFactory
    {
        @Override
        public
        Tokenizer<?> create(
                final String s)
        {
            return FrenchTokenizer.factory().getTokenizer(new StringReader(s), "tokenizeNLs=false");
        }
    }

    private static
    class InternalSpanishTokenizerFactory
    implements InternalTokenizerFactory
    {
        @Override
        public
        Tokenizer<?> create(
                final String s)
        {
            return SpanishTokenizer.factory(new CoreLabelTokenFactory(), null).getTokenizer(
                    new StringReader(s));
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
