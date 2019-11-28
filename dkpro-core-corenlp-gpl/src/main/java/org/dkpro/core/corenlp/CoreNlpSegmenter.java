/*
 * Copyright 2007-2019
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.corenlp;

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
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.api.segmentation.SegmenterBase;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator.TokenizerType;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.process.WordToSentenceProcessor;
import edu.stanford.nlp.process.WordToSentenceProcessor.NewlineIsSentenceBreak;
import edu.stanford.nlp.util.CoreMap;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Tokenizer and sentence splitter using from Stanford CoreNLP.
 */
@ResourceMetaData(name = "CoreNLP Segmenter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class CoreNlpSegmenter
    extends SegmenterBase
{
    private boolean verbose;
    
    /**
     * The set of boundary tokens.
     * 
     * @see WordToSentenceProcessor#WordToSentenceProcessor
     */
    public static final String PARAM_BOUNDARY_TOKEN_REGEX = "boundaryTokenRegex";
    @ConfigurationParameter(name = PARAM_BOUNDARY_TOKEN_REGEX, mandatory = false, 
            defaultValue = WordToSentenceProcessor.DEFAULT_BOUNDARY_REGEX)
    private String boundaryTokenRegex;

    /**
     * A TokensRegex multi-token pattern for finding boundaries.
     */
    public static final String PARAM_BOUNDARY_MULTI_TOKEN_REGEX = "boundaryMultiTokenRegex";
    @ConfigurationParameter(name = PARAM_BOUNDARY_MULTI_TOKEN_REGEX, mandatory = false)
    private String boundaryMultiTokenRegex;

    /**
     * These are elements like "p" or "sent", which will be wrapped into regular expressions for
     * approximate XML matching. They will be deleted in the output, and will always trigger a
     * sentence boundary.
     */
    public static final String PARAM_HTML_ELEMENTS_TO_DISCARD = "htmlElementsToDiscard";
    @ConfigurationParameter(name = PARAM_HTML_ELEMENTS_TO_DISCARD, mandatory = false)
    private Set<String> htmlElementsToDiscard;

    /**
     * The set of regular expressions for sentence boundary tokens that should be discarded.
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
     * The set of regular expressions for sentence boundary tokens that should be discarded.
     */
    public static final String PARAM_TOKEN_REGEXES_TO_DISCARD = "tokenRegexesToDiscard";
    @ConfigurationParameter(name = PARAM_TOKEN_REGEXES_TO_DISCARD, mandatory = false, 
            defaultValue = {})
    private Set<String> tokenRegexesToDiscard;
    
    /**
     * Variant of a model the model. Used to address a specific model if here are multiple models
     * for one language.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    private String variant;

    /**
     * Location from which the model is read.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    /** 
     * Additional options that should be passed to the tokenizers. 
     */ 
    public static final String PARAM_TOKENIZATION_OPTIONS = "tokenizationOption"; 
    @ConfigurationParameter(name = PARAM_TOKENIZATION_OPTIONS, mandatory = false) 
    private String options;
    
    private ModelProviderBase<WordsToSentencesAnnotator> sentenceAnnotator;
    private ModelProviderBase<TokenizerAnnotator> tokenizerAnnotator;
    private boolean useCoreLabelWord = false;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        tokenizerAnnotator = new ModelProviderBase<TokenizerAnnotator>(this, "corenlp", "tokenizer")
        {
            {
                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/corenlp/lib/tokenizer-${language}-${variant}.properties");
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
                //coreNlpProps.setProperty("tokenize.keepeol", "false");
                
                if (options == null) {
                    options = TokenizerType.getTokenizerType(coreNlpProps).getDefaultOptions();
                } 
                
                if (options.contains("splitAll=true")) {
                    useCoreLabelWord = true;
                }
                
                NewlineIsSentenceBreak breakNL = 
                    WordToSentenceProcessor.stringToNewlineIsSentenceBreak(newlineIsSentenceBreak);
                if (NewlineIsSentenceBreak.ALWAYS == breakNL || 
                        NewlineIsSentenceBreak.TWO_CONSECUTIVE == breakNL) {
                    options = "tokenizeNLs=true," + options;
                }
                
                coreNlpProps.setProperty("tokenize.options", options);
                
                if (aUrl != null) {
                    String modelFile = aUrl.toString();
                    
                    // Loading gzipped files from URL is broken in CoreNLP
                    // https://github.com/stanfordnlp/CoreNLP/issues/94
                    if (modelFile.startsWith("jar:") && modelFile.endsWith(".gz")) {
                        modelFile = org.apache.commons.lang3.StringUtils.substringAfter(modelFile, "!/");
                    }
                    
                    coreNlpProps.setProperty("segment.model", modelFile);
                }
                
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
                //useCoreLabelWord to be set to true when allowing clitics in the language
                if (useCoreLabelWord) {
                    createToken(aJCas, 
                            token.word(),
                            token.get(CharacterOffsetBeginAnnotation.class) + aZoneBegin,
                            token.get(CharacterOffsetEndAnnotation.class) + aZoneBegin);
                } else {
                    createToken(aJCas, 
                            token.get(CharacterOffsetBeginAnnotation.class) + aZoneBegin,
                            token.get(CharacterOffsetEndAnnotation.class) + aZoneBegin);
                }
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
