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

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.SurfaceForm;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import eu.openminted.share.annotations.api.DocumentationResource;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Writes a file in the CoNLL-U format.
 * 
 * @see <a href="http://universaldependencies.github.io/docs/format.html">CoNLL-U Format</a>
 */
@ResourceMetaData(name = "CoNLL-U Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({ MimeTypes.TEXT_X_CONLL_U })
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
public class ConllUWriter
    extends JCasFileWriter_ImplBase
{
    private static final String UNUSED = "_";
    private static final int UNUSED_INT = -1;

    /**
     * Character encoding of the output data.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String targetEncoding;

    /**
     * Use this filename extension.
     */
    public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, defaultValue = ".conllu")
    private String filenameSuffix;

    /**
     * Write fine-grained part-of-speech information.
     */
    public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
    @ConfigurationParameter(name = PARAM_WRITE_POS, defaultValue = "true")
    private boolean writePos;

    /**
     * Write coarse-grained part-of-speech information.
     */
    public static final String PARAM_WRITE_CPOS = ComponentParameters.PARAM_WRITE_CPOS;
    @ConfigurationParameter(name = PARAM_WRITE_CPOS, defaultValue = "true")
    private boolean writeCPos;

    /**
     * Write morphological features.
     */
    public static final String PARAM_WRITE_MORPH = ComponentParameters.PARAM_WRITE_MORPH;
    @ConfigurationParameter(name = PARAM_WRITE_MORPH, defaultValue = "true")
    private boolean writeMorph;

    /**
     * Write lemma information.
     */
    public static final String PARAM_WRITE_LEMMA = ComponentParameters.PARAM_WRITE_LEMMA;
    @ConfigurationParameter(name = PARAM_WRITE_LEMMA, defaultValue = "true")
    private boolean writeLemma;

    /**
     * Write syntactic dependency information.
     */
    public static final String PARAM_WRITE_DEPENDENCY = ComponentParameters.PARAM_WRITE_DEPENDENCY;
    @ConfigurationParameter(name = PARAM_WRITE_DEPENDENCY, defaultValue = "true")
    private boolean writeDependency;

    /**
     * Write text covered by the token instead of the token form.
     */
    public static final String PARAM_WRITE_COVERED_TEXT = ComponentParameters.PARAM_WRITE_COVERED_TEXT;
    @ConfigurationParameter(name = PARAM_WRITE_COVERED_TEXT, defaultValue = "true")
    private boolean writeCovered;

    /**
     * Include the full sentence text as a comment in front of each sentence.
     */
    public static final String PARAM_WRITE_TEXT_COMMENT = "writeTextComment";
    @ConfigurationParameter(name = PARAM_WRITE_TEXT_COMMENT, defaultValue = "true")
    private boolean writeTextHeader;

    /**
     * Write paragraph information as {@code # newpar id} comments.
     */
    public static final String PARAM_WRITE_PARAGRAPH = ComponentParameters.PARAM_WRITE_PARAGRAPH;
    @ConfigurationParameter(name = PARAM_WRITE_PARAGRAPH, defaultValue = "true")
    private boolean writeParagraph;

    /**
     * Write the document ID as a {@code # newdoc id} comment in front of the first sentence. This
     * is disabled by default because the document ID is not necessarily derived from a
     * {@code # newdoc id} comment - it may just as well have been derived from the file name, in
     * which case writing it back out would add information that was not present in the input.
     */
    public static final String PARAM_WRITE_DOCUMENT_ID = "writeDocumentId";
    @ConfigurationParameter(name = PARAM_WRITE_DOCUMENT_ID, defaultValue = "false")
    private boolean writeDocumentId;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
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
        Map<SurfaceForm, List<Token>> surfaceIdx = indexCovered(aJCas, SurfaceForm.class,
                Token.class);
        Int2ObjectMap<SurfaceForm> surfaceBeginIdx = new Int2ObjectOpenHashMap<>();
        for (SurfaceForm sf : select(aJCas, SurfaceForm.class)) {
            surfaceBeginIdx.put(sf.getBegin(), sf);
        }

        // Paragraphs are marked on the sentence that starts them, so index them by their begin
        // offset to be able to detect where a new paragraph starts.
        Int2ObjectMap<Paragraph> paragraphBeginIdx = new Int2ObjectOpenHashMap<>();
        if (writeParagraph) {
            for (Paragraph para : select(aJCas, Paragraph.class)) {
                paragraphBeginIdx.put(para.getBegin(), para);
            }
        }

        boolean firstSentence = true;
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            Map<Token, Row> ctokens = new LinkedHashMap<>();

            // Comments
            if (firstSentence && writeDocumentId) {
                String documentId = DocumentMetaData.get(aJCas).getDocumentId();
                if (documentId != null) {
                    aOut.printf("# %s = %s\n", ConllUReader.META_DOCUMENT_ID, documentId);
                }
            }
            firstSentence = false;

            Paragraph para = paragraphBeginIdx.get(sentence.getBegin());
            if (para != null) {
                if (para.getId() != null) {
                    aOut.printf("# %s = %s\n", ConllUReader.META_PARAGRAPH_ID, para.getId());
                }
                else {
                    aOut.printf("# %s\n", ConllUReader.META_PARAGRAPH);
                }
            }

            if (sentence.getId() != null) {
                aOut.printf("# %s = %s\n", ConllUReader.META_SEND_ID, sentence.getId());
            }
            if (writeTextHeader) {
                String sentenceText = sentence.getCoveredText();
                // CoNLL-U does not support line breaks in the sentence text, so we need to replace
                // such characters.
                sentenceText = StringUtils.replaceChars(sentenceText, "\n\r", "  ");
                aOut.printf("# %s = %s\n", ConllUReader.META_TEXT, sentenceText);
            }

            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);

            int tokenId = 0;
            for (int i = 0; i < tokens.size(); i++) {
                Row row = new Row();
                row.token = tokens.get(i);

                // An empty node is modelled as a zero-width token carrying its original decimal ID
                // (e.g. `5.1`). It is not part of the regular token numbering.
                row.emptyNode = isEmptyNode(row.token);
                if (row.emptyNode) {
                    row.id = row.token.getId();
                }
                else {
                    tokenId++;
                    row.id = Integer.toString(tokenId);
                    row.tokenId = tokenId;
                }

                if (!row.emptyNode) {
                    // The next regular token determines whether a space follows. Empty nodes have
                    // no
                    // surface form and must therefore be skipped when looking ahead.
                    Token next = null;
                    for (int j = i + 1; j < tokens.size(); j++) {
                        if (!isEmptyNode(tokens.get(j))) {
                            next = tokens.get(j);
                            break;
                        }
                    }

                    if (next != null) {
                        row.noSpaceAfter = row.token.getEnd() == next.getBegin();
                    }
                    else {
                        // For the last token of the sentence, the following character is not
                        // covered
                        // by a token of this sentence, so consult the document text instead. Only
                        // if
                        // the token is directly followed by a non-whitespace character (or by
                        // nothing at all) is there really no space after it.
                        int end = row.token.getEnd();
                        String text = aJCas.getDocumentText();
                        row.noSpaceAfter = end < text.length()
                                && !Character.isWhitespace(text.charAt(end));
                    }
                }

                ctokens.put(row.token, row);
            }

            // Dependencies
            for (Dependency rel : selectCovered(Dependency.class, sentence)) {
                Row row = ctokens.get(rel.getDependent());
                if (row == null) {
                    // The dependent is not a token of this sentence - such a relation cannot be
                    // expressed in CoNLL-U, which addresses tokens by their in-sentence index.
                    throw new IllegalStateException(
                            "Illegal dependency structure - dependency [" + rel.getDependencyType()
                                    + "] has a dependent which is not part of the sentence.");
                }

                if (StringUtils.isBlank(rel.getFlavor())
                        || DependencyFlavor.BASIC.equals(rel.getFlavor())) {
                    if (row.deprel != null) {
                        String form = row.token.getCoveredText();
                        if (!writeCovered) {
                            form = row.token.getText();
                        }

                        throw new IllegalStateException(
                                "Illegal basic dependency structure - token [" + form
                                        + "] is dependent of more than one dependency.");
                    }
                    row.deprel = rel;
                }
                else {
                    row.deps.add(rel);
                }
            }

            // Write sentence in CONLL-U format
            for (Row row : ctokens.values()) {

                // An empty node covers no text, so its form can only come from the token form.
                String form = (writeCovered && !row.emptyNode) ? row.token.getCoveredText()
                        : row.token.getText();

                String lemma = UNUSED;
                if (writeLemma && (row.token.getLemma() != null)) {
                    lemma = row.token.getLemma().getValue();
                }

                String pos = UNUSED;
                if (writePos && (row.token.getPos() != null)
                        && row.token.getPos().getPosValue() != null) {
                    POS posAnno = row.token.getPos();
                    pos = posAnno.getPosValue();
                }

                String cpos = UNUSED;
                if (writeCPos && (row.token.getPos() != null)
                        && row.token.getPos().getCoarseValue() != null) {
                    POS posAnno = row.token.getPos();
                    cpos = posAnno.getCoarseValue();
                }

                String head = UNUSED;
                String deprel = UNUSED;
                String deps = UNUSED;
                if (writeDependency) {
                    // The HEAD and DEPREL columns are only defined for regular tokens - an empty
                    // node participates in the enhanced graph only.
                    if (row.deprel != null && !row.emptyNode) {
                        deprel = row.deprel.getDependencyType();
                        Row governor = governorRow(ctokens, row.deprel);
                        // ROOT dependencies may be modeled as a loop, ignore these.
                        head = governor == row ? "0" : Integer.toString(governor.tokenId);
                    }

                    StringBuilder depsBuf = new StringBuilder();
                    for (Dependency d : row.deps) {
                        if (depsBuf.length() > 0) {
                            depsBuf.append('|');
                        }
                        // Resolve self-looping root to 0-indexed root
                        Row governor = governorRow(ctokens, d);
                        depsBuf.append(governor == row ? "0" : governor.id);
                        depsBuf.append(':');
                        depsBuf.append(d.getDependencyType());
                    }
                    if (depsBuf.length() > 0) {
                        deps = depsBuf.toString();
                    }
                }

                String feats = UNUSED;
                if (writeMorph && (row.token.getMorph() != null)) {
                    feats = row.token.getMorph().getValue();
                }

                String misc = UNUSED;
                if (row.noSpaceAfter) {
                    misc = "SpaceAfter=No";
                }

                // A zero-width empty node may share its begin offset with a surface form, but it is
                // not the token that starts the multi-word token.
                SurfaceForm sf = row.emptyNode ? null : surfaceBeginIdx.get(row.token.getBegin());
                if (sf != null) {
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    List<Token> covered = (List) surfaceIdx.get(sf);
                    int id1 = ctokens.get(covered.get(0)).tokenId;
                    int id2 = ctokens.get(covered.get(covered.size() - 1)).tokenId;
                    // The ID column holds the `id1-id2` range, followed by the FORM and the eight
                    // remaining columns which are all unused for a multi-word token.
                    aOut.printf("%d-%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", id1, id2,
                            sf.getValue(), UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, UNUSED,
                            UNUSED);
                }

                aOut.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", row.id, form, lemma, cpos,
                        pos, feats, head, deprel, deps, misc);
            }

            aOut.println();
        }
    }

    /**
     * Resolve the row of the governor of the given dependency.
     */
    private static Row governorRow(Map<Token, Row> aCtokens, Dependency aRel)
    {
        Row governor = aCtokens.get(aRel.getGovernor());
        if (governor == null) {
            // CoNLL-U addresses heads by their in-sentence index, so a governor outside the
            // sentence cannot be expressed.
            throw new IllegalStateException(
                    "Illegal dependency structure - dependency [" + aRel.getDependencyType()
                            + "] has a governor which is not part of the sentence.");
        }
        return governor;
    }

    /**
     * An empty node is a zero-width token which carries a decimal ID (e.g. {@code 5.1}).
     */
    private static boolean isEmptyNode(Token aToken)
    {
        return aToken.getBegin() == aToken.getEnd() && aToken.getId() != null
                && aToken.getId().indexOf('.') >= 0;
    }

    private static final class Row
    {
        /**
         * The ID as written to the ID column - an integer for regular tokens, a decimal ID for
         * empty nodes.
         */
        String id;

        /**
         * The regular token index, or {@link #UNUSED_INT} for empty nodes which are not part of the
         * regular token numbering.
         */
        int tokenId = UNUSED_INT;

        boolean emptyNode;
        Token token;
        boolean noSpaceAfter;
        Dependency deprel;
        List<Dependency> deps = new ArrayList<>();
    }
}
