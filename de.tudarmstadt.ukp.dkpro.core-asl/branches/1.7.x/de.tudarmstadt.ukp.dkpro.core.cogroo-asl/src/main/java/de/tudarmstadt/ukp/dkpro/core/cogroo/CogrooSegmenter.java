/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.cogroo;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cogroo.analyzer.Analyzer;
import org.cogroo.analyzer.ComponentFactory;
import org.cogroo.text.Document;
import org.cogroo.text.Sentence;
import org.cogroo.text.Token;
import org.cogroo.text.impl.DocumentImpl;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;

/**
 * Tokenizer and sentence splitter using CoGrOO.
 */
@TypeCapability(
	    outputs = {
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class CogrooSegmenter
	extends SegmenterBase
{
	/**
	 * Use this language instead of the document language to resolve the model.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	private CasConfigurableProviderBase<Analyzer> sentenceModelProvider;
    private CasConfigurableProviderBase<Analyzer> tokenModelProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		sentenceModelProvider = new ModelProviderBase<Analyzer>() {
			{
			    setContextObject(CogrooSegmenter.this);

				setDefault(LOCATION, NOT_REQUIRED);
				setOverride(LANGUAGE, language);
			}

			@Override
            protected Analyzer produceResource(URL aUrl)
                throws IOException
            {
			    Properties props = getAggregatedProperties();
			    String language = props.getProperty(LANGUAGE);
			    
                return ComponentFactory.create(Locale.forLanguageTag(language))
                        .createSentenceDetector();
			}
		};
		
		tokenModelProvider = new ModelProviderBase<Analyzer>() {
            {
                setContextObject(CogrooSegmenter.this);

                setDefault(LOCATION, NOT_REQUIRED);
                setOverride(LANGUAGE, language);
            }

            @Override
            protected Analyzer produceResource(URL aUrl)
                throws IOException
            {
                Properties props = getAggregatedProperties();
                String language = props.getProperty(LANGUAGE);
                
                return ComponentFactory.create(Locale.forLanguageTag(language))
                        .createTokenizer();
            }
        };
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		CAS cas = aJCas.getCas();
		sentenceModelProvider.configure(cas);
        tokenModelProvider.configure(cas);

		super.process(aJCas);
	}

	@Override
	protected void process(JCas aJCas, String aText, int aZoneBegin)
		throws AnalysisEngineProcessException
	{
	    Document doc = new DocumentImpl();
	    doc.setText(aJCas.getDocumentText());
	    
	    sentenceModelProvider.getResource().analyze(doc);
        tokenModelProvider.getResource().analyze(doc);
	 
        for (Sentence s : doc.getSentences()) {
            createSentence(aJCas, s.getStart() + aZoneBegin, s.getEnd() + aZoneBegin);
            for (Token t : s.getTokens()) {
                createToken(aJCas, t.getStart() + s.getStart() + aZoneBegin,
                        t.getEnd() + s.getStart() + aZoneBegin);
            }
        }
	}
}
