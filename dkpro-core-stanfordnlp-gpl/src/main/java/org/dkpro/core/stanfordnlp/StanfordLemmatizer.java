/*
 * Copyright 2007-2018
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
package org.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.Messages;
import org.dkpro.core.stanfordnlp.internal.TokenKey;
import org.dkpro.core.stanfordnlp.util.CoreNlpUtils;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.util.CoreMap;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Stanford Lemmatizer component. The Stanford Morphology-class computes the base form of English
 * words, by removing just inflections (not derivational morphology). That is, it only does noun
 * plurals, pronoun case, and verb endings, and not things like comparative adjectives or derived
 * nominals. It is based on a finite-state transducer implemented by John Carroll et al., written in
 * flex and publicly available. See:
 * http://www.informatics.susx.ac.uk/research/nlp/carroll/morph.html
 * 
 * <p>This only works for ENGLISH.</p>
 */
@Component(OperationType.LEMMATIZER)
@ResourceMetaData(name = "CoreNLP Lemmatizer (old API)")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability("en")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" },
        outputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"})
public class StanfordLemmatizer
    extends JCasAnnotator_ImplBase
{
    /**
     * Enable all traditional PTB3 token transforms (like -LRB-, -RRB-).
     *
     * @see PTBEscapingProcessor
     */
    public static final String PARAM_PTB3_ESCAPING = "ptb3Escaping";
    @ConfigurationParameter(name = PARAM_PTB3_ESCAPING, mandatory = true, defaultValue = "true")
    private boolean ptb3Escaping;

    /**
     * List of extra token texts (usually single character strings) that should be treated like
     * opening quotes and escaped accordingly before being sent to the parser.
     */
    public static final String PARAM_QUOTE_BEGIN = "quoteBegin";
    @ConfigurationParameter(name = PARAM_QUOTE_BEGIN, mandatory = false)
    private List<String> quoteBegin;

    /**
     * List of extra token texts (usually single character strings) that should be treated like
     * closing quotes and escaped accordingly before being sent to the parser.
     */
    public static final String PARAM_QUOTE_END = "quoteEnd";
    @ConfigurationParameter(name = PARAM_QUOTE_END, mandatory = false)
    private List<String> quoteEnd;
    
    private MorphaAnnotator annotator;
    private CoreLabelTokenFactory tokenFactory = new CoreLabelTokenFactory();

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        annotator = new MorphaAnnotator(false);
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        if (!"en".equals(aJCas.getDocumentLanguage())) {
            throw new AnalysisEngineProcessException(Messages.BUNDLE,
                    Messages.ERR_UNSUPPORTED_LANGUAGE,
                    new String[] { aJCas.getDocumentLanguage() });
        }
        
        Annotation document = new Annotation(aJCas.getDocumentText());
        List<CoreMap> sentences = new ArrayList<>();
        for (Sentence s : select(aJCas, Sentence.class)) {
            Annotation sentence = new Annotation(s.getCoveredText());
            sentence.set(CharacterOffsetBeginAnnotation.class, s.getBegin());
            sentence.set(CharacterOffsetEndAnnotation.class, s.getEnd());
            sentence.set(SentenceIndexAnnotation.class, sentences.size());
            
            List<CoreLabel> tokens = new ArrayList<>();
            for (Token t : selectCovered(Token.class, s)) {
                CoreLabel token = tokenFactory.makeToken(t.getText(), t.getBegin(),
                        t.getEnd() - t.getBegin());
                // First add token so that tokens.size() returns a 1-based counting as required
                // by IndexAnnotation
                tokens.add(token);
                token.set(SentenceIndexAnnotation.class, sentences.size());
                token.set(IndexAnnotation.class, tokens.size());
                token.set(TokenKey.class, t);
                POS pos = t.getPos();
                if (pos == null) {
                    throw new AnalysisEngineProcessException(
                            new IllegalStateException("No POS tag available for token:\n" + t));
                }
                else {
                    token.set(PartOfSpeechAnnotation.class, pos.getPosValue());
                }
            }

            if (ptb3Escaping) {
                tokens = CoreNlpUtils.applyPtbEscaping(tokens, quoteBegin, quoteEnd);
            }

            sentence.set(TokensAnnotation.class, tokens);
            sentences.add(sentence);
        }
        
        document.set(SentencesAnnotation.class, sentences);

        annotator.annotate(document);

        for (CoreMap s : document.get(SentencesAnnotation.class)) {
            for (CoreLabel t : s.get(TokensAnnotation.class)) {
                Token token = t.get(TokenKey.class);
                String tag = t.get(LemmaAnnotation.class);
                Lemma anno = new Lemma(aJCas, token.getBegin(), token.getEnd());
                anno.setValue(tag);
                anno.addToIndexes();
                token.setLemma(anno);
            }
        }
    }
}
