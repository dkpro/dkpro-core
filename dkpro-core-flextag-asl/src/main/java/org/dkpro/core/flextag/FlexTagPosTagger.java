/**
 * Copyright 2007-2023
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
package org.dkpro.core.flextag;

import static org.dkpro.core.api.resources.MappingProviderFactory.createPosMappingProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.lexmorph.pos.POSUtils;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.api.resources.ResourceUtils;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.ml.uima.TcAnnotator;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Flexible part-of-speech tagger.
 */
@Component(OperationType.PART_OF_SPEECH_TAGGER)
@ResourceMetaData(name = "FlexTag POS-Tagger")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class FlexTagPosTagger
    extends JCasAnnotator_ImplBase
{
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
     * Location from which the model is read. This is either a local path or a classpath location.
     * In the latter case, the model artifact (if any) is searched as well.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    /**
     * Use this language instead of the document language to resolve the model and tag set mapping.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    /**
     * Variant of a model the model. Used to address a specific model if here are multiple models
     * for one language.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    private String variant;
    
    /**
     * Enable/disable type mapping.
     */
    public static final String PARAM_MAPPING_ENABLED = ComponentParameters.PARAM_MAPPING_ENABLED;
    @ConfigurationParameter(name = PARAM_MAPPING_ENABLED, mandatory = true, defaultValue = 
            ComponentParameters.DEFAULT_MAPPING_ENABLED)
    protected boolean mappingEnabled;

    /**
     * Location of the mapping file for part-of-speech tags to UIMA types.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = 
            ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    private String posMappingLocation;

    private AnalysisEngine flexTagEngine = null;
    private ModelProviderBase<File> modelProvider = null;
    private MappingProvider mappingProvider = null;

    @Override
    public void initialize(final UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        initModelProvider();
        
        mappingProvider = createPosMappingProvider(this, posMappingLocation, language,
                modelProvider);

        flexTagEngine = AnalysisEngineFactory.createEngine(TcAnnotator.class,
                TcAnnotator.PARAM_TC_MODEL_LOCATION, modelProvider.getResource(),
                TcAnnotator.PARAM_NAME_SEQUENCE_ANNOTATION, Sentence.class.getName(),
                TcAnnotator.PARAM_NAME_UNIT_ANNOTATION, Token.class.getName());
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        mappingProvider.configure(aJCas.getCas());
        
        flexTagEngine.process(aJCas);

        annotateTaggingResultsLinkToTokens(aJCas);
    }

    private void annotateTaggingResultsLinkToTokens(JCas aJCas)
    {
        List<Token> tokens = getTokens(aJCas);
        List<TextClassificationOutcome> outcomes = getPredictions(aJCas);

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            TextClassificationOutcome outcome = outcomes.get(i);
            String posTag = outcome.getOutcome();

            POS p = createPartOfSpeechAnnotationFromOutcome(aJCas, token.getBegin(),
                    token.getEnd(), posTag);
            token.setPos(p);
            outcome.removeFromIndexes(aJCas);
        }

    }

    private POS createPartOfSpeechAnnotationFromOutcome(JCas aJCas, int begin, int end,
            String aOutcome)
    {
        Type posTag = mappingProvider.getTagType(aOutcome);
        POS posAnno = (POS) aJCas.getCas().createAnnotation(posTag, begin, end);
        posAnno.setPosValue(aOutcome);
        POSUtils.assignCoarseValue(posAnno);
        posAnno.addToIndexes();
        
        return posAnno;

    }

    private List<TextClassificationOutcome> getPredictions(JCas aJCas)
    {
        return new ArrayList<TextClassificationOutcome>(JCasUtil.select(aJCas,
                TextClassificationOutcome.class));
    }

    private List<Token> getTokens(JCas aJCas)
    {
        return new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
    }

    private void initModelProvider()
        throws ResourceInitializationException
    {
        modelProvider = new ModelProviderBase<File>()
        {
            {
                setContextObject(FlexTagPosTagger.this);

                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(ARTIFACT_ID, "${groupId}.flextag-model-${language}-${variant}");
                setDefault(LOCATION,
                        "classpath:/de/tudarmstadt/ukp/dkpro/core/flextag/lib/tagger-${language}-${variant}.properties");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected File produceResource(URL aUrl)
                throws IOException
            {
                File folder = ResourceUtils.getClasspathAsFolder(aUrl.toString(), true);
                return folder;
            }
        };
        try {
            modelProvider.configure();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }
}
