/*
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;

/**
 * Lemmatizer using OpenNLP.
 */
@TypeCapability(
        inputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"}, 
        outputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class OpenNlpLemmatizer
	extends JCasAnnotator_ImplBase
{
	/**
	 * Use this language instead of the document language to resolve the model.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	/**
	 * Override the default variant used to locate the model.
	 */
	public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
	@ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
	protected String variant;

	/**
	 * Load the model from this location instead of locating the model automatically.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

    /**
     * The character encoding used by the model.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = false)
    private String modelEncoding;

	private CasConfigurableProviderBase<LemmatizerME> modelProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

        modelProvider = new ModelProviderBase<LemmatizerME>(this, "lemma")
        {
            @Override
            protected LemmatizerME produceResource(InputStream aStream)
                throws Exception
            {
                // Load the lemmatizer model from the location the model provider offers
                LemmatizerModel model = new LemmatizerModel(aStream);

                // Create a new POS tagger instance from the loaded model
                return new LemmatizerME(model);
            }
        };
	}

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();

        // Document-specific configuration of model and mapping provider in process()
        modelProvider.configure(cas);

        Map<Sentence, Collection<Token>> index = indexCovered(aJCas, Sentence.class, Token.class);
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            Collection<Token> tokens = index.get(sentence);
            
            String[] toks = new String[tokens.size()];
            String[] tags = new String[tokens.size()];

            int i = 0;
            for (Token t : tokens) {
                toks[i] = t.getCoveredText();
                tags[i] = t.getPosValue();
                i++;
            }
            
            // Fetch the OpenNLP lemmatizer instance configured with the right model and use it to
            // tag the text
            LemmatizerME lemmatizer = modelProvider.getResource();
            String[] lemmas = lemmatizer.lemmatize(toks, tags);

            int n = 0;
            for (Token t : tokens) {
                Lemma lemmaAnno = new Lemma(aJCas, t.getBegin(), t.getEnd());
                lemmaAnno.setValue(lemmas[n]);
                lemmaAnno.addToIndexes();
                
                // Connect the Lemma annotation to the respective token annotation
                t.setLemma(lemmaAnno);
                n++;
            }
        }
    }
}
