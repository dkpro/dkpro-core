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
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.util.Level.INFO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

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

import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ResourceParameter;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CoreNlpUtils;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.util.CoreMap;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Stanford Named Entity Recognizer component.
 */
@Component(OperationType.NAMED_ENTITITY_RECOGNIZER)
@ResourceMetaData(name = "CoreNLP Named Entity Recogizer (old API)")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
public class StanfordNamedEntityRecognizer
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
    @ResourceParameter(MimeTypes.APPLICATION_X_STANFORDNLP_NER)
    protected String modelLocation;

    /**
     * Location of the mapping file for named entity tags to UIMA types.
     */
    public static final String PARAM_NAMED_ENTITY_MAPPING_LOCATION = 
            ComponentParameters.PARAM_NAMED_ENTITY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_MAPPING_LOCATION, mandatory = false)
    protected String mappingLocation;

    /**
     * Enable all traditional PTB3 token transforms (like -LRB-, -RRB-).
     *
     * @see PTBEscapingProcessor
     */
    public static final String PARAM_PTB3_ESCAPING = "ptb3Escaping";
    @ConfigurationParameter(name = PARAM_PTB3_ESCAPING, mandatory = true, defaultValue = "true")
    private boolean ptb3Escaping;
    
    /**
     * List of extra token texts (usually single character strings) that should be treated like
     * opening quotes and escaped accordingly before being sent to the parser.
     */
    public static final String PARAM_QUOTE_BEGIN = "quoteBegin";
    @ConfigurationParameter(name = PARAM_QUOTE_BEGIN, mandatory = false)
    private List<String> quoteBegin;

    /**
     * List of extra token texts (usually single character strings) that should be treated like
     * closing quotes and escaped accordingly before being sent to the parser.
     */
    public static final String PARAM_QUOTE_END = "quoteEnd";
    @ConfigurationParameter(name = PARAM_QUOTE_END, mandatory = false)
    private List<String> quoteEnd;
    
    private StanfordNlpNamedEntityRecognizerModelProvider modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new StanfordNlpNamedEntityRecognizerModelProvider(this);

        mappingProvider = new MappingProvider();
        mappingProvider
                .setDefaultVariantsLocation("de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/ner-default-variants.map");
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/"
                + "core/stanfordnlp/lib/ner-${language}-${variant}.map");
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, NamedEntity.class.getName());
        mappingProvider.setOverride(MappingProvider.LOCATION, mappingLocation);
        mappingProvider.setOverride(MappingProvider.LANGUAGE, language);
        mappingProvider.setOverride(MappingProvider.VARIANT, variant);
        mappingProvider.addTagMappingImport("ner", modelProvider);
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

            List<HasWord> words = new ArrayList<>(tokens.size());
            for (Token t : tokens) {
                words.add(CoreNlpUtils.tokenToWord(t));
            }
            
            if (ptb3Escaping) {
                words = CoreNlpUtils.applyPtbEscaping(words, quoteBegin, quoteEnd);
            }
            
            List<CoreMap> taggedWords = modelProvider.getResource().classifySentence(words);

            int entityBegin = -1;
            int entityEnd   = -1;
            String entityType = null;
            
            for (CoreMap t : taggedWords) {
                String tokenType = mappingProvider
                        .getTag(t.get(CoreAnnotations.AnswerAnnotation.class));
                
                // If an entity is currently open, then close it
                if ("O".equals(tokenType) || !tokenType.equals(entityType)) {
                    if (entityType != null) {
                        Type type = mappingProvider.getTagType(entityType);
                        NamedEntity neAnno = (NamedEntity) cas.createAnnotation(type, entityBegin,
                                entityEnd);
                        neAnno.setValue(entityType);
                        neAnno.addToIndexes();
                        entityType = null;
                    }
                }
                
                // If a new entity starts or continues, track it
                if (!"O".equals(tokenType)) {
                    if (entityType == null) {
                        entityType = tokenType;
                        entityBegin = t.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    }
                    entityEnd = t.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                }
            }
            // If the last entity is still open, then close it
            if (entityType != null) {
                Type type = mappingProvider.getTagType(entityType);
                NamedEntity neAnno = (NamedEntity) cas.createAnnotation(type, entityBegin,
                        entityEnd);
                neAnno.setValue(entityType);
                neAnno.addToIndexes();
            }
        }
    }
    
    private class StanfordNlpNamedEntityRecognizerModelProvider
        extends ModelProviderBase<AbstractSequenceClassifier<CoreMap>>
    {
        public StanfordNlpNamedEntityRecognizerModelProvider(Object aObject)
        {
            super(aObject, "stanfordnlp", "ner");
            // setDefault(PACKAGE, "de/tudarmstadt/ukp/dkpro/core/stanfordnlp");
            setDefault(LOCATION,
                    "classpath:/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/ner-${language}-${variant}.properties");
        }
        
        @Override
        protected AbstractSequenceClassifier<CoreMap> produceResource(URL aUrl)
            throws IOException
        {
            Properties metadata = getResourceMetaData();
            
            InputStream is = null;
            try {
                is = aUrl.openStream();
                if (aUrl.toString().endsWith(".gz")) {
                    // it's faster to do the buffering _outside_ the gzipping as here
                    is = new GZIPInputStream(is);
                }

                AbstractSequenceClassifier<CoreMap> classifier = 
                        (AbstractSequenceClassifier<CoreMap>) CRFClassifier.getClassifier(is);

                String tagsetName = metadata.getProperty("ner.tagset");
                if (tagsetName == null) {
                    tagsetName = "unknown";
                }
                
                SingletonTagset tsdp = new SingletonTagset(NamedEntity.class, tagsetName);
                for (String tag : classifier.classIndex) {
                    String mapped = metadata.getProperty("ner.tag.map." + tag);
                    String finalTag = mapped != null ? mapped : tag;
                    
                    // "O" has a special meaning in the CRF-NER: not a named entity
                    if (!"O".equals(finalTag)) {
                        tsdp.add(finalTag);
                    }
                }
                addTagset(tsdp);
                
                if (printTagSet) {
                    getContext().getLogger().log(INFO, tsdp.toString());
                }
                
                return classifier;
            }
            catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
            finally {
                closeQuietly(is);
            }
        }
    }
}
