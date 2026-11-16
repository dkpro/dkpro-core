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
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.dkpro.core.testing.AssertAnnotations.assertMorph;
import static org.dkpro.core.testing.AssertAnnotations.assertPOS;
import static org.dkpro.core.testing.AssertAnnotations.assertSentence;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Document;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.SurfaceForm;
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
        // The CAS document ID is always derived from the file name - the `# newdoc id` is retained
        // on the sub-document span instead.
        final String expectedDocumentID = "conllu-paragraph_and_document_boundaries.conllu";

        assertThat(actualDocumentID).as("Document ID mismatch").isEqualTo(expectedDocumentID);
        assertThat(subDocuments(jcas)).extracting(Document::getId).containsExactly("mf920901-001");
    }

    @Test
    public void testMultipleDocumentIDs() throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(ConllUReader.class,
                ConllUReader.PARAM_LANGUAGE, "en", ConllUReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/conll/u_v2/", ConllUReader.PARAM_PATTERNS,
                "conllu-multiple_document_IDs.conllu");

        JCas jcas = new JCasIterable(reader).iterator().next();

        AnnotationIndex<DocumentMetaData> index = jcas.getAnnotationIndex(DocumentMetaData.class);
        DocumentMetaData m = index.iterator().get();
        final String actualDocumentID = m.getDocumentId();
        // The file contains several documents. Their IDs are retained on the sub-document spans,
        // while the CAS document ID remains the one derived from the file name.
        final String expectedDocumentID = "conllu-multiple_document_IDs.conllu";
        assertThat(select(jcas, Document.class)).extracting(Document::getId) //
                .containsExactly("mf920901-001", "mf920901-002");

        assertThat(actualDocumentID).as("Document ID mismatch").isEqualTo(expectedDocumentID);
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
        assertThat(jcas.getDocumentText()).isEqualTo("Sue likes coffee and Bill tea");

        List<Token> tokens = new ArrayList<>(select(jcas, Token.class));
        assertThat(tokens.size()).isEqualTo(7);

        // The empty node is sorted in at the position where it appears and is zero-width.
        Token emptyNode = tokens.get(5);
        assertThat(emptyNode.getId()).isEqualTo("5.1");
        assertThat(emptyNode.getEnd()).isEqualTo(emptyNode.getBegin());
        assertThat(emptyNode.getCoveredText()).isEqualTo("");
        assertThat(emptyNode.getLemma().getValue()).isEqualTo("like");

        // Regular tokens do not carry an ID.
        assertThat(tokens.get(4).getId()).isNull();
    }

    /**
     * Empty nodes {@code i.1}, {@code i.2}, ... stand for elided material following the word
     * {@code i}, so they are placed as zero-width tokens at the end of that word - where the elided
     * material would be in the text. This also holds if the word has {@code SpaceAfter=No}, even
     * though the end of the word is then also the begin of the next word.
     * <p>
     * The CAS index cannot define an order between several empty nodes and the next word at the
     * same offset, so the order is not tested here - it is defined by the IDs.
     */
    @Test
    public void thatEmptyNodesArePlacedAfterPrecedingWord() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1\tobj\t1:obj\tSpaceAfter=No\n" //
                + "2.1\tc\tc\t_\t_\t_\t_\t_\t1:conj\t_\n" //
                + "2.2\te\te\t_\t_\t_\t_\t_\t1:conj\t_\n" //
                + "3\td\td\t_\t_\t_\t1\tconj\t2.1:obj|2.2:obj\t_\n", //
                ConllUReader.PARAM_READ_EMPTY_NODES, true);

        assertThat(jcas.getDocumentText()).isEqualTo("a bd");

        Token b = select(jcas, Token.class).stream() //
                .filter(t -> t.getId() == null && "b".equals(t.getCoveredText())) //
                .findFirst() //
                .get();

        List<Token> emptyNodes = select(jcas, Token.class).stream() //
                .filter(t -> t.getId() != null) //
                .collect(toList());

        assertThat(emptyNodes).extracting(Token::getId).containsExactlyInAnyOrder("2.1", "2.2");
        assertThat(emptyNodes).allSatisfy(node -> {
            assertThat(node.getBegin()).isEqualTo(node.getEnd());
            assertThat(node.getBegin()).isEqualTo(b.getEnd());
        });
    }

    /**
     * Empty nodes may precede the first word of a sentence (IDs {@code 0.1}, {@code 0.2}, ...).
     * They must be part of that sentence, not of the preceding one.
     * <p>
     * The CAS index cannot define an order between the empty nodes and the first word at the same
     * offset, so the order is not tested here - it is defined by the IDs.
     */
    @Test
    public void thatEmptyNodesAtSentenceStartBelongToTheirSentence() throws Exception
    {
        JCas jcas = readString("# newpar id = p1\n" //
                + "# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "\n" //
                + "# sent_id = s2\n" //
                + "0.1\tc\tc\t_\t_\t_\t_\t_\t0:root\t_\n" //
                + "0.2\te\te\t_\t_\t_\t_\t_\t0:root\t_\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t0.1:obj|0.2:obj\t_\n", //
                ConllUReader.PARAM_READ_EMPTY_NODES, true);

        assertThat(jcas.getDocumentText()).isEqualTo("a b");

        List<Sentence> sentences = new ArrayList<>(select(jcas, Sentence.class));
        assertThat(sentences.size()).isEqualTo(2);

        List<String> s1Tokens = selectCovered(Token.class, sentences.get(0)).stream() //
                .map(t -> t.getId() != null ? t.getId() : t.getCoveredText()) //
                .collect(toList());
        List<String> s2Tokens = selectCovered(Token.class, sentences.get(1)).stream() //
                .map(t -> t.getId() != null ? t.getId() : t.getCoveredText()) //
                .collect(toList());

        assertThat(s1Tokens).containsExactly("a");
        assertThat(s2Tokens).containsExactlyInAnyOrder("0.1", "0.2", "b");
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

        assertThat(jcas.getDocumentText()).isEqualTo("Sue likes tea");

        List<Token> tokens = new ArrayList<>(select(jcas, Token.class));
        assertThat(tokens.size()).isEqualTo(3);
        assertThat(tokens).allSatisfy(t -> assertThat(t.getBegin()).isLessThan(t.getEnd()));

        // The `2.1:obj` relation refers to a node which was not created and is therefore dropped.
        List<String> enhanced = select(jcas, Dependency.class).stream()
                .filter(d -> DependencyFlavor.ENHANCED.equals(d.getFlavor()))
                .map(Dependency::getDependencyType).collect(toList());
        assertThat(enhanced).isEqualTo(asList("nsubj", "root"));
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

        assertThat(enhanced).isEqualTo(asList("nsubj", "root", "conj:and"));
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

        assertThat(jcas.getDocumentText()).isEqualTo("a b d");

        List<String> enhanced = select(jcas, Dependency.class).stream()
                .filter(d -> DependencyFlavor.ENHANCED.equals(d.getFlavor()))
                .map(Dependency::getDependencyType).collect(toList());

        // The `2:conj` of the empty node and the `2.1:nsubj` pointing at it are both represented.
        assertThat(enhanced).isEqualTo(asList("nsubj", "root", "conj", "nsubj"));

        // The `2.1:nsubj` relation has the empty node as its governor.
        Dependency onEmptyNode = select(jcas, Dependency.class).stream()
                .filter(d -> DependencyFlavor.ENHANCED.equals(d.getFlavor()))
                .filter(d -> "d".equals(d.getDependent().getCoveredText())).findFirst().get();
        assertThat(onEmptyNode.getGovernor().getId()).isEqualTo("2.1");
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

        assertThat(morph).isEqualTo(asList("Foreign", "Number=Sing|Foreign"));
        assertThat(select(jcas, MorphologicalFeatures.class).stream().skip(1).findFirst().get()
                .getNumber()).isEqualTo("Sing");
    }

    /**
     * A line may have an empty MISC field, which still needs to be counted as a field.
     */
    @Test
    public void thatTrailingEmptyFieldIsAccepted() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t0\troot\t_\t\n");

        assertThat(jcas.getDocumentText()).isEqualTo("a");
        assertThat(select(jcas, Token.class).size()).isEqualTo(1);
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

        assertThat(jcas.getDocumentText()).isEqualTo("ab");
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

        assertThat(jcas.getDocumentText()).isEqualTo("a\n\nb");
        assertThat(select(jcas, Paragraph.class).size()).isEqualTo(2);
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

        assertThat(jcas.getDocumentText()).isEqualTo("a\n\nb");

        List<String> paragraphs = select(jcas, Paragraph.class).stream() //
                .map(Paragraph::getCoveredText) //
                .collect(toList());
        assertThat(paragraphs).containsExactly("b");
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

        assertThat(jcas.getDocumentText()).isEqualTo("vamos de el.");
    }

    /**
     * A file may end directly after a comment marker without a trailing line break.
     */
    @Test
    public void thatTruncatedCommentDoesNotFail() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n\n#");

        assertThat(jcas.getDocumentText()).isEqualTo("a");
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
                        .hasMessageContaining("[9]");
    }

    /**
     * The HEAD column can only refer to words or to the root ({@code 0}) - not to empty nodes. The
     * reader must fail with a message pointing at the offending head instead of a bare number
     * format error.
     */
    @Test
    public void thatBasicHeadReferringToEmptyNodeIsRejected() throws Exception
    {
        assertThatThrownBy(() -> readString("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "1.1\tx\tx\t_\t_\t_\t_\t_\t1:dep\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1.1\tobj\t1.1:obj\t_\n")) //
                        .rootCause() //
                        .isInstanceOf(IOException.class) //
                        .hasMessageContaining("1.1");
    }

    /**
     * A head which is not a valid ID at all makes the file invalid. The reader must fail with a
     * message pointing at the offending head instead of a bare number format error.
     */
    @Test
    public void thatMalformedHeadIsRejected() throws Exception
    {
        assertThatThrownBy(() -> readString("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "2\tb\tb\t_\t_\t_\tx\tobj\t_\t_\n")) //
                        .rootCause() //
                        .isInstanceOf(IOException.class) //
                        .hasMessageContaining("[x]");

        assertThatThrownBy(() -> readString("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1\tobj\tx:obj\t_\n")) //
                        .rootCause() //
                        .isInstanceOf(IOException.class) //
                        .hasMessageContaining("[x]");
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
                        .hasMessageContaining("[9]");
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

        assertThat(jcas.getDocumentText()).isEqualTo("a\nb");
        assertThat(select(jcas, Paragraph.class)).isEmpty();
    }

    /**
     * A file may contain several documents, each starting with a {@code # newdoc} comment. Each
     * document is represented as a sub-document span reaching from the begin of its first sentence
     * to the end of its last sentence.
     */
    @Test
    public void thatNewdocCreatesDocumentSpans() throws Exception
    {
        JCas jcas = readString("# newdoc id = d1\n" //
                + "# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# sent_id = s2\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newdoc id = d2\n" //
                + "# sent_id = s3\n" //
                + "1\tc\tc\t_\t_\t_\t0\troot\t_\t_\n");

        List<Sentence> sentences = new ArrayList<>(select(jcas, Sentence.class));
        List<Document> subDocuments = subDocuments(jcas);

        assertThat(subDocuments).extracting(Document::getId).containsExactly("d1", "d2");

        assertThat(subDocuments.get(0).getBegin()).isEqualTo(sentences.get(0).getBegin());
        assertThat(subDocuments.get(0).getEnd()).isEqualTo(sentences.get(1).getEnd());
        assertThat(subDocuments.get(1).getBegin()).isEqualTo(sentences.get(2).getBegin());
        assertThat(subDocuments.get(1).getEnd()).isEqualTo(sentences.get(2).getEnd());
    }

    /**
     * The spec allows {@code # newdoc} without an ID. Such a document is still represented as a
     * sub-document span, just without an ID.
     */
    @Test
    public void thatValuelessNewdocCreatesDocumentSpanWithoutId() throws Exception
    {
        JCas jcas = readString("# newdoc\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newdoc\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n");

        assertThat(subDocuments(jcas)).hasSize(2) //
                .allSatisfy(div -> assertThat(div.getId()).isNull());
    }

    /**
     * A file does not need to start with {@code # newdoc}, e.g. if a document was split between
     * files. Sentences before the first {@code # newdoc} must not be attributed to any
     * sub-document, since the file provides no information about their document.
     */
    @Test
    public void thatSentencesBeforeFirstNewdocAreNotInDocument() throws Exception
    {
        JCas jcas = readString("# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newdoc id = d1\n" //
                + "# sent_id = s2\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n");

        List<Sentence> sentences = new ArrayList<>(select(jcas, Sentence.class));
        List<Document> subDocuments = subDocuments(jcas);

        assertThat(subDocuments).hasSize(1);
        assertThat(subDocuments.get(0).getBegin()).isEqualTo(sentences.get(1).getBegin());
        assertThat(subDocuments.get(0).getEnd()).isEqualTo(sentences.get(1).getEnd());
    }

    /**
     * A paragraph cannot span across documents. A {@code # newdoc} therefore ends the current
     * paragraph even if the new document does not start with a {@code # newpar}.
     */
    @Test
    public void thatParagraphEndsAtDocumentBoundary() throws Exception
    {
        JCas jcas = readString("# newdoc id = d1\n" //
                + "# newpar id = p1\n" //
                + "# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newdoc id = d2\n" //
                + "# sent_id = s2\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n");

        List<Sentence> sentences = new ArrayList<>(select(jcas, Sentence.class));
        Sentence s1 = sentences.get(0);
        Sentence s2 = sentences.get(1);

        Paragraph p1 = select(jcas, Paragraph.class).stream() //
                .filter(p -> "p1".equals(p.getId())) //
                .findFirst() //
                .get();
        assertThat(p1.getEnd()).isEqualTo(s1.getEnd());

        assertThat(select(jcas, Paragraph.class)) //
                .noneMatch(p -> p.getBegin() <= s1.getBegin() && p.getEnd() >= s2.getEnd());
    }

    private static List<Document> subDocuments(JCas aJCas)
    {
        List<Document> subDocuments = new ArrayList<>(select(aJCas, Document.class));
        assertThat(subDocuments).allSatisfy(d -> assertThat(d.getDivType()).isEqualTo("doc"));
        return subDocuments;
    }

    /**
     * {@code SpaceAfter=No} must be matched as an exact MISC item. Other items merely containing
     * this string (such as {@code CorrectSpaceAfter=No} in UD French-GSD, or
     * {@code SpaceAfter=Nope}) must not remove the space - neither on word lines nor on multi-word
     * token lines.
     */
    @Test
    public void thatSpaceAfterNoIsMatchedExactly() throws Exception
    {
        JCas jcas = readString("1\tL\tle\t_\t_\t_\t2\tdet\t_\tCorrectSpaceAfter=No\n" //
                + "2\teuro\teuro\t_\t_\t_\t0\troot\t_\t_\n" //
                + "3-4\tdu\t_\t_\t_\t_\t_\t_\t_\tCorrectSpaceAfter=No\n" //
                + "3\tde\tde\t_\t_\t_\t5\tcase\t_\t_\n" //
                + "4\tle\tle\t_\t_\t_\t5\tdet\t_\t_\n" //
                + "5\tpays\tpays\t_\t_\t_\t2\tnmod\t_\tSpaceAfter=Nope\n" //
                + "6\tx\tx\t_\t_\t_\t2\tdep\t_\tFoo=Bar|SpaceAfter=No\n" //
                + "7\t.\t.\t_\t_\t_\t2\tpunct\t_\t_\n");

        assertThat(jcas.getDocumentText()).isEqualTo("L euro de le pays x.");
    }

    /**
     * Sentences which are not part of any paragraph are placed on separate lines - also if other
     * parts of the file use paragraph markers.
     */
    @Test
    public void thatSentencesOutsideParagraphsArePlacedOnSeparateLines() throws Exception
    {
        JCas jcas = readString("# newdoc id = d1\n" //
                + "# newpar id = p1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newdoc id = d2\n" //
                + "1\tc\tc\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "1\td\td\t_\t_\t_\t0\troot\t_\t_\n");

        assertThat(jcas.getDocumentText()).isEqualTo("a b\n\nc\nd");
        assertThat(select(jcas, Paragraph.class)) //
                .extracting(Paragraph::getCoveredText) //
                .containsExactly("a b");
    }

    /**
     * Documents are separated by an empty line, also if the file contains no paragraph markers.
     */
    @Test
    public void thatDocumentsAreSeparatedByEmptyLine() throws Exception
    {
        JCas jcas = readString("# newdoc id = d1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newdoc id = d2\n" //
                + "1\tc\tc\t_\t_\t_\t0\troot\t_\t_\n");

        assertThat(jcas.getDocumentText()).isEqualTo("a\nb\n\nc");
    }

    /**
     * Malformed multi-word token ranges - including ranges which do not refer to words of the
     * sentence - make the file invalid and must be rejected with a message naming the range.
     */
    @Test
    public void thatMalformedMultiwordTokenRangesAreRejected() throws Exception
    {
        for (String range : asList("2-1", "0-1", "1-", "a-b", "1-3")) {
            assertRejected(range + "\tab\t_\t_\t_\t_\t_\t_\t_\t_\n" //
                    + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                    + "2\tb\tb\t_\t_\t_\t1\tdep\t_\t_\n", //
                    "[" + range + "]");
        }
    }

    /**
     * A word ID which is not a number makes the file invalid and must be rejected with a message
     * naming the ID.
     */
    @Test
    public void thatMalformedWordIdIsRejected() throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "x\tb\tb\t_\t_\t_\t1\tdep\t_\t_\n", //
                "[x]");
    }

    /**
     * Each word ID may only occur once per sentence.
     */
    @Test
    public void thatDuplicateWordIdIsRejected() throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n", //
                "[1]");
    }

    /**
     * Malformed empty node IDs make the file invalid and must be rejected with a message naming the
     * ID - whether empty nodes are read or not.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void thatMalformedEmptyNodeIdsAreRejected(boolean aReadEmptyNodes) throws Exception
    {
        for (String id : asList("a.1", ".1", "1.")) {
            assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                    + id + "\tx\tx\t_\t_\t_\t_\t_\t1:dep\t_\n", //
                    "[" + id + "]", //
                    ConllUReader.PARAM_READ_EMPTY_NODES, aReadEmptyNodes);
        }
    }

    /**
     * A DEPS item must consist of a head and a non-empty relation.
     */
    @Test
    public void thatMalformedDepsItemsAreRejected() throws Exception
    {
        for (String item : asList("1", "1:")) {
            assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                    + "2\tb\tb\t_\t_\t_\t1\tdep\t" + item + "\t_\n", //
                    "[" + item + "]");
        }
    }

    /**
     * A word cannot be its own head - neither in the basic nor in the enhanced graph. Such a
     * relation must not be mistaken for the root, which is modelled as a loop in DKPro Core.
     */
    @Test
    public void thatSelfLoopsAreRejected() throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "2\tb\tb\t_\t_\t_\t2\tdep\t_\t_\n", //
                "[2]");

        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1\tdep\t2:dep\t_\n", //
                "[2]");
    }

    /**
     * If empty nodes are not read, relations involving them are dropped - but a head which is not a
     * valid empty node ID must still be rejected instead of being counted as such a relation.
     */
    @Test
    public void thatMalformedEmptyNodeHeadIsRejectedWhenEmptyNodesAreNotRead() throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1\tdep\tx.y:dep\t_\n", //
                "[x.y]");
    }

    /**
     * A file may start with a UTF-8 byte order mark. It is not part of the content and must be
     * skipped - also if the file starts with a comment.
     */
    @Test
    public void thatByteOrderMarkIsSkipped() throws Exception
    {
        JCas withComment = readString("\uFEFF# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n");

        assertThat(withComment.getDocumentText()).isEqualTo("a");
        assertThat(select(withComment, Sentence.class)) //
                .extracting(Sentence::getId) //
                .containsExactly("s1");

        JCas withoutComment = readString("\uFEFF1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n");

        assertThat(withoutComment.getDocumentText()).isEqualTo("a");
    }

    /**
     * Comment lines belong to the sentence they directly precede. Comments which are followed by an
     * empty line or by the end of the file do not belong to any sentence. Since they may carry
     * relevant information (e.g. a {@code # newdoc} boundary), they must be rejected with a message
     * pointing at the comment instead of being dropped silently.
     */
    @Test
    public void thatCommentsNotFollowedBySentenceAreRejected() throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newdoc id = d2\n" //
                + "\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n", //
                "# newdoc id = d2");

        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# some comment\n", //
                "# some comment");
    }

    /**
     * Comment lines are only allowed before the words of a sentence. A comment between word lines
     * (e.g. a misplaced {@code # newpar}) must be rejected instead of being dropped silently.
     */
    @Test
    public void thatCommentBetweenWordLinesIsRejected() throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "# newpar id = p2\n" //
                + "2\tb\tb\t_\t_\t_\t1\tdep\t_\t_\n", //
                "# newpar id = p2");
    }

    /**
     * Each empty node ID may only occur once per sentence - a repeated ID breaks the sequence
     * {@code i.1}, {@code i.2}, ... and is rejected, whether empty nodes are read or not.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void thatDuplicateEmptyNodeIdIsRejected(boolean aReadEmptyNodes) throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "1.1\tx\tx\t_\t_\t_\t_\t_\t1:dep\t_\n" //
                + "1.1\ty\ty\t_\t_\t_\t_\t_\t1:dep\t_\n", //
                "[1.1]", //
                ConllUReader.PARAM_READ_EMPTY_NODES, aReadEmptyNodes);
    }

    /**
     * IDs which are too large to be represented must be rejected with a helpful message instead of
     * failing with a number format error.
     */
    @Test
    public void thatOversizedWordIdIsRejected() throws Exception
    {
        assertRejected("99999999999\ta\ta\t_\t_\t_\t0\troot\t_\t_\n", "[99999999999]");
    }

    /**
     * Empty node IDs which are too large to be represented must be rejected with a helpful message
     * - whether empty nodes are read or not.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void thatOversizedEmptyNodeIdIsRejected(boolean aReadEmptyNodes) throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "99999999999.1\tx\tx\t_\t_\t_\t_\t_\t1:dep\t_\n", //
                "[99999999999.1]", //
                ConllUReader.PARAM_READ_EMPTY_NODES, aReadEmptyNodes);
    }

    /**
     * IDs and heads must be written in canonical form - without leading zeros or a sign. Otherwise,
     * e.g. a DEPS head {@code 02} on word 2 would escape the self-loop check and be mistaken for
     * the root.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("nonCanonicalIds")
    public void thatNonCanonicalIdsAreRejected(String aCase, String aConllU, String aMention)
        throws Exception
    {
        assertRejected(aConllU, aMention, ConllUReader.PARAM_READ_EMPTY_NODES, true);
    }

    static Stream<Arguments> nonCanonicalIds()
    {
        return Stream.of( //
                Arguments.of("DEPS head with leading zero referring to the word itself",
                        "1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n"
                                + "2\tb\tb\t_\t_\t_\t1\tdep\t02:dep\t_\n",
                        "[02]"),
                Arguments.of("DEPS head with sign",
                        "1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n"
                                + "2\tb\tb\t_\t_\t_\t1\tdep\t+1:dep\t_\n",
                        "[+1]"),
                Arguments.of("HEAD with sign",
                        "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" + "2\tb\tb\t_\t_\t_\t+1\tdep\t_\t_\n",
                        "[+1]"),
                Arguments.of("Word ID with leading zero", "01\ta\ta\t_\t_\t_\t0\troot\t_\t_\n",
                        "[01]"),
                Arguments.of("Empty node ID with leading zero",
                        "1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n"
                                + "01.1\tx\tx\t_\t_\t_\t_\t_\t1:dep\t_\n",
                        "[01.1]"));
    }

    /**
     * Word IDs must form the sequence 1, 2, 3, ...
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("wordIdsOutOfSequence")
    public void thatWordIdsMustBeInSequence(String aCase, String aConllU, String aMention)
        throws Exception
    {
        assertRejected(aConllU, aMention);
    }

    static Stream<Arguments> wordIdsOutOfSequence()
    {
        return Stream.of( //
                Arguments.of("Gap",
                        "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" + "3\tb\tb\t_\t_\t_\t1\tdep\t_\t_\n",
                        "[3]"),
                Arguments.of("Not starting at 1",
                        "2\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" + "1\tb\tb\t_\t_\t_\t2\tdep\t_\t_\n",
                        "[2]"));
    }

    /**
     * An empty node {@code i.k} must directly follow word {@code i} - whether empty nodes are read
     * or not.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void thatEmptyNodeMustDirectlyFollowItsWord(boolean aReadEmptyNodes) throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1\tdep\t1:dep\t_\n" //
                + "1.1\tx\tx\t_\t_\t_\t_\t_\t1:dep\t_\n", //
                "[1.1]", //
                ConllUReader.PARAM_READ_EMPTY_NODES, aReadEmptyNodes);
    }

    /**
     * The empty nodes following a word must be numbered {@code i.1}, {@code i.2}, ... - whether
     * empty nodes are read or not.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void thatEmptyNodesMustBeNumberedFromOne(boolean aReadEmptyNodes) throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "1.2\tx\tx\t_\t_\t_\t_\t_\t1:dep\t_\n", //
                "[1.2]", //
                ConllUReader.PARAM_READ_EMPTY_NODES, aReadEmptyNodes);
    }

    /**
     * Errors concerning empty nodes must point at the sentence like all other errors.
     */
    @Test
    public void thatEmptyNodeErrorNamesSentence() throws Exception
    {
        assertRejected("# sent_id = s1\n" //
                + "1.1\tx\tx\t_\t_\t_\t_\t_\t1:dep\t_\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n", //
                "[s1]", //
                ConllUReader.PARAM_READ_EMPTY_NODES, true);
    }

    /**
     * An empty node must have enhanced dependencies - otherwise it is not connected to anything.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void thatEmptyNodeWithoutDepsIsRejected(boolean aReadEmptyNodes) throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "1.1\tx\tx\t_\t_\t_\t_\t_\t_\t_\n", //
                "[1.1]", //
                ConllUReader.PARAM_READ_EMPTY_NODES, aReadEmptyNodes);
    }

    /**
     * A DEPS head referring to an empty node which does not exist makes the file invalid - it is
     * rejected whether empty nodes are read or not.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void thatDepsHeadReferringToMissingEmptyNodeIsRejected(boolean aReadEmptyNodes)
        throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1\tdep\t7.1:dep\t_\n", //
                "[7.1]", //
                ConllUReader.PARAM_READ_EMPTY_NODES, aReadEmptyNodes);
    }

    /**
     * A DEPREL without a HEAD cannot be attached to anything and must be rejected.
     */
    @Test
    public void thatDeprelWithoutHeadIsRejected() throws Exception
    {
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "2\tb\tb\t_\t_\t_\t_\tdep\t_\t_\n", //
                "HEAD");
    }

    /**
     * A HEAD without a DEPREL is an unlabeled dependency - a common state during annotation, which
     * annotation tools write as such. It is read as a dependency without a relation type.
     */
    @Test
    public void thatHeadWithoutDeprelIsReadAsUnlabeledDependency() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1\t_\t_\t_\n");

        Dependency dep = select(jcas, Dependency.class).stream() //
                .filter(d -> "b".equals(d.getDependent().getCoveredText())) //
                .findFirst() //
                .get();
        assertThat(dep.getGovernor().getCoveredText()).isEqualTo("a");
        assertThat(dep.getDependencyType()).isNull();
        assertThat(dep.getFlavor()).isEqualTo(DependencyFlavor.BASIC);
    }

    /**
     * Some tools (e.g. spaCy without a parser) write a word's own ID as HEAD with an unspecified
     * DEPREL to indicate that the word has not been parsed. This is not a dependency.
     */
    @Test
    public void thatSelfReferencingHeadWithoutDeprelIsIgnored() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t1\t_\t_\t_\n" //
                + "2\tb\tb\t_\t_\t_\t2\t_\t_\t_\n");

        assertThat(select(jcas, Dependency.class)).isEmpty();
    }

    /**
     * A sentence must contain at least one word - a sentence consisting only of empty nodes has no
     * text and cannot be represented.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void thatSentenceWithoutWordsIsRejected(boolean aReadEmptyNodes) throws Exception
    {
        assertRejected("0.1\tx\tx\t_\t_\t_\t_\t_\t0:root\t_\n", //
                "no words", //
                ConllUReader.PARAM_READ_EMPTY_NODES, aReadEmptyNodes);
    }

    /**
     * Columns must not be empty - an unspecified value is written as {@code _}. An empty value
     * cannot be told apart from a missing one and would not survive a round trip.
     */
    @Test
    public void thatEmptyColumnsAreRejected() throws Exception
    {
        assertRejected("1\ta\t\t_\t_\t_\t0\troot\t_\t_\n", "empty LEMMA column");
        assertRejected("1\ta\ta\t\t_\t_\t0\troot\t_\t_\n", "empty UPOS column");
        assertRejected("1\ta\ta\t_\t\t_\t0\troot\t_\t_\n", "empty XPOS column");
        assertRejected("1\ta\ta\t_\t_\t\t0\troot\t_\t_\n", "empty FEATS column");
        assertRejected("1\ta\ta\t_\t_\t_\t0\t\t_\t_\n", "empty DEPREL column");
        assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t\t_\n", "empty DEPS column");
        assertRejected("1\ta\ta\t_\t_\t_\t \troot\t_\t_\n", "empty HEAD column");
    }

    /**
     * If dependencies are not read, the dependency columns are not validated - they may even be
     * empty. The other columns are still validated.
     */
    @Test
    public void thatEmptyDependencyColumnsAreAcceptedIfNotRead() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t\t\t\t_\n", //
                ConllUReader.PARAM_READ_DEPENDENCY, false);

        assertThat(jcas.getDocumentText()).isEqualTo("a");
        assertThat(select(jcas, Dependency.class)).isEmpty();

        assertRejected("1\ta\t\t_\t_\t_\t\t\t\t_\n", "empty LEMMA column", //
                ConllUReader.PARAM_READ_DEPENDENCY, false);
    }

    /**
     * Older versions of the writer placed {@code SpaceAfter=No} on the words of multi-word tokens
     * and wrote empty values as empty columns. In lenient mode, such files can be read - the
     * deviations are reported once per kind and document.
     */
    @Test
    public void thatOldWriterOutputIsAcceptedInLenientMode() throws Throwable
    {
        String conllu = "1-2\tdel\t_\t_\t_\t_\t_\t_\t_\t_\n" //
                + "1\tde\tde\tADP\t_\t_\t0\troot\t_\tSpaceAfter=No\n" //
                + "2\tel\tel\tDET\t_\t_\t1\tdet\t_\tSpaceAfter=No\n" //
                + "3\t.\t.\tPUNCT\t_\t_\t1\tpunct\t_\t_\n" //
                + "\n" //
                + "1-2\tal\t_\t_\t_\t_\t_\t_\t_\t_\n" //
                + "1\ta\ta\tADP\t_\t_\t0\troot\t_\tSpaceAfter=No\n" //
                + "2\tel\t\tDET\t_\t_\t1\tdet\t_\t_\n";

        assertRejected(conllu, "SpaceAfter");

        JCas jcas = readString(conllu, //
                ConllUReader.PARAM_LENIENT, true);

        assertThat(jcas.getDocumentText()).isEqualTo("deel.\nael");
        assertThat(select(jcas, SurfaceForm.class)) //
                .extracting(SurfaceForm::getValue, SurfaceForm::getCoveredText) //
                .containsExactly(tuple("del", "deel"), tuple("al", "ael"));
        assertThat(select(jcas, Lemma.class)) //
                .extracting(Lemma::getValue) //
                .containsExactly("de", "el", ".", "a");
    }

    /**
     * Older versions of the reader ignored misplaced comments. In lenient mode, they are ignored
     * with a warning.
     */
    @Test
    public void thatMisplacedCommentsAreIgnoredInLenientMode() throws Throwable
    {
        String conllu = "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "# between words\n" //
                + "2\tb\tb\t_\t_\t_\t1\tdep\t_\t_\n" //
                + "\n" //
                + "# before empty line\n" //
                + "\n" //
                + "1\tc\tc\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# at end of file\n";

        assertRejected(conllu, "# between words");

        JCas jcas = readString(conllu, //
                ConllUReader.PARAM_LENIENT, true);

        assertThat(jcas.getDocumentText()).isEqualTo("a b\nc");
        assertThat(select(jcas, Sentence.class)).hasSize(2);
    }

    /**
     * Older versions of the reader created dependencies whose head refers to the word itself or to
     * a word which does not exist (with a null governor). In lenient mode, such dependencies are
     * dropped with a warning - the valid dependencies of the sentence are retained.
     */
    @Test
    public void thatInvalidHeadsAreDroppedInLenientMode() throws Throwable
    {
        String conllu = "1\ta\ta\t_\t_\t_\t0\troot\t0:root|1:self\t_\n" //
                + "2\tb\tb\t_\t_\t_\t2\tdep\t1:dep\t_\n" //
                + "3\tc\tc\t_\t_\t_\t9\tdep\t1:dep|9:dep\t_\n";

        assertRejected(conllu, "refers to the word itself");

        JCas jcas = readString(conllu, //
                ConllUReader.PARAM_LENIENT, true);

        assertThat(select(jcas, Dependency.class)) //
                .extracting(d -> d.getFlavor(), d -> d.getGovernor().getCoveredText(),
                        d -> d.getDependent().getCoveredText(), Dependency::getDependencyType) //
                .containsExactlyInAnyOrder( //
                        tuple(DependencyFlavor.BASIC, "a", "a", "root"), //
                        tuple(DependencyFlavor.ENHANCED, "a", "a", "root"), //
                        tuple(DependencyFlavor.ENHANCED, "a", "b", "dep"), //
                        tuple(DependencyFlavor.ENHANCED, "a", "c", "dep"));
    }

    /**
     * In lenient mode, the writer writes empty nodes without enhanced dependencies. The lenient
     * reader accepts them - they are read without enhanced dependencies.
     */
    @Test
    public void thatEmptyNodeWithoutDepsIsAcceptedInLenientMode() throws Throwable
    {
        String conllu = "1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "1.1\tx\tx\t_\t_\t_\t_\t_\t_\t_\n";

        JCas jcas = readString(conllu, //
                ConllUReader.PARAM_LENIENT, true, //
                ConllUReader.PARAM_READ_EMPTY_NODES, true);

        assertThat(select(jcas, Token.class)) //
                .filteredOn(t -> t.getBegin() == t.getEnd()) //
                .extracting(Token::getId) //
                .containsExactly("1.1");
    }

    /**
     * If paragraphs are not read, {@code # newpar} comments are not validated either.
     */
    @Test
    public void thatMalformedNewparIsIgnoredIfParagraphsAreNotRead() throws Exception
    {
        JCas jcas = readString("# newpar id =\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n", //
                ConllUReader.PARAM_READ_PARAGRAPH, false);

        assertThat(select(jcas, Paragraph.class)).isEmpty();
    }

    /**
     * Errors must name the file - the message itself only identifies the sentence within the file,
     * which is not sufficient to locate the problem in a corpus of many files.
     */
    @Test
    public void thatErrorsNameTheFile() throws Exception
    {
        assertThatThrownBy(() -> readString("1\ta\t\t_\t_\t_\t0\troot\t_\t_\n")) //
                .hasStackTraceContaining("Unable to read [") //
                .hasStackTraceContaining("test.conllu]");
    }

    /**
     * DEPS items are separated by {@code |} - empty items (e.g. in {@code ||} or a DEPS column
     * consisting only of {@code |}) make the column invalid.
     */
    @Test
    public void thatEmptyDepsItemsAreRejected() throws Exception
    {
        for (String deps : asList("|", "||", "0:root||1:x", "0:root|")) {
            assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t" + deps + "\t_\n", //
                    "[" + deps + "]");
        }
    }

    /**
     * {@code SpaceAfter=No} applies to tokens - within a multi-word token, it may only appear on
     * the multi-word token line, not on its words.
     */
    @Test
    public void thatSpaceAfterOnWordOfMultiwordTokenIsRejected() throws Exception
    {
        assertRejected("1-2\tab\t_\t_\t_\t_\t_\t_\t_\t_\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\tSpaceAfter=No\n" //
                + "2\tb\tb\t_\t_\t_\t1\tdep\t_\t_\n", //
                "SpaceAfter");
    }

    /**
     * The enhanced dependencies of empty nodes must be valid - also if empty nodes are not read and
     * the dependencies are dropped.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void thatInvalidDepsOfEmptyNodeAreRejected(boolean aReadEmptyNodes) throws Exception
    {
        // A head referring to an empty node which does not exist is only an error if empty nodes
        // are read - otherwise, the relation is dropped like all relations involving empty nodes.
        List<String> invalidDeps = aReadEmptyNodes ? asList("9:x", "7.7:y", "foo")
                : asList("9:x", "foo");
        for (String deps : invalidDeps) {
            assertRejected("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                    + "1.1\tx\tx\t_\t_\t_\t_\t_\t" + deps + "\t_\n", //
                    deps.contains(":") ? "[" + deps.split(":")[0] + "]" : "[" + deps + "]", //
                    ConllUReader.PARAM_READ_EMPTY_NODES, aReadEmptyNodes);
        }
    }

    /**
     * If dependencies are not read, the dependency columns are not validated either - this includes
     * the requirement that empty nodes have enhanced dependencies.
     */
    @Test
    public void thatDependencyColumnsAreNotValidatedIfNotRead() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "1.1\tx\tx\t_\t_\t_\t_\t_\t_\t_\n", //
                ConllUReader.PARAM_READ_DEPENDENCY, false);

        assertThat(jcas.getDocumentText()).isEqualTo("a");
    }

    /**
     * Error messages must be well-formed.
     */
    @Test
    public void thatMalformedHeadMessageIsReadable() throws Exception
    {
        assertThatThrownBy(() -> readString("1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "2\tb\tb\t_\t_\t_\tx\tobj\t_\t_\n")) //
                        .rootCause() //
                        .hasMessageStartingWith("Word [2] has HEAD [x] which is not a valid ID");
    }

    /**
     * The FORM column must not be empty - a word without a form would become a zero-width token.
     */
    @Test
    public void thatEmptyFormIsRejected() throws Exception
    {
        assertRejected("1\t\ta\t_\t_\t_\t0\troot\t_\t_\n", //
                "FORM");
    }

    /**
     * FEATS items are mapped onto the features of the morphological features annotation. Items
     * which happen to match built-in features (e.g. {@code Begin}) or the feature holding the raw
     * FEATS string ({@code Value}) must not be mapped - they must neither fail nor corrupt the
     * annotation.
     */
    @Test
    public void thatFeatsDoNotOverwriteBuiltInFeatures() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\tBegin=5|Value=X|Number=Sing\t0\troot\t_\t_\n");

        MorphologicalFeatures morph = select(jcas, MorphologicalFeatures.class).iterator().next();
        assertThat(morph.getValue()).isEqualTo("Begin=5|Value=X|Number=Sing");
        assertThat(morph.getNumber()).isEqualTo("Sing");
        assertThat(morph.getBegin()).isEqualTo(0);
        assertThat(morph.getEnd()).isEqualTo(1);
    }

    /**
     * {@code # newdoc} and {@code # newpar} comments carry structure, so malformed variants must
     * not be dropped silently - they are rejected. In lenient mode, they mark a boundary without an
     * ID.
     */
    @ParameterizedTest
    @ValueSource(strings = { "# newdoc = X", "# newdoc idd = X", "# newdoc id =", "# newdoc X",
            "# newdoc id X" })
    public void thatMalformedNewdocIsRejectedOrStartsDocumentWithoutId(String aComment)
        throws Throwable
    {
        String conllu = aComment + "\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n";

        assertRejected(conllu, aComment);

        JCas jcas = readString(conllu, //
                ConllUReader.PARAM_LENIENT, true);

        assertThat(subDocuments(jcas)).singleElement() //
                .satisfies(doc -> assertThat(doc.getId()).isNull());
    }

    /**
     * Like {@code # newdoc}, a malformed {@code # newpar} is rejected - in lenient mode, it marks a
     * paragraph without an ID.
     */
    @Test
    public void thatMalformedNewparIsRejectedOrStartsParagraphWithoutId() throws Throwable
    {
        String conllu = "# newpar idd = p1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n";

        assertRejected(conllu, "# newpar idd = p1");

        JCas jcas = readString(conllu, ConllUReader.PARAM_LENIENT, true);

        assertThat(select(jcas, Paragraph.class)).singleElement() //
                .satisfies(p -> assertThat(p.getId()).isNull());
    }

    /**
     * Variations in whitespace do not make a {@code # newdoc} or {@code # newpar} comment
     * malformed, and comments which merely start with the same letters are not boundaries.
     */
    @Test
    public void thatWellFormedBoundaryCommentVariantsAreAccepted() throws Exception
    {
        JCas jcas = readString("#newdoc  id=d1 \n" //
                + "# newpar\tid = p1\n" //
                + "# newparagraphs are great\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n");

        assertThat(subDocuments(jcas)).singleElement() //
                .satisfies(doc -> assertThat(doc.getId()).isEqualTo("d1"));
        assertThat(select(jcas, Paragraph.class)).singleElement() //
                .satisfies(p -> assertThat(p.getId()).isEqualTo("p1"));
    }

    /**
     * The reader can also be invoked directly on a CAS without document metadata. Reporting dropped
     * empty nodes must not fail in this case.
     */
    @Test
    public void thatConvertWorksWithoutDocumentMetadata() throws Exception
    {
        ConllUReader reader = (ConllUReader) createReader(ConllUReader.class, //
                ConllUReader.PARAM_LANGUAGE, "en", //
                ConllUReader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/u/", //
                ConllUReader.PARAM_PATTERNS, "conllu-en-orig.conllu");
        JCas jcas = JCasFactory.createJCas();

        reader.convert(jcas,
                new BufferedReader(new StringReader("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n"
                        + "1.1\tx\tx\t_\t_\t_\t_\t_\t1:dep\t_\n")));

        assertThat(jcas.getDocumentText()).isEqualTo("a");
    }

    /**
     * Files with Windows line endings must be read just like files with Unix line endings.
     */
    @Test
    public void thatWindowsLineEndingsAreSupported() throws Exception
    {
        JCas jcas = readString("# sent_id = s1\r\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\tSpaceAfter=No\r\n" //
                + "2\t.\t.\t_\t_\t_\t1\tpunct\t_\t_\r\n" //
                + "\r\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\r\n");

        assertThat(jcas.getDocumentText()).isEqualTo("a.\nb");
        assertThat(select(jcas, Sentence.class)).extracting(Sentence::getId) //
                .containsExactly("s1", null);
    }

    /**
     * If dependencies are not read, no dependency annotations are created.
     */
    @Test
    public void thatDependenciesAreNotCreatedIfNotRead() throws Exception
    {
        JCas jcas = readString("1\ta\ta\t_\t_\t_\t0\troot\t0:root\t_\n" //
                + "2\tb\tb\t_\t_\t_\t1\tdep\t1:dep\t_\n", //
                ConllUReader.PARAM_READ_DEPENDENCY, false);

        assertThat(select(jcas, Dependency.class)).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("misplacedMultiwordTokenRanges")
    public void thatMultiwordTokenRangeMustStartAtFollowingWord(String aCase, String aConllU,
            String aMention)
        throws Exception
    {
        assertRejected(aConllU, aMention);
    }

    static Stream<Arguments> misplacedMultiwordTokenRanges()
    {
        return Stream.of( //
                Arguments.of("Range after its first word",
                        "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" + "1-2\tab\t_\t_\t_\t_\t_\t_\t_\t_\n"
                                + "2\tb\tb\t_\t_\t_\t1\tdep\t_\t_\n",
                        "[1-2]"),
                Arguments.of("Nested range", "1-3\tabc\t_\t_\t_\t_\t_\t_\t_\t_\n"
                        + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" + "2-3\tbc\t_\t_\t_\t_\t_\t_\t_\t_\n"
                        + "2\tb\tb\t_\t_\t_\t1\tdep\t_\t_\n" + "3\tc\tc\t_\t_\t_\t1\tdep\t_\t_\n",
                        "[2-3]"));
    }

    /**
     * Every word line must have exactly 10 tab-separated columns.
     */
    @Test
    public void thatWrongNumberOfColumnsIsRejected() throws Exception
    {
        assertRejected("1\ta\ta\t_\n", "10 tab-separated fields");
    }

    /**
     * A universal POS tag without a language-specific one is read as the coarse value of a POS
     * annotation.
     */
    @Test
    public void thatCoarsePosWithoutFinePosIsRead() throws Exception
    {
        JCas jcas = readString("1\ta\ta\tNOUN\t_\t_\t0\troot\t_\t_\n");

        POS pos = select(jcas, POS.class).iterator().next();
        assertThat(pos.getCoarseValue()).isEqualTo("NOUN");
        assertThat(pos.getPosValue()).isNull();
    }

    private void assertRejected(String aConllU, String aExpectedMention, Object... aParams)
    {
        assertThatThrownBy(() -> readString(aConllU, aParams)) //
                .as("Input: %s", aConllU) //
                .rootCause() //
                .isInstanceOf(IOException.class) //
                .hasMessageContaining(aExpectedMention);
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

        assertThat(paragraphIDs).isEqualTo(expectedParagraphIDs);

        final String expectedTextContent = "Slovenská ústava: pro i proti Slovenská ústava: pro i"
                + " proti\n" + "\n" + "Slovenská ústava: pro i proti";
        final String actualTextContent = jcas.getDocumentText();
        assertThat(actualTextContent).isEqualTo(expectedTextContent);

        String[] sentences = { "Slovenská ústava: pro i proti", "Slovenská ústava: pro i proti",
                "Slovenská ústava: pro i proti" };
        assertSentence(sentences, select(jcas, Sentence.class));
    }
}
