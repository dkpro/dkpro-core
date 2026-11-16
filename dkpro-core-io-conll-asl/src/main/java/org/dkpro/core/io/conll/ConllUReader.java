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
import static de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor.ENHANCED;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.dkpro.core.api.resources.MappingProviderFactory.createPosMappingProvider;
import static org.dkpro.core.io.conll.internal.ConllUFormat.EMPTY_NODE_ID;
import static org.dkpro.core.io.conll.internal.ConllUFormat.HEAD_ID;
import static org.dkpro.core.io.conll.internal.ConllUFormat.META_DOCUMENT;
import static org.dkpro.core.io.conll.internal.ConllUFormat.META_PARAGRAPH;
import static org.dkpro.core.io.conll.internal.ConllUFormat.ROOT_ID;
import static org.dkpro.core.io.conll.internal.ConllUFormat.SPACE_AFTER_NO;
import static org.dkpro.core.io.conll.internal.ConllUFormat.UNUSED;
import static org.dkpro.core.io.conll.internal.ConllUFormat.WORD_ID;
import static org.dkpro.core.io.conll.internal.ConllUFormat.documentName;
import static org.dkpro.core.io.conll.internal.ConllUFormat.emptyNodeIndex;
import static org.dkpro.core.io.conll.internal.ConllUFormat.emptyNodeWordIndex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.CompressionUtils;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.io.conll.internal.ConllReader_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Div;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Document;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.SurfaceForm;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.TokenForm;
import eu.openminted.share.annotations.api.DocumentationResource;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Reads a file in the CoNLL-U format.
 * <p>
 * The document text is reconstructed from the words. Comments are mapped as follows:
 * <ul>
 * <li>{@code # newdoc} - a {@link Document} annotation covering the sentences of the document and
 * holding its ID (if any). The document ID of the CAS is always derived from the path of the file
 * (relative to the source location), also if the file contains only a single document.</li>
 * <li>{@code # newpar} - a {@link Paragraph} annotation covering the sentences of the paragraph and
 * holding its ID (if any).</li>
 * <li>{@code # sent_id} - the ID of the {@link Sentence}.</li>
 * </ul>
 * Other comments, including {@code # text}, are not retained. Of the MISC column, only
 * {@code SpaceAfter=No} is used.
 * 
 * @see <a href="https://universaldependencies.org/format.html">CoNLL-U Format</a>
 */
@ResourceMetaData(name = "CoNLL-U Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({ MimeTypes.TEXT_X_CONLL_U })
@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.SurfaceForm",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.TokenForm",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Document",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
public class ConllUReader
    extends ConllReader_ImplBase
{
    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String sourceEncoding;

    /**
     * Read fine-grained part-of-speech information.
     */
    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name = PARAM_READ_POS, defaultValue = "true")
    private boolean readPos;

    /**
     * Read coarse-grained part-of-speech information.
     */
    public static final String PARAM_READ_CPOS = ComponentParameters.PARAM_READ_CPOS;
    @ConfigurationParameter(name = PARAM_READ_CPOS, defaultValue = "true")
    private boolean readCPos;

    /**
     * Treat coarse-grained part-of-speech as fine-grained part-of-speech information.
     */
    public static final String PARAM_USE_CPOS_AS_POS = "useCPosAsPos";
    @ConfigurationParameter(name = PARAM_USE_CPOS_AS_POS, defaultValue = "false")
    private boolean useCPosAsPos;

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
    @ConfigurationParameter(name = PARAM_MAPPING_ENABLED, defaultValue = ComponentParameters.DEFAULT_MAPPING_ENABLED)
    protected boolean mappingEnabled;

    /**
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating the
     * mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    /**
     * Read morphological features.
     */
    public static final String PARAM_READ_MORPH = ComponentParameters.PARAM_READ_MORPH;
    @ConfigurationParameter(name = PARAM_READ_MORPH, defaultValue = "true")
    private boolean readMorph;

    /**
     * Read lemma information.
     */
    public static final String PARAM_READ_LEMMA = ComponentParameters.PARAM_READ_LEMMA;
    @ConfigurationParameter(name = PARAM_READ_LEMMA, defaultValue = "true")
    private boolean readLemma;

    /**
     * Read syntactic dependency information. If disabled, the HEAD, DEPREL and DEPS columns are
     * neither read nor validated - they may even be empty.
     */
    public static final String PARAM_READ_DEPENDENCY = ComponentParameters.PARAM_READ_DEPENDENCY;
    @ConfigurationParameter(name = PARAM_READ_DEPENDENCY, defaultValue = "true")
    private boolean readDependency;

    /**
     * Read paragraph information. If enabled, {@code # newpar} comments are turned into
     * {@link Paragraph} annotations; sentences within a paragraph are separated by a space and
     * paragraphs by an empty line. If no paragraph information is provided in the file, or if set
     * to false, then each sentence is placed on its own line and no paragraphs are created.
     * Documents are always separated by an empty line.
     */
    public static final String PARAM_READ_PARAGRAPH = ComponentParameters.PARAM_READ_PARAGRAPH;
    @ConfigurationParameter(name = PARAM_READ_PARAGRAPH, defaultValue = "true")
    private boolean readParagraph;

    /**
     * Read empty nodes. Empty nodes have a decimal ID (e.g. {@code 5.1}) and are used in the
     * enhanced dependency representation to stand in for elided material. Since they have no
     * surface form, they are modelled as zero-width tokens which retain their original ID. If
     * disabled, empty nodes and any enhanced dependencies referring to them are discarded and a
     * warning reporting the number of discarded empty nodes and dependencies is logged.
     * <p>
     * Note that if two sentences directly touch (i.e. there is no whitespace between them), an
     * empty node at their boundary is covered by both sentences. Its ID determines which sentence
     * it belongs to: {@code 0.k} belongs to the following sentence, {@code i.k} to the preceding
     * one.
     * <p>
     * This is disabled by default because zero-width tokens are not generally considered valid in
     * DKPro Core - components may assume that every token covers at least one character. Enable
     * this only if the consumers of the CAS are prepared to handle zero-width tokens.
     */
    public static final String PARAM_READ_EMPTY_NODES = "readEmptyNodes";
    @ConfigurationParameter(name = PARAM_READ_EMPTY_NODES, defaultValue = "false")
    private boolean readEmptyNodes;

    /**
     * Accept deviations from the CoNLL-U specification which older versions of the DKPro Core
     * CoNLL-U writer (and other tools) produce, and log a warning instead of failing. This
     * concerns:
     * <ul>
     * <li>{@code SpaceAfter=No} on the words of a multi-word token - it is applied to the words as
     * if they were tokens;</li>
     * <li>empty LEMMA, UPOS, XPOS, FEATS, HEAD, DEPREL or DEPS columns - they are treated as
     * unspecified ({@code _});</li>
     * <li>comment lines between the words of a sentence, or followed by an empty line or the end of
     * the file - they are ignored;</li>
     * <li>malformed {@code # newdoc} or {@code # newpar} comments (e.g. {@code # newdoc idd = x} or
     * {@code # newpar id =}) - they mark a boundary without an ID;</li>
     * <li>empty nodes without enhanced dependencies (DEPS) - they are read without them;</li>
     * <li>HEAD or DEPS heads which refer to the word itself or to a word which does not exist - the
     * dependency is dropped. An unlabeled HEAD referring to the word itself is always accepted and
     * means that the word has not been parsed.</li>
     * </ul>
     * The warnings are aggregated per document. By default, such input makes the reader fail.
     */
    public static final String PARAM_LENIENT = "lenient";
    @ConfigurationParameter(name = PARAM_LENIENT, defaultValue = "false")
    private boolean lenient;

    private static final int ID = 0;
    private static final int FORM = 1;
    private static final int LEMMA = 2;
    private static final int CPOSTAG = 3;
    private static final int POSTAG = 4;
    private static final int FEATS = 5;
    private static final int HEAD = 6;
    private static final int DEPREL = 7;
    private static final int DEPS = 8;
    private static final int MISC = 9;

    public static final String META_SEND_ID = "sent_id";
    public static final String META_DOCUMENT_ID = "newdoc id";
    public static final String META_PARAGRAPH_ID = "newpar id";
    public static final String META_TEXT = "text";

    /**
     * A {@code # newdoc} or {@code # newpar} comment - group 1 is the keyword, group 2 the rest of
     * the line.
     */
    private static final Pattern BOUNDARY_COMMENT = Pattern
            .compile("\\s*(" + META_DOCUMENT + "|" + META_PARAGRAPH + ")\\b(.*)");

    /**
     * The rest of a {@code # newdoc id = ...} or {@code # newpar id = ...} comment - group 1 is the
     * ID.
     */
    private static final Pattern BOUNDARY_ID = Pattern.compile("\\s+id\\s*=(.*\\S.*)");

    /**
     * The {@link Div#getDivType() div type} of the {@link Document} annotations created for
     * {@code # newdoc} comments.
     */
    private static final String DIV_TYPE_DOCUMENT = "doc";

    /**
     * The {@link Div#getDivType() div type} of the {@link Paragraph} annotations created for
     * {@code # newpar} comments - {@code p} as the HTML tag for paragraphs.
     */
    private static final String DIV_TYPE_PARAGRAPH = "p";

    private static final String FEATURE_VALUE = "value";

    private static final String[] COLUMN_NAMES = { "ID", "FORM", "LEMMA", "UPOS", "XPOS", "FEATS",
            "HEAD", "DEPREL", "DEPS", "MISC" };

    private MappingProvider posMappingProvider;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        posMappingProvider = createPosMappingProvider(this, posMappingLocation, posTagset,
                getLanguage());
    }

    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
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
        catch (IOException e) {
            // Name the file - the message itself only identifies the sentence within the file.
            throw new IOException("Unable to read [" + res.getLocation() + "]: " + e.getMessage(),
                    e);
        }
        finally {
            closeQuietly(reader);
        }
    }

    public void convert(JCas aJCas, BufferedReader aReader) throws IOException
    {
        if (readPos) {
            try {
                posMappingProvider.configure(aJCas.getCas());
            }
            catch (AnalysisEngineProcessException e) {
                throw new IOException(e);
            }
        }

        // Skip a UTF-8 byte order mark at the start of the file - it is not part of the content.
        aReader.mark(1);
        if (aReader.read() != '\uFEFF') {
            aReader.reset();
        }

        JCasBuilder builder = new JCasBuilder(aJCas);

        Paragraph paragraph = null;
        Document document = null;
        int lastSentenceEndPosition = 0;
        boolean shouldAddSpace = false;
        int droppedEmptyNodes = 0;
        int droppedEmptyNodeRelations = 0;
        // Deviations accepted in lenient mode, by kind - reported once per document
        Map<String, LenientIssue> lenientIssues = new LinkedHashMap<>();
        while (true) {
            // Read sentence comments (if any)
            List<String> commentLines = new ArrayList<>();
            Map<String, String> comments = readSentenceComments(aReader, commentLines,
                    lenientIssues);

            boolean sentenceStartsParagraph = comments.containsKey(META_PARAGRAPH_ID)
                    || comments.containsKey(META_PARAGRAPH);
            boolean sentenceStartsDocument = comments.containsKey(META_DOCUMENT_ID)
                    || comments.containsKey(META_DOCUMENT);

            // Read sentence
            List<String[]> words = readSentence(aReader, lenientIssues);

            // Comments belong to the sentence they directly precede. Comments followed by an empty
            // line or by the end of the file belong to no sentence - and since they may carry
            // relevant information (e.g. a document boundary), they must not be dropped silently.
            // Older versions of the reader ignored them - in lenient mode, they are ignored.
            if ((words == null || words.isEmpty()) && !commentLines.isEmpty()) {
                problem(lenientIssues, "comments not directly preceding a sentence",
                        "Invalid file format. Comment lines must directly precede the "
                                + "words of a sentence, but [" + commentLines.get(0)
                                + "] is followed by "
                                + (words == null ? "the end of the file." : "an empty line."));
            }

            if (words == null) {
                // End of file
                break;
            }

            if (words.isEmpty()) {
                // Ignore empty sentences. This can happen when there are multiple end-of-sentence
                // markers following each other.
                continue;
            }

            boolean boundary = (readParagraph && sentenceStartsParagraph) || sentenceStartsDocument;
            // A sentence continues the current paragraph if a paragraph is open and the sentence
            // does not start a new paragraph or document.
            addSentenceSeparator(builder, boundary, !shouldAddSpace,
                    !boundary && paragraph != null);
            shouldAddSpace = false;

            int sentenceBegin = builder.getPosition();
            int sentenceEnd = sentenceBegin;

            int surfaceBegin = -1;
            int surfaceEnd = -1;
            String surfaceRange = null;
            String surfaceString = null;
            boolean mwtNoSpaceAfter = false;

            // Tokens, Lemma, POS
            Int2ObjectMap<Token> tokens = new Int2ObjectOpenHashMap<>();
            // Empty nodes are addressed by their decimal ID (e.g. `5.1`), so they cannot be held in
            // the integer-keyed token map.
            Map<String, Token> emptyNodes = new LinkedHashMap<>();
            // The IDs of all empty nodes in the sentence - also if empty nodes are not read
            Set<String> emptyNodeIds = new HashSet<>();
            int lastEmptyNodeK = 0;
            Iterator<String[]> wordIterator = words.iterator();
            while (wordIterator.hasNext()) {
                String[] word = wordIterator.next();

                String idValue = trim(word[ID]);
                String formValue = trim(word[FORM]);
                if (formValue.isEmpty()) {
                    throw new IOException("Line [" + idValue + "] has an empty FORM column - in "
                            + "sentence " + sentenceDescription(comments, words));
                }
                // If dependencies are not read, their columns are not validated either.
                int lastValidatedColumn = readDependency ? DEPS : FEATS;
                for (int column = LEMMA; column <= lastValidatedColumn; column++) {
                    if (trim(word[column]).isEmpty()) {
                        // Older versions of the writer produced empty columns for empty values.
                        problem(lenientIssues, "empty columns",
                                "Line [" + idValue + "] has an empty " + COLUMN_NAMES[column]
                                        + " column - unspecified values must be written as [_] - "
                                        + "in sentence " + sentenceDescription(comments, words));
                        word[column] = UNUSED;
                    }
                }

                if (idValue.contains("-")) {
                    int[] range = parseRange(idValue, comments, words);
                    if (surfaceRange != null || range[0] != tokens.size() + 1) {
                        throw new IOException("Multi-word token range [" + idValue
                                + "] must start at the word directly following it - in sentence "
                                + sentenceDescription(comments, words));
                    }
                    surfaceBegin = range[0];
                    surfaceEnd = range[1];
                    surfaceRange = idValue;
                    surfaceString = formValue;
                    // A multi-word token line carries the SpaceAfter information for the surface
                    // form. The words it covers do not repeat it, so it must be picked up here -
                    // but it only takes effect after the last covered word has been written.
                    mwtNoSpaceAfter = isNoSpaceAfter(word[MISC]);
                    continue;
                }

                // Empty nodes (decimal IDs such as `5.1`) represent elided material in the enhanced
                // dependency graph. They contribute no surface text, so they are modelled as
                // zero-width tokens which retain their original ID. This keeps them addressable as
                // governors of enhanced dependencies without affecting the document text.
                if (idValue.contains(".")) {
                    // Validate the ID even if empty nodes are not read - the file is invalid
                    // either way.
                    if (!EMPTY_NODE_ID.matcher(idValue).matches()) {
                        throw new IOException("Invalid empty node ID [" + idValue
                                + "] - in sentence " + sentenceDescription(comments, words));
                    }

                    // Empty nodes i.1, i.2, ... must directly follow word i (or precede the first
                    // word for 0.k).
                    int wordIdx = emptyNodeWordIndex(idValue);
                    int k = emptyNodeIndex(idValue);
                    if (wordIdx != tokens.size() || k != lastEmptyNodeK + 1) {
                        throw new IOException("Empty node [" + idValue + "] is out of sequence - "
                                + "the empty nodes following word " + wordIdx + " must be numbered "
                                + wordIdx + ".1, " + wordIdx + ".2, ... and directly follow that "
                                + "word - in sentence " + sentenceDescription(comments, words));
                    }
                    lastEmptyNodeK = k;
                    emptyNodeIds.add(idValue);

                    if (!readEmptyNodes) {
                        droppedEmptyNodes++;
                    }
                    else {
                        int pos = wordIdx == 0 ? sentenceBegin : tokens.get(wordIdx).getEnd();
                        Token emptyNode = new Token(aJCas, pos, pos);
                        emptyNode.setId(idValue);
                        // The token covers no text, so the form has to be retained explicitly.
                        TokenForm form = new TokenForm(aJCas, pos, pos);
                        form.setValue(formValue);
                        form.addToIndexes();
                        emptyNode.setForm(form);
                        emptyNode.addToIndexes();
                        emptyNodes.put(idValue, emptyNode);
                        readTokenAnnotations(aJCas, emptyNode, word);
                    }
                    // An empty node has no surface form, so it must neither consume the pending
                    // space nor change whether the next token is preceded by one.
                    continue;
                }

                // Multi-word token lines and empty nodes have been handled above - they must not
                // insert a space.
                if (shouldAddSpace) {
                    builder.add(" ");
                }

                // Read token
                int tokenIdx = parseWordId(idValue, comments, words);
                if (tokenIdx != tokens.size() + 1) {
                    throw new IOException("Word ID [" + idValue
                            + "] is out of sequence - expected [" + (tokens.size() + 1)
                            + "] - in sentence " + sentenceDescription(comments, words));
                }
                lastEmptyNodeK = 0;
                Token token = builder.add(formValue, Token.class);
                tokens.put(tokenIdx, token);
                if (surfaceRange != null && isNoSpaceAfter(word[MISC])) {
                    // Older versions of the writer placed SpaceAfter=No on the words. In lenient
                    // mode, it is applied to the words as if they were tokens.
                    problem(lenientIssues, "SpaceAfter=No on words of multi-word tokens",
                            "Word [" + idValue + "] of multi-word token [" + surfaceRange
                                    + "] has SpaceAfter=No - SpaceAfter applies to tokens and must "
                                    + "be placed on the multi-word token line - in sentence "
                                    + sentenceDescription(comments, words));
                }
                shouldAddSpace = !isNoSpaceAfter(word[MISC]);

                readTokenAnnotations(aJCas, token, word);

                // Read surface form
                if (tokenIdx == surfaceEnd) {
                    int begin = tokens.get(surfaceBegin).getBegin();
                    int end = tokens.get(surfaceEnd).getEnd();
                    SurfaceForm surfaceForm = new SurfaceForm(aJCas, begin, end);
                    surfaceForm.setValue(surfaceString);
                    surfaceForm.addToIndexes();
                    surfaceBegin = -1;
                    surfaceEnd = -1;
                    surfaceRange = null;
                    surfaceString = null;
                    // The SpaceAfter of the multi-word token line governs what follows the surface
                    // form as a whole, so it overrides the value from the last covered word.
                    if (mwtNoSpaceAfter) {
                        shouldAddSpace = false;
                        mwtNoSpaceAfter = false;
                    }
                }

                sentenceEnd = token.getEnd();
            }

            if (surfaceRange != null) {
                throw new IOException("Multi-word token range [" + surfaceRange
                        + "] refers to words which do not exist - in sentence "
                        + sentenceDescription(comments, words));
            }

            if (tokens.isEmpty()) {
                throw new IOException("Sentence " + sentenceDescription(comments, words)
                        + " contains no words - a sentence must contain at least one word.");
            }

            // Dependencies
            if (readDependency) {
                for (String[] word : words) {
                    String idValue = trim(word[ID]);

                    // Multi-word token lines never bear dependencies.
                    if (idValue.contains("-")) {
                        continue;
                    }

                    boolean isEmptyNode = idValue.contains(".");

                    // Basic dependencies are only defined for regular tokens - the HEAD and DEPREL
                    // of an empty node are always unspecified.
                    if (!isEmptyNode) {
                        String depRelValue = cleanTag(word[DEPREL]);
                        String headValue = trim(word[HEAD]);

                        if (UNUSED.equals(headValue) && !UNUSED.equals(depRelValue)) {
                            throw new IOException("Word [" + idValue + "] has DEPREL ["
                                    + depRelValue + "] but no HEAD - in sentence "
                                    + sentenceDescription(comments, words));
                        }

                        // A HEAD without a DEPREL is an unlabeled dependency (e.g. not yet labeled
                        // during annotation) - unless it refers to the word itself, which some
                        // tools (e.g. spaCy without a parser) write to indicate that the word has
                        // not been parsed.
                        boolean unlabeled = UNUSED.equals(depRelValue);
                        if (!UNUSED.equals(headValue)
                                && !(unlabeled && headValue.equals(idValue))) {
                            Token dependent = tokens.get(Integer.parseInt(idValue));
                            if (headValue.contains(".")) {
                                throw new IOException("Word [" + idValue + "] has HEAD ["
                                        + headValue + "] which refers to an empty node, but HEAD "
                                        + "can only refer to a word or to the root (0) - in "
                                        + "sentence " + sentenceDescription(comments, words));
                            }
                            int headIdx = parseHead(headValue, "HEAD", idValue, comments, words);
                            boolean isRoot = headIdx == 0;
                            // The root is modelled as a loop of the dependent onto itself
                            Token governor = isRoot ? dependent : tokens.get(headIdx);

                            // Older versions of the reader accepted such dependencies. In lenient
                            // mode, they are dropped.
                            if (headIdx == Integer.parseInt(idValue)) {
                                problem(lenientIssues, "heads referring to the word itself",
                                        "Word [" + idValue + "] has HEAD [" + headValue
                                                + "] which refers to the word itself - in "
                                                + "sentence "
                                                + sentenceDescription(comments, words));
                            }
                            else if (governor == null) {
                                problem(lenientIssues, "heads referring to nonexistent words",
                                        "Word [" + idValue + "] has HEAD [" + headValue
                                                + "] which does not refer to a word in sentence "
                                                + sentenceDescription(comments, words));
                            }
                            else {
                                makeDependency(aJCas, governor, dependent,
                                        unlabeled ? null : depRelValue, BASIC, isRoot);
                            }
                        }
                    }

                    String depsValue = trim(word[DEPS]);
                    if (isEmptyNode && UNUSED.equals(depsValue)) {
                        // The writer writes such nodes in lenient mode - in lenient mode, they
                        // are read without enhanced dependencies.
                        problem(lenientIssues, "empty nodes without enhanced dependencies",
                                "Empty node [" + idValue + "] has no enhanced dependencies (DEPS) "
                                        + "- in sentence " + sentenceDescription(comments, words));
                    }

                    if (!UNUSED.equals(depsValue)) {
                        // Null if the dependent is an empty node which is not read - its
                        // dependencies are validated but dropped.
                        Token dependent = isEmptyNode ? emptyNodes.get(idValue)
                                : tokens.get(Integer.parseInt(idValue));

                        // list items separated by vertical bar
                        String[] items = depsValue.split("\\|", -1);
                        for (String item : items) {
                            if (StringUtils.isBlank(item)) {
                                throw new IOException("Word [" + idValue + "] has an invalid DEPS "
                                        + "column [" + depsValue
                                        + "] - it contains an empty item - " + "in sentence "
                                        + sentenceDescription(comments, words));
                            }

                            // The head is separated from the relation by the first colon. The
                            // relation itself may contain further colons in its subtypes, e.g.
                            // `5:conj:and`, which must be preserved.
                            String[] sItem = trim(item).split(":", 2);
                            if (sItem.length < 2 || StringUtils.isBlank(sItem[1])) {
                                throw new IOException("Word [" + idValue + "] has an invalid DEPS "
                                        + "item [" + trim(item) + "] - it must consist of a head "
                                        + "and a relation - in sentence "
                                        + sentenceDescription(comments, words));
                            }

                            String govValue = trim(sItem[0]);
                            if (govValue.equals(idValue)) {
                                // Older versions of the reader accepted such dependencies. In
                                // lenient mode, they are dropped.
                                problem(lenientIssues, "heads referring to the word itself",
                                        "Word [" + idValue + "] has a DEPS head [" + govValue
                                                + "] which refers to the word itself - in "
                                                + "sentence "
                                                + sentenceDescription(comments, words));
                                continue;
                            }

                            // A head may be an empty node (e.g. `5.1:nsubj`).
                            Token governor;
                            if (govValue.contains(".")) {
                                if (!EMPTY_NODE_ID.matcher(govValue).matches()) {
                                    throw new IOException("Word [" + idValue + "] has a DEPS head ["
                                            + govValue + "] which is not a valid ID - in sentence "
                                            + sentenceDescription(comments, words));
                                }
                                // Validate the head even if empty nodes are not read - the file is
                                // invalid either way.
                                if (!emptyNodeIds.contains(govValue)) {
                                    throw new IOException("Word [" + idValue + "] has a DEPS head ["
                                            + govValue + "] which does not refer to a node in "
                                            + "sentence " + sentenceDescription(comments, words));
                                }
                                governor = emptyNodes.get(govValue);
                            }
                            else if (ROOT_ID.equals(govValue)) {
                                // The artificial root is modelled as a loop onto the dependent.
                                governor = dependent;
                            }
                            else {
                                int headIdx = parseHead(govValue, "a DEPS head", idValue, comments,
                                        words);
                                if (!tokens.containsKey(headIdx)) {
                                    // Older versions of the reader accepted such dependencies. In
                                    // lenient mode, they are dropped.
                                    problem(lenientIssues, "heads referring to nonexistent words",
                                            "Word [" + idValue + "] has a DEPS head [" + govValue
                                                    + "] which does not refer to a node in "
                                                    + "sentence "
                                                    + sentenceDescription(comments, words));
                                    continue;
                                }
                                governor = tokens.get(headIdx);
                            }

                            if (!readEmptyNodes && (isEmptyNode || govValue.contains("."))) {
                                // Empty nodes are not read, so relations involving them are
                                // dropped - after they have been validated.
                                droppedEmptyNodeRelations++;
                                continue;
                            }

                            if (governor == null) {
                                throw new IOException("Word [" + idValue + "] has a DEPS head ["
                                        + govValue + "] which does not refer to a node in "
                                        + "sentence " + sentenceDescription(comments, words));
                            }

                            makeDependency(aJCas, governor, dependent, cleanTag(sItem[1]), ENHANCED,
                                    ROOT_ID.equals(govValue));
                        }
                    }
                }
            }

            // Sentence
            Sentence sentence = new Sentence(aJCas, sentenceBegin, sentenceEnd);
            sentence.setId(comments.get(META_SEND_ID));
            sentence.addToIndexes();

            // A document boundary ends the current sub-document as well as the current paragraph,
            // since a paragraph cannot span across documents.
            if (sentenceStartsDocument) {
                closeDiv(document, lastSentenceEndPosition);
                document = new Document(aJCas, sentenceBegin, sentenceEnd);
                // The ID is null if the document was marked using a valueless `# newdoc` comment.
                document.setId(comments.get(META_DOCUMENT_ID));
                document.setDivType(DIV_TYPE_DOCUMENT);

                closeDiv(paragraph, lastSentenceEndPosition);
                paragraph = null;
            }

            if (readParagraph && sentenceStartsParagraph) {
                closeDiv(paragraph, lastSentenceEndPosition);
                paragraph = new Paragraph(aJCas, sentenceBegin, sentenceEnd);
                // The ID is null if the paragraph was marked using a valueless `# newpar` comment.
                paragraph.setId(comments.get(META_PARAGRAPH_ID));
                paragraph.setDivType(DIV_TYPE_PARAGRAPH);
            }

            lastSentenceEndPosition = sentenceEnd;
        }

        closeDiv(paragraph, lastSentenceEndPosition);
        closeDiv(document, lastSentenceEndPosition);

        if (droppedEmptyNodes > 0 || droppedEmptyNodeRelations > 0) {
            getLogger().warn(String.format(
                    "Dropped %d empty node(s) and %d enhanced dependencies involving them in "
                            + "[%s] - set [%s] to true to keep them",
                    droppedEmptyNodes, droppedEmptyNodeRelations, documentName(aJCas),
                    PARAM_READ_EMPTY_NODES));
        }

        for (Entry<String, LenientIssue> issue : lenientIssues.entrySet()) {
            getLogger().warn(
                    String.format("Accepted %d case(s) of %s in [%s] (lenient mode), e.g.: %s",
                            issue.getValue().count, issue.getKey(), documentName(aJCas),
                            issue.getValue().example));
        }

        builder.close();
    }

    /**
     * Report input which violates the CoNLL-U specification but which can be read nevertheless. By
     * default, this fails - in lenient mode, the problem is recorded so that it can be reported
     * once per document, and the caller continues.
     *
     * @param aIssues
     *            the problems recorded so far, by kind.
     * @param aKind
     *            the kind of problem - problems of the same kind are reported together.
     * @param aMessage
     *            the description of this particular occurrence.
     */
    private void problem(Map<String, LenientIssue> aIssues, String aKind, String aMessage)
        throws IOException
    {
        if (!lenient) {
            throw new IOException(aMessage);
        }

        aIssues.computeIfAbsent(aKind, k -> new LenientIssue(aMessage)).count++;
    }

    /**
     * Add the text separating the next sentence from the previous one.
     *
     * @param aDoc
     *            the document builder.
     * @param aBoundary
     *            whether the next sentence starts a new paragraph or document.
     * @param aPreviousNoSpaceAfter
     *            whether the last word of the previous sentence had {@code SpaceAfter=No}.
     * @param aInParagraph
     *            whether the next sentence continues the current paragraph.
     */
    private static void addSentenceSeparator(JCasBuilder aDoc, boolean aBoundary,
            boolean aPreviousNoSpaceAfter, boolean aInParagraph)
    {
        if (aDoc.getPosition() == 0) {
            return;
        }

        if (aBoundary) {
            // Paragraphs and documents are always separated by an empty line.
            aDoc.add("\n\n");
        }
        else if (!aInParagraph) {
            // Sentences which are not part of a paragraph are placed on their own line. This line
            // break is a convention of this reader and not part of the CoNLL-U data, so a
            // sentence-final `SpaceAfter=No` does not remove it. Older treebanks also use such a
            // `SpaceAfter=No` where the original text actually had a line break.
            aDoc.add("\n");
        }
        else if (!aPreviousNoSpaceAfter) {
            // Sentences within a paragraph are separated by a space - unless the last word of the
            // previous sentence has `SpaceAfter=No`, in which case the next sentence follows
            // without any whitespace (e.g. in Chinese or Japanese text).
            aDoc.add(" ");
        }
    }

    /**
     * Parse the index of the word a head refers to.
     *
     * @throws IOException
     *             if the head is not a valid word index.
     */
    private static int parseHead(String aHead, String aLabel, String aWordId,
            Map<String, String> aComments, List<String[]> aWords)
        throws IOException
    {
        if (!HEAD_ID.matcher(aHead).matches()) {
            throw new IOException("Word [" + aWordId + "] has " + aLabel + " [" + aHead
                    + "] which is not a valid ID - in sentence "
                    + sentenceDescription(aComments, aWords));
        }
        return Integer.parseInt(aHead);
    }

    /**
     * Whether the given MISC field contains the item {@code SpaceAfter=No}. The item must match
     * exactly - other items merely containing this string (e.g. {@code CorrectSpaceAfter=No}) do
     * not count.
     */
    private static boolean isNoSpaceAfter(String aMisc)
    {
        for (String item : aMisc.split("\\|")) {
            if (SPACE_AFTER_NO.equals(item.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parse a multi-word token range such as {@code 2-3}.
     *
     * @return the IDs of the first and last word covered by the range.
     * @throws IOException
     *             if the range is malformed.
     */
    private static int[] parseRange(String aRange, Map<String, String> aComments,
            List<String[]> aWords)
        throws IOException
    {
        String[] fragments = aRange.split("-", -1);
        if (fragments.length == 2 && WORD_ID.matcher(fragments[0].trim()).matches()
                && WORD_ID.matcher(fragments[1].trim()).matches()) {
            int begin = Integer.parseInt(fragments[0].trim());
            int end = Integer.parseInt(fragments[1].trim());
            if (begin < end) {
                return new int[] { begin, end };
            }
        }

        throw new IOException("Invalid multi-word token range [" + aRange + "] - in sentence "
                + sentenceDescription(aComments, aWords));
    }

    /**
     * Parse the ID of a regular word.
     *
     * @throws IOException
     *             if the ID is not a positive integer.
     */
    private static int parseWordId(String aId, Map<String, String> aComments, List<String[]> aWords)
        throws IOException
    {
        if (!WORD_ID.matcher(aId).matches()) {
            throw new IOException("Invalid word ID [" + aId + "] - in sentence "
                    + sentenceDescription(aComments, aWords));
        }
        return Integer.parseInt(aId);
    }

    private static void closeDiv(Div aDiv, int aEnd)
    {
        if (aDiv != null) {
            aDiv.setEnd(aEnd);
            aDiv.addToIndexes();
        }
    }

    private static String sentenceDescription(Map<String, String> aComments, List<String[]> aWords)
    {
        String sentenceId = aComments.get(META_SEND_ID);
        if (sentenceId != null) {
            return "[" + sentenceId + "]";
        }

        return "starting with [" + String.join("\t", aWords.get(0)) + "]";
    }

    /**
     * Read the lemma, part-of-speech and morphological features of a word line and attach them to
     * the given token. The token may be a zero-width token representing an empty node.
     */
    private void readTokenAnnotations(JCas aJCas, Token aToken, String[] aWord)
    {
        // Read lemma
        String lemmaValue = trim(aWord[LEMMA]);
        if (!UNUSED.equals(lemmaValue) && readLemma) {
            Lemma lemma = new Lemma(aJCas, aToken.getBegin(), aToken.getEnd());
            lemma.setValue(lemmaValue);
            lemma.addToIndexes();
            aToken.setLemma(lemma);
        }

        // Read part-of-speech tag
        POS pos = null;
        String cPosTag = cleanTag(aWord[CPOSTAG]);
        String tag = useCPosAsPos ? cPosTag : cleanTag(aWord[POSTAG]);
        if (!UNUSED.equals(tag) && readPos) {
            Type posTag = posMappingProvider.getTagType(tag);
            pos = (POS) aJCas.getCas().createAnnotation(posTag, aToken.getBegin(), aToken.getEnd());
            pos.setPosValue(tag);
        }

        // Read coarse part-of-speech tag
        if (!UNUSED.equals(cPosTag) && readCPos) {
            if (pos == null) {
                pos = new POS(aJCas, aToken.getBegin(), aToken.getEnd());
            }
            pos.setCoarseValue(cPosTag);
        }

        if (pos != null) {
            pos.addToIndexes();
            aToken.setPos(pos);
        }

        // Read morphological features
        String featsValue = cleanTag(aWord[FEATS]);
        if (!UNUSED.equals(featsValue) && readMorph) {
            MorphologicalFeatures morphtag = new MorphologicalFeatures(aJCas, aToken.getBegin(),
                    aToken.getEnd());
            morphtag.setValue(featsValue);
            morphtag.addToIndexes();
            aToken.setMorph(morphtag);

            // Try parsing out individual feature values. Since the DKPro Core
            // MorphologicalFeatures type is based on the definition from the UD project,
            // we can do this rather straightforwardly.
            Type morphType = morphtag.getType();
            String[] items = featsValue.split("\\|");
            for (String item : items) {
                // Feature values may themselves contain `=`, so split only on the first one. Items
                // without any `=` have no corresponding feature and are therefore only retained as
                // part of the raw value.
                String[] keyValue = item.split("=", 2);
                if (keyValue.length < 2) {
                    continue;
                }
                StringBuilder key = new StringBuilder(trim(keyValue[0]));
                if (key.length() == 0) {
                    continue;
                }
                key.setCharAt(0, Character.toLowerCase(key.charAt(0)));
                String value = trim(keyValue[1]);

                Feature feat = morphType.getFeatureByBaseName(key.toString());
                // Only map onto the UD features declared by the morphological features type
                // itself - not onto inherited built-in features (e.g. `begin`) or onto the feature
                // holding the raw FEATS string.
                if (feat != null && morphType.equals(feat.getDomain())
                        && !FEATURE_VALUE.equals(feat.getShortName())
                        && CAS.TYPE_NAME_STRING.equals(feat.getRange().getName())) {
                    morphtag.setStringValue(feat, value);
                }
            }
        }
    }

    /**
     * Read the comment lines preceding a sentence.
     *
     * @param aCommentLines
     *            receives all non-empty comment lines as they appear in the file.
     * @param aIssues
     *            the problems accepted in lenient mode so far, by kind.
     * @return the recognized comments by key.
     */
    private Map<String, String> readSentenceComments(BufferedReader aReader,
            List<String> aCommentLines, Map<String, LenientIssue> aIssues)
        throws IOException
    {
        Map<String, String> comments = new LinkedHashMap<>();

        while (true) {
            // Check if the next line could be a header line
            aReader.mark(2);
            char character = (char) aReader.read();
            if ('#' == character) {
                // Read the rest of the line - this is null if the file ends directly after the
                // comment marker without a line break.
                String line = aReader.readLine();
                if (line == null) {
                    break;
                }

                if (!line.isBlank()) {
                    aCommentLines.add("#" + line);
                }

                Matcher boundary = BOUNDARY_COMMENT.matcher(line);
                if (boundary.matches()) {
                    readBoundaryComment(boundary, line, comments, aIssues);
                }
                else if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    comments.put(parts[0].trim(), parts[1].trim());
                }
            }
            else {
                aReader.reset();
                break;
            }
        }

        return comments;
    }

    /**
     * Read a {@code # newdoc} or {@code # newpar} comment. The valid forms are the valueless
     * comment, which marks a boundary without an ID, and {@code # newdoc id = ...} with a non-empty
     * ID. Since these comments carry structure, malformed variants (e.g. {@code # newdoc idd = x}
     * or {@code # newpar id =}) are not silently dropped - in lenient mode, they mark a boundary
     * without an ID.
     */
    private void readBoundaryComment(Matcher aBoundary, String aLine, Map<String, String> aComments,
            Map<String, LenientIssue> aIssues)
        throws IOException
    {
        String key = aBoundary.group(1);
        String rest = aBoundary.group(2);

        if (rest.isBlank()) {
            // Valueless comments mark a boundary without providing an ID. They are recorded with a
            // null value so that the boundary is still detected.
            aComments.put(key, null);
            return;
        }

        Matcher id = BOUNDARY_ID.matcher(rest);
        if (id.matches()) {
            aComments.put(key + " id", id.group(1).trim());
            return;
        }

        // If paragraphs are not read, `# newpar` comments are not validated either.
        if (META_PARAGRAPH.equals(key) && !readParagraph) {
            return;
        }

        problem(aIssues, "malformed boundary comments",
                "Invalid file format. Comment [#" + aLine + "] must be either [# " + key
                        + "] or [# " + key + " id = ...] with a non-empty " + "ID.");
        aComments.put(key, null);
    }

    /**
     * Read a single sentence.
     *
     * @param aIssues
     *            the problems accepted in lenient mode so far, by kind.
     */
    private List<String[]> readSentence(BufferedReader aReader, Map<String, LenientIssue> aIssues)
        throws IOException
    {
        List<String[]> words = new ArrayList<>();
        String line;
        while ((line = aReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                break; // End of sentence
            }
            if (line.startsWith("#")) {
                // Comments are only allowed before the words of a sentence. Older versions of the
                // reader ignored them - in lenient mode, they are ignored.
                problem(aIssues, "comments not directly preceding a sentence",
                        "Invalid file format. Comment lines must directly precede the "
                                + "words of a sentence, but [" + line
                                + "] appears between word lines.");
                continue;
            }
            // Use a negative limit so that trailing empty fields are retained - otherwise a line
            // with an empty MISC field would appear to have only 9 fields.
            String[] fields = line.split("\t", -1);
            // Tolerate trailing tabs after the MISC field, as long as they only add empty fields.
            if (fields.length > 10
                    && Arrays.stream(fields, 10, fields.length).allMatch(String::isEmpty)) {
                fields = Arrays.copyOf(fields, 10);
            }
            if (fields.length != 10) {
                throw new IOException(
                        "Invalid file format. Line needs to have 10 tab-separated fields, but it has "
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

    /**
     * Occurrences of one kind of problem accepted in lenient mode.
     */
    private static final class LenientIssue
    {
        final String example;
        int count;

        LenientIssue(String aExample)
        {
            example = aExample;
        }
    }
}
