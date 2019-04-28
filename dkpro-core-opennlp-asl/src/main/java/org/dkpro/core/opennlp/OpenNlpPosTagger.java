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
package org.dkpro.core.opennlp;

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.apache.uima.util.Level.INFO;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.lexmorph.pos.POSUtils;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.parameter.ResourceParameter;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.MappingProviderFactory;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.opennlp.internal.OpenNlpTagsetDescriptionProvider;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

/**
 * Part-of-Speech annotator using OpenNLP.
 */
@Component(OperationType.PART_OF_SPEECH_TAGGER)
@ResourceMetaData(name = "OpenNLP POS-Tagger")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
//NOTE: This file contains Asciidoc markers for partial inclusion of this file in the documentation
//Do not remove these tags!
// tag::capabilities[]
@TypeCapability(
        inputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" }, 
        outputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class OpenNlpPosTagger
    extends JCasAnnotator_ImplBase
{
// end::capabilities[]
    
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
    @ResourceParameter(MimeTypes.APPLICATION_X_OPENNLP_TAGGER)
    protected String modelLocation;

    /**
     * The character encoding used by the model.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = false)
    private String modelEncoding;

    /**
     * Enable/disable type mapping.
     */
    public static final String PARAM_MAPPING_ENABLED = ComponentParameters.PARAM_MAPPING_ENABLED;
    @ConfigurationParameter(name = PARAM_MAPPING_ENABLED, mandatory = true, defaultValue = 
            ComponentParameters.DEFAULT_MAPPING_ENABLED)
    protected boolean mappingEnabled;

    /**
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = 
            ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    /**
     * Log the tag set(s) when a model is loaded.
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

    protected CasConfigurableProviderBase<POSTaggerME> modelProvider;
    private MappingProvider mappingProvider;
    private Charset encoding;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        encoding = modelEncoding != null ? Charset.forName(modelEncoding) : null;
        
// tag::model-provider-decl[]
        // Use ModelProviderBase convenience constructor to set up a model provider that
        // auto-detects most of its settings and is configured to use default variants.
        // Auto-detection inspects the configuration parameter fields (@ConfigurationParameter)
        // of the analysis engine class and looks for default parameters such as PARAM_LANGUAGE,
        // PARAM_VARIANT, and PARAM_MODEL_LOCATION.
        modelProvider = new ModelProviderBase<POSTaggerME>(this, "tagger")
        {
            {
                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(LOCATION,
                        "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/tagger-${language}-${variant}.properties");
            }
            
            @Override
            protected POSTaggerME produceResource(InputStream aStream)
                throws Exception
            {
                // Load the POS tagger model from the location the model provider offers
                POSModel model = new POSModel(aStream);
// end::model-provider-decl[]

                // Extract tagset information from the model
                OpenNlpTagsetDescriptionProvider tsdp = new OpenNlpTagsetDescriptionProvider(
                        getResourceMetaData().getProperty("pos.tagset"), POS.class,
                        model.getPosModel());
                if (getResourceMetaData().containsKey("pos.tagset.tagSplitPattern")) {
                    tsdp.setTagSplitPattern(getResourceMetaData().getProperty(
                            "pos.tagset.tagSplitPattern"));
                }
                addTagset(tsdp);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, tsdp.toString());
                }

// tag::model-provider-decl[]
                // Create a new POS tagger instance from the loaded model
                return new POSTaggerME(model);
            }
        };
// end::model-provider-decl[]

// tag::mapping-provider-decl[]
        // General setup of the mapping provider in initialize()
        mappingProvider = MappingProviderFactory.createPosMappingProvider(this, posMappingLocation,
                language, modelProvider);
// end::mapping-provider-decl[]
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
// tag::model-provider-use-1[]
        CAS cas = aJCas.getCas();

        // Document-specific configuration of model and mapping provider in process()
        modelProvider.configure(cas);
// end::model-provider-use-1[]
        
// tag::mapping-provider-use-1[]
        // Mind the mapping provider must be configured after the model provider as it uses the
        // model metadata
        mappingProvider.configure(cas);
// end::mapping-provider-use-1[]

        // When packaging a model, it is possible to store additional metadata. Here we fetch such a
        // model metadata property that we use to determine if the tag produced by the tagger needs
        // to be post-processed. This property is specific to the DKPro Core OpenNLP models
        String tagSplitPattern = modelProvider.getResourceMetaData().getProperty(
                "pos.tagset.tagSplitPattern");
        
        Map<Sentence, Collection<Token>> index = indexCovered(aJCas, Sentence.class, Token.class);
        for (Sentence sentence : select(aJCas, Sentence.class)) {
// tag::model-provider-use-2[]
            Collection<Token> tokens = index.get(sentence);
            String[] tokenTexts = toText(tokens).toArray(new String[tokens.size()]);
            fixEncoding(tokenTexts);
            
            // Fetch the OpenNLP pos tagger instance configured with the right model and use it to
            // tag the text
            String[] tags = modelProvider.getResource().tag(tokenTexts);
// end::model-provider-use-2[]

            int i = 0;
            for (Token t : tokens) {
                String tag = tags[i];

                // Post-process the tag if necessary
                if (tagSplitPattern != null) {
                    tag = tag.split(tagSplitPattern)[0];
                }

// tag::mapping-provider-use-2[]
                // Convert the tag produced by the tagger to an UIMA type, create an annotation
                // of this type, and add it to the document.
                Type posTag = mappingProvider.getTagType(tag);
                POS posAnno = (POS) cas.createAnnotation(posTag, t.getBegin(), t.getEnd());
                // To save memory, we typically intern() tag strings
                posAnno.setPosValue(tag != null ? tag.intern() : null);
                POSUtils.assignCoarseValue(posAnno);
                posAnno.addToIndexes();
// end::mapping-provider-use-2[]
                
                // Connect the POS annotation to the respective token annotation
                t.setPos(posAnno);
                i++;
            }
        }
    }

    private void fixEncoding(String[] aTokenTexts)
        throws AnalysisEngineProcessException
    {
        // "Fix" encoding before passing to a model which was trained with encoding problems
        if (encoding != null && !"UTF-8".equals(encoding.name())) {
            for (int i = 0; i < aTokenTexts.length; i++) {
                aTokenTexts[i] = new String(aTokenTexts[i].getBytes(StandardCharsets.UTF_8),
                        encoding);
            }
        }
    }
}
