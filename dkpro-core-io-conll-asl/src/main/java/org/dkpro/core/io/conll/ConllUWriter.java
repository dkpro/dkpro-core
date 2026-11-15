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

import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Document;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.SurfaceForm;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import eu.openminted.share.annotations.api.DocumentationResource;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Writes a file in the CoNLL-U format.
 * 
 * @see <a href="https://universaldependencies.org/format.html">CoNLL-U Format</a>
 */
@ResourceMetaData(name = "CoNLL-U Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({ MimeTypes.TEXT_X_CONLL_U })
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Document",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.SurfaceForm",
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
     * Write {@link Paragraph} annotations as {@code # newpar} comments (with an {@code id} if the
     * paragraph has one) in front of the first sentence they contain.
     */
    public static final String PARAM_WRITE_PARAGRAPH = ComponentParameters.PARAM_WRITE_PARAGRAPH;
    @ConfigurationParameter(name = PARAM_WRITE_PARAGRAPH, defaultValue = "true")
    private boolean writeParagraph;

    /**
     * Write {@link Document} annotations as {@code # newdoc} comments in front of the first
     * sentence they contain (with an {@code id} if the document has one). If the CAS contains no
     * {@link Document} annotations, no {@code # newdoc} comments are written - in particular, the
     * CAS document ID is not used because it is typically derived from the file name and writing it
     * out would add information that was not present in the input.
     */
    public static final String PARAM_WRITE_DOCUMENTS = "writeDocuments";
    @ConfigurationParameter(name = PARAM_WRITE_DOCUMENTS, defaultValue = "true")
    private boolean writeDocuments;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        // Convert into a buffer first so that no empty or partial file is left behind if the CAS
        // cannot be converted.
        StringWriter buffer = new StringWriter();
        try (PrintWriter out = new PrintWriter(buffer)) {
            convert(aJCas, out);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

        try (Writer out = new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix),
                targetEncoding)) {
            out.write(buffer.toString());
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
        SurfaceForm previousSf = null;
        for (SurfaceForm sf : select(aJCas, SurfaceForm.class)) {
            List<Token> words = surfaceIdx.get(sf).stream() //
                    .filter(t -> !isEmptyNode(t)) //
                    .collect(toList());
            if (words.size() < 2 || words.get(0).getBegin() != sf.getBegin()
                    || words.get(words.size() - 1).getEnd() != sf.getEnd()) {
                throw new IllegalStateException("Cannot write multi-word token [" + label(sf)
                        + "] - a multi-word token must cover at least two complete words.");
            }
            if (previousSf != null && sf.getBegin() < previousSf.getEnd()) {
                throw new IllegalStateException("Cannot write multi-word token [" + label(sf)
                        + "] - it overlaps with the multi-word token [" + label(previousSf) + "].");
            }
            if (previousSf == null || sf.getEnd() > previousSf.getEnd()) {
                previousSf = sf;
            }
            surfaceBeginIdx.put(sf.getBegin(), sf);
        }

        // Offsets at which one sentence ends and the next one begins without any text in between.
        IntSet touchOffsets = new IntOpenHashSet();
        IntSet sentenceEnds = new IntOpenHashSet();
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            sentenceEnds.add(sentence.getEnd());
        }
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            if (sentenceEnds.contains(sentence.getBegin())) {
                touchOffsets.add(sentence.getBegin());
            }
        }

        // Paragraphs and sub-documents are marked on the first sentence they contain. This
        // sentence does not necessarily start at the same offset as the paragraph or sub-document,
        // e.g. if there is leading whitespace.
        Map<Sentence, Paragraph> paragraphStarts = writeParagraph
                ? firstSentences(aJCas, Paragraph.class)
                : emptyMap();
        Map<Sentence, Document> subDocumentStarts = writeDocuments
                ? firstSentences(aJCas, Document.class)
                : emptyMap();

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            // Comments
            Document subDocument = subDocumentStarts.get(sentence);
            if (subDocument != null) {
                writeBoundaryComment(aOut, ConllUReader.META_DOCUMENT,
                        ConllUReader.META_DOCUMENT_ID, subDocument.getId());
            }

            Paragraph para = paragraphStarts.get(sentence);
            if (para != null) {
                writeBoundaryComment(aOut, ConllUReader.META_PARAGRAPH,
                        ConllUReader.META_PARAGRAPH_ID, para.getId());
            }

            if (sentence.getId() != null) {
                aOut.printf("# %s = %s\n", ConllUReader.META_SEND_ID,
                        checkComment(ConllUReader.META_SEND_ID, sentence.getId()));
            }
            if (writeTextHeader) {
                String sentenceText = sentenceText(sentence);
                // CoNLL-U does not support line breaks in the sentence text, so we need to replace
                // such characters.
                sentenceText = StringUtils.replaceChars(sentenceText, "\n\r", "  ");
                aOut.printf("# %s = %s\n", ConllUReader.META_TEXT, sentenceText);
            }

            Map<Token, Row> ctokens = buildRows(aJCas, sentence, touchOffsets);

            // Dependencies
            for (Dependency rel : selectCovered(Dependency.class, sentence)) {
                if (rel.getDependent() == null) {
                    throw new IllegalStateException("Illegal dependency structure - dependency ["
                            + rel.getDependencyType() + "] has no dependent.");
                }

                Row row = ctokens.get(rel.getDependent());
                if (row == null && isEmptyNode(rel.getDependent())
                        && !belongsToSentence(rel.getDependent(), sentence, touchOffsets)) {
                    // Relation of an empty node at the boundary to a touching neighbour sentence -
                    // it is written as part of that sentence.
                    continue;
                }
                if (row == null) {
                    // The dependent is not a token of this sentence - such a relation cannot be
                    // expressed in CoNLL-U, which addresses tokens by their in-sentence index.
                    throw new IllegalStateException(
                            "Illegal dependency structure - dependency [" + rel.getDependencyType()
                                    + "] has a dependent which is not part of the sentence.");
                }

                if (StringUtils.isBlank(rel.getFlavor())
                        || DependencyFlavor.BASIC.equals(rel.getFlavor())) {
                    if (row.emptyNode) {
                        throw new IllegalStateException("Illegal basic dependency structure - "
                                + "the empty node [" + row.id + "] is the dependent of a basic "
                                + "dependency, but empty nodes can only take part in enhanced "
                                + "dependencies.");
                    }
                    if (row.deprel != null) {
                        throw new IllegalStateException(
                                "Illegal basic dependency structure - token [" + form(row)
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
                String form = checkColumn("FORM", form(row));

                String lemma = UNUSED;
                if (writeLemma && (row.token.getLemma() != null)) {
                    lemma = value("LEMMA", row.token.getLemma().getValue());
                }

                String pos = UNUSED;
                if (writePos && (row.token.getPos() != null)) {
                    pos = value("XPOS", row.token.getPos().getPosValue());
                }

                String cpos = UNUSED;
                if (writeCPos && (row.token.getPos() != null)) {
                    cpos = value("UPOS", row.token.getPos().getCoarseValue());
                }

                String head = UNUSED;
                String deprel = UNUSED;
                String deps = UNUSED;
                if (writeDependency) {
                    // The HEAD and DEPREL columns are only defined for regular tokens - an empty
                    // node participates in the enhanced graph only.
                    if (row.deprel != null && !row.emptyNode) {
                        deprel = relation("DEPREL", row.deprel);
                        Row governor = governorRow(ctokens, row.deprel);
                        if (governor.emptyNode) {
                            throw new IllegalStateException("Illegal basic dependency structure - "
                                    + "token [" + form + "] has the empty node [" + governor.id
                                    + "] as its governor, but the HEAD column can only refer to "
                                    + "regular tokens.");
                        }
                        // ROOT dependencies may be modeled as a loop, ignore these.
                        head = governor == row ? "0" : Integer.toString(governor.tokenId);
                    }

                    // The DEPS column must be sorted by head.
                    List<String[]> depsItems = new ArrayList<>();
                    for (Dependency d : row.deps) {
                        // Resolve self-looping root to 0-indexed root
                        Row governor = governorRow(ctokens, d);
                        depsItems.add(new String[] { governor == row ? "0" : governor.id,
                                relation("DEPS", d) });
                    }
                    depsItems.sort(comparing((String[] item) -> item[0], NODE_ID_ORDER));
                    if (!depsItems.isEmpty()) {
                        deps = depsItems.stream() //
                                .map(item -> item[0] + ":" + item[1]) //
                                .collect(joining("|"));
                    }
                }

                String feats = UNUSED;
                if (writeMorph && (row.token.getMorph() != null)) {
                    feats = value("FEATS", row.token.getMorph().getValue());
                }

                // A zero-width empty node may share its begin offset with a surface form, but it is
                // not the token that starts the multi-word token.
                SurfaceForm sf = row.emptyNode ? null : surfaceBeginIdx.get(row.token.getBegin());
                if (sf != null) {
                    // Empty nodes are not part of multi-word tokens, even if they are positioned
                    // between their words.
                    List<Token> words = surfaceIdx.get(sf).stream() //
                            .filter(t -> !isEmptyNode(t)) //
                            .collect(toList());
                    List<Row> covered = words.stream() //
                            .map(ctokens::get) //
                            .filter(r -> r != null) //
                            .collect(toList());
                    if (covered.size() != words.size()) {
                        throw new IllegalStateException("Cannot write multi-word token ["
                                + label(sf) + "] - it spans multiple sentences.");
                    }
                    Row last = covered.get(covered.size() - 1);
                    // The SpaceAfter of a multi-word token belongs on its range line, not on the
                    // last of its words.
                    String sfMisc = last.noSpaceAfter ? "SpaceAfter=No" : UNUSED;
                    last.noSpaceAfter = false;
                    // The ID column holds the `id1-id2` range, followed by the FORM and the eight
                    // remaining columns which are all unused for a multi-word token.
                    aOut.printf("%d-%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
                            covered.get(0).tokenId, last.tokenId, value("FORM", sf.getValue()),
                            UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, sfMisc);
                }

                String misc = UNUSED;
                if (row.noSpaceAfter) {
                    misc = "SpaceAfter=No";
                }

                aOut.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", row.id, form, lemma, cpos,
                        pos, feats, head, deprel, deps, misc);
            }

            aOut.println();
        }
    }

    /**
     * Build the rows of a sentence in output order. Regular tokens are numbered in the order of the
     * CAS index. Empty nodes cannot be ordered via the CAS index because several of them may share
     * the same offsets - instead, an empty node {@code i.k} is placed after the regular token
     * {@code i} (or at the start of the sentence for {@code 0.k}), ordered by {@code k}.
     */
    private Map<Token, Row> buildRows(JCas aJCas, Sentence aSentence, IntSet aTouchOffsets)
    {
        List<Token> tokens = selectCovered(Token.class, aSentence);

        List<Token> regularTokens = new ArrayList<>();
        List<Token> emptyNodes = new ArrayList<>();
        for (Token token : tokens) {
            if (isEmptyNode(token)) {
                if (belongsToSentence(token, aSentence, aTouchOffsets)) {
                    emptyNodes.add(token);
                }
            }
            else {
                regularTokens.add(token);
            }
        }

        Set<String> emptyNodeIds = new HashSet<>();
        for (Token emptyNode : emptyNodes) {
            if (wordIndex(emptyNode) > regularTokens.size()) {
                throw new IllegalStateException("Empty node [" + emptyNode.getId()
                        + "] refers to a word which does not exist in the sentence.");
            }
            if (!emptyNodeIds.add(emptyNode.getId())) {
                throw new IllegalStateException(
                        "Duplicate empty node ID [" + emptyNode.getId() + "] in the sentence.");
            }
        }
        emptyNodes.sort(comparing(Token::getId, NODE_ID_ORDER));

        Map<Token, Row> ctokens = new LinkedHashMap<>();
        Iterator<Token> emptyNodeIterator = emptyNodes.iterator();
        Token nextEmptyNode = emptyNodeIterator.hasNext() ? emptyNodeIterator.next() : null;
        for (int i = 0; i <= regularTokens.size(); i++) {
            if (i > 0) {
                Row row = new Row();
                row.token = regularTokens.get(i - 1);
                row.tokenId = i;
                row.id = Integer.toString(i);
                row.noSpaceAfter = isNoSpaceAfter(aJCas, regularTokens, i - 1);
                ctokens.put(row.token, row);
            }

            // Empty nodes following the current regular token - any remaining empty nodes which
            // refer to a token beyond the end of the sentence are placed at the end.
            while (nextEmptyNode != null
                    && (i == regularTokens.size() || wordIndex(nextEmptyNode) <= i)) {
                Row row = new Row();
                row.token = nextEmptyNode;
                row.emptyNode = true;
                row.id = nextEmptyNode.getId();
                ctokens.put(row.token, row);
                nextEmptyNode = emptyNodeIterator.hasNext() ? emptyNodeIterator.next() : null;
            }
        }

        return ctokens;
    }

    /**
     * The text of the sentence as it appears in the original text. The words of a multi-word token
     * are replaced with its surface string, e.g. {@code de el} with {@code del}.
     */
    private static String sentenceText(Sentence aSentence)
    {
        String text = aSentence.getCoveredText();
        int begin = aSentence.getBegin();
        StringBuilder result = new StringBuilder();
        int pos = begin;
        for (SurfaceForm sf : selectCovered(SurfaceForm.class, aSentence)) {
            if (sf.getValue() == null || sf.getBegin() < pos) {
                continue;
            }
            result.append(text, pos - begin, sf.getBegin() - begin);
            result.append(sf.getValue());
            pos = sf.getEnd();
        }
        result.append(text, pos - begin, text.length());
        return result.toString();
    }

    private static boolean isNoSpaceAfter(JCas aJCas, List<Token> aRegularTokens, int aIndex)
    {
        Token token = aRegularTokens.get(aIndex);
        if (aIndex + 1 < aRegularTokens.size()) {
            return token.getEnd() == aRegularTokens.get(aIndex + 1).getBegin();
        }

        // For the last token of the sentence, the following character is not covered by a token
        // of this sentence, so consult the document text instead. Only if the token is directly
        // followed by a non-whitespace character is there really no space after it.
        String text = aJCas.getDocumentText();
        int end = token.getEnd();
        return end < text.length() && !Character.isWhitespace(text.charAt(end));
    }

    private String form(Row aRow)
    {
        // An empty node covers no text, so its form can only come from the token form.
        String form = (writeCovered && !aRow.emptyNode) ? aRow.token.getCoveredText()
                : aRow.token.getText();
        return StringUtils.isEmpty(form) ? UNUSED : form;
    }

    private static void writeBoundaryComment(PrintWriter aOut, String aKey, String aIdKey,
            String aId)
    {
        if (aId != null) {
            aOut.printf("# %s = %s\n", aIdKey, checkComment(aIdKey, aId));
        }
        else {
            aOut.printf("# %s\n", aKey);
        }
    }

    /**
     * Map each annotation of the given type to the first sentence it contains.
     */
    private static <T extends Annotation> Map<Sentence, T> firstSentences(JCas aJCas,
            Class<T> aType)
    {
        Map<Sentence, T> result = new HashMap<>();
        Map<T, List<Sentence>> idx = indexCovered(aJCas, aType, Sentence.class);
        for (T annotation : select(aJCas, aType)) {
            List<Sentence> sentences = idx.get(annotation);
            if (sentences != null && !sentences.isEmpty()) {
                result.putIfAbsent(sentences.get(0), annotation);
            }
        }
        return result;
    }

    /**
     * CoNLL-U has no escape syntax, so a column value containing a tab or line break would corrupt
     * the file.
     */
    private static String checkColumn(String aColumn, String aValue)
    {
        if (StringUtils.containsAny(aValue, '\t', '\n', '\r')) {
            throw new IllegalStateException(
                    "Cannot write value [" + escape(aValue) + "] to column " + aColumn
                            + " - CoNLL-U does not support tabs or line breaks in column values.");
        }
        return aValue;
    }

    /**
     * A comment ends at the end of the line, so a comment value containing a line break would turn
     * the remainder of the value into an invalid line.
     */
    private static String checkComment(String aComment, String aValue)
    {
        if (StringUtils.containsAny(aValue, '\n', '\r')) {
            throw new IllegalStateException(
                    "Cannot write value [" + escape(aValue) + "] to comment # " + aComment
                            + " - CoNLL-U does not support line breaks in comments.");
        }
        return aValue;
    }

    private static String escape(String aValue)
    {
        return aValue.replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
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

    /**
     * If two sentences touch (e.g. due to a sentence-final {@code SpaceAfter=No}), a zero-width
     * empty node at the boundary is covered by both of them. Its ID decides where it belongs: an
     * empty node {@code 0.k} precedes the first word and belongs to the sentence starting at its
     * offset, while an empty node {@code i.k} with {@code i > 0} follows a word and belongs to the
     * sentence ending at its offset.
     */
    private static boolean belongsToSentence(Token aEmptyNode, Sentence aSentence,
            IntSet aTouchOffsets)
    {
        if (!aTouchOffsets.contains(aEmptyNode.getBegin())) {
            // Not at the boundary to a touching neighbour sentence, so the node unambiguously
            // belongs to the sentence covering it.
            return true;
        }

        if (wordIndex(aEmptyNode) == 0) {
            return aEmptyNode.getBegin() == aSentence.getBegin();
        }

        return aEmptyNode.getBegin() > aSentence.getBegin();
    }

    /**
     * The index of the regular word an empty node {@code i.k} follows, i.e. {@code i}.
     */
    private static int wordIndex(Token aEmptyNode)
    {
        String id = aEmptyNode.getId();
        if (!ConllUReader.EMPTY_NODE_ID.matcher(id).matches()) {
            throw new IllegalStateException("Invalid empty node ID [" + id
                    + "] - empty node IDs must have the form i.k (e.g. 5.1).");
        }
        return Integer.parseInt(id.substring(0, id.indexOf('.')));
    }

    /**
     * Unspecified values are written as {@code _}.
     */
    private static String value(String aColumn, String aValue)
    {
        return StringUtils.isEmpty(aValue) ? UNUSED : checkColumn(aColumn, aValue);
    }

    /**
     * The relation of a dependency is mandatory. In the DEPS column, it must also not contain the
     * {@code |} character which separates the items of the column.
     */
    private static String relation(String aColumn, Dependency aRel)
    {
        String type = aRel.getDependencyType();
        if (StringUtils.isBlank(type)) {
            throw new IllegalStateException("Cannot write a dependency without a relation type "
                    + "to column " + aColumn + ".");
        }
        if ("DEPS".equals(aColumn) && type.indexOf('|') >= 0) {
            throw new IllegalStateException("Cannot write relation [" + type + "] to column "
                    + aColumn + " - the character [|] separates the items of that column.");
        }
        return checkColumn(aColumn, type);
    }

    private static String label(SurfaceForm aSurfaceForm)
    {
        return aSurfaceForm.getValue() != null ? aSurfaceForm.getValue()
                : aSurfaceForm.getCoveredText();
    }

    /**
     * Orders node IDs numerically - regular IDs ({@code 3}) before empty nodes following them
     * ({@code 3.1}), and empty nodes by their numeric suffix ({@code 3.2} before {@code 3.10}).
     */
    private static final Comparator<String> NODE_ID_ORDER = comparingInt(
            (String id) -> Integer.parseInt(StringUtils.substringBefore(id, "."))).thenComparingInt(
                    id -> id.contains(".") ? Integer.parseInt(StringUtils.substringAfter(id, "."))
                            : 0);

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
