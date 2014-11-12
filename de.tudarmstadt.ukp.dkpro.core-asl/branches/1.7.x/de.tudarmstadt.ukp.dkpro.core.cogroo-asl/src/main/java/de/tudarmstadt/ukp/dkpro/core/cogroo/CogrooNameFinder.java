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

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cogroo.analyzer.Analyzer;
import org.cogroo.analyzer.ComponentFactory;
import org.cogroo.config.Analyzers;
import org.cogroo.text.Document;
import org.cogroo.text.impl.DocumentImpl;
import org.cogroo.text.impl.SentenceImpl;
import org.cogroo.text.impl.TokenImpl;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Tokenizer and sentence splitter using CoGrOO.
 */
@TypeCapability(
	    inputs = {
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
public class CogrooNameFinder
	extends JCasAnnotator_ImplBase
{
	/**
	 * Use this language instead of the document language to resolve the model.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	private CasConfigurableProviderBase<Analyzer> modelProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		modelProvider = new ModelProviderBase<Analyzer>() {
			{
			    setContextObject(CogrooNameFinder.this);

				setDefault(LOCATION, NOT_REQUIRED);
				setOverride(LANGUAGE, language);
			}

			@Override
            protected Analyzer produceResource(URL aUrl)
                throws IOException
            {
                Properties props = getAggregatedProperties();

                String language = props.getProperty(LANGUAGE);

                return ComponentFactory.create(Locale.forLanguageTag(language)).createNameFinder();
            }
		};
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		CAS cas = aJCas.getCas();
		modelProvider.configure(cas);

        // This is actually quite some overhead, because internally Cogroo is just using the
        // OpenNLP namefinder which simply takes a string array and returns and arrays of spans...
		// It would be much more efficient to use the model directly.
		
        // Convert from UIMA to Cogroo model
        Document doc = new DocumentImpl();
        doc.setText(aJCas.getDocumentText());
        List<org.cogroo.text.Sentence> sentences = new ArrayList<org.cogroo.text.Sentence>();
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            org.cogroo.text.Sentence s = new SentenceImpl(sentence.getBegin(), sentence.getEnd(), doc);
            List<org.cogroo.text.Token> tokens = new ArrayList<org.cogroo.text.Token>();
            for (Token token : selectCovered(Token.class, sentence)) {
                tokens.add(new TokenImpl(token.getBegin() - sentence.getBegin(),
                        token.getEnd() - sentence.getBegin(), token.getCoveredText()));
            }
            s.setTokens(tokens);
            sentences.add(s);
        }
        doc.setSentences(sentences);
        
        // Process
        modelProvider.getResource().analyze(doc);
        
        // Convert from Cogroo to UIMA model
        for (org.cogroo.text.Sentence s : doc.getSentences()) {
            for (org.cogroo.text.Token t : s.getTokens()) {
                if ("P".equals(t.getAdditionalContext(Analyzers.NAME_FINDER))) {
                    NamedEntity ne = new NamedEntity(aJCas, s.getStart() + t.getStart(),
                            s.getStart() + t.getEnd());
                    ne.setValue("P");
                    ne.addToIndexes();
                }
            }
        }
	}
}
