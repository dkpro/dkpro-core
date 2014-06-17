/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.apache.uima.util.Level.INFO;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.Tagset;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableStreamProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.opennlp.internal.OpenNlpTagsetDescriptionProvider;

/**
 * OpenNLP name finder wrapper.
 */
public class OpenNlpNameFinder
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
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = true, defaultValue="person")
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

    private CasConfigurableProviderBase<TokenNameFinder> modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new CasConfigurableStreamProviderBase<TokenNameFinder>()
        {
            {
                setContextObject(OpenNlpNameFinder.this);

                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(ARTIFACT_ID,
                        "de.tudarmstadt.ukp.dkpro.core.opennlp-model-ner-${language}-${variant}");

                setDefaultVariantsLocation("de/tudarmstadt/ukp/dkpro/core/opennlp/lib/ner-default-variants.map");
                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/"
                        + "ner-${language}-${variant}.bin");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected TokenNameFinder produceResource(InputStream aStream)
                throws Exception
            {
                TokenNameFinderModel model = new TokenNameFinderModel(aStream);

                if (printTagSet) {
                    Tagset tsdp = new OpenNlpTagsetDescriptionProvider(
                            null, NamedEntity.class, model.getNameFinderModel());
                    getContext().getLogger().log(INFO, tsdp.toString());
                }

                return new NameFinderME(model);
            }
        };

        mappingProvider = new MappingProvider();
        mappingProvider
                .setDefaultVariantsLocation("de/tudarmstadt/ukp/dkpro/core/opennlp/lib/ner-default-variants.map");
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/"
                + "core/opennlp/lib/ner-${language}-${variant}.map");
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

        modelProvider.getResource().clearAdaptiveData();

        // get the document text
        List<Token> tokenList = new ArrayList<Token>(select(aJCas, Token.class));
        String[] tokens = toText(tokenList).toArray(new String[tokenList.size()]);

        // test the string
        Span[] namedEntities = modelProvider.getResource().find(tokens);

        // get the named entities and their character offsets
        for (Span namedEntity : namedEntities) {
            int begin = tokenList.get(namedEntity.getStart()).getBegin();
            int end = tokenList.get(namedEntity.getEnd()-1).getEnd();

            Type type = mappingProvider.getTagType(namedEntity.getType());
            NamedEntity neAnno = (NamedEntity) cas.createAnnotation(type, begin, end);
            neAnno.setValue(namedEntity.getType());
            neAnno.addToIndexes();
        }
    }
}
