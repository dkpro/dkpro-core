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
package de.tudarmstadt.ukp.dkpro.core.corenlp;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.process.WordToSentenceProcessor;
import edu.stanford.nlp.util.CoreMap;

/**
 * Tokenizer and sentence splitter using from CoreNLP.
 */
@ResourceMetaData(name = "CoreNLP Segmenter")
@TypeCapability(
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class CoreNlpSegmenter
    extends SegmenterBase
{
    private boolean verbose;
    
    /**
     * The set of boundary tokens. If null, use default.
     * 
     * @see WordToSentenceProcessor#WordToSentenceProcessor
     */
    public static final String PARAM_BOUNDARY_TOKEN_REGEX = "boundaryTokenRegex";
    @ConfigurationParameter(name = PARAM_BOUNDARY_TOKEN_REGEX, mandatory = false, 
            defaultValue = WordToSentenceProcessor.DEFAULT_BOUNDARY_REGEX)
    private String boundaryTokenRegex;

    public static final String PARAM_BOUNDARY_MULTI_TOKEN_REGEX = "boundaryMultiTokenRegex";
    @ConfigurationParameter(name = PARAM_BOUNDARY_MULTI_TOKEN_REGEX, mandatory = false)
    private String boundaryMultiTokenRegex;

    /**
     * These are elements like "p" or "sent", which will be wrapped into regex for approximate XML
     * matching. They will be deleted in the output, and will always trigger a sentence boundary.
     */
    public static final String PARAM_HTML_ELEMENTS_TO_DISCARD = "htmlElementsToDiscard";
    @ConfigurationParameter(name = PARAM_HTML_ELEMENTS_TO_DISCARD, mandatory = false)
    private Set<String> htmlElementsToDiscard;

    /**
     * The set of regex for sentence boundary tokens that should be discarded.
     * 
     * @see WordToSentenceProcessor#DEFAULT_SENTENCE_BOUNDARIES_TO_DISCARD
     */
    public static final String PARAM_BOUNDARIES_TO_DISCARD = "boundaryToDiscard";
    @ConfigurationParameter(name = PARAM_BOUNDARIES_TO_DISCARD, mandatory = false, defaultValue = {
            "\n", "*NL*" })
    private Set<String> boundaryToDiscard;

    /**
     * Strategy for treating newlines as sentence breaks.
     */
    public static final String PARAM_NEWLINE_IS_SENTENCE_BREAK = "newlineIsSentenceBreak";
    @ConfigurationParameter(name = PARAM_NEWLINE_IS_SENTENCE_BREAK, mandatory = false, defaultValue = "two")
    private String newlineIsSentenceBreak;

    /**
     * The set of regex for sentence boundary tokens that should be discarded.
     */
    public static final String PARAM_TOKEN_REGEXES_TO_DISCARD = "tokenRegexesToDiscard";
    @ConfigurationParameter(name = PARAM_TOKEN_REGEXES_TO_DISCARD, mandatory = false, 
            defaultValue = {})
    private Set<String> tokenRegexesToDiscard;
    
    private ModelProviderBase<WordsToSentencesAnnotator> sentenceAnnotator;
    private ModelProviderBase<TokenizerAnnotator> tokenizerAnnotator;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        tokenizerAnnotator = new ModelProviderBase<TokenizerAnnotator>(this, "corenlp", "tokenizer")
        {
            {
                setDefault(LOCATION, NOT_REQUIRED);
            }
            
            @Override
            protected TokenizerAnnotator produceResource(URL aUrl)
                throws IOException
            {
                Properties props = getAggregatedProperties();

                Properties coreNlpProps = new Properties();
                coreNlpProps.setProperty("tokenize.language", props.getProperty(LANGUAGE));
                //coreNlpProps.setProperty("tokenize.class", null);
                //coreNlpProps.setProperty("tokenize.whitespace", "false");
                //coreNlpProps.setProperty("tokenize.options", null);
                //coreNlpProps.setProperty("tokenize.keepeol", "false");
                
                String extraOptions = null;
                
                TokenizerAnnotator annotator = new TokenizerAnnotator(verbose, coreNlpProps,
                        extraOptions);
                
                return annotator;
            }
        };
        
        sentenceAnnotator = new ModelProviderBase<WordsToSentencesAnnotator>(this, "corenlp", "sentence")
        {
            {
                setDefault(LOCATION, NOT_REQUIRED);
            }
            
            @Override
            protected WordsToSentencesAnnotator produceResource(URL aUrl)
                throws IOException
            {
                WordsToSentencesAnnotator annotator = new WordsToSentencesAnnotator(verbose,
                        boundaryTokenRegex, boundaryToDiscard, htmlElementsToDiscard,
                        newlineIsSentenceBreak, boundaryMultiTokenRegex, tokenRegexesToDiscard);
                
                return annotator;
            }
        };
    }
    
    @Override
    protected void process(JCas aJCas, String aText, int aZoneBegin)
        throws AnalysisEngineProcessException
    {
        Annotation document = new Annotation(aText);
        
        if (isWriteToken()) {
            tokenizerAnnotator.configure(aJCas.getCas());
            tokenizerAnnotator.getResource().annotate(document);

            for (CoreLabel token : document.get(CoreAnnotations.TokensAnnotation.class)) {
                createToken(aJCas, 
                        token.get(CharacterOffsetBeginAnnotation.class) + aZoneBegin,
                        token.get(CharacterOffsetEndAnnotation.class) + aZoneBegin);
            }
        }

        if (isWriteSentence()) {
            sentenceAnnotator.configure(aJCas.getCas());
            sentenceAnnotator.getResource().annotate(document);
            
            for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
                createSentence(aJCas,
                        sentence.get(CharacterOffsetBeginAnnotation.class) + aZoneBegin,
                        sentence.get(CharacterOffsetEndAnnotation.class) + aZoneBegin);
            }
        }
    }
}
