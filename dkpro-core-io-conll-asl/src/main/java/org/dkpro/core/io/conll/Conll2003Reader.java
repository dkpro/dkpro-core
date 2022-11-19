/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.conll;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.dkpro.core.api.resources.MappingProviderFactory.createChunkMappingProvider;
import static org.dkpro.core.api.resources.MappingProviderFactory.createPosMappingProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.IobDecoder;
import org.dkpro.core.api.lexmorph.pos.POSUtils;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.CompressionUtils;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.io.conll.internal.ConllReader_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reads the CoNLL 2003 format.
 * 
 * @see <a href="http://www.cnts.ua.ac.be/conll2003/ner/">CoNLL 2003 shared task</a>
 */
@ResourceMetaData(name = "CoNLL 2003 Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.TEXT_X_CONLL_2003})
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk",
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
public class Conll2003Reader
    extends ConllReader_ImplBase
{
    private static final int FORM = 0;
    private static final int POSTAG = 1;
    private static final int CHUNK = 2;
    private static final int NAMED_ENTITY = 3;

    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String sourceEncoding;

    /**
     * Read part-of-speech information.
     */
    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "true")
    private boolean posEnabled;

    /**
     * Use this part-of-speech tag set to use to resolve the tag set mapping instead of using the
     * tag set defined as part of the model meta data. This can be useful if a custom model is
     * specified which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_POS_TAG_SET = ComponentParameters.PARAM_POS_TAG_SET;
    @ConfigurationParameter(name = PARAM_POS_TAG_SET, mandatory = false)
    protected String posTagset;

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
     * Read chunk information.
     */
    public static final String PARAM_READ_CHUNK = ComponentParameters.PARAM_READ_CHUNK;
    @ConfigurationParameter(name = PARAM_READ_CHUNK, mandatory = true, defaultValue = "true")
    private boolean chunkEnabled;

    /**
     * Use this chunk tag set to use to resolve the tag set mapping instead of using the
     * tag set defined as part of the model meta data. This can be useful if a custom model is
     * specified which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_CHUNK_TAG_SET = ComponentParameters.PARAM_CHUNK_TAG_SET;
    @ConfigurationParameter(name = PARAM_CHUNK_TAG_SET, mandatory = false)
    protected String chunkTagset;
    
    /**
     * Load the chunk tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_CHUNK_MAPPING_LOCATION = 
            ComponentParameters.PARAM_CHUNK_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CHUNK_MAPPING_LOCATION, mandatory = false)
    protected String chunkMappingLocation;
    
    /**
     * Read named entity information.
     */
    public static final String PARAM_READ_NAMED_ENTITY = 
            ComponentParameters.PARAM_READ_NAMED_ENTITY;
    @ConfigurationParameter(name = PARAM_READ_NAMED_ENTITY, mandatory = true, defaultValue = "true")
    private boolean namedEntityEnabled;

    /**
     * Location of the mapping file for named entity tags to UIMA types.
     */
    public static final String PARAM_NAMED_ENTITY_MAPPING_LOCATION = 
            ComponentParameters.PARAM_NAMED_ENTITY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_MAPPING_LOCATION, mandatory = false)
    private String namedEntityMappingLocation;

    private MappingProvider posMappingProvider;
    private MappingProvider chunkMappingProvider;
    private MappingProvider namedEntityMappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        posMappingProvider = createPosMappingProvider(this, posMappingLocation, posTagset,
                getLanguage());

        chunkMappingProvider = createChunkMappingProvider(this, chunkMappingLocation, chunkTagset,
                getLanguage());
        
        namedEntityMappingProvider = new MappingProvider();
        namedEntityMappingProvider.setDefault(MappingProvider.LOCATION,
                "classpath:/there/is/no/mapping/yet");
        namedEntityMappingProvider.setDefault(MappingProvider.BASE_TYPE,
                NamedEntity.class.getName());
        namedEntityMappingProvider.setOverride(MappingProvider.LOCATION,
                namedEntityMappingLocation);
        namedEntityMappingProvider.setOverride(MappingProvider.LANGUAGE, getLanguage());
    }
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        try {
            if (posEnabled) {
                posMappingProvider.configure(aJCas.getCas());
            }
            if (chunkEnabled) {
                chunkMappingProvider.configure(aJCas.getCas());
            }
            if (namedEntityEnabled) {
                namedEntityMappingProvider.configure(aJCas.getCas());
            }
        }
        catch (AnalysisEngineProcessException e) {
            throw new IOException(e);
        }
        
        Resource res = nextFile();
        initCas(aJCas, res);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    CompressionUtils.getInputStream(res.getLocation(), res.getInputStream()),
                    sourceEncoding));
            convert(aJCas, reader);
        }
        finally {
            closeQuietly(reader);
        }
    }

    private void convert(JCas aJCas, BufferedReader aReader)
        throws IOException
    {
        JCasBuilder doc = new JCasBuilder(aJCas);

        Type chunkType = JCasUtil.getType(aJCas, Chunk.class);
        Feature chunkValue = chunkType.getFeatureByBaseName("chunkValue");
        IobDecoder chunkDecoder = new IobDecoder(aJCas.getCas(), chunkValue, chunkMappingProvider);
        
        Type namedEntityType = JCasUtil.getType(aJCas, NamedEntity.class);
        Feature namedEntityValue = namedEntityType.getFeatureByBaseName("value");
        IobDecoder neDecoder = new IobDecoder(aJCas.getCas(), namedEntityValue,
                namedEntityMappingProvider);
        
        List<String[]> words;
        while ((words = readSentence(aReader)) != null) {
            if (words.isEmpty()) {
                continue;
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            List<Token> tokens = new ArrayList<Token>();
            String[] chunkTags = new String[words.size()];
            String[] namedEntityTags = new String[words.size()];
            
            // Tokens, POS
            int i = 0;
            for (String[] word : words) {
                // Read token
                Token token = doc.add(trim(word[FORM]), Token.class);
                sentenceEnd = token.getEnd();
                doc.add(" ");
                
                if (posEnabled) {
                    String posTagValue = cleanTag(word[POSTAG]);
                    
                    Type posTag = posMappingProvider.getTagType(posTagValue);
                    POS pos = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(),
                            token.getEnd());
                    pos.setPosValue(posTagValue);
                    POSUtils.assignCoarseValue(pos);
                    pos.addToIndexes();
                    token.setPos(pos);
                }
                
                tokens.add(token);
                chunkTags[i] = cleanTag(word[CHUNK]);
                namedEntityTags[i] = cleanTag(word[NAMED_ENTITY]);
                i++;
            }
            
            if (chunkEnabled) {
                chunkDecoder.decode(tokens, chunkTags);
            }

            if (namedEntityEnabled) {
                neDecoder.decode(tokens, namedEntityTags);
            }
            
            // Sentence
            Sentence sentence = new Sentence(aJCas, sentenceBegin, sentenceEnd);
            sentence.addToIndexes();

            // Once sentence per line.
            doc.add("\n");
        }

        doc.close();
    }

    /**
     * Read a single sentence.
     */
    private static List<String[]> readSentence(BufferedReader aReader)
        throws IOException
    {
        List<String[]> words = new ArrayList<String[]>();
        String line;
        while ((line = aReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                break; // End of sentence
            }
            String[] fields = line.split(" ");
            if (fields.length != 4) {
                throw new IOException(
                        "Invalid file format. Line needs to have 4 space-separted fields: [" + line
                                + "]");
            }
            words.add(fields);
        }

        if (line == null && words.isEmpty()) {
            return null;
        }
        else {
            return words;
        }
    }
}
