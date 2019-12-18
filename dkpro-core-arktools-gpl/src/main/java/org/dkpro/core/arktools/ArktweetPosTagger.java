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
package org.dkpro.core.arktools;

import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.parameter.ResourceParameter;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.MappingProviderFactory;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.api.resources.ResourceUtils;

import cmu.arktweetnlp.Twokenize;
import cmu.arktweetnlp.impl.Model;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;
import cmu.arktweetnlp.impl.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;


/**
 * Wrapper for Twitter Tokenizer and POS Tagger.
 *
 * As described in: Olutobi Owoputi, Brendan O’Connor, Chris Dyer, Kevin Gimpel, Nathan Schneider
 * and Noah A. Smith. Improved Part-of-Speech Tagging for Online Conversational Text with Word
 * Clusters In Proceedings of NAACL 2013.
 */
@Component(OperationType.POS_TAGGING)
@ResourceMetaData(name = "ArkTweet POS-Tagger")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
      inputs = { 
          "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" }, 
      outputs = { 
          "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class ArktweetPosTagger
    extends JCasAnnotator_ImplBase
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
     * Location from which the model is read.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    @ResourceParameter(MimeTypes.APPLICATION_X_ARKTWEET_TAGGER)
    protected String modelLocation;

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
    protected String posMappingLocation;

    private CasConfigurableProviderBase<TweetTagger> modelProvider;
    private MappingProvider mappingProvider;

    /**
     * Loads a model from a file. The tagger should be ready to tag after calling this.
     */
    public class TweetTagger
    {
        Model model;
        FeatureExtractor featureExtractor;

        public void loadModel(String modelFilename)
            throws IOException
        {
            model = Model.loadModelFromText(modelFilename);
            featureExtractor = new FeatureExtractor(model, false);
        }
    }

    /**
     * One token and its tag.
     **/
    public static class TaggedToken
    {
        public Token token;
        public String tag;

        private int getBegin() {
            return token.getBegin();
        }

        private int getEnd() {
            return token.getEnd();
        }
    }

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        modelProvider = new ModelProviderBase<TweetTagger>()
        {
            {
                setContextObject(ArktweetPosTagger.this);

                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(ARTIFACT_ID, "${groupId}.arktools-model-tagger-${language}-${variant}");
                setDefault(LOCATION,
                        "classpath:/de/tudarmstadt/ukp/dkpro/core/arktools/lib/tagger-${language}-${variant}.properties");
                setDefault(VARIANT, "default");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected TweetTagger produceResource(URL aUrl)
                throws IOException
            {
                try {
                    TweetTagger model = new TweetTagger();
                    model.loadModel(ResourceUtils.getUrlAsFile(aUrl, false).getAbsolutePath());

                    return model;
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };

        mappingProvider = MappingProviderFactory.createPosMappingProvider(this, posMappingLocation,
                language, modelProvider);

    }

    @Override
    public void process(JCas jCas)
        throws AnalysisEngineProcessException
    {

        modelProvider.configure(jCas.getCas());
        mappingProvider.configure(jCas.getCas());

        List<Token> tokens = selectCovered(jCas, Token.class, 0, jCas.getDocumentText().length());
        List<TaggedToken> taggedTokens = tagTweetTokens(tokens, modelProvider.getResource());

        for (TaggedToken taggedToken : taggedTokens) {

            Type posType = mappingProvider.getTagType(taggedToken.tag);

            POS pos = (POS) jCas.getCas().createAnnotation(posType, taggedToken.getBegin(),
                    taggedToken.getEnd());
            pos.setPosValue(taggedToken.tag.intern());
            pos.addToIndexes();
            taggedToken.token.setPos(pos);
        }
    }

    private List<TaggedToken> tagTweetTokens(List<Token> annotatedTokens,
            TweetTagger tweetTagModel)
    {

        List<String> tokens = new LinkedList<String>();
        for (Token a : annotatedTokens) {
            String tokenText = a.getText();
            tokenText = Twokenize.normalizeTextForTagger(tokenText);
            tokens.add(tokenText);
        }

        Sentence sentence = new Sentence();
        sentence.tokens = tokens;
        ModelSentence ms = new ModelSentence(sentence.T());
        tweetTagModel.featureExtractor.computeFeatures(sentence, ms);
        tweetTagModel.model.greedyDecode(ms, false);

        ArrayList<TaggedToken> taggedTokens = new ArrayList<TaggedToken>();

        for (int t = 0; t < sentence.T(); t++) {
            TaggedToken tt = new TaggedToken();
            tt.token = annotatedTokens.get(t);
            tt.tag = tweetTagModel.model.labelVocab.name(ms.labels[t]);
            taggedTokens.add(tt);
        }
        return taggedTokens;
    }
}
