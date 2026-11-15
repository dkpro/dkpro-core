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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.dkpro.core.testing.AssertAnnotations.assertMorph;
import static org.dkpro.core.testing.AssertAnnotations.assertPOS;
import static org.dkpro.core.testing.AssertAnnotations.assertSentence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;

public class ConllUReaderTest
{
    @TempDir
    File tempDir;

    @Test
    public void test() throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(ConllUReader.class,
                ConllUReader.PARAM_LANGUAGE, "en", ConllUReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/conll/u/", ConllUReader.PARAM_PATTERNS,
                "conllu-en-orig.conllu");

        JCas jcas = new JCasIterable(reader).iterator().next();

        String[] sentences = { "They buy and sell books.", "I have not a clue." };

        String[] posMapped = { "POS", "POS_VERB", "POS_CONJ", "POS_VERB", "POS_NOUN", "POS_PUNCT",
                "POS", "POS_VERB", "POS_ADV", "POS_DET", "POS_NOUN", "POS_PUNCT" };

        String[] posOriginal = { "PRN", "VB", "CC", "VB", "NNS", ".", "PRN", "VB", "RB", "DT", "NN",
                "." };

        String[] morphologicalFeeatures = {
                "[  0,  4]     -     -  Nom    -    -     -    -    -  Plur      -  -    -    -    -     -      -     - They (Case=Nom|Number=Plur)",
                "[  5,  8]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -  Pres      -     - buy (Number=Plur|Person=3|Tense=Pres)",
                "[ 13, 17]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -  Pres      -     - sell (Number=Plur|Person=3|Tense=Pres)",
                "[ 18, 23]     -     -    -    -    -     -    -    -  Plur      -  -    -    -    -     -      -     - books (Number=Plur)",
                "[ 25, 26]     -     -  Nom    -    -     -    -    -  Sing      -  1    -    -    -     -      -     - I (Case=Nom|Number=Sing|Person=1)",
                "[ 27, 31]     -     -    -    -    -     -    -    -  Sing      -  1    -    -    -  Pres      -     - have (Number=Sing|Person=1|Tense=Pres)",
                "[ 32, 35]     -     -    -    -    -     -    -  Neg     -      -  -    -    -    -     -      -     - not (Negative=Neg)",
                "[ 36, 37]     -     -    -    -    -     -    -    -     -      -  -    -  Art    -     -      -     - a (Definite=Ind|PronType=Art)",
                "[ 38, 42]     -     -    -    -    -     -    -    -  Sing      -  -    -    -    -     -      -     - clue (Number=Sing)" };

        assertSentence(sentences, select(jcas, Sentence.class));
        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertMorph(morphologicalFeeatures, select(jcas, MorphologicalFeatures.class));
    }

    @Test
    public void testDocumentID() throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(ConllUReader.class,
                ConllUReader.PARAM_LANGUAGE, "en", ConllUReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/conll/u_v2/", ConllUReader.PARAM_PATTERNS,
                "conllu-paragraph_and_document_boundaries.conllu");

        JCas jcas = new JCasIterable(reader).iterator().next();

        AnnotationIndex<DocumentMetaData> index = jcas.getAnnotationIndex(DocumentMetaData.class);
        DocumentMetaData m = index.iterator().get();
        final String actualDocumentID = m.getDocumentId();
        final String expectedDocumentID = "mf920901-001";

        assertEquals(expectedDocumentID, actualDocumentID, "Document ID mismatch");
    }

    @Test
    public void testMultipleDocumentIDs() throws Exception
    {
        // final TestAppender appender = new TestAppender();
        // final Logger logger = Logger.getRootLogger();
        // logger.addAppender(appender);
        // try {
        CollectionReaderDescription reader = createReaderDescription(ConllUReader.class,
                ConllUReader.PARAM_LANGUAGE, "en", ConllUReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/conll/u_v2/", ConllUReader.PARAM_PATTERNS,
                "conllu-multiple_document_IDs.conllu");

        JCas jcas = new JCasIterable(reader).iterator().next();

        AnnotationIndex<DocumentMetaData> index = jcas.getAnnotationIndex(DocumentMetaData.class);
        DocumentMetaData m = index.iterator().get();
        final String actualDocumentID = m.getDocumentId();
        final String expectedDocumentID = "mf920901-001;mf920901-002";

        assertEquals(expectedDocumentID, actualDocumentID, "Document ID mismatch");
    }

    /**
     * Empty nodes have decimal IDs (e.g. {@code 5.1}) and represent elided material. They have no
     * surface form and are therefore modelled as zero-width tokens retaining their original ID.
     */
    @Test
    public void thatEmptyNodesAreReadAsZeroWidthTokens() throws Exception
    {
        JCas jcas = readString("1\tSue\tSue\t_\t_\t_\t2\tnsubj\t_\t_\n"
                + "2\tlikes\tlike\t_\t_\t_\t0\troot\t_\t_\n"
                + "3\tcoffee\tcoffee\t_\t_\t_\t2\tobj\t_\t_\n"
                + "4\tand\tand\t_\t_\t_\t6\tcc\t_\t_\n" + "5\tBill\tBill\t_\t_\t_\t6\tnsubj\t_\t_\n"
                + "5.1\tlikes\tlike\t_\t_\t_\t_\t_\t2:conj\t_\n"
                + "6\ttea\ttea\t_\t_\t_\t2\tconj\t_\t_\n", ConllUReader.PARAM_READ_EMPTY_NODES,
                true);

        // The empty node contributes no surface text.
        assertEquals("Sue likes coffee and Bill tea", jcas.getDocumentText());

        List<Token> tokens = new ArrayList<>(select(jcas, Token.class));
        assertEquals(7, tokens.size());

        // The empty node is sorted in at the position where it appears and is zero-width.
        Token emptyNode = tokens.get(5);
        assertEquals("5.1", emptyNode.getId());
        assertEquals(emptyNode.getBegin(), emptyNode.getEnd());
        assertEquals("", emptyNode.getCoveredText());
        assertEquals("like", emptyNode.getLemma().getValue());

        // Regular tokens do not carry an ID.
        assertNull(tokens.get(4).getId());
    }

    /**
     * An empty node following a token with {@code SpaceAfter=No} sits at the same offset as the
     * begin of the next token. It must still be sorted in between the two regular tokens so that
     * the original order is retained.
     */
    @Test
    public void thatEmptyNodeAfterNoSpaceAfterTokenRetainsOrder() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1\tobj\t1:obj\tSpaceAfter=No\n" //
                + "2.1\tc\tc\t_\t_\t_\t_\t_\t1:conj\t_\n" //
                + "3\td\td\t_\t_\t_\t1\tconj\t2.1:obj\t_\n", //
                ConllUReader.PARAM_READ_EMPTY_NODES, true);

        assertEquals("a bd", jcas.getDocumentText());

        List<String> tokens = select(jcas, Token.class).stream() //
                .map(t -> t.getId() != null ? t.getId() : t.getCoveredText()) //
                .collect(toList());

        assertEquals(asList("a", "b", "2.1", "d"), tokens);
    }

    /**
     * An empty node may precede the first word of a sentence (ID {@code 0.1}). It must be part of
     * that sentence, not of the preceding one.
     */
    @Test
    public void thatEmptyNodeAtSentenceStartBelongsToItsSentence() throws Exception
    {
        JCas jcas = readString("# newpar id = p1\n" //
                + "# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "\n" //
                + "# sent_id = s2\n" //
                + "0.1\tc\tc\t_\t_\t_\t_\t_\t0:root\t_\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t0.1:obj\t_\n", //
                ConllUReader.PARAM_READ_EMPTY_NODES, true);

        assertEquals("a b", jcas.getDocumentText());

        List<Sentence> sentences = new ArrayList<>(select(jcas, Sentence.class));
        assertEquals(2, sentences.size());

        List<String> s1Tokens = selectCovered(Token.class, sentences.get(0)).stream() //
                .map(t -> t.getId() != null ? t.getId() : t.getCoveredText()) //
                .collect(toList());
        List<String> s2Tokens = selectCovered(Token.class, sentences.get(1)).stream() //
                .map(t -> t.getId() != null ? t.getId() : t.getCoveredText()) //
                .collect(toList());

        assertEquals(asList("a"), s1Tokens);
        assertEquals(asList("0.1", "b"), s2Tokens);
    }

    /**
     * Empty nodes are discarded by default, because zero-width tokens are not generally considered
     * valid in DKPro Core.
     */
    @Test
    public void thatEmptyNodesAreDiscardedByDefault() throws Exception
    {
        JCas jcas = readString("1\tSue\tSue\t_\t_\t_\t2\tnsubj\t2:nsubj\t_\n"
                + "2\tlikes\tlike\t_\t_\t_\t0\troot\t0:root\t_\n"
                + "2.1\tlikes\tlike\t_\t_\t_\t_\t_\t2:conj\t_\n"
                + "3\ttea\ttea\t_\t_\t_\t2\tconj\t2.1:obj\t_\n");

        assertEquals("Sue likes tea", jcas.getDocumentText());

        List<Token> tokens = new ArrayList<>(select(jcas, Token.class));
        assertEquals(3, tokens.size());
        assertThat(tokens).allSatisfy(t -> assertThat(t.getBegin()).isLessThan(t.getEnd()));

        // The `2.1:obj` relation refers to a node which was not created and is therefore dropped.
        List<String> enhanced = select(jcas, Dependency.class).stream()
                .filter(d -> DependencyFlavor.ENHANCED.equals(d.getFlavor()))
                .map(Dependency::getDependencyType).collect(toList());
        assertEquals(asList("nsubj", "root"), enhanced);
    }

    /**
     * Enhanced dependencies may carry subtyped relations such as {@code conj:and}. The subtype is
     * part of the relation and must not be truncated.
     */
    @Test
    public void thatEnhancedDependencySubtypesArePreserved() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t2\tnsubj\t2:nsubj\t_\n"
                + "2\tb\tb\t_\t_\t_\t0\troot\t0:root\t_\n"
                + "3\tc\tc\t_\t_\t_\t2\tconj\t2:conj:and\t_\n");

        List<String> enhanced = select(jcas, Dependency.class).stream()
                .filter(d -> DependencyFlavor.ENHANCED.equals(d.getFlavor()))
                .map(Dependency::getDependencyType).collect(toList());

        assertEquals(asList("nsubj", "root", "conj:and"), enhanced);
    }

    /**
     * An enhanced dependency may point at an empty node. Since empty nodes are modelled as
     * zero-width tokens, such a relation is representable.
     */
    @Test
    public void thatEnhancedDependencyOnEmptyNodeIsResolved() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t2\tnsubj\t2:nsubj\t_\n"
                + "2\tb\tb\t_\t_\t_\t0\troot\t0:root\t_\n" + "2.1\tc\tc\t_\t_\t_\t_\t_\t2:conj\t_\n"
                + "3\td\td\t_\t_\t_\t2\tconj\t2.1:nsubj\t_\n", ConllUReader.PARAM_READ_EMPTY_NODES,
                true);

        assertEquals("a b d", jcas.getDocumentText());

        List<String> enhanced = select(jcas, Dependency.class).stream()
                .filter(d -> DependencyFlavor.ENHANCED.equals(d.getFlavor()))
                .map(Dependency::getDependencyType).collect(toList());

        // The `2:conj` of the empty node and the `2.1:nsubj` pointing at it are both represented.
        assertEquals(asList("nsubj", "root", "conj", "nsubj"), enhanced);

        // The `2.1:nsubj` relation has the empty node as its governor.
        Dependency onEmptyNode = select(jcas, Dependency.class).stream()
                .filter(d -> DependencyFlavor.ENHANCED.equals(d.getFlavor()))
                .filter(d -> "d".equals(d.getDependent().getCoveredText())).findFirst().get();
        assertEquals("2.1", onEmptyNode.getGovernor().getId());
    }

    /**
     * The FEATS field may contain items which are not key-value pairs. These are not valid UD
     * features, but they must not make the reader fail.
     */
    @Test
    public void thatFeatureWithoutValueDoesNotFail() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\tForeign\t0\troot\t_\t_\n"
                + "2\tb\tb\t_\t_\tNumber=Sing|Foreign\t1\tobj\t_\t_\n");

        List<String> morph = select(jcas, MorphologicalFeatures.class).stream()
                .map(MorphologicalFeatures::getValue).collect(toList());

        assertEquals(asList("Foreign", "Number=Sing|Foreign"), morph);
        assertEquals("Sing", select(jcas, MorphologicalFeatures.class).stream().skip(1).findFirst()
                .get().getNumber());
    }

    /**
     * A line may have an empty MISC field, which still needs to be counted as a field.
     */
    @Test
    public void thatTrailingEmptyFieldIsAccepted() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t0\troot\t_\t\n");

        assertEquals("a", jcas.getDocumentText());
        assertEquals(1, select(jcas, Token.class).size());
    }

    /**
     * A `SpaceAfter=No` on the last token of a sentence indicates that there is no whitespace
     * between this sentence and the next one (e.g. in Chinese or Japanese treebanks). Within a
     * paragraph, the following sentence must therefore directly follow the current one.
     */
    @Test
    public void thatSentenceFinalNoSpaceAfterIsHonoredInParagraph() throws Exception
    {
        JCas jcas = readString("# newpar id = p1\n" //
                + "# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\tSpaceAfter=No\n" //
                + "\n" //
                + "# sent_id = s2\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n");

        assertEquals("ab", jcas.getDocumentText());
    }

    /**
     * The spec allows `# newpar` and `# newdoc` without an ID.
     */
    @Test
    public void thatValuelessNewparIsDetected() throws Exception
    {
        JCas jcas = readString("# newpar\n" //
                + "# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newpar\n" //
                + "# sent_id = s2\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n");

        assertEquals("a\n\nb", jcas.getDocumentText());
        assertEquals(2, select(jcas, Paragraph.class).size());
    }

    /**
     * A document may start without a paragraph marker and only use one on a later sentence. The
     * marked sentence must still start a new paragraph instead of being attached to the previous
     * sentence.
     */
    @Test
    public void thatFirstNewparAfterUnmarkedSentenceStartsParagraph() throws Exception
    {
        JCas jcas = readString("# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newpar\n" //
                + "# sent_id = s2\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n");

        assertEquals("a\n\nb", jcas.getDocumentText());

        List<String> paragraphs = select(jcas, Paragraph.class).stream() //
                .map(Paragraph::getCoveredText) //
                .collect(toList());
        assertThat(paragraphs).contains("b");
    }

    /**
     * A `SpaceAfter=No` on a multi-word token line applies to the surface form as a whole.
     */
    @Test
    public void thatNoSpaceAfterOnMultiwordTokenIsHonored() throws Exception
    {
        JCas jcas = readString("1\tvamos\tir\t_\t_\t_\t0\troot\t_\t_\n"
                + "2-3\tdel\t_\t_\t_\t_\t_\t_\t_\tSpaceAfter=No\n"
                + "2\tde\tde\t_\t_\t_\t1\tcase\t_\t_\n" + "3\tel\tel\t_\t_\t_\t1\tdet\t_\t_\n"
                + "4\t.\t.\t_\t_\t_\t1\tpunct\t_\t_\n");

        assertEquals("vamos de el.", jcas.getDocumentText());
    }

    /**
     * A file may end directly after a comment marker without a trailing line break.
     */
    @Test
    public void thatTruncatedCommentDoesNotFail() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n\n#");

        assertEquals("a", jcas.getDocumentText());
    }

    /**
     * A HEAD referring to a token which does not exist in the sentence makes the file invalid. The
     * reader must fail with a message pointing at the offending head instead of creating a
     * dependency without a governor.
     */
    @Test
    public void thatBasicHeadReferringToMissingTokenIsRejected() throws Exception
    {
        assertThatThrownBy(() -> readString("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "2\tb\tb\t_\t_\t_\t9\tobj\t_\t_\n")) //
                        .rootCause() //
                        .isInstanceOf(IOException.class) //
                        .hasMessageContaining("9");
    }

    /**
     * An enhanced head referring to a token which does not exist in the sentence makes the file
     * invalid. The reader must fail with a message pointing at the offending head instead of
     * silently dropping the relation.
     */
    @Test
    public void thatEnhancedHeadReferringToMissingTokenIsRejected() throws Exception
    {
        assertThatThrownBy(() -> readString("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1\tobj\t9:obj\t_\n")) //
                        .rootCause() //
                        .isInstanceOf(IOException.class) //
                        .hasMessageContaining("9");
    }

    /**
     * If reading paragraphs is disabled, paragraph markers must neither affect the text layout nor
     * produce paragraph annotations - regardless of whether the marker carries an ID.
     */
    @Test
    public void thatParagraphsAreNotCreatedWhenReadingParagraphsIsDisabled() throws Exception
    {
        JCas jcas = readString("# newpar id = p1\n" //
                + "# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newpar\n" //
                + "# sent_id = s2\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n", //
                ConllUReader.PARAM_READ_PARAGRAPH, false);

        assertEquals("a\nb", jcas.getDocumentText());
        assertThat(select(jcas, Paragraph.class)).isEmpty();
    }

    /**
     * Read the given CoNLL-U snippet through a regular reader pipeline.
     */
    private JCas readString(String aConllU, Object... aParams) throws Exception
    {
        File file = new File(tempDir, "test.conllu");
        FileUtils.write(file, aConllU, UTF_8);

        List<Object> params = new ArrayList<>(asList(ConllUReader.PARAM_LANGUAGE, "en",
                ConllUReader.PARAM_SOURCE_LOCATION, file.getPath()));
        params.addAll(asList(aParams));

        CollectionReaderDescription reader = createReaderDescription(ConllUReader.class,
                params.toArray());

        return new JCasIterable(reader).iterator().next();
    }

    @Test
    public void testParagraphs() throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(ConllUReader.class,
                ConllUReader.PARAM_LANGUAGE, "en", ConllUReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/conll/u_v2/", ConllUReader.PARAM_PATTERNS,
                "conllu-multiple_paragraphs.conllu");

        JCas jcas = new JCasIterable(reader).iterator().next();

        AnnotationIndex<Paragraph> index = jcas.getAnnotationIndex(Paragraph.class);

        List<String> paragraphIDs = index.stream().map(Paragraph::getId).collect(toList());
        List<String> expectedParagraphIDs = asList("mf920901-001-p1", "mf920901-001-p2");

        assertEquals(expectedParagraphIDs, paragraphIDs);

        final String expectedTextContent = "Slovenská ústava: pro i proti Slovenská ústava: pro i"
                + " proti\n" + "\n" + "Slovenská ústava: pro i proti";
        final String actualTextContent = jcas.getDocumentText();
        assertEquals(expectedTextContent, actualTextContent);

        String[] sentences = { "Slovenská ústava: pro i proti", "Slovenská ústava: pro i proti",
                "Slovenská ústava: pro i proti" };
        assertSentence(sentences, select(jcas, Sentence.class));
    }
}
