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
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.util.Level.INFO;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.nlp4j.internal.EmoryNlp2Uima;
import de.tudarmstadt.ukp.dkpro.core.nlp4j.internal.EmoryNlpUtils;
import de.tudarmstadt.ukp.dkpro.core.nlp4j.internal.OnlineComponentTagsetDescriptionProvider;
import de.tudarmstadt.ukp.dkpro.core.nlp4j.internal.Uima2EmoryNlp;
import edu.emory.mathcs.nlp.common.util.NLPUtils;
import edu.emory.mathcs.nlp.component.pos.POSState;
import edu.emory.mathcs.nlp.component.template.OnlineComponent;
import edu.emory.mathcs.nlp.component.template.node.NLPNode;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Part-of-Speech annotator using Emory NLP4J. Requires {@link Sentence}s to be annotated before.
 */
@Component(OperationType.PART_OF_SPEECH_TAGGER)
@ResourceMetaData(name = "NLP4J POS-Tagger")
@TypeCapability(
        inputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" }, 
        outputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class Nlp4JPosTagger
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
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = 
            ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spaming the heap with thousands of strings representing only a few different tags.
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internTags;

    /**
     * Log the tag set(s) when a model is loaded.
     *
     * Default: {@code false}
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

    /**
     * Process anyway, even if the model relies on features that are not supported by this
     * component.
     * 
     * Default: {@code false}
     */
    public static final String PARAM_IGNORE_MISSING_FEATURES = "ignoreMissingFeatures";
    @ConfigurationParameter(name = PARAM_IGNORE_MISSING_FEATURES, mandatory = true, defaultValue = "false")
    protected boolean ignoreMissingFeatures;

    private Nlp4JPosTaggerModelProvider modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new Nlp4JPosTaggerModelProvider(this);
        
        // General setup of the mapping provider in initialize()
        mappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
                language, modelProvider);
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();

        // Document-specific configuration of model and mapping provider in process()
        modelProvider.configure(cas);
        
        // Mind the mapping provider must be configured after the model provider as it uses the
        // model metadata
        mappingProvider.configure(cas);

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<Token> tokens = selectCovered(aJCas, Token.class, sentence);
            NLPNode[] nodes = Uima2EmoryNlp.convertSentence(tokens);
            
            // Process the sentences - new results will be stored in the existing NLPNodes
            modelProvider.getResource().process(nodes);
            
            EmoryNlp2Uima.convertPos(cas, tokens, nodes, mappingProvider, internTags);
        }
    }
    
    private class Nlp4JPosTaggerModelProvider
        extends ModelProviderBase<OnlineComponent<NLPNode, POSState<NLPNode>>>
    {
        public Nlp4JPosTaggerModelProvider(Object aOwner)
        {
            super(aOwner, "nlp4j", "tagger");
        }
        
        @Override
        protected OnlineComponent<NLPNode, POSState<NLPNode>> produceResource(InputStream aStream)
            throws Exception
        {
            String language = getAggregatedProperties().getProperty(LANGUAGE);

            if (!language.equals("en")) {
                throw new IllegalArgumentException(new Throwable(
                        "Emory NLP4J supports only English"));
            }
            
            EmoryNlpUtils.initGlobalLexica();

            // Load the POS tagger model from the location the model provider offers
            OnlineComponent<NLPNode, POSState<NLPNode>> component = (OnlineComponent) 
                    NLPUtils.getComponent(aStream);

            // Extract tagset information from the model
            OnlineComponentTagsetDescriptionProvider<NLPNode, POSState<NLPNode>> tsdp = 
                    new OnlineComponentTagsetDescriptionProvider<>(
                            getResourceMetaData().getProperty("pos.tagset"), POS.class, component);
            addTagset(tsdp);

            if (printTagSet) {
                getContext().getLogger().log(INFO, tsdp.toString());
            }

            Set<String> features = EmoryNlpUtils.extractFeatures(component);
            getLogger().info("Model uses these features: " + features);

            
            Set<String> unsupportedFeatures = EmoryNlpUtils.extractUnsupportedFeatures(component);
            if (!unsupportedFeatures.isEmpty()) {
                String message = "Model these uses unsupported features: " + unsupportedFeatures;
                if (ignoreMissingFeatures) {
                    getLogger().warn(message); 
                }
                else {
                    throw new IOException(message);
                }
            }

            // Create a new POS tagger instance from the loaded model
            return component;
        }
    };
}
