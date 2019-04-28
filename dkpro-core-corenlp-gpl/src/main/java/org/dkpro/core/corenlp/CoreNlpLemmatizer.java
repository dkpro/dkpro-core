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
package org.dkpro.core.corenlp;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.Messages;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.corenlp.internal.CoreNlp2DKPro;
import org.dkpro.core.corenlp.internal.DKPro2CoreNlp;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.MorphaAnnotator;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Lemmatizer from CoreNLP.
 */
@Component(OperationType.LEMMATIZER)
@ResourceMetaData(name = "CoreNLP Lemmatizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" },
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"})
public class CoreNlpLemmatizer
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
    
    private boolean verbose = false;
    
    private CasConfigurableProviderBase<MorphaAnnotator> annotatorProvider;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        annotatorProvider = new ModelProviderBase<MorphaAnnotator>(this, "stanfordnlp", "lemma") {
            {
                setDefault(LOCATION, NOT_REQUIRED);
            }
            
            @Override
            protected MorphaAnnotator produceResource(URL aUrl) throws IOException
            {
                MorphaAnnotator annotator = new MorphaAnnotator(verbose);
                return annotator;
            }
        };
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
        
        CAS cas = aJCas.getCas();
        
        annotatorProvider.configure(cas);
        
        // Transfer from CAS to CoreNLP
        DKPro2CoreNlp converter = new DKPro2CoreNlp();
        converter.setPtb3Escaping(ptb3Escaping);
        converter.setQuoteBegin(quoteBegin);
        converter.setQuoteEnd(quoteEnd);
        
        Annotation document = new Annotation((String) null);
        converter.convert(aJCas, document);

        // Actual processing
        annotatorProvider.getResource().annotate(document);
        
        // Transfer back into the CAS
        CoreNlp2DKPro.convertLemmas(aJCas, document);
    };
}
