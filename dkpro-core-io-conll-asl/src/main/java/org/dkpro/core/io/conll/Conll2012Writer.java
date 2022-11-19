/*
 * Copyright 2014
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

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.indexCovering;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.penntree.PennTreeNode;
import org.dkpro.core.io.penntree.PennTreeUtils;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArg;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArgLink;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.WordSense;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Writer for the CoNLL-2012 format.
 */
@ResourceMetaData(name = "CoNLL 2012 Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.TEXT_X_CONLL_2012})
@TypeCapability(
        inputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
                "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred",
                "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArg",
                "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.WordSense" })
public class Conll2012Writer
    extends JCasFileWriter_ImplBase
{
    private static final String UNUSED = "-";

    private static final String ALT_UNUSED = "*";

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
     * Write lemma information.
     */
    public static final String PARAM_WRITE_LEMMA = ComponentParameters.PARAM_WRITE_LEMMA;
    @ConfigurationParameter(name = PARAM_WRITE_LEMMA, mandatory = true, defaultValue = "true")
    private boolean writeLemma;

    /**
     * Write semantic predicate infomation.
     */
    public static final String PARAM_WRITE_SEMANTIC_PREDICATE = "writeSemanticPredicate";
    @ConfigurationParameter(name = PARAM_WRITE_SEMANTIC_PREDICATE, mandatory = true, defaultValue = "true")
    private boolean writeSemanticPredicate;
    
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
            
            String documentId = DocumentMetaData.get(aJCas).getDocumentId();
            int partNumber = 0;
            if (documentId.contains("#")) {
                partNumber = Integer.parseInt(StringUtils.substringAfterLast(documentId, "#"));
                documentId = StringUtils.substringBeforeLast(documentId, "#");
            }
            out.printf("#begin document (%s); part %03d%n", documentId, partNumber);
            
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
        Map<Token, List<SemPred>> predIdx = indexCovered(aJCas, Token.class, SemPred.class);
        Map<SemArg, List<Token>> argIdx = indexCovered(aJCas, SemArg.class, Token.class);
        Map<Token, List<NamedEntity>> neIdx = indexCovering(aJCas, Token.class,
                NamedEntity.class);
        Map<Token, List<WordSense>> wordSenseIdx = indexCovered(aJCas, Token.class,
                WordSense.class);
        Map<Token, List<CoreferenceLink>> corefIdx = indexCovering(aJCas, Token.class,
                CoreferenceLink.class);
        Map<CoreferenceLink, Integer> corefChainIdx = new HashMap<>();
        
        int chainId = 1;
        for (CoreferenceChain chain : select(aJCas, CoreferenceChain.class)) {
            for (CoreferenceLink link : chain.links()) {
                corefChainIdx.put(link, chainId);
            }
            chainId++;
        }
        
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            HashMap<Token, Row> ctokens = new LinkedHashMap<Token, Row>();

            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);
            
            // Collect the predicates for this sentence in text order
            List<SemPred> preds = new ArrayList<>();
            for (Entry<Token, List<SemPred>> e : predIdx.entrySet()) {
                if (!e.getValue().isEmpty() && tokens.contains(e.getKey())) {
                    // If there are multiple semantic predicates for the current token, then 
                    // we keep only the first
                    preds.add(e.getValue().get(0));
                }
            }
            preds.sort(Comparator
                    .comparing(SemPred::getBegin)
                    .thenComparing(SemPred::getEnd, Comparator.reverseOrder()));
            
            String[] parseFragments = null;
            List<ROOT> root = selectCovered(ROOT.class, sentence);
            if (root.size() == 1) {
                PennTreeNode rootNode = PennTreeUtils.convertPennTree(root.get(0));
                if ("ROOT".equals(rootNode.getLabel())) {
                    rootNode.setLabel("TOP");
                }
                parseFragments = toPrettyPennTree(rootNode);
            }
            
            if (parseFragments != null && parseFragments.length != tokens.size()) {
                throw new IllegalStateException("Parse fragments do not match tokens - tokens: "
                        + tokens + " parse: " + asList(parseFragments));
            }
            
            for (int i = 0; i < tokens.size(); i++) {
                Row row = new Row();
                row.id = i;
                row.token = tokens.get(i);
                row.args = new SemArgLink[preds.size()];
                row.parse = parseFragments != null ? parseFragments[i] : UNUSED;
                
                // If there are multiple semantic predicates for the current token, then 
                // we keep only the first
                Collection<SemPred> predsForToken = predIdx.get(row.token);
                if (predsForToken != null && !predsForToken.isEmpty()) {
                    row.pred = predsForToken.iterator().next();
                }
                
                // If there are multiple named entities for the current token, we keep only the
                // first
                Collection<NamedEntity> neForToken = neIdx.get(row.token);
                if (neForToken != null && !neForToken.isEmpty()) {
                    row.ne = neForToken.iterator().next();
                }

                // If there are multiple word senses for the current token, we keep only the
                // first
                Collection<WordSense> senseForToken = wordSenseIdx.get(row.token);
                if (senseForToken != null && !senseForToken.isEmpty()) {
                    row.wordSense = senseForToken.iterator().next();
                }

                row.coref = corefIdx.get(row.token);
                
                ctokens.put(row.token, row);
            }

            // Semantic arguments
            for (int p = 0; p < preds.size(); p++) {
                FSArray args = preds.get(p).getArguments();
                for (SemArgLink arg : select(args, SemArgLink.class)) {
                    for (Token t : argIdx.get(arg.getTarget())) {
                        Row row = ctokens.get(t);
                        row.args[p] = arg;
                    }
                }
            }
            
            // Write sentence in CONLL 2012 format
            for (Row row : ctokens.values()) {
                String documentId = DocumentMetaData.get(aJCas).getDocumentId();
                if (StringUtils.isBlank(documentId)) {
                    documentId = UNUSED;
                }
                
                int partNumber = 0;
                
                if (documentId.contains("#")) {
                    partNumber = Integer.parseInt(StringUtils.substringAfterLast(documentId, "#"));
                    documentId = StringUtils.substringBeforeLast(documentId, "#");
                }
                
                int id = row.id;
                
                String form = row.token.getCoveredText();
                if (!writeCovered) {
                    form = row.token.getText();
                }
                
                String lemma = UNUSED + " ";
                if (writeLemma && (row.token.getLemma() != null)) {
                    lemma = row.token.getLemma().getValue();
                }

                String pos = UNUSED;
                if (writePos && (row.token.getPos() != null)) {
                    POS posAnno = row.token.getPos();
                    pos = posAnno.getPosValue();
                }

                String parse = row.parse;
                if (!parse.endsWith(")")) {
                    // This is just the curious way that the CoNLL files are encoded...
                    parse += " ";
                }
                
                String wordSense = UNUSED;
                if (row.wordSense != null) {
                    wordSense = row.wordSense.getValue();
                }
                
                String speaker = UNUSED; // FIXME
                
                String namedEntity = ALT_UNUSED + " ";
                if (row.ne != null) {
                    namedEntity = encodeMultiTokenAnnotation(row.token, row.ne, row.ne.getValue());
                }
                
                String pred = UNUSED;
                StringBuilder apreds = new StringBuilder();
                if (writeSemanticPredicate) {
                    if (row.pred != null) {
                        pred = row.pred.getCategory();
                    }
                    
                    // Get which args column the current predicate is destined for. Every args
                    // column only gets a single predicate.
                    int predForCol = preds.indexOf(row.pred);
                    
                    int argsCol = 0;
                    for (SemArgLink link : row.args) {
                        if (apreds.length() > 0) {
                            apreds.append("             ");
                        }
                        
                        String value;
                        // If this is the column for the predicate marker,then we write that - 
                        // ignoring any arguments that might be at the same position
                        if (row.pred != null && argsCol == predForCol) {
                            value = "(V*)";
                        }
                        // ... otherwise - if there is an argument at this position, then write that
                        else if (link != null) {
                            value = encodeMultiTokenAnnotation(row.token, link.getTarget(),
                                    link.getRole());
                        }
                        // ... and if there is neither the predicate marker nor the argument, then
                        // mark as unused
                        else {
                            value = ALT_UNUSED + ' ';
                        }
                        
                        apreds.append(String.format("%10s", value));
                        
                        argsCol++;
                    }
                }
                
                StringBuilder coref = new StringBuilder();
                if (!row.coref.isEmpty()) {
                    for (CoreferenceLink link : row.coref) {
                        if (coref.length() > 0) {
                            coref.append('|');
                        }
                        coref.append(encodeMultiTokenLink(row.token, link,
                                corefChainIdx.get(link)));
                    }
                }
                if (coref.length() == 0) {
                    coref.append(UNUSED);
                }

                aOut.printf("%s %3d %3d %10s %5s %13s %9s %3s %3s %10s %10s %10s %s\n", documentId,
                        partNumber, id, form, pos, parse, lemma, pred, wordSense, speaker,
                        namedEntity, apreds, coref);
            }

            aOut.println();
        }
        
        aOut.println("#end document");
    }
    
    private String encodeMultiTokenAnnotation(Token aToken, AnnotationFS aAnnotation, String aLabel)
    {
        boolean begin = aAnnotation.getBegin() == aToken.getBegin();
        boolean end = aAnnotation.getEnd() == aToken.getEnd();
        
        StringBuilder buf = new StringBuilder();
        if (begin) {
            buf.append('(');
            buf.append(aLabel);
            if (!end) {
                buf.append('*');
            }
        }
        else {
            buf.append('*');
        }        

        if (end) {
            buf.append(')');
        }
        else {
            buf.append(' ');
        }
        
        return buf.toString();
    }

    private String encodeMultiTokenLink(Token aToken, AnnotationFS aAnnotation, Integer aChainId)
    {
        boolean begin = aAnnotation.getBegin() == aToken.getBegin();
        boolean end = aAnnotation.getEnd() == aToken.getEnd();
        
        StringBuilder buf = new StringBuilder();
        if (begin) {
            buf.append('(');
        }
        if (begin | end) {
            buf.append(aChainId);
        }
        if (end) {
            buf.append(')');
        }
        
        return buf.toString();
    }

    private static final class Row
    {
        NamedEntity ne;
        String parse;
        WordSense wordSense;
        int id;
        Token token;
        SemPred pred;
        SemArgLink[] args; // These are the arguments roles for the current token!
        Collection<CoreferenceLink> coref;
    }
    
    public static String[] toPrettyPennTree(PennTreeNode aNode)
    {
        StringBuilder sb = new StringBuilder();
        toPennTree(sb, aNode);
        return sb.toString().trim().split("\n+");
    }

    private static void toPennTree(StringBuilder aSb, PennTreeNode aNode)
    {
        // This is a "(Label Token)"
        if (aNode.isPreTerminal()) {
            aSb.append("*");
        }
        else {
            aSb.append('(');
            aSb.append(aNode.getLabel());
            
            Iterator<PennTreeNode> i = aNode.getChildren().iterator();
            while (i.hasNext()) {
                PennTreeNode child = i.next();
                toPennTree(aSb, child);
                if (i.hasNext()) {
                    aSb.append("\n");
                }
            }
            
            aSb.append(')');
        }
    }
}
