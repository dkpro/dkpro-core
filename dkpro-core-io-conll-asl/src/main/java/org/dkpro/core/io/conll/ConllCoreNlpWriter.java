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

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.io.sequencecodec.AdjacentLabelCodec;
import org.dkpro.core.api.io.sequencecodec.SequenceItem;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Write files in the default CoreNLP CoNLL format.
 * 
 * @see <a href="https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/pipeline/CoNLLOutputter.html">CoreNLP CoNLLOutputter</a>
 */
@ResourceMetaData(name = "CoNLL CoreNLP Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.TEXT_X_CONLL_CORENLP})
@TypeCapability(inputs = {
        "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
public class ConllCoreNlpWriter
    extends JCasFileWriter_ImplBase
{
    private static final String UNUSED = "_";
    private static final int UNUSED_INT = -1;

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
     * Write fine-grained part-of-speech information.
     */
    public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
    @ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "true")
    private boolean writePos;

    /**
     * Write named entity information.
     */
    public static final String PARAM_WRITE_NAMED_ENTITY = 
            ComponentParameters.PARAM_WRITE_NAMED_ENTITY;
    @ConfigurationParameter(name = PARAM_WRITE_NAMED_ENTITY, mandatory = true, defaultValue = "true")
    private boolean writeNamedEntity;

    /**
     * Write lemma information.
     */
    public static final String PARAM_WRITE_LEMMA = ComponentParameters.PARAM_WRITE_LEMMA;
    @ConfigurationParameter(name = PARAM_WRITE_LEMMA, mandatory = true, defaultValue = "true")
    private boolean writeLemma;

    /**
     * Write syntactic dependency information.
     */
    public static final String PARAM_WRITE_DEPENDENCY = ComponentParameters.PARAM_WRITE_DEPENDENCY;
    @ConfigurationParameter(name = PARAM_WRITE_DEPENDENCY, mandatory = true, defaultValue = "true")
    private boolean writeDependency;
    
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
        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix), targetEncoding));) {

            convert(aJCas, out);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void convert(JCas aJCas, PrintWriter aOut)
    {
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            Map<Token, Row> ctokens = new LinkedHashMap<>();
            NavigableMap<Integer, Token> tokenBeginIndex = new TreeMap<>();
            NavigableMap<Integer, Token> tokenEndIndex = new TreeMap<>();

            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);
            
            for (int i = 0; i < tokens.size(); i++) {
                Row row = new Row();
                row.id = i + 1;
                row.token = tokens.get(i);
                ctokens.put(row.token, row);
                tokenBeginIndex.put(row.token.getBegin(), row.token);
                tokenEndIndex.put(row.token.getEnd(), row.token);
            }

            // Dependencies
            List<Dependency> basicDeps = selectCovered(Dependency.class, sentence).stream()
                    .filter(dep -> dep.getFlavor() == null || 
                            DependencyFlavor.BASIC.equals(dep.getFlavor()))
                    .collect(Collectors.toList());
            for (Dependency rel : basicDeps) {
                Row row =  ctokens.get(rel.getDependent());
                if (row.deprel != null) {
                    String form = row.token.getCoveredText();
                    throw new IllegalStateException("Illegal basic dependency structure - token ["
                            + form
                            + "] is dependent of more than one dependency.");
                }
                row.deprel = rel;
            }
            
            // Named entities
            List<SequenceItem> nerSpans = new ArrayList<>();
            for (NamedEntity ne : selectCovered(NamedEntity.class, sentence)) {
                Token beginToken = tokenBeginIndex.floorEntry(ne.getBegin()).getValue();
                Token endToken = tokenEndIndex.ceilingEntry(ne.getEnd()).getValue();
                nerSpans.add(new SequenceItem(ctokens.get(beginToken).id, ctokens.get(endToken).id,
                        ne.getValue()));
            }
            AdjacentLabelCodec codec = new AdjacentLabelCodec(1);
            List<SequenceItem> encodedNe = codec.encode(nerSpans, tokens.size());
            for (int i = 0; i < encodedNe.size(); i++) {
                ctokens.get(tokens.get(i)).ne = encodedNe.get(i).getLabel(); 
            }
            
            // Write sentence
            for (Row row : ctokens.values()) {
                String form = row.token.getCoveredText();
                if (!writeCovered) {
                    form = row.token.getText();
                }
                
                String lemma = UNUSED;
                if (writeLemma && (row.token.getLemma() != null)) {
                    lemma = row.token.getLemma().getValue();
                }

                String pos = UNUSED;
                if (writePos && (row.token.getPos() != null)) {
                    POS posAnno = row.token.getPos();
                    pos = posAnno.getPosValue();
                }

                int headId = UNUSED_INT;
                String deprel = UNUSED;
                if (writeDependency && (row.deprel != null)) {
                    deprel = row.deprel.getDependencyType();
                    headId = ctokens.get(row.deprel.getGovernor()).id;
                    if (headId == row.id) {
                        // ROOT dependencies may be modeled as a loop, ignore these.
                        headId = 0;
                    }
                }
                
                String head = UNUSED;
                if (headId != UNUSED_INT) {
                    head = Integer.toString(headId);
                }
                
                String ner = UNUSED;
                if (writeNamedEntity && (row.ne != null)) {
                    ner = row.ne;
                }
                
                aOut.printf("%d\t%s\t%s\t%s\t%s\t%s\t%s\n", row.id, form, lemma, pos, ner, head,
                        deprel);
            }

            aOut.println();
        }
    }

    private static final class Row
    {
        int id;
        Token token;
        String ne;
        Dependency deprel;
    }
}
