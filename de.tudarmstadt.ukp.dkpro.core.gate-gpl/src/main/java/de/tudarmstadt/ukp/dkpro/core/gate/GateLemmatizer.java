/**
 * Copyright 2007-2014
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.core.gate;

import gate.creole.ResourceInstantiationException;
import gate.creole.morph.Interpret;

import java.io.IOException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Wrapper for the GATE rule based lemmatizer.
 *
 * Based on code by Asher Stern from the BIUTEE textual entailment tool.
 *
 * @author zesch
 * @since 1.4.0
 */
public class GateLemmatizer
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

	// constants
	public static final String GATE_LEMMATIZER_VERB_CATEGORY_STRING = "VB";
	public static final String GATE_LEMMATIZER_NOUN_CATEGORY_STRING = "NN";
	public static final String GATE_LEMMATIZER_ALL_CATEGORIES_STRING = "*";

    private CasConfigurableProviderBase<Interpret> modelProvider;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		modelProvider = new CasConfigurableProviderBase<Interpret>() {
            {
                setContextObject(GateLemmatizer.this);

                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/gate/lib/" +
                        "morph-${language}-${variant}.rul");
                setDefault(VARIANT, "default");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected Interpret produceResource(URL aUrl) throws IOException
            {
                try {
                    Interpret gateLemmatizerInterpretObject = new Interpret();
                    gateLemmatizerInterpretObject.init(aUrl);
                    return gateLemmatizerInterpretObject;
                }
                catch (ResourceInstantiationException e) {
                    throw new IOException(e);
                }
            }
        };
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{
	    modelProvider.configure(jcas.getCas());

		String category = null;
		for (Token token : JCasUtil.select(jcas, Token.class)) {
			POS pos = token.getPos();

			if (pos != null) {
				if (pos.getClass().equals(V.class)) {
					category = GATE_LEMMATIZER_VERB_CATEGORY_STRING;
				}
				else if (pos.getClass().equals(N.class)) {
					category = GATE_LEMMATIZER_NOUN_CATEGORY_STRING;
				}
				else if (pos.getClass().equals(PR.class)) {
					category = GATE_LEMMATIZER_NOUN_CATEGORY_STRING;
				}
				else {
					category = GATE_LEMMATIZER_ALL_CATEGORIES_STRING;
				}
			}
			else {
				category = GATE_LEMMATIZER_ALL_CATEGORIES_STRING;
			}

			String tokenString = token.getCoveredText();
			String lemmaString = modelProvider.getResource().runMorpher(tokenString, category);

			Lemma lemma = new Lemma(jcas, token.getBegin(), token.getEnd());
			lemma.setValue(lemmaString);
			lemma.addToIndexes();

			// remove (a potentially existing) old lemma before adding a new one
			if (token.getLemma() != null) {
				Lemma oldLemma = token.getLemma();
				oldLemma.removeFromIndexes();
			}

			token.setLemma(lemma);
		}
	}
}