/*******************************************************************************
 * Copyright 2013
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.IobDecoder;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Writes the CoNLL 2002 named entity format. The columns are separated by a single space, unlike
 * illustrated below.
 * 
 * <pre>
 * Wolff      B-PER
 * ,          O
 * currently  O
 * a          O
 * journalist O
 * in         O
 * Argentina  B-LOC
 * ,          O
 * played     O
 * with       O
 * Del        B-PER
 * Bosque     I-PER
 * in         O
 * the        O
 * final      O
 * years      O
 * of         O
 * the        O
 * seventies  O
 * in         O
 * Real       B-ORG
 * Madrid     I-ORG
 * .          O
 * </pre>
 * 
 * <ol>
 * <li>FORM - token</li>
 * <li>NER - named entity (BIO encoded)</li>
 * </ol>
 * 
 * Sentences are separated by a blank new line.
 * 
 * @see <a href="http://www.clips.ua.ac.be/conll2002/ner/">CoNLL 2002 shared task</a>
 */
public class Conll2002Reader
    extends JCasResourceCollectionReader_ImplBase
{
    private static final int FORM = 0;
    private static final int IOB = 1;

    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /**
     * The language.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spamming the heap with thousands of strings representing only a few different tags.
     *
     * Default: {@code true}
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internTags;

    /**
     * Write named entity information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_READ_NAMED_ENTITY = ComponentParameters.PARAM_READ_NAMED_ENTITY;
    @ConfigurationParameter(name = PARAM_READ_NAMED_ENTITY, mandatory = true, defaultValue = "true")
    private boolean namedEntityEnabled;

//    /**
//     * Load the chunk tag to UIMA type mapping from this location instead of locating
//     * the mapping automatically.
//     */
//    public static final String PARAM_NAMED_ENTITY_MAPPING_LOCATION = ComponentParameters.PARAM_NAMED_ENTITY_MAPPING_LOCATION;
//    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_MAPPING_LOCATION, mandatory = false)
//    protected String namedEntityMappingLocation;
//    
    private MappingProvider namedEntityMappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        namedEntityMappingProvider = new MappingProvider();
//        namedEntityMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/"
//                + "dkpro/core/api/syntax/tagset/${language}-${chunk.tagset}-chunk.map");
        namedEntityMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/there/is/no/mapping/yet");
        namedEntityMappingProvider.setDefault(MappingProvider.BASE_TYPE, NamedEntity.class.getName());
//        namedEntityMappingProvider.setOverride(MappingProvider.LOCATION, namedEntityMappingLocation);
//        namedEntityMappingProvider.setOverride(MappingProvider.LANGUAGE, language);
    }
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        if (namedEntityEnabled) {
            namedEntityMappingProvider.configure(aJCas.getCas());
        }
        
        Resource res = nextFile();
        initCas(aJCas, res);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(res.getInputStream(), encoding));
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

        Type namedEntityType = JCasUtil.getType(aJCas, NamedEntity.class);
        Feature namedEntityValue = namedEntityType.getFeatureByBaseName("value");
        IobDecoder decoder = new IobDecoder(aJCas.getCas(), namedEntityValue, namedEntityMappingProvider);
        decoder.setInternTags(internTags);
        
        List<String[]> words;
        while ((words = readSentence(aReader)) != null) {
            if (words.isEmpty()) {
                continue;
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            List<Token> tokens = new ArrayList<Token>();
            String[] namedEntityTags = new String[words.size()];
            
            // Tokens, POS
            int i = 0;
            for (String[] word : words) {
                // Read token
                Token token = doc.add(word[FORM], Token.class);
                sentenceEnd = token.getEnd();
                doc.add(" ");
                
                tokens.add(token);
                namedEntityTags[i] = word[IOB];
                i++;
            }
            
            if (namedEntityEnabled) {
                decoder.decode(tokens, namedEntityTags);
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
            if (fields.length != 2) {
                throw new IOException(
                        "Invalid file format. Line needs to have 2 space-separted fields.");
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