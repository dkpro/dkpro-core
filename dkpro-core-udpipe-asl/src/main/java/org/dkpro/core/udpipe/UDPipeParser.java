/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.udpipe;

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.api.resources.MappingProviderFactory.createDependencyMappingProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.api.resources.ResourceUtils;
import org.dkpro.core.udpipe.internal.DKPro2UDPipe;
import org.dkpro.core.udpipe.internal.UDPipe2DKPro;
import org.dkpro.core.udpipe.internal.UDPipeUtils;

import cz.cuni.mff.ufal.udpipe.Model;
import cz.cuni.mff.ufal.udpipe.ProcessingError;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Dependency parser using UDPipe. UDPipe uses Parsito, a greedy transition-based parser
 * utilizing an artificial neural network.
 * 
 * @see <a href="https://github.com/ufal/udpipe/tree/master/src/parsito">Parsito in UDPipe</a>
 */
@Component(OperationType.DEPENDENCY_PARSER)
@ResourceMetaData(name = "UDPipe Parsito Dependency Parser")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence", 
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures"},
        outputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"})
public class UDPipeParser
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

    /**
     * Enable/disable type mapping.
     */
    public static final String PARAM_MAPPING_ENABLED = ComponentParameters.PARAM_MAPPING_ENABLED;
    @ConfigurationParameter(name = PARAM_MAPPING_ENABLED, mandatory = true, defaultValue = 
            ComponentParameters.DEFAULT_MAPPING_ENABLED)
    protected boolean mappingEnabled;

    /**
     * Load the dependency to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_DEPENDENCY_MAPPING_LOCATION = 
            ComponentParameters.PARAM_DEPENDENCY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_DEPENDENCY_MAPPING_LOCATION, mandatory = false)
    protected String dependencyMappingLocation;

    private ModelProviderBase<Model> modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<Model>()
        {
            {
                setContextObject(UDPipeParser.this);
                setDefault(LOCATION, "classpath:/org/dkpro/core/udpipe/lib/" +
                        "parser-${language}-${variant}.properties");
                setDefault(VARIANT, "ud");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected Model produceResource(URL aUrl)
                throws IOException
            {
                UDPipeUtils.init();
                
                File modelFile = ResourceUtils.getUrlAsFile(aUrl, true);
                return Model.load(modelFile.getAbsolutePath());
            }
        };
        
        mappingProvider = createDependencyMappingProvider(this, dependencyMappingLocation, language,
                modelProvider);
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
        
        Map<Sentence, Collection<Token>> index = indexCovered(aJCas, Sentence.class, Token.class);
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<Token> tokens = new ArrayList<>(index.get(sentence));
            
            cz.cuni.mff.ufal.udpipe.Sentence udSent = new cz.cuni.mff.ufal.udpipe.Sentence();
            DKPro2UDPipe.convert(tokens, udSent);
            
            ProcessingError error = new ProcessingError();
            modelProvider.getResource().parse(udSent, Model.getDEFAULT(), error);
            if (error.occurred()) {
                throw new AnalysisEngineProcessException(
                        new IllegalStateException(error.getMessage()));
            }

            UDPipe2DKPro.convertParse(udSent, tokens, aJCas, mappingProvider);
        }
    }
}
