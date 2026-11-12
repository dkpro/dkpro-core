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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.CompressionUtils;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.io.conll.internal.ConllReader_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
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
 * 
 * @see <a href="http://universaldependencies.github.io/docs/format.html">CoNLL-U Format</a>
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
     * Read syntactic dependency information.
     */
    public static final String PARAM_READ_DEPENDENCY = ComponentParameters.PARAM_READ_DEPENDENCY;
    @ConfigurationParameter(name = PARAM_READ_DEPENDENCY, defaultValue = "true")
    private boolean readDependency;

    /**
     * Read paragraph information. If no paragraph information is provided in the file, or if set to
     * false, then output one sentence per line, separated by an empty line.
     */
    public static final String PARAM_READ_PARAGRAPH = ComponentParameters.PARAM_READ_PARAGRAPH;
    @ConfigurationParameter(name = PARAM_READ_PARAGRAPH, defaultValue = "true")
    private boolean readParagraph;

    /**
     * Read empty nodes. Empty nodes have a decimal ID (e.g. {@code 5.1}) and are used in the
     * enhanced dependency representation to stand in for elided material. Since they have no
     * surface form, they are modelled as zero-width tokens which retain their original ID. If
     * disabled, empty nodes and any enhanced dependencies referring to them are discarded.
     * <p>
     * This is disabled by default because zero-width tokens are not generally considered valid in
     * DKPro Core - components may assume that every token covers at least one character. Enable
     * this only if the consumers of the CAS are prepared to handle zero-width tokens.
     */
    public static final String PARAM_READ_EMPTY_NODES = "readEmptyNodes";
    @ConfigurationParameter(name = PARAM_READ_EMPTY_NODES, defaultValue = "false")
    private boolean readEmptyNodes;

    private static final String UNUSED = "_";

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
     * Document boundary without an ID - the spec allows {@code # newdoc} without a value.
     */
    public static final String META_DOCUMENT = "newdoc";

    /**
     * Paragraph boundary without an ID - the spec allows {@code # newpar} without a value.
     */
    public static final String META_PARAGRAPH = "newpar";

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

        JCasBuilder doc = new JCasBuilder(aJCas);

        Paragraph p = null;
        int lastSentenceEndPosition = 0;
        boolean shouldAddSpace = false;
        Optional<Boolean> documentContainsParagraphInformation = Optional.empty();
        List<String> documentIDValues = new ArrayList<>();
        while (true) {
            // Read sentence comments (if any)
            Map<String, String> comments = readSentenceComments(aReader);

            boolean sentenceStartsParagraph = comments.keySet().contains(META_PARAGRAPH_ID)
                    || comments.keySet().contains(META_PARAGRAPH);

            // Whether the document uses paragraph markers is decided by the first sentence that
            // carries one. Once a marker has been seen, it remains in effect for the rest of the
            // document so that later markers are handled consistently with earlier ones.
            if (!documentContainsParagraphInformation.isPresent() || sentenceStartsParagraph) {
                documentContainsParagraphInformation = Optional.of(sentenceStartsParagraph);
            }

            // Read sentence
            List<String[]> words = readSentence(aReader);
            if (words == null) {
                // End of file
                break;
            }

            if (words.isEmpty()) {
                // Ignore empty sentences. This can happen when there are multiple end-of-sentence
                // markers following each other.
                continue;
            }
            if (comments.keySet().contains(META_DOCUMENT_ID)) {
                documentIDValues.add(comments.get(META_DOCUMENT_ID));
            }

            // A `SpaceAfter=No` on the last token of the previous sentence refers to the surface
            // text within that sentence and must not suppress the separator to the next one -
            // otherwise the two sentences would be glued together without any separator.
            if (!readParagraph || !documentContainsParagraphInformation.get()) {
                if (doc.getPosition() > 0) {
                    doc.add("\n");
                    shouldAddSpace = false;
                }
            }
            else if (readParagraph) {
                if (p != null && sentenceStartsParagraph) {
                    doc.add("\n\n");
                    shouldAddSpace = false;
                }
                else if (doc.getPosition() > 0) {
                    // Sentences within a paragraph are not separated by a line break, so a space
                    // is required to keep them apart.
                    shouldAddSpace = true;
                }
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            int surfaceBegin = -1;
            int surfaceEnd = -1;
            String surfaceString = null;
            boolean mwtNoSpaceAfter = false;

            // Tokens, Lemma, POS
            Int2ObjectMap<Token> tokens = new Int2ObjectOpenHashMap<>();
            // Empty nodes are addressed by their decimal ID (e.g. `5.1`), so they cannot be held in
            // the integer-keyed token map.
            Map<String, Token> emptyNodes = new LinkedHashMap<>();
            Iterator<String[]> wordIterator = words.iterator();
            while (wordIterator.hasNext()) {
                String[] word = wordIterator.next();

                String idValue = trim(word[ID]);
                String formValue = trim(word[FORM]);

                if (idValue.contains("-")) {
                    String[] fragments = idValue.split("-");
                    surfaceBegin = Integer.valueOf(trim(fragments[0]));
                    surfaceEnd = Integer.valueOf(trim(fragments[1]));
                    surfaceString = formValue;
                    // A multi-word token line carries the SpaceAfter information for the surface
                    // form. The words it covers do not repeat it, so it must be picked up here -
                    // but it only takes effect after the last covered word has been written.
                    mwtNoSpaceAfter = StringUtils.contains(word[MISC], "SpaceAfter=No");
                    continue;
                }

                // Empty nodes (decimal IDs such as `5.1`) represent elided material in the enhanced
                // dependency graph. They contribute no surface text, so they are modelled as
                // zero-width tokens which retain their original ID. This keeps them addressable as
                // governors of enhanced dependencies without affecting the document text.
                if (idValue.contains(".")) {
                    if (readEmptyNodes) {
                        int pos = doc.getPosition();
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

                // the following must be placed after check for dashes in ID in order not to insert
                // unnecessary spaces
                if (shouldAddSpace) {
                    if (doc.getPosition() == sentenceBegin) {
                        sentenceBegin++;
                    }
                    doc.add(" ");
                }

                // Read token
                int tokenIdx = Integer.valueOf(idValue);
                Token token = doc.add(formValue, Token.class);
                tokens.put(tokenIdx, token);
                shouldAddSpace = !StringUtils.contains(word[MISC], "SpaceAfter=No");

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

            // Dependencies
            if (readDependency) {
                for (String[] word : words) {
                    String idValue = trim(word[ID]);

                    // Multi-word token lines never bear dependencies.
                    if (idValue.contains("-")) {
                        continue;
                    }

                    boolean isEmptyNode = idValue.contains(".");

                    // The empty node was not created, so nothing can refer to it.
                    if (isEmptyNode && !readEmptyNodes) {
                        continue;
                    }

                    // Basic dependencies are only defined for regular tokens - the HEAD and DEPREL
                    // of an empty node are always unspecified.
                    if (!isEmptyNode) {
                        String depRelValue = cleanTag(word[DEPREL]);
                        String headValue = trim(word[HEAD]);

                        if (!UNUSED.equals(depRelValue) && !UNUSED.equals(headValue)) {
                            int depId = Integer.valueOf(idValue);
                            int govId = Integer.valueOf(headValue);

                            // Model the root as a loop onto itself
                            makeDependency(aJCas, govId, depId, depRelValue, BASIC, tokens, word);
                        }
                    }

                    String depsValue = trim(word[DEPS]);
                    if (!UNUSED.equals(depsValue)) {
                        Token dependent = isEmptyNode ? emptyNodes.get(idValue)
                                : tokens.get(Integer.parseInt(idValue));

                        // list items separated by vertical bar
                        String[] items = depsValue.split("\\|");
                        for (String item : items) {
                            // The head is separated from the relation by the first colon. The
                            // relation itself may contain further colons in its subtypes, e.g.
                            // `5:conj:and`, which must be preserved.
                            String[] sItem = trim(item).split(":", 2);
                            if (sItem.length < 2) {
                                continue;
                            }

                            String govValue = trim(sItem[0]);

                            // A head may be an empty node (e.g. `5.1:nsubj`).
                            Token governor;
                            if (govValue.contains(".")) {
                                governor = emptyNodes.get(govValue);
                            }
                            else if ("0".equals(govValue)) {
                                // The artificial root is modelled as a loop onto the dependent.
                                governor = dependent;
                            }
                            else {
                                governor = tokens.get(Integer.parseInt(govValue));
                            }

                            if (governor == null || dependent == null) {
                                // Refers to a node which does not exist - e.g. an empty node in a
                                // file where reading empty nodes is disabled.
                                continue;
                            }

                            makeDependency(aJCas, governor, dependent, cleanTag(sItem[1]),
                                    ENHANCED);
                        }
                    }
                }
            }

            // Sentence
            Sentence sentence = new Sentence(aJCas, sentenceBegin, sentenceEnd);
            sentence.setId(comments.get(META_SEND_ID));
            sentence.addToIndexes();

            if (sentenceStartsParagraph) {
                // May be null if the paragraph was marked using a valueless `# newpar` comment.
                final String paragraphID = comments.get(META_PARAGRAPH_ID);
                if (p != null) {
                    // do nothing
                    p.setEnd(lastSentenceEndPosition);
                    p.addToIndexes();
                }
                p = new Paragraph(aJCas, sentenceBegin, sentenceEnd);
                p.setId(paragraphID);
                p.setDivType("p"); // `p` for paragraph - using the HTML tag here
            }
            lastSentenceEndPosition = sentenceEnd;
        }
        if (p != null) {
            p.setEnd(lastSentenceEndPosition);
            p.addToIndexes();
        }
        if (documentIDValues.size() > 0) {
            DocumentMetaData m = DocumentMetaData.get(aJCas);
            String documentID = String.join(";", documentIDValues);
            if (documentIDValues.size() > 1) {
                final String fileUri = m.getDocumentUri();
                getLogger().warn(String.format("File %s contains multiple document IDs: %s",
                        fileUri, documentIDValues));

            }
            m.setDocumentId(documentID);
        }
        doc.close();
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
                if (feat != null) {
                    morphtag.setStringValue(feat, value);
                }
            }
        }
    }

    private Map<String, String> readSentenceComments(BufferedReader aReader) throws IOException
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

                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    comments.put(parts[0].trim(), parts[1].trim());
                }
                else {
                    // Valueless comments such as `# newdoc` and `# newpar` mark a boundary without
                    // providing an ID. They are recorded with an empty value so that the boundary
                    // is still detected.
                    String key = line.trim();
                    if (META_DOCUMENT.equals(key) || META_PARAGRAPH.equals(key)) {
                        comments.put(key, null);
                    }
                    else {
                        // Comment or unknown header line
                    }
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
     * Read a single sentence.
     */
    private static List<String[]> readSentence(BufferedReader aReader) throws IOException
    {
        List<String[]> words = new ArrayList<>();
        String line;
        while ((line = aReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                break; // End of sentence
            }
            if (line.startsWith("#")) {
                // Comment line
                continue;
            }
            // Use a negative limit so that trailing empty fields are retained - otherwise a line
            // with an empty MISC field would appear to have only 9 fields.
            String[] fields = line.split("\t", -1);
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
}
