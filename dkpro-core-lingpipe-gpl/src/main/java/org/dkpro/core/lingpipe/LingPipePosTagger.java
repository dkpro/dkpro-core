/*
 * Copyright 2007-2019
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
package org.dkpro.core.lingpipe;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.apache.uima.util.Level.INFO;
import static org.dkpro.core.api.resources.MappingProviderFactory.createPosMappingProvider;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Locale;

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
import org.dkpro.core.api.metadata.SingletonTagset;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.ModelProviderBase;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * LingPipe part-of-speech tagger.
 */
@Component(OperationType.PART_OF_SPEECH_TAGGER)
@ResourceMetaData(name = "LingPipe POS-Tagger")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class LingPipePosTagger
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

    /**
     * Lingpipe models tend to be trained on lower-case tags, but our POS mappings use uppercase.
     */
    public static final String PARAM_UPPERCASE_TAGS = "uppercaseTags";
    @ConfigurationParameter(name = PARAM_UPPERCASE_TAGS, mandatory = true, defaultValue = "true")
    protected boolean uppercaseTags;
    
    private CasConfigurableProviderBase<HmmDecoder> modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<HmmDecoder>(this, "lingpipe", "tagger") {
            {
                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(LOCATION,
                        "classpath:/de/tudarmstadt/ukp/dkpro/core/lingpipe/lib/tagger-${language}-${variant}.properties");
            }
            
            @Override
            protected HmmDecoder produceResource(InputStream aStream)
                throws Exception
            {
                ObjectInputStream ois = new ObjectInputStream(aStream);
                HiddenMarkovModel hmm = (HiddenMarkovModel) ois.readObject();

                SingletonTagset tags = new SingletonTagset(POS.class, getResourceMetaData()
                        .getProperty(("pos.tagset")));
                for (int n = 0; n < hmm.stateSymbolTable().numSymbols(); n++) {
                    String tag = hmm.stateSymbolTable().idToSymbol(n);
                    if (uppercaseTags) {
                        tag = tag.toUpperCase(Locale.US);
                    }
                    tags.add(tag);
                }
                addTagset(tags);
                
                if (printTagSet) {
                    getContext().getLogger().log(INFO, getTagset().toString());
                }

                return new HmmDecoder(hmm);
            }
        };

        mappingProvider = createPosMappingProvider(this, posMappingLocation, language,
                modelProvider);
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
            String[] tokenTexts = toText(tokens).toArray(new String[tokens.size()]);

            Tagging<String> tagging = modelProvider.getResource().tag(asList(tokenTexts));

            for (int n = 0; n < tagging.size(); n++) {
                Token t = tokens.get(n);
                String tag = tagging.tag(n);
                if (uppercaseTags) {
                    tag = tag.toUpperCase(Locale.US);
                }
                Type posTag = mappingProvider.getTagType(tag);
                POS posAnno = (POS) cas.createAnnotation(posTag, t.getBegin(), t.getEnd());
                posAnno.setPosValue(tag != null ? tag.intern() : null);
                POSUtils.assignCoarseValue(posAnno);
                posAnno.addToIndexes();
                t.setPos(posAnno);
            }
        }
    }
}
