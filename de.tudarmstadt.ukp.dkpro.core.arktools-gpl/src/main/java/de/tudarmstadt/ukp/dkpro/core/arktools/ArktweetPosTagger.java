/**
 * Copyright 2007-2014
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.core.arktools;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;
import cmu.arktweetnlp.Twokenize;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Wrapper for Twitter Tokenizer and POS Tagger.

 * As described in:
 * Olutobi Owoputi, Brendan O’Connor, Chris Dyer, Kevin Gimpel, Nathan Schneider and Noah A. Smith.
 * Improved Part-of-Speech Tagging for Online Conversational Text with Word Clusters
 * In Proceedings of NAACL 2013. 
 *
 * @author zesch
 *
 */
public class ArktweetPosTagger
    extends CasAnnotator_ImplBase
{

    /**
     * Use this language instead of the document language to resolve the model and tag set mapping.
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
     * Location of the mapping file for part-of-speech tags to UIMA types.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    
    private Type tokenType;
    private Feature featPos;

    private CasConfigurableProviderBase<Tagger> modelProvider;
    private MappingProvider mappingProvider;
   
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        modelProvider = new ModelProviderBase<Tagger>() {
            {
                setContextObject(ArktweetPosTagger.this);

                setDefault(ARTIFACT_ID,
                        "${groupId}.arktools-model-tagger-${language}-${variant}");
                setDefault(LOCATION, "classpath:/${package}/lib/tagger-${language}-${variant}.properties");
                setDefault(VARIANT, "default");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }


            @Override
            protected Tagger produceResource(URL aUrl) throws IOException
            {
                try {
                    Tagger tagger = new Tagger();
                    tagger.loadModel(ResourceUtils.getUrlAsFile(aUrl, false).getAbsolutePath());
                    
                    return tagger;
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };

        mappingProvider = new MappingProvider();
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
                "core/api/lexmorph/tagset/en-arktweet.map");
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        mappingProvider.setDefault("pos.tagset", "arktweet");
    }

    @Override
    public void typeSystemInit(TypeSystem aTypeSystem)
        throws AnalysisEngineProcessException
    {
        super.typeSystemInit(aTypeSystem);

        tokenType = aTypeSystem.getType(Token.class.getName());
        featPos = tokenType.getFeatureByBaseName("pos");
    }

    @Override
    public void process(CAS cas) throws AnalysisEngineProcessException {

        String text = cas.getDocumentText();
        // possibly normalized text as used inside ArktweetTagger
        String normalizedText = Twokenize.normalizeTextForTagger(text);

        mappingProvider.configure(cas);
        modelProvider.configure(cas);

        List<TaggedToken> taggedTokens = modelProvider.getResource().tokenizeAndTag(text);

        int start = 0;
        int end = 0;
        int searchOffset = 0;
        for (TaggedToken taggedToken : taggedTokens) {
            String token = taggedToken.token;
            String tag = taggedToken.tag;
            
            int tokenOffset = text.indexOf(token, searchOffset);
            int normalizedOffset = normalizedText.indexOf(token, searchOffset);
          
            // the token cannot be found in the original text
            // i.e. it has been normalized
            // we need to find the replaced text
            if (tokenOffset == -1) {
                int ampersandOffset = text.indexOf("&", searchOffset);
                int semicolonOffset = text.indexOf(";", searchOffset);

                start = normalizedOffset;
                end = normalizedOffset + token.length() + (semicolonOffset - ampersandOffset);
            } 
            else {
                start = tokenOffset;
                end = tokenOffset + token.length();
            }            
            
            Type posType = mappingProvider.getTagType(tag);

            AnnotationFS posAnno = cas.createAnnotation(posType, start, end);
            posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"), tag);
            cas.addFsToIndexes(posAnno);

            AnnotationFS tokenAnno = cas.createAnnotation(tokenType, start, end);
            tokenAnno.setFeatureValue(featPos, posAnno);
            cas.addFsToIndexes(tokenAnno);

            searchOffset = end;
        }
    }
}