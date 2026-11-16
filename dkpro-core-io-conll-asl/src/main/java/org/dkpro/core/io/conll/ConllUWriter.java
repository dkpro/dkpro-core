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

import static java.util.Locale.ROOT;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.dkpro.core.io.conll.internal.ConllUFormat.EMPTY_NODE_ID;
import static org.dkpro.core.io.conll.internal.ConllUFormat.META_DOCUMENT;
import static org.dkpro.core.io.conll.internal.ConllUFormat.META_PARAGRAPH;
import static org.dkpro.core.io.conll.internal.ConllUFormat.NODE_ID_ORDER;
import static org.dkpro.core.io.conll.internal.ConllUFormat.ROOT_ID;
import static org.dkpro.core.io.conll.internal.ConllUFormat.SPACE_AFTER_NO;
import static org.dkpro.core.io.conll.internal.ConllUFormat.UNUSED;
import static org.dkpro.core.io.conll.internal.ConllUFormat.documentName;
import static org.dkpro.core.io.conll.internal.ConllUFormat.emptyNodeWordIndex;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Document;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.SurfaceForm;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import eu.openminted.share.annotations.api.DocumentationResource;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Writes a file in the CoNLL-U format.
 * <p>
 * {@code SpaceAfter=No} is derived from the document text. {@link Document} and {@link Paragraph}
 * annotations are written as {@code # newdoc} and {@code # newpar} comments, sentence IDs as
 * {@code # sent_id}, and the sentence text as {@code # text} (using the surface strings of
 * multi-word tokens). Empty nodes - zero-width tokens with a decimal ID - are ordered by their ID.
 * Unspecified values are written as {@code _}.
 * <p>
 * Data which cannot be represented in CoNLL-U makes the writer fail, unless {@link #PARAM_LENIENT}
 * is enabled. In that case, such data is skipped and a warning is logged.
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
    private static final int UNUSED_INT = -1;

    /**
     * Columns whose values must not contain any whitespace. FORM and LEMMA may contain inner
     * whitespace, but must not start or end with it.
     */
    private static final Set<String> NO_WHITESPACE_COLUMNS = Set.of("UPOS", "XPOS", "FEATS",
            "DEPREL", "DEPS");

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
     * Write syntactic dependency information. If disabled, empty nodes are not written either,
     * since they only exist as part of the enhanced dependency graph.
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

    /**
     * Skip data which cannot be represented in CoNLL-U and log a warning instead of failing. This
     * concerns e.g. tokens which are the dependent of more than one basic dependency (only the
     * first valid one is written), dependencies crossing sentence boundaries or lacking a governor
     * or dependent, enhanced dependencies without a relation type, surface forms which cannot be
     * written as multi-word tokens, tokens outside any sentence, and empty nodes with an invalid or
     * duplicate ID or referring to a word which does not exist in the sentence (together with the
     * dependencies involving them), and tokens whose form is empty or consists only of whitespace
     * (together with the dependencies involving them). Tabs and line breaks in values are replaced
     * with spaces. Empty nodes without enhanced dependencies and values with disallowed whitespace
     * are written nevertheless. By default, such data makes the writer fail.
     */
    public static final String PARAM_LENIENT = "lenient";
    @ConfigurationParameter(name = PARAM_LENIENT, defaultValue = "false")
    private boolean lenient;

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
            throw failure(aJCas, e);
        }

        try (Writer out = new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix),
                targetEncoding)) {
            out.append(buffer.getBuffer());
        }
        catch (Exception e) {
            throw failure(aJCas, e);
        }
    }

    /**
     * Name the document in the error - the message itself at most identifies the sentence within
     * the document.
     */
    private static AnalysisEngineProcessException failure(JCas aJCas, Exception aCause)
    {
        return new AnalysisEngineProcessException(new IllegalStateException(
                "Unable to write [" + documentName(aJCas) + "]: " + aCause.getMessage(), aCause));
    }

    private void convert(JCas aJCas, PrintWriter aOut)
    {
        Map<SurfaceForm, List<Token>> surfaceIdx = indexCovered(aJCas, SurfaceForm.class,
                Token.class);
        Int2ObjectMap<SurfaceForm> surfaceBeginIdx = indexSurfaceForms(aJCas, surfaceIdx);
        IntSet touchOffsets = touchOffsets(aJCas);

        // Paragraphs and documents are marked on the first sentence they contain. This sentence
        // does not necessarily start at the same offset as the paragraph or document, e.g. if there
        // is leading whitespace.
        Map<Sentence, Paragraph> paragraphStarts = writeParagraph
                ? firstSentences(aJCas, Paragraph.class)
                : emptyMap();
        Map<Sentence, Document> documentStarts = writeDocuments
                ? firstSentences(aJCas, Document.class)
                : emptyMap();

        // Dependencies belong to the sentence of their dependent. If they are not written, they are
        // not validated either.
        Map<Token, List<Dependency>> dependenciesByDependent = writeDependency
                ? indexDependencies(aJCas)
                : emptyMap();

        String text = aJCas.getDocumentText();
        Set<Token> writtenTokens = new HashSet<>();
        // Tokens which cannot be written (lenient mode) or which belong to a sentence that is
        // skipped - dependencies involving them are skipped along with them.
        Set<Token> skippedTokens = new HashSet<>();
        // Skipped tokens which have not been reported as a problem - the tokens of sentences
        // without
        // words, which are skipped also in strict mode. Dependencies on them from other sentences
        // are reported instead.
        Set<Token> unreportedSkippedTokens = new HashSet<>();
        Document pendingDocument = null;
        Paragraph pendingParagraph = null;
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            if (sentence.getEnd() > text.length()) {
                problem("Sentence at [" + sentence.getBegin() + "-" + sentence.getEnd()
                        + "] extends beyond the end of the document text");
                continue;
            }

            // Markers on a sentence which is skipped are carried over to the next sentence written
            // -
            // but only if that sentence is still part of the paragraph or document.
            if (pendingDocument != null && !covers(pendingDocument, sentence)) {
                pendingDocument = null;
            }
            if (pendingParagraph != null && !covers(pendingParagraph, sentence)) {
                pendingParagraph = null;
            }
            Document document = documentStarts.get(sentence);
            if (document != null) {
                pendingDocument = document;
                pendingParagraph = null;
            }
            Paragraph paragraph = paragraphStarts.get(sentence);
            if (paragraph != null) {
                pendingParagraph = paragraph;
            }

            Map<Token, Row> rows = buildRows(aJCas, sentence, touchOffsets, skippedTokens);
            if (rows.values().stream().allMatch(row -> row.emptyNode)) {
                // An empty sentence block would make the file invalid - and so would a sentence
                // consisting only of empty nodes, since a sentence must contain at least one word.
                // The empty nodes are skipped along with the sentence, including the dependencies
                // involving them.
                getLogger().warn("Skipping sentence which contains no words" + in(sentence)
                        + " - CoNLL-U does not support sentences without words.");
                skippedTokens.addAll(rows.keySet());
                unreportedSkippedTokens.addAll(rows.keySet());
                continue;
            }
            writtenTokens.addAll(rows.keySet());

            // Comments
            if (pendingDocument != null) {
                writeBoundaryComment(aOut, META_DOCUMENT, ConllUReader.META_DOCUMENT_ID,
                        pendingDocument.getId(), sentence);
                pendingDocument = null;
            }
            if (pendingParagraph != null) {
                writeBoundaryComment(aOut, META_PARAGRAPH, ConllUReader.META_PARAGRAPH_ID,
                        pendingParagraph.getId(), sentence);
                pendingParagraph = null;
            }
            if (sentence.getId() != null) {
                aOut.printf(ROOT, "# %s = %s\n", ConllUReader.META_SEND_ID,
                        comment(ConllUReader.META_SEND_ID, sentence.getId(), sentence));
            }
            if (writeTextHeader) {
                // CoNLL-U does not support line breaks in the sentence text, so we need to replace
                // such characters.
                String sentenceText = StringUtils.replaceChars(sentenceText(sentence), "\n\r",
                        "  ");
                aOut.printf(ROOT, "# %s = %s\n", ConllUReader.META_TEXT, sentenceText);
            }

            assignDependencies(rows, dependenciesByDependent, sentence, skippedTokens,
                    unreportedSkippedTokens);

            // Write sentence in CONLL-U format
            for (Row row : rows.values()) {
                String form = column("FORM", form(row), sentence);

                String lemma = UNUSED;
                if (writeLemma && (row.token.getLemma() != null)) {
                    lemma = value("LEMMA", row.token.getLemma().getValue(), sentence);
                }

                String pos = UNUSED;
                if (writePos && (row.token.getPos() != null)) {
                    pos = value("XPOS", row.token.getPos().getPosValue(), sentence);
                }

                String cpos = UNUSED;
                if (writeCPos && (row.token.getPos() != null)) {
                    cpos = value("UPOS", row.token.getPos().getCoarseValue(), sentence);
                }

                String head = UNUSED;
                String deprel = UNUSED;
                String deps = UNUSED;
                if (writeDependency) {
                    // The HEAD and DEPREL columns are only defined for regular tokens - an empty
                    // node participates in the enhanced graph only.
                    if (row.deprel != null) {
                        // ROOT dependencies may be modeled as a loop. A dependency without a
                        // relation type (e.g. not yet labeled) has an unspecified DEPREL.
                        head = row.head == row ? ROOT_ID : Integer.toString(row.head.tokenId);
                        deprel = value("DEPREL", row.deprel.getDependencyType(), sentence);
                    }

                    // The DEPS column must be sorted by head and then by relation.
                    List<String[]> depsItems = new ArrayList<>();
                    for (Dependency d : row.deps) {
                        Row governor = governorRow(rows, d, sentence, skippedTokens,
                                unreportedSkippedTokens);
                        String relation = depsRelation(d, sentence);
                        if (governor != null && relation != null) {
                            // Resolve self-looping root to 0-indexed root
                            depsItems.add(new String[] { governor == row ? ROOT_ID : governor.id,
                                    relation });
                        }
                    }
                    depsItems.sort(comparing((String[] item) -> item[0], NODE_ID_ORDER)
                            .thenComparing(item -> item[1]));
                    if (!depsItems.isEmpty()) {
                        deps = depsItems.stream() //
                                .map(item -> item[0] + ":" + item[1]) //
                                .collect(joining("|"));
                    }
                    else if (row.emptyNode) {
                        // Empty nodes only exist as part of the enhanced graph. The node is
                        // written nevertheless in lenient mode since other nodes may already refer
                        // to it - the lenient reader accepts it.
                        problem("Cannot write empty node [" + row.id + "] - it has no enhanced "
                                + "dependencies, but CoNLL-U requires empty nodes to have DEPS"
                                + in(sentence), "written without DEPS");
                    }
                }

                String feats = UNUSED;
                if (writeMorph && (row.token.getMorph() != null)) {
                    feats = value("FEATS", sortFeatures(row.token.getMorph().getValue()), sentence);
                }

                // A zero-width empty node may share its begin offset with a surface form, but it is
                // not the token that starts the multi-word token. Other tokens may share the begin
                // offset as well (e.g. zero-width or overlapping tokens), so the surface form is
                // removed from the index once written to ensure that its range line appears only
                // once.
                SurfaceForm sf = row.emptyNode ? null
                        : surfaceBeginIdx.remove(row.token.getBegin());
                if (sf != null) {
                    writeMultiWordTokenLine(aOut, sf, surfaceIdx.get(sf), rows, sentence,
                            skippedTokens);
                }

                String misc = UNUSED;
                if (row.noSpaceAfter) {
                    misc = SPACE_AFTER_NO;
                }

                aOut.printf(ROOT, "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", row.id, form, lemma,
                        cpos, pos, feats, head, deprel, deps, misc);
            }

            aOut.print('\n');
        }

        reportUncovered(aJCas, writtenTokens, skippedTokens, dependenciesByDependent);
    }

    /**
     * Write the range line of a multi-word token. Empty nodes are not part of multi-word tokens,
     * even if they are positioned between their words, and neither are tokens which are not written
     * (lenient mode).
     */
    private void writeMultiWordTokenLine(PrintWriter aOut, SurfaceForm aSurfaceForm,
            List<Token> aCovered, Map<Token, Row> aRows, Sentence aSentence,
            Set<Token> aSkippedTokens)
    {
        List<Token> words = aCovered.stream() //
                .filter(t -> !isEmptyNode(t) && !aSkippedTokens.contains(t)) //
                .collect(toList());
        List<Row> covered = words.stream() //
                .map(aRows::get) //
                .filter(r -> r != null) //
                .collect(toList());
        if (covered.size() < 2 || covered.size() != words.size()) {
            problem("Cannot write multi-word token [" + label(aSurfaceForm)
                    + "] - it spans multiple sentences or covers words which cannot be written"
                    + in(aSentence));
            return;
        }

        Row last = covered.get(covered.size() - 1);
        // The SpaceAfter of a multi-word token belongs on its range line - SpaceAfter applies to
        // tokens, so the words themselves never carry it.
        String misc = last.noSpaceAfter ? SPACE_AFTER_NO : UNUSED;
        covered.forEach(r -> r.noSpaceAfter = false);
        // The ID column holds the `id1-id2` range, followed by the FORM and the eight remaining
        // columns which are all unused for a multi-word token.
        aOut.printf(ROOT, "%d-%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", covered.get(0).tokenId,
                last.tokenId, value("FORM", aSurfaceForm.getValue(), aSentence), UNUSED, UNUSED,
                UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, misc);
    }

    /**
     * Index the surface forms by their begin offset. Surface forms which cannot be written as
     * multi-word tokens are reported as problems.
     */
    private Int2ObjectMap<SurfaceForm> indexSurfaceForms(JCas aJCas,
            Map<SurfaceForm, List<Token>> aSurfaceIdx)
    {
        Int2ObjectMap<SurfaceForm> surfaceBeginIdx = new Int2ObjectOpenHashMap<>();
        SurfaceForm previous = null;
        for (SurfaceForm sf : select(aJCas, SurfaceForm.class)) {
            List<Token> words = aSurfaceIdx.get(sf).stream() //
                    .filter(t -> !isEmptyNode(t)) //
                    .collect(toList());
            if (words.size() < 2 || words.get(0).getBegin() != sf.getBegin()
                    || words.get(words.size() - 1).getEnd() != sf.getEnd()) {
                problem("Cannot write multi-word token [" + label(sf) + "] at [" + sf.getBegin()
                        + "-" + sf.getEnd()
                        + "] - a multi-word token must cover at least two complete words");
                continue;
            }
            if (previous != null && sf.getBegin() < previous.getEnd()) {
                problem("Cannot write multi-word token [" + label(sf) + "] at [" + sf.getBegin()
                        + "-" + sf.getEnd() + "] - it overlaps with the multi-word token ["
                        + label(previous) + "]");
                continue;
            }
            previous = sf;
            surfaceBeginIdx.put(sf.getBegin(), sf);
        }
        return surfaceBeginIdx;
    }

    /**
     * Offsets at which one sentence ends and the next one begins without any text in between.
     */
    private static IntSet touchOffsets(JCas aJCas)
    {
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
        return touchOffsets;
    }

    /**
     * Index the dependencies by their dependent. Dependencies without a dependent are reported as
     * problems.
     */
    private Map<Token, List<Dependency>> indexDependencies(JCas aJCas)
    {
        Map<Token, List<Dependency>> index = new HashMap<>();
        for (Dependency rel : select(aJCas, Dependency.class)) {
            if (rel.getDependent() == null) {
                problem("Illegal dependency structure - dependency [" + rel.getDependencyType()
                        + "] at [" + rel.getBegin() + "-" + rel.getEnd() + "] has no dependent");
                continue;
            }
            index.computeIfAbsent(rel.getDependent(), k -> new ArrayList<>()).add(rel);
        }
        return index;
    }

    /**
     * Assign the dependencies of the tokens of a sentence to their rows - a basic dependency to the
     * HEAD/DEPREL columns, all others to the DEPS column.
     */
    private void assignDependencies(Map<Token, Row> aRows,
            Map<Token, List<Dependency>> aDependenciesByDependent, Sentence aSentence,
            Set<Token> aSkippedTokens, Set<Token> aUnreportedSkippedTokens)
    {
        for (Row row : aRows.values()) {
            for (Dependency rel : aDependenciesByDependent.getOrDefault(row.token, emptyList())) {
                if (!isBasic(rel)) {
                    row.deps.add(rel);
                }
                else if (row.emptyNode) {
                    problem("Illegal basic dependency structure - the empty node [" + row.id
                            + "] is the dependent of a basic dependency, but empty nodes can only "
                            + "take part in enhanced dependencies" + in(aSentence));
                }
                else if (row.deprel != null) {
                    problem("Illegal basic dependency structure - token [" + form(row)
                            + "] is dependent of more than one dependency" + in(aSentence));
                }
                else {
                    // The governor is validated before the dependency is assigned so that in
                    // lenient mode, an invalid dependency does not displace a valid one.
                    Row governor = governorRow(aRows, rel, aSentence, aSkippedTokens,
                            aUnreportedSkippedTokens);
                    if (governor != null && governor.emptyNode) {
                        problem("Illegal basic dependency structure - token [" + form(row)
                                + "] has the empty node [" + governor.id + "] as its governor, "
                                + "but the HEAD column can only refer to regular tokens"
                                + in(aSentence));
                    }
                    else if (governor != null) {
                        row.deprel = rel;
                        row.head = governor;
                    }
                }
            }
        }
    }

    /**
     * Tokens and dependencies which are not covered by any sentence cannot be written.
     */
    private void reportUncovered(JCas aJCas, Set<Token> aWrittenTokens, Set<Token> aSkippedTokens,
            Map<Token, List<Dependency>> aDependenciesByDependent)
    {
        // Skipped empty nodes and their dependencies have already been reported.
        List<Token> uncoveredTokens = select(aJCas, Token.class).stream() //
                .filter(t -> !aWrittenTokens.contains(t) && !aSkippedTokens.contains(t)) //
                .collect(toList());
        if (!uncoveredTokens.isEmpty()) {
            Token example = uncoveredTokens.get(0);
            problem(uncoveredTokens.size() + " token(s) are not covered by any sentence, e.g. ["
                    + example.getCoveredText() + "] at [" + example.getBegin() + "-"
                    + example.getEnd() + "]");
        }

        List<Dependency> uncoveredDependencies = aDependenciesByDependent.entrySet().stream() //
                .filter(e -> !aWrittenTokens.contains(e.getKey())
                        && !aSkippedTokens.contains(e.getKey())) //
                .flatMap(e -> e.getValue().stream()) //
                .collect(toList());
        if (!uncoveredDependencies.isEmpty()) {
            Dependency example = uncoveredDependencies.get(0);
            problem(uncoveredDependencies.size()
                    + " dependencies are not covered by any sentence, e.g. dependency ["
                    + example.getDependencyType() + "] at [" + example.getBegin() + "-"
                    + example.getEnd() + "]");
        }
    }

    /**
     * Build the rows of a sentence in output order. Regular tokens are numbered in the order of the
     * CAS index. Empty nodes cannot be ordered via the CAS index because several of them may share
     * the same offsets - instead, an empty node {@code i.k} is placed after the regular token
     * {@code i} (or at the start of the sentence for {@code 0.k}), ordered by {@code k}. The empty
     * nodes following a token are renumbered {@code i.1}, {@code i.2}, ... so that the output is
     * valid also if some of them have been removed.
     *
     * @param aSkippedTokens
     *            receives the tokens which cannot be written (lenient mode).
     */
    private Map<Token, Row> buildRows(JCas aJCas, Sentence aSentence, IntSet aTouchOffsets,
            Set<Token> aSkippedTokens)
    {
        List<Token> tokens = selectCovered(Token.class, aSentence);

        List<Token> regularTokens = new ArrayList<>();
        // Empty nodes refer to the regular token they follow by its index among all regular tokens
        // of the sentence. If tokens are skipped, the index changes: element i is the number of
        // regular tokens written among the first i regular tokens.
        IntList writtenBefore = new IntArrayList();
        writtenBefore.add(0);
        List<Token> emptyNodes = new ArrayList<>();
        for (Token token : tokens) {
            if (!isEmptyNode(token)) {
                // CoNLL-U requires every word to have a form. Writing `_` instead would be read
                // back as a literal underscore.
                if (aSkippedTokens.contains(token)) {
                    // A zero-width token at the boundary of touching sentences is covered by both
                    // of them - it has been reported for the first one already.
                }
                else if (StringUtils.isBlank(form(token, false))) {
                    aSkippedTokens.add(token);
                    problem("Cannot write token at [" + token.getBegin() + "-" + token.getEnd()
                            + "] - its form is empty or consists only of whitespace, but CoNLL-U "
                            + "requires every word to have a form" + in(aSentence));
                }
                else {
                    regularTokens.add(token);
                }
                writtenBefore.add(regularTokens.size());
                continue;
            }

            if (aSkippedTokens.contains(token)) {
                // Already reported for a touching neighbour sentence
                continue;
            }

            if (!writeDependency) {
                // Empty nodes only exist as part of the enhanced dependency graph - if dependencies
                // are not written, empty nodes are not written either.
                aSkippedTokens.add(token);
                continue;
            }

            if (!EMPTY_NODE_ID.matcher(token.getId()).matches()) {
                skipEmptyNode(token, "its ID must have the form i.k (e.g. 5.1)", aSentence,
                        aSkippedTokens);
                continue;
            }

            if (belongsToSentence(token, aSentence, aTouchOffsets)) {
                emptyNodes.add(token);
            }
        }

        Set<String> emptyNodeIds = new HashSet<>();
        Iterator<Token> emptyNodeValidator = emptyNodes.iterator();
        while (emptyNodeValidator.hasNext()) {
            Token emptyNode = emptyNodeValidator.next();
            if (wordIndex(emptyNode) >= writtenBefore.size()) {
                skipEmptyNode(emptyNode, "it refers to a word which does not exist", aSentence,
                        aSkippedTokens);
                emptyNodeValidator.remove();
            }
            else if (!emptyNodeIds.add(emptyNode.getId())) {
                skipEmptyNode(emptyNode, "another empty node has the same ID", aSentence,
                        aSkippedTokens);
                emptyNodeValidator.remove();
            }
        }
        emptyNodes.sort(comparing(Token::getId, NODE_ID_ORDER));

        Map<Token, Row> rows = new LinkedHashMap<>();
        Iterator<Token> emptyNodeIterator = emptyNodes.iterator();
        Token nextEmptyNode = emptyNodeIterator.hasNext() ? emptyNodeIterator.next() : null;
        for (int i = 0; i <= regularTokens.size(); i++) {
            if (i > 0) {
                Row row = new Row();
                row.token = regularTokens.get(i - 1);
                row.tokenId = i;
                row.id = Integer.toString(i);
                row.noSpaceAfter = isNoSpaceAfter(aJCas, regularTokens, i - 1);
                rows.put(row.token, row);
            }

            // Empty nodes following the current regular token
            int k = 0;
            while (nextEmptyNode != null && writtenBefore.getInt(wordIndex(nextEmptyNode)) == i) {
                k++;
                Row row = new Row();
                row.token = nextEmptyNode;
                row.emptyNode = true;
                row.id = i + "." + k;
                rows.put(row.token, row);
                nextEmptyNode = emptyNodeIterator.hasNext() ? emptyNodeIterator.next() : null;
            }
        }

        return rows;
    }

    private void skipEmptyNode(Token aEmptyNode, String aReason, Sentence aSentence,
            Set<Token> aSkippedTokens)
    {
        aSkippedTokens.add(aEmptyNode);
        problem("Cannot write empty node [" + aEmptyNode.getId() + "] - " + aReason
                + in(aSentence));
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
            if (StringUtils.isEmpty(sf.getValue()) || sf.getBegin() < pos) {
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
        String form = form(aRow.token, aRow.emptyNode);
        // Only empty nodes may lack a form - regular tokens without a form are not written.
        return StringUtils.isEmpty(form) ? UNUSED : form;
    }

    private String form(Token aToken, boolean aEmptyNode)
    {
        // An empty node covers no text, so its form can only come from the token form.
        return (writeCovered && !aEmptyNode) ? aToken.getCoveredText() : aToken.getText();
    }

    private void writeBoundaryComment(PrintWriter aOut, String aKey, String aIdKey, String aId,
            Sentence aSentence)
    {
        // A blank ID cannot be written - `# newpar id = ` is invalid - so the boundary is written
        // without an ID.
        if (StringUtils.isNotBlank(aId)) {
            aOut.printf(ROOT, "# %s = %s\n", aIdKey, comment(aIdKey, aId, aSentence));
        }
        else {
            aOut.printf(ROOT, "# %s\n", aKey);
        }
    }

    private static boolean covers(Annotation aOuter, Annotation aInner)
    {
        return aOuter.getBegin() <= aInner.getBegin() && aInner.getEnd() <= aOuter.getEnd();
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
     * Report data which cannot be written. By default, this fails - in lenient mode, a warning is
     * logged and the data is skipped by the caller.
     */
    private void problem(String aMessage)
    {
        problem(aMessage, "skipped");
    }

    /**
     * Report data which cannot be written properly. By default, this fails - in lenient mode, a
     * warning is logged and the caller handles the data as described by the given outcome.
     */
    private void problem(String aMessage, String aLenientOutcome)
    {
        if (!lenient) {
            throw new IllegalStateException(aMessage + ".");
        }
        getLogger().warn(aMessage + " - " + aLenientOutcome + " (lenient mode).");
    }

    /**
     * Describe the location of a sentence for messages - by its ID if it has one, otherwise by its
     * offsets.
     */
    private static String in(Sentence aSentence)
    {
        if (aSentence.getId() != null) {
            return " - in sentence [" + aSentence.getId() + "]";
        }
        return " - in sentence at [" + aSentence.getBegin() + "-" + aSentence.getEnd() + "]";
    }

    /**
     * CoNLL-U has no escape syntax, so a column value containing a tab or line break would corrupt
     * the file. In lenient mode, such characters are replaced with spaces.
     */
    private String column(String aColumn, String aValue, Sentence aSentence)
    {
        String value = aValue;
        if (StringUtils.containsAny(value, '\t', '\n', '\r')) {
            problem("Cannot write value [" + escape(value) + "] to column " + aColumn
                    + " - CoNLL-U does not support tabs or line breaks in column values"
                    + in(aSentence), "replaced with spaces");
            value = value.replaceAll("[\\t\\n\\r]", " ");
        }

        // Whitespace in these values would not survive being read back. In lenient mode, the
        // value is written as it is.
        if (NO_WHITESPACE_COLUMNS.contains(aColumn) && value.chars().anyMatch(c -> isSpace(c))) {
            problem("Cannot write value [" + value + "] to column " + aColumn
                    + " - CoNLL-U does not allow whitespace in this column" + in(aSentence),
                    "written as it is");
        }
        else if (!value.isEmpty()
                && (isSpace(value.charAt(0)) || isSpace(value.charAt(value.length() - 1)))) {
            problem("Cannot write value [" + value + "] to column " + aColumn
                    + " - CoNLL-U does not allow leading or trailing whitespace" + in(aSentence),
                    "written as it is");
        }
        return value;
    }

    private static boolean isSpace(int aChar)
    {
        return Character.isWhitespace(aChar) || Character.isSpaceChar(aChar);
    }

    /**
     * A comment ends at the end of the line, so a comment value containing a line break would turn
     * the remainder of the value into an invalid line. In lenient mode, line breaks are replaced
     * with spaces.
     */
    private String comment(String aComment, String aValue, Sentence aSentence)
    {
        if (StringUtils.containsAny(aValue, '\n', '\r')) {
            problem("Cannot write value [" + escape(aValue) + "] to comment # " + aComment
                    + " - CoNLL-U does not support line breaks in comments" + in(aSentence),
                    "replaced with spaces");
            return aValue.replaceAll("[\\n\\r]", " ");
        }
        return aValue;
    }

    private static String escape(String aValue)
    {
        return aValue.replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Resolve the row of the governor of the given dependency.
     *
     * @return the row or {@code null} if the governor cannot be resolved (lenient mode).
     */
    private Row governorRow(Map<Token, Row> aRows, Dependency aRel, Sentence aSentence,
            Set<Token> aSkippedTokens, Set<Token> aUnreportedSkippedTokens)
    {
        if (aRel.getGovernor() == null) {
            problem("Illegal dependency structure - dependency [" + aRel.getDependencyType()
                    + "] has no governor" + in(aSentence));
            return null;
        }

        if (aSkippedTokens.contains(aRel.getGovernor())) {
            // If the governor has been reported as a problem, the dependency is skipped along with
            // it. Otherwise (e.g. the governor is an empty node in a sentence without words, which
            // is skipped also in strict mode), the dependency must not vanish silently.
            if (aUnreportedSkippedTokens.contains(aRel.getGovernor())) {
                problem("Illegal dependency structure - dependency [" + aRel.getDependencyType()
                        + "] has a governor in a sentence which is skipped" + in(aSentence));
            }
            return null;
        }

        Row governor = aRows.get(aRel.getGovernor());
        if (governor == null) {
            // CoNLL-U addresses heads by their in-sentence index, so a governor outside the
            // sentence cannot be expressed.
            problem("Illegal dependency structure - dependency [" + aRel.getDependencyType()
                    + "] has a governor which is not part of the sentence" + in(aSentence));
        }
        return governor;
    }

    private static boolean isBasic(Dependency aRel)
    {
        return StringUtils.isBlank(aRel.getFlavor())
                || DependencyFlavor.BASIC.equals(aRel.getFlavor());
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

        if (aSentence.getBegin() == aSentence.getEnd()) {
            // A zero-width sentence at the boundary contains no words the node could belong to.
            return false;
        }

        if (wordIndex(aEmptyNode) == 0) {
            return aEmptyNode.getBegin() == aSentence.getBegin();
        }

        return aEmptyNode.getBegin() > aSentence.getBegin();
    }

    /**
     * The index of the regular word an empty node {@code i.k} follows, i.e. {@code i}. The ID of
     * the empty node must be valid.
     */
    private static int wordIndex(Token aEmptyNode)
    {
        return emptyNodeWordIndex(aEmptyNode.getId());
    }

    /**
     * Unspecified values are written as {@code _}.
     */
    private String value(String aColumn, String aValue, Sentence aSentence)
    {
        return StringUtils.isBlank(aValue) ? UNUSED : column(aColumn, aValue, aSentence);
    }

    /**
     * The DEPS column has no notation for an unspecified relation, and the relation must not
     * contain the {@code |} character which separates the items of the column.
     *
     * @return the relation or {@code null} if it cannot be written (lenient mode).
     */
    private String depsRelation(Dependency aRel, Sentence aSentence)
    {
        String type = aRel.getDependencyType();
        if (StringUtils.isBlank(type)) {
            problem("Cannot write an enhanced dependency without a relation type to column DEPS"
                    + in(aSentence));
            return null;
        }
        if (type.indexOf('|') >= 0) {
            problem("Cannot write relation [" + type + "] to column DEPS - the character [|] "
                    + "separates the items of that column" + in(aSentence));
            return null;
        }
        return column("DEPS", type, aSentence);
    }

    /**
     * CoNLL-U requires the features to be sorted alphabetically by name (case-insensitive). Data
     * read from CoNLL-U is already sorted, but data from other sources may not be.
     */
    private static String sortFeatures(String aFeats)
    {
        if (StringUtils.isBlank(aFeats)) {
            return aFeats;
        }

        return Arrays.stream(aFeats.split("\\|")) //
                .sorted(comparing(feature -> StringUtils.substringBefore(feature, "="),
                        String.CASE_INSENSITIVE_ORDER)) //
                .collect(joining("|"));
    }

    private static String label(SurfaceForm aSurfaceForm)
    {
        return aSurfaceForm.getValue() != null ? aSurfaceForm.getValue()
                : aSurfaceForm.getCoveredText();
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

        /**
         * The row of the governor of {@link #deprel} - the row itself for the root.
         */
        Row head;
        List<Dependency> deps = new ArrayList<>();
    }
}
