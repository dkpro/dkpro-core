/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.nlp4j;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.IOException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.emory.mathcs.nlp.common.util.StringUtils;
import edu.emory.mathcs.nlp.component.morph.MorphAnalyzer;
import edu.emory.mathcs.nlp.component.morph.english.EnglishMorphAnalyzer;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Emory NLP4J lemmatizer. This is a lower-casing lemmatizer.
 */
@Component(OperationType.LEMMATIZER)
@ResourceMetaData(name = "NLP4J Lemmatizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"},
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class Nlp4JLemmatizer
    extends JCasAnnotator_ImplBase
{
    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;
    
    private ModelProviderBase<MorphAnalyzer> modelProvider;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        modelProvider = new ModelProviderBase<MorphAnalyzer>() {
            {
                setContextObject(Nlp4JLemmatizer.this);
                setDefault(LOCATION, NOT_REQUIRED + "-${language}");
                setOverride(LANGUAGE, language);
            }
            
            @Override
            protected MorphAnalyzer produceResource(URL aUrl)
                throws IOException
            {
                String language = getAggregatedProperties().getProperty(LANGUAGE);
                
                if (!language.equals("en")) {
                    throw new IllegalArgumentException(new Throwable(
                            "Emory NLP4J supports only English"));
                }
                
                return new EnglishMorphAnalyzer();
            }
        };
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        modelProvider.configure(aJCas.getCas());
        
        MorphAnalyzer lemmatizer = modelProvider.getResource();
        
        for (Token t : select(aJCas, Token.class)) {
            String pos = null;
            if (t.getPos() != null) {
                pos = t.getPos().getPosValue();
            }
            
            Lemma lemma = new Lemma(aJCas, t.getBegin(), t.getEnd());
            lemma.setValue(lemmatizer.lemmatize(StringUtils.toSimplifiedForm(t.getText()),
                    pos));
            lemma.addToIndexes();
            
            t.setLemma(lemma);
        }
    }
}
