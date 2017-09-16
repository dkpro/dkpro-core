/*
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;

/**
 * Writes a file in the CoNLL-2006 format (aka CoNLL-X).
 * 
 * @see <a href="https://web.archive.org/web/20131216222420/http://ilk.uvt.nl/conll/">CoNLL-X Shared Task: Multi-lingual Dependency Parsing</a>
 */
@ResourceMetaData(name="CoNLL 2006 Writer")
@MimeTypeCapability({MimeTypes.TEXT_X_CONLL_2006})
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
public class Conll2006Writer
    extends JCasFileWriter_ImplBase
{
    private static final String UNUSED = "_";
    private static final int UNUSED_INT = -1;

    /**
     * Character encoding of the output data.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String targetEncoding;

    public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".conll")
    private String filenameSuffix;

    public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
    @ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "true")
    private boolean writePos;

    public static final String PARAM_WRITE_CPOS = ComponentParameters.PARAM_WRITE_CPOS;
    @ConfigurationParameter(name = PARAM_WRITE_CPOS, mandatory = true, defaultValue = "true")
    private boolean writeCPos;

    public static final String PARAM_WRITE_MORPH = "writeMorph";
    @ConfigurationParameter(name = PARAM_WRITE_MORPH, mandatory = true, defaultValue = "true")
    private boolean writeMorph;

    public static final String PARAM_WRITE_LEMMA = ComponentParameters.PARAM_WRITE_LEMMA;
    @ConfigurationParameter(name = PARAM_WRITE_LEMMA, mandatory = true, defaultValue = "true")
    private boolean writeLemma;

    public static final String PARAM_WRITE_DEPENDENCY = ComponentParameters.PARAM_WRITE_DEPENDENCY;
    @ConfigurationParameter(name = PARAM_WRITE_DEPENDENCY, mandatory = true, defaultValue = "true")
    private boolean writeDependency;

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
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            HashMap<Token, Row> ctokens = new LinkedHashMap<Token, Row>();

            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);
            
            // Check if we should try to include the FEATS in output
            List<MorphologicalFeatures> morphology = selectCovered(MorphologicalFeatures.class, sentence);
            boolean useFeats = tokens.size() == morphology.size();
            
            for (int i = 0; i < tokens.size(); i++) {
                Row row = new Row();
                row.id = i+1;
                row.token = tokens.get(i);
                if (useFeats) {
                    row.feats = morphology.get(i);
                }
                ctokens.put(row.token, row);
            }

            // Dependencies
            List<Dependency> basicDeps = selectCovered(Dependency.class, sentence).stream()
                    .filter(dep -> dep.getFlavor() == null || DependencyFlavor.BASIC.equals(dep.getFlavor()))
                    .collect(Collectors.toList());
            for (Dependency rel : basicDeps) {
                Row row =  ctokens.get(rel.getDependent());
                if (row.deprel != null) {
                    throw new IllegalStateException("Illegal basic dependency structure - token ["
                            + row.token.getCoveredText()
                            + "] is dependent of more than one dependency.");
                }
                row.deprel = rel;
            }

            // Write sentence in CONLL 2006 format
            for (Row row : ctokens.values()) {
                String lemma = UNUSED;
                if (writeLemma && (row.token.getLemma() != null)) {
                    lemma = row.token.getLemma().getValue();
                }

                String pos = UNUSED;
                if (writePos && (row.token.getPos() != null)) {
                    POS posAnno = row.token.getPos();
                    pos = posAnno.getPosValue();
                }

                String cpos = UNUSED;
                if (writeCPos && (row.token.getPos() != null)
                        && row.token.getPos().getCoarseValue() != null) {
                    POS posAnno = row.token.getPos();
                    cpos = posAnno.getCoarseValue();
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
                
                String feats = UNUSED;
                if (writeMorph && (row.feats != null)) {
                    feats = row.feats.getValue();
                }
                
                String phead = UNUSED;
                String pdeprel = UNUSED;

                aOut.printf("%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", row.id,
                        row.token.getCoveredText(), lemma, cpos, pos, feats, head, deprel, phead,
                        pdeprel);
            }

            aOut.println();
        }
    }

    private static final class Row
    {
        int id;
        Token token;
        MorphologicalFeatures feats;
        Dependency deprel;
    }
}
