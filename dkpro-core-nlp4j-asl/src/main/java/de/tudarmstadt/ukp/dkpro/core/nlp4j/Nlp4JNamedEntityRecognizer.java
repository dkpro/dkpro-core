/*******************************************************************************
 * Copyright 2016
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
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.nlp4j.internal.EmoryNlp2Uima;
import de.tudarmstadt.ukp.dkpro.core.nlp4j.internal.EmoryNlpUtils;
import de.tudarmstadt.ukp.dkpro.core.nlp4j.internal.OnlineComponentTagsetDescriptionProvider;
import de.tudarmstadt.ukp.dkpro.core.nlp4j.internal.Uima2EmoryNlp;
import edu.emory.mathcs.nlp.component.ner.NERState;
import edu.emory.mathcs.nlp.component.template.OnlineComponent;
import edu.emory.mathcs.nlp.component.template.node.NLPNode;
import edu.emory.mathcs.nlp.decode.NLPUtils;

/**
 * Emory NLP4J name finder wrapper.
 */
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
public class Nlp4JNamedEntityRecognizer
    extends JCasAnnotator_ImplBase
{
    /**
     * Log the tag set(s) when a model is loaded.
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Variant of a model the model. Used to address a specific model if here are multiple models
     * for one language.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * Location from which the model is read.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;

    /**
     * Location of the mapping file for named entity tags to UIMA types.
     */
    public static final String PARAM_NAMED_ENTITY_MAPPING_LOCATION = ComponentParameters.PARAM_NAMED_ENTITY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_MAPPING_LOCATION, mandatory = false)
    protected String mappingLocation;

    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spaming the heap with thousands of strings representing only a few different tags.
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internTags;

    /**
     * Process anyway, even if the model relies on features that are not supported by this
     * component.
     * 
     * Default: {@code false}
     */
    public static final String PARAM_IGNORE_MISSING_FEATURES = "ignoreMissingFeatures";
    @ConfigurationParameter(name = PARAM_IGNORE_MISSING_FEATURES, mandatory = true, defaultValue = "false")
    protected boolean ignoreMissingFeatures;

    private Nlp4JNamedEntityRecognizerModelProvider modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new Nlp4JNamedEntityRecognizerModelProvider(this);
        
        mappingProvider = new MappingProvider();
        mappingProvider
                .setDefaultVariantsLocation("de/tudarmstadt/ukp/dkpro/core/nlp4j/lib/ner-default-variants.map");
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/"
                + "core/nlp4j/lib/ner-${language}-${variant}.map");
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, NamedEntity.class.getName());
        mappingProvider.setOverride(MappingProvider.LOCATION, mappingLocation);
        mappingProvider.setOverride(MappingProvider.LANGUAGE, language);
        mappingProvider.setOverride(MappingProvider.VARIANT, variant);
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();
        modelProvider.configure(cas);
        mappingProvider.configure(cas);

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<Token> tokens = selectCovered(aJCas, Token.class, sentence);
            NLPNode[] nodes = Uima2EmoryNlp.convertSentence(tokens);
            
            // Process the sentences - new results will be stored in the existing NLPNodes
            modelProvider.getResource().process(nodes);
            
            EmoryNlp2Uima.convertNamedEntities(cas, tokens, nodes, mappingProvider, internTags);
        }
    }
    
    private class Nlp4JNamedEntityRecognizerModelProvider
        extends ModelProviderBase<OnlineComponent<NLPNode, NERState<NLPNode>>>
    {
        public Nlp4JNamedEntityRecognizerModelProvider(Object aOwner)
        {
            super(aOwner, "nlp4j", "ner");
        }

        @Override
        protected OnlineComponent<NLPNode, NERState<NLPNode>> produceResource(InputStream aStream)
            throws Exception
        {
            String language = getAggregatedProperties().getProperty(LANGUAGE);

            if (!language.equals("en")) {
                throw new IllegalArgumentException(new Throwable(
                        "Emory NLP4J supports only English"));
            }

            EmoryNlpUtils.initGlobalLexica();

            // Load the POS tagger model from the location the model provider offers
            OnlineComponent<NLPNode, NERState<NLPNode>> component = (OnlineComponent) NLPUtils
                    .getComponent(aStream);

            // Extract tagset information from the model
            OnlineComponentTagsetDescriptionProvider<NLPNode, NERState<NLPNode>> tsdp = 
                    new OnlineComponentTagsetDescriptionProvider<NLPNode, NERState<NLPNode>>(
                            getResourceMetaData().getProperty("ner.tagset"), POS.class, component);
            // addTagset(tsdp);

            if (printTagSet) {
                getContext().getLogger().log(INFO, tsdp.toString());
            }

            Set<String> features = EmoryNlpUtils.extractFeatures(component);
            getLogger().info("Model uses these features: " + features);

            Set<String> unsupportedFeatures = EmoryNlpUtils.extractUnsupportedFeatures(component,
                    "named_entity_tag");
            if (!unsupportedFeatures.isEmpty()) {
                String message = "Model these uses unsupported features: " + unsupportedFeatures;
                if (ignoreMissingFeatures) {
                    getLogger().warn(message);
                }
                else {
                    throw new IOException(message);
                }
            }

            return component;
        }
    };
}
