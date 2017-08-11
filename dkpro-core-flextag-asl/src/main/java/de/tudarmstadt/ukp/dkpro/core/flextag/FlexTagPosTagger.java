/**
 * Copyright 2007-2017
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
package de.tudarmstadt.ukp.dkpro.core.flextag;

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
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.ml.uima.TcAnnotator;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.pos.POSUtils;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Flexible part-of-speech tagger.
 */
@ResourceMetaData(name="FlexTag POS-Tagger")
public class FlexTagPosTagger
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    private String variant;
    
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    private String posMappingLocation;
    

    private AnalysisEngine flexTagEngine = null;
    private ModelProviderBase<File> modelProvider = null;
    private MappingProvider mappingProvider=null;

    @Override
    public void initialize(final UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        initModelProvider();
        mappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
                language, modelProvider);

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
            String outcome = outcomes.get(i).getOutcome();

            POS p = createPartOfSpeechAnnotationFromOutcome(aJCas, token.getBegin(),
                    token.getEnd(), outcome);
            token.setPos(p);
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

                setDefault(ARTIFACT_ID, "${groupId}.flextag-model-${language}-${variant}");
                setDefault(LOCATION,
                        "classpath:/${package}/lib/tagger-${language}-${variant}.properties");

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
