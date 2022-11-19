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
package org.dkpro.core.io.conll;

import static de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor.BASIC;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.dkpro.core.api.resources.MappingProviderFactory.createPosMappingProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.sequencecodec.AdjacentLabelCodec;
import org.dkpro.core.api.io.sequencecodec.SequenceItem;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.CompressionUtils;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.io.conll.internal.ConllReader_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reads files in the default CoreNLP CoNLL format.
 * 
 * @see <a href="https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/pipeline/CoNLLOutputter.html">CoreNLP CoNLLOutputter</a>
 */
@ResourceMetaData(name = "CoNLL CoreNLP Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.TEXT_X_CONLL_CORENLP})
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
public class ConllCoreNlpReader
    extends ConllReader_ImplBase
{
    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String sourceEncoding;

    /**
     * Read fine-grained part-of-speech information.
     */
    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "true")
    private boolean readPos;

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
     * Location of the mapping file for named entity tags to UIMA types.
     */
    public static final String PARAM_NAMED_ENTITY_MAPPING_LOCATION = 
            ComponentParameters.PARAM_NAMED_ENTITY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_MAPPING_LOCATION, mandatory = false)
    private String namedEntityMappingLocation;

    /**
     * Read morphological features.
     */
    public static final String PARAM_READ_NAMED_ENTITY = 
            ComponentParameters.PARAM_READ_NAMED_ENTITY;
    @ConfigurationParameter(name = PARAM_READ_NAMED_ENTITY, mandatory = true, defaultValue = "true")
    private boolean readNer;

    /**
     * Read lemma information.
     */
    public static final String PARAM_READ_LEMMA = ComponentParameters.PARAM_READ_LEMMA;
    @ConfigurationParameter(name = PARAM_READ_LEMMA, mandatory = true, defaultValue = "true")
    private boolean readLemma;

    /**
     * Read syntactic dependency information.
     */
    public static final String PARAM_READ_DEPENDENCY = ComponentParameters.PARAM_READ_DEPENDENCY;
    @ConfigurationParameter(name = PARAM_READ_DEPENDENCY, mandatory = true, defaultValue = "true")
    private boolean readDependency;

    private static final String UNUSED = "_";

    private static final int ID = 0;
    private static final int FORM = 1;
    private static final int LEMMA = 2;
    private static final int POSTAG = 3;
    private static final int NER = 4;
    private static final int HEAD = 5;
    private static final int DEPREL = 6;

    private MappingProvider posMappingProvider;
    private MappingProvider namedEntityMappingProvider;
    

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        posMappingProvider = createPosMappingProvider(this, posMappingLocation, posTagset,
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

    public void convert(JCas aJCas, BufferedReader aReader)
        throws IOException
    {
        if (readPos) {
            try {
                posMappingProvider.configure(aJCas.getCas());
            }
            catch (AnalysisEngineProcessException e) {
                throw new IOException(e);
            }
        }

        if (readNer) {
            try {
                namedEntityMappingProvider.configure(aJCas.getCas());
            }
            catch (AnalysisEngineProcessException e) {
                throw new IOException(e);
            }
        }

        JCasBuilder doc = new JCasBuilder(aJCas);

        List<String[]> words;
        while ((words = readSentence(aReader)) != null) {
            if (words.isEmpty()) {
                 // Ignore empty sentences. This can happen when there are multiple end-of-sentence
                 // markers following each other.
                continue; 
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            // Tokens, Lemma, POS
            Map<Integer, Token> tokens = new HashMap<Integer, Token>();
            Iterator<String[]> wordIterator = words.iterator();
            while (wordIterator.hasNext()) {
                String[] word = wordIterator.next();
                // Read token
                Token token = doc.add(trim(word[FORM]), Token.class);
                tokens.put(Integer.valueOf(trim(word[ID])), token);
                if (wordIterator.hasNext()) {
                    doc.add(" ");
                }

                // Read lemma
                String lemmaValue = trim(word[LEMMA]);
                if (!UNUSED.equals(lemmaValue) && readLemma) {
                    Lemma lemma = new Lemma(aJCas, token.getBegin(), token.getEnd());
                    lemma.setValue(lemmaValue);
                    lemma.addToIndexes();
                    token.setLemma(lemma);
                }

                // Read part-of-speech tag
                String tag = cleanTag(word[POSTAG]);
                if (!UNUSED.equals(tag) && readPos) {
                    Type posTag = posMappingProvider.getTagType(tag);
                    POS pos = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(),
                            token.getEnd());
                    pos.setPosValue(tag);
                    pos.addToIndexes();
                    token.setPos(pos);
                }

                sentenceEnd = token.getEnd();
            }

            // Read named entities
            if (readNer) {
                List<SequenceItem> encodedNerSpans = words.stream().map(w -> {
                    int id = Integer.valueOf(trim(w[ID]));
                    return new SequenceItem(id, id, trim(w[NER]));
                }).collect(Collectors.toList());
                
                AdjacentLabelCodec codec = new AdjacentLabelCodec(1);
                List<SequenceItem> decodedNerSpans = codec.decode(encodedNerSpans);
                                
                for (SequenceItem nerSpan : decodedNerSpans) {
                    Type nerType = namedEntityMappingProvider.getTagType(nerSpan.getLabel());
                    Token beginToken = tokens.get(nerSpan.getBegin());
                    Token endToken = tokens.get(nerSpan.getEnd());
                    NamedEntity ne = (NamedEntity) aJCas.getCas().createAnnotation(nerType,
                            beginToken.getBegin(), endToken.getEnd());
                    ne.setValue(cleanTag(nerSpan.getLabel()));
                    ne.addToIndexes();
                }
            }

            // Read dependencies
            if (readDependency) {
                for (String[] word : words) {
                    String depRel = cleanTag(word[DEPREL]);
                    if (!UNUSED.equals(depRel)) {
                        int depId = Integer.valueOf(trim(word[ID]));
                        int govId = Integer.valueOf(trim(word[HEAD]));
                        
                        // Model the root as a loop onto itself
                        if (govId == 0) {
                            Dependency rel = new ROOT(aJCas);
                            rel.setGovernor(tokens.get(depId));
                            rel.setDependent(tokens.get(depId));
                            rel.setDependencyType(depRel);
                            rel.setBegin(rel.getDependent().getBegin());
                            rel.setEnd(rel.getDependent().getEnd());
                            rel.setFlavor(BASIC);
                            rel.addToIndexes();
                        }
                        else {
                            Dependency rel = new Dependency(aJCas);
                            rel.setGovernor(tokens.get(govId));
                            rel.setDependent(tokens.get(depId));
                            rel.setDependencyType(depRel);
                            rel.setBegin(rel.getDependent().getBegin());
                            rel.setEnd(rel.getDependent().getEnd());
                            rel.setFlavor(BASIC);
                            rel.addToIndexes();
                        }
                    }
                }
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
        boolean firstLineOfSentence = true;
        while ((line = aReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                firstLineOfSentence = true;
                break; // End of sentence
            }
            
            if (line.startsWith("<") && line.endsWith(">")) {
                // FinnTreeBank uses pseudo-XML to attach extra metadata to sentences.
                // Currently, we just ignore this.
                break; // Consider end of sentence
            }
            
            if (firstLineOfSentence && line.startsWith("#")) {
                // GUM uses a comment to attach extra metadata to sentences.
                // Currently, we just ignore this.
                break; // Consider end of sentence
            }

            firstLineOfSentence = false;
            
            String[] fields = line.split("\t");
            if (fields.length != 7) {
                throw new IOException(
                        "Invalid file format. Line needs to have 7 tab-separated fields, but it has "
                                + fields.length + ": [" + line + "]");
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
