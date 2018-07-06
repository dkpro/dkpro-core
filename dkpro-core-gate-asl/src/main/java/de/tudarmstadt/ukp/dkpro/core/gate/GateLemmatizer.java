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
package de.tudarmstadt.ukp.dkpro.core.gate;

import java.io.IOException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NOUN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PRON;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_VERB;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import gate.creole.ResourceInstantiationException;
import gate.creole.morph.Interpret;

/**
 * Wrapper for the GATE rule based lemmatizer.
 *
 * Based on code by Asher Stern from the BIUTEE textual entailment tool.
 *
 * @since 1.4.0
 */
@Component(OperationType.LEMMATIZER)
@ResourceMetaData(name = "GATE Lemmatizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
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
     * URI of the model artifact. This can be used to override the default model resolving 
     * mechanism and directly address a particular model.
     * 
     * <p>The URI format is {@code mvn:${groupId}:${artifactId}:${version}}. Remember to set
     * the variant parameter to match the artifact. If the artifact contains the model in
     * a non-default location, you  also have to specify the model location parameter, e.g.
     * {@code classpath:/model/path/in/artifact/model.bin}.</p>
     */
    public static final String PARAM_MODEL_ARTIFACT_URI = 
            ComponentParameters.PARAM_MODEL_ARTIFACT_URI;
    @ConfigurationParameter(name = PARAM_MODEL_ARTIFACT_URI, mandatory = false)
    protected String modelArtifactUri;
    
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
                        "lemmatizer-${language}-${variant}.properties");
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
                if (pos.getClass().equals(POS_VERB.class)) {
                    category = GATE_LEMMATIZER_VERB_CATEGORY_STRING;
                }
                else if (pos.getClass().equals(POS_NOUN.class)) {
                    category = GATE_LEMMATIZER_NOUN_CATEGORY_STRING;
                }
                else if (pos.getClass().equals(POS_PRON.class)) {
                    category = GATE_LEMMATIZER_NOUN_CATEGORY_STRING;
                }
                else {
                    category = GATE_LEMMATIZER_ALL_CATEGORIES_STRING;
                }
            }
            else {
                category = GATE_LEMMATIZER_ALL_CATEGORIES_STRING;
            }

            String tokenString = token.getText();
            String lemmaString = modelProvider.getResource().runMorpher(tokenString, category);
            if (lemmaString == null) {
                lemmaString = tokenString;
            }

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
