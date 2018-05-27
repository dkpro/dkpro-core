/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.io.tuebadz;

import static org.apache.commons.io.IOUtils.closeQuietly;

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

import de.tudarmstadt.ukp.dkpro.core.api.io.IobDecoder;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reads the Tüba-D/Z chunking format.
 * 
 * @see <a href="http://www.sfs.uni-tuebingen.de/en/ascl/resources/corpora/tueba-dz.html">TüBA-D/Z
 *      Web page</a>
 */
@ResourceMetaData(name = "Tüba-D/Z Chunking Format Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({ MimeTypes.APPLICATION_X_TUEBADZ_CHUNK })
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk" })
public class TuebaDZReader
    extends JCasResourceCollectionReader_ImplBase
{
    private static final int FORM = 0;
    private static final int POSTAG = 1;
    private static final int IOB = 2;

    private static final String TAB = "\t";
    private static final String EQUAL_SIGN = "=";
    private static final String SENTENCE_HEADER = "%% sent no.";
    private static final int SENTENCE_HEADER_LEN = SENTENCE_HEADER.length();

    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String sourceEncoding;

    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid spamming
     * the heap with thousands of strings representing only a few different tags.
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internTags;

    /**
     * Write part-of-speech information.
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
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating the
     * mapping automatically.
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
     * Read named entity information.
     */
    public static final String PARAM_READ_NAMED_ENTITY = 
            ComponentParameters.PARAM_READ_NAMED_ENTITY;
    @ConfigurationParameter(name = PARAM_READ_NAMED_ENTITY, mandatory = true, defaultValue = "false")
    private boolean namedEntityEnabled;

    /**
     * Use this chunk tag set to use to resolve the tag set mapping instead of using the tag set
     * defined as part of the model meta data. This can be useful if a custom model is specified
     * which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_CHUNK_TAG_SET = ComponentParameters.PARAM_CHUNK_TAG_SET;
    @ConfigurationParameter(name = PARAM_CHUNK_TAG_SET, mandatory = false)
    protected String chunkTagset;

    /**
     * Load the chunk tag to UIMA type mapping from this location instead of locating the mapping
     * automatically.
     */
    public static final String PARAM_CHUNK_MAPPING_LOCATION = 
            ComponentParameters.PARAM_CHUNK_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CHUNK_MAPPING_LOCATION, mandatory = false)
    protected String chunkMappingLocation;

    private MappingProvider posMappingProvider;
    private MappingProvider chunkMappingProvider;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        posMappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
                posTagset, getLanguage());

        chunkMappingProvider = MappingProviderFactory
                .createChunkMappingProvider(chunkMappingLocation, chunkTagset, getLanguage());
    }

    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
    {
        try {
            if (posEnabled) {
                posMappingProvider.configure(aJCas.getCas());
            }
            if (chunkEnabled) {
                chunkMappingProvider.configure(aJCas.getCas());
            }
        }
        catch (AnalysisEngineProcessException e) {
            throw new IOException(e);
        }

        Resource res = nextFile();
        initCas(aJCas, res);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(res.getInputStream(), sourceEncoding));
            convert(aJCas, reader);
        }
        finally {
            closeQuietly(reader);
        }
    }

    private void convert(JCas aJCas, BufferedReader aReader) throws IOException
    {
        JCasBuilder doc = new JCasBuilder(aJCas);

        Type chunkType = JCasUtil.getType(aJCas, Chunk.class);
        Feature chunkValue = chunkType.getFeatureByBaseName("chunkValue");
        IobDecoder decoder = new IobDecoder(aJCas.getCas(), chunkValue, chunkMappingProvider);
        decoder.setInternTags(internTags);

        List<String[]> words;
        while ((words = readSentence(aReader)) != null) {
            if (words.isEmpty()) {
                continue;
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            List<Token> tokens = new ArrayList<Token>();
            String[] chunkTags = new String[words.size()];

            // Tokens, POS
            int i = 0;
            for (String[] word : words) {
                // Read token
                Token token = doc.add(word[FORM], Token.class);
                sentenceEnd = token.getEnd();
                doc.add(" ");

                if (posEnabled) {
                    Type posTag = posMappingProvider.getTagType(word[POSTAG]);
                    POS pos = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(),
                            token.getEnd());
                    pos.setPosValue(word[POSTAG]);
                    pos.addToIndexes();
                    token.setPos(pos);
                }

                tokens.add(token);

                // Chunk tag may be simple (B-PX, I-PX) or compound, like B-NX=ORG or I-NX=PER for
                // named entities
                // Currently, the reader uses only the chunk part. In the future, it might also use
                // the
                // name entity information.
                String[] chunkTag = word[IOB].split(EQUAL_SIGN);
                chunkTags[i] = chunkTag[0];
                i++;
            }

            if (chunkEnabled) {
                decoder.decode(tokens, chunkTags);
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
    private static List<String[]> readSentence(BufferedReader aReader) throws IOException
    {
        List<String[]> words = new ArrayList<String[]>();
        String line;
        while ((line = aReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                break; // End of sentence
            }
            if (StringUtils.left(line, SENTENCE_HEADER_LEN).equals(SENTENCE_HEADER)) {
                break; // Ignore sentence header line
            }
            String[] fields = line.split(TAB);
            if (fields.length != 3) {
                throw new IOException(
                        "Invalid file format. Line needs to have 3 tab-separated fields.");
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
