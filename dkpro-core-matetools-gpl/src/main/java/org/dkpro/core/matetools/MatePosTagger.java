/*
 * Copyright 2007-2018
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.matetools;

import static java.util.Arrays.asList;
import static org.apache.uima.util.Level.INFO;
import static org.dkpro.core.api.resources.MappingProviderFactory.createPosMappingProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.lexmorph.pos.POSUtils;
import org.dkpro.core.api.metadata.SingletonTagset;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.api.resources.ResourceUtils;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.tag.Options;
import is2.tag.Tagger;

/**
 * DKPro Annotator for the MateToolsPosTagger
 */
@Component(OperationType.PART_OF_SPEECH_TAGGER)
@ResourceMetaData(name = "Mate Tools POS-Tagger")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"
        },
        outputs = {"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"})
public class MatePosTagger
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
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating the
     * mapping automatically.
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

    private CasConfigurableProviderBase<Tagger> modelProvider;
    private MappingProvider posMappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<Tagger>(this, "matetools", "tagger")
        {
            {
                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(LOCATION,
                        "classpath:/de/tudarmstadt/ukp/dkpro/core/matetools/lib/tagger-${language}-${variant}.properties");
            }
            
            @Override
            protected Tagger produceResource(URL aUrl)
                throws IOException
            {
                File modelFile = ResourceUtils.getUrlAsFile(aUrl, true);

                String[] args = { "-model", modelFile.getPath() };
                Options option = new Options(args);
                Tagger tagger = new Tagger(option); // create a POSTagger

                HashMap<String, HashMap<String, Integer>> featureSet = tagger.mf.getFeatureSet();
                SingletonTagset posTags = new SingletonTagset(POS.class, getResourceMetaData()
                        .getProperty("pos.tagset"));
                HashMap<String, Integer> posTagFeatures = featureSet.get("POS");
                posTags.addAll(posTagFeatures.keySet());
                posTags.removeAll(asList("<None>", "<root-POS>"));
                addTagset(posTags);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, getTagset().toString());
                }

                return tagger;
            }
        };

        posMappingProvider = createPosMappingProvider(this, posMappingLocation, language,
                modelProvider);
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        CAS cas = jcas.getCas();

        modelProvider.configure(cas);
        posMappingProvider.configure(cas);

        for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
            List<Token> tokens = JCasUtil.selectCovered(Token.class, sentence);

            List<String> forms = new LinkedList<String>();
            forms.add(CONLLReader09.ROOT);
            forms.addAll(JCasUtil.toText(tokens));

            List<String> lemmas = new LinkedList<String>();
            lemmas.add(CONLLReader09.ROOT_LEMMA);
            for (Token token : tokens) {
                if (token.getLemma() != null) {
                    lemmas.add(token.getLemma().getValue());
                } else {
                    lemmas.add("_");
                }
            }

            SentenceData09 sd = new SentenceData09();
            sd.init(forms.toArray(new String[0]));
            sd.setLemmas(lemmas.toArray(new String[0]));
            String[] posTags = modelProvider.getResource().apply(sd).ppos;

            for (int i = 1; i < posTags.length; i++) {
                Token token = tokens.get(i - 1);
                Type posType = posMappingProvider.getTagType(posTags[i]);
                POS posTag = (POS) cas.createAnnotation(posType, token.getBegin(), token.getEnd());
                posTag.setPosValue(posTags[i] != null ? posTags[i].intern() : null);
                POSUtils.assignCoarseValue(posTag);
                posTag.addToIndexes();
                token.setPos(posTag);
            }
        }
    }
}
