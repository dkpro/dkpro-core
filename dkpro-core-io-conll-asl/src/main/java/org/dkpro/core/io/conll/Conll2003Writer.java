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
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.IobEncoder;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Writes the CoNLL 2003 format.
 *
 * @see <a href="http://www.cnts.ua.ac.be/conll2003/ner/">CoNLL 2003 shared task</a>
 */
@ResourceMetaData(name = "CoNLL 2003 Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.TEXT_X_CONLL_2003})
@TypeCapability(
        inputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk",
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
public class Conll2003Writer
    extends JCasFileWriter_ImplBase
{
    private static final String UNUSED = "_";

    /**
     * Character encoding of the output data.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String targetEncoding;

    /**
     * Use this filename extension.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".conll")
    private String filenameSuffix;

    /**
     * Write part-of-speech information.
     */
    public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
    @ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "true")
    private boolean writePos;

    /**
     * Write chunking information.
     */
    public static final String PARAM_WRITE_CHUNK = ComponentParameters.PARAM_WRITE_CHUNK;
    @ConfigurationParameter(name = PARAM_WRITE_CHUNK, mandatory = true, defaultValue = "true")
    private boolean writeChunk;

    /**
     * Write named entity information.
     */
    public static final String PARAM_WRITE_NAMED_ENTITY = 
            ComponentParameters.PARAM_WRITE_NAMED_ENTITY;
    @ConfigurationParameter(name = PARAM_WRITE_NAMED_ENTITY, mandatory = true, defaultValue = "true")
    private boolean writeNamedEntity;

    /**
     * Write text covered by the token instead of the token form.
     */
    public static final String PARAM_WRITE_COVERED_TEXT = 
            ComponentParameters.PARAM_WRITE_COVERED_TEXT;
    @ConfigurationParameter(name = PARAM_WRITE_COVERED_TEXT, mandatory = true, defaultValue = "true")
    private boolean writeCovered;
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix),
                    targetEncoding));
            convert(aJCas, out);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(out);
        }
    }

    private void convert(JCas aJCas, PrintWriter aOut)
    {
        Type chunkType = JCasUtil.getType(aJCas, Chunk.class);
        Feature chunkValue = chunkType.getFeatureByBaseName("chunkValue");

        Type neType = JCasUtil.getType(aJCas, NamedEntity.class);
        Feature neValue = neType.getFeatureByBaseName("value");
        
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            HashMap<Token, Row> ctokens = new LinkedHashMap<Token, Row>();

            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);

            // Chunks
            IobEncoder chunkEncoder = new IobEncoder(aJCas.getCas(), chunkType, chunkValue, true);

            // Named Entities
            IobEncoder neEncoder = new IobEncoder(aJCas.getCas(), neType, neValue, true);
            
            for (Token token : tokens) {
                Row row = new Row();
                row.token = token;
                row.chunk = chunkEncoder.encode(token);
                row.ne = neEncoder.encode(token);
                ctokens.put(row.token, row);
            }

            // Write sentence in CONLL 2006 format
            for (Row row : ctokens.values()) {
                String form = row.token.getCoveredText();
                if (!writeCovered) {
                    form = row.token.getText();
                }
                
                String pos = UNUSED;
                if (writePos && (row.token.getPos() != null)) {
                    POS posAnno = row.token.getPos();
                    pos = posAnno.getPosValue();
                }

                String chunk = UNUSED;
                if (writeChunk && (row.chunk != null)) {
                    chunk = row.chunk;
                }

                String namedEntity = UNUSED;
                if (writeNamedEntity && (row.ne != null)) {
                    namedEntity = row.ne;
                }

                aOut.printf("%s %s %s %s\n", form, pos, chunk, namedEntity);
            }

            aOut.println();
        }
    }

    private static final class Row
    {
        Token token;
        String chunk;
        String ne;
    }
}
