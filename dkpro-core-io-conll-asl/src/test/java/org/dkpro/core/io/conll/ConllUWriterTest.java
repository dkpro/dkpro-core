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
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.SurfaceForm;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Document;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;

public class ConllUWriterTest
{
    @Test
    public void thatLineBreaksDoNotBreakTheFormat(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("Test\ntest.");
        new Sentence(jcas, 0, 10).addToIndexes();
        new Token(jcas, 0, 4).addToIndexes();
        new Token(jcas, 5, 9).addToIndexes();
        new Token(jcas, 9, 10).addToIndexes();

        DocumentMetaData dmd = DocumentMetaData.create(jcas);
        dmd.setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, ConllUWriter.PARAM_TARGET_LOCATION,
                tempDir);

        writer.process(jcas);

        String reference = readFileToString(
                new File("src/test/resources/conll/u_v2/conllu-linebreaks.conllu"), "UTF-8").trim();
        String actual = readFileToString(new File(tempDir, "output.conllu"), "UTF-8").trim();

        assertThat(actual).isEqualToNormalizingNewlines(reference);
    }

    /**
     * Empty nodes are modelled as zero-width tokens and must be written back out with their
     * original decimal ID and form. This cannot be covered by a regular round-trip test because
     * {@code NoZeroSizeTokensAndSentencesCheck} - which {@code IOTestRunner} applies to all
     * round-trip tests - rejects zero-width tokens.
     */
    @Test
    public void thatEmptyNodesRoundTrip(@TempDir File tempDir) throws Exception
    {
        File reference = new File("src/test/resources/conll/u_v2/conllu-empty_nodes.conllu");

        SimplePipeline.runPipeline(
                createReaderDescription(ConllUReader.class, ConllUReader.PARAM_LANGUAGE, "en",
                        ConllUReader.PARAM_SOURCE_LOCATION, reference.getPath(),
                        ConllUReader.PARAM_READ_EMPTY_NODES, true),
                createEngineDescription(ConllUWriter.class, ConllUWriter.PARAM_TARGET_LOCATION,
                        tempDir, ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false,
                        ConllUWriter.PARAM_OVERWRITE, true));

        String actual = readFileToString(new File(tempDir, "conllu-empty_nodes.conllu.conllu"),
                "UTF-8").trim();

        assertThat(actual)
                .isEqualToNormalizingNewlines(readFileToString(reference, "UTF-8").trim());
    }

    /**
     * The enhanced dependency graph may refer to empty nodes by their decimal ID.
     */
    @Test
    public void thatEnhancedDependenciesOnEmptyNodesRoundTrip(@TempDir File tempDir)
        throws Exception
    {
        File source = new File(tempDir, "in.conllu");
        String conllu = "1\tSue\tSue\t_\t_\t_\t2\tnsubj\t2:nsubj\t_\n"
                + "2\tlikes\tlike\t_\t_\t_\t0\troot\t0:root\t_\n"
                + "3\tcoffee\tcoffee\t_\t_\t_\t2\tobj\t2:obj\t_\n"
                + "4\tand\tand\t_\t_\t_\t6\tcc\t6:cc\t_\n"
                + "5\tBill\tBill\t_\t_\t_\t6\tnsubj\t5.1:nsubj\t_\n"
                + "5.1\tlikes\tlike\t_\t_\t_\t_\t_\t2:conj:and\t_\n"
                + "6\ttea\ttea\t_\t_\t_\t2\tconj\t5.1:obj\t_\n";
        write(source, conllu, UTF_8);

        SimplePipeline.runPipeline(
                createReaderDescription(ConllUReader.class, ConllUReader.PARAM_LANGUAGE, "en",
                        ConllUReader.PARAM_SOURCE_LOCATION, source.getPath(),
                        ConllUReader.PARAM_READ_EMPTY_NODES, true),
                createEngineDescription(ConllUWriter.class, ConllUWriter.PARAM_TARGET_LOCATION,
                        tempDir, ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false,
                        ConllUWriter.PARAM_OVERWRITE, true));

        String actual = readFileToString(new File(tempDir, "in.conllu.conllu"), "UTF-8").trim();

        assertThat(actual).isEqualToNormalizingNewlines(conllu.trim());
    }

    /**
     * The HEAD column can only refer to regular tokens. A basic dependency governed by an empty
     * node cannot be expressed and must be rejected instead of producing an invalid HEAD value.
     */
    @Test
    public void thatBasicDependencyGovernedByEmptyNodeIsRejected(@TempDir File tempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = new Token(jcas, 0, 1);
        a.addToIndexes();
        Token emptyNode = new Token(jcas, 1, 1);
        emptyNode.setId("1.1");
        emptyNode.addToIndexes();
        Token b = new Token(jcas, 2, 3);
        b.addToIndexes();

        Dependency dep = new Dependency(jcas, 2, 3);
        dep.setGovernor(emptyNode);
        dep.setDependent(b);
        dep.setDependencyType("obj");
        dep.setFlavor(DependencyFlavor.BASIC);
        dep.addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir);

        assertThatThrownBy(() -> writer.process(jcas)) //
                .rootCause() //
                .isInstanceOf(IllegalStateException.class) //
                .hasMessageContaining("[1.1]");
    }

    /**
     * The HEAD and DEPREL columns can only hold a single basic dependency per token. A token which
     * is the dependent of more than one basic dependency cannot be expressed and must be rejected
     * instead of silently dropping all but one of the relations.
     */
    @Test
    public void thatTokenWithMultipleBasicDependenciesIsRejected(@TempDir File tempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("a b c");
        new Sentence(jcas, 0, 5).addToIndexes();
        Token a = new Token(jcas, 0, 1);
        a.addToIndexes();
        Token b = new Token(jcas, 2, 3);
        b.addToIndexes();
        Token c = new Token(jcas, 4, 5);
        c.addToIndexes();

        for (Token governor : List.of(a, b)) {
            Dependency dep = new Dependency(jcas, 4, 5);
            dep.setGovernor(governor);
            dep.setDependent(c);
            dep.setDependencyType("dep");
            dep.setFlavor(DependencyFlavor.BASIC);
            dep.addToIndexes();
        }

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir);

        assertThatThrownBy(() -> writer.process(jcas)) //
                .rootCause() //
                .isInstanceOf(IllegalStateException.class) //
                .hasMessageContaining("more than one");
    }

    /**
     * Sub-document spans are written as {@code # newdoc} comments in front of the first sentence
     * they contain - by default, and also if the span starts before that sentence (e.g. due to
     * indentation).
     */
    @Test
    public void thatDocumentsAreWrittenAsNewdoc(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b\n\n  c");
        new Sentence(jcas, 0, 1).addToIndexes();
        new Sentence(jcas, 2, 3).addToIndexes();
        new Sentence(jcas, 7, 8).addToIndexes();
        new Token(jcas, 0, 1).addToIndexes();
        new Token(jcas, 2, 3).addToIndexes();
        new Token(jcas, 7, 8).addToIndexes();

        Document d1 = new Document(jcas, 0, 3);
        d1.setDivType("doc");
        d1.setId("d1");
        d1.addToIndexes();

        Document d2 = new Document(jcas, 5, 8);
        d2.setDivType("doc");
        d2.addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false);

        writer.process(jcas);

        String actual = readFileToString(new File(tempDir, "output.conllu"), "UTF-8").trim();

        assertThat(actual).isEqualToNormalizingNewlines(String.join("\n", //
                "# newdoc id = d1", //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                "", //
                "1\tb\t_\t_\t_\t_\t_\t_\t_\t_", //
                "", //
                "# newdoc", //
                "1\tc\t_\t_\t_\t_\t_\t_\t_\t_"));
    }

    /**
     * Without sub-document spans, the writer must not invent a {@code # newdoc} comment from the
     * document ID, which is typically derived from the file name.
     */
    @Test
    public void thatNoNewdocIsWrittenWithoutDocuments(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        new Token(jcas, 0, 1).addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir);

        writer.process(jcas);

        String actual = readFileToString(new File(tempDir, "output.conllu"), "UTF-8");

        assertThat(newdocLines(actual)).isEmpty();
    }

    /**
     * CoNLL-U addresses heads by their index within the sentence, so a dependency whose governor is
     * in a different sentence cannot be expressed and must be rejected.
     */
    @Test
    public void thatGovernorInOtherSentenceIsRejected(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 1).addToIndexes();
        new Sentence(jcas, 2, 3).addToIndexes();
        Token a = new Token(jcas, 0, 1);
        a.addToIndexes();
        Token b = new Token(jcas, 2, 3);
        b.addToIndexes();

        Dependency dep = new Dependency(jcas, 2, 3);
        dep.setGovernor(a);
        dep.setDependent(b);
        dep.setDependencyType("dep");
        dep.setFlavor(DependencyFlavor.BASIC);
        dep.addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir);

        assertThatThrownBy(() -> writer.process(jcas)) //
                .rootCause() //
                .isInstanceOf(IllegalStateException.class) //
                .hasMessageContaining("sentence");
    }

    /**
     * If the CAS cannot be written, no output file must be left behind - an empty or partial file
     * is useless and may be mistaken for a successful conversion.
     */
    @Test
    public void thatNoOutputFileIsLeftBehindWhenWritingFails(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b c");
        new Sentence(jcas, 0, 5).addToIndexes();
        Token a = new Token(jcas, 0, 1);
        a.addToIndexes();
        Token b = new Token(jcas, 2, 3);
        b.addToIndexes();
        Token c = new Token(jcas, 4, 5);
        c.addToIndexes();

        // Two basic dependencies on the same token cannot be expressed and make the writer fail.
        for (Token governor : List.of(a, b)) {
            Dependency dep = new Dependency(jcas, 4, 5);
            dep.setGovernor(governor);
            dep.setDependent(c);
            dep.setDependencyType("dep");
            dep.setFlavor(DependencyFlavor.BASIC);
            dep.addToIndexes();
        }

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir);

        assertThatThrownBy(() -> writer.process(jcas)) //
                .rootCause() //
                .isInstanceOf(IllegalStateException.class) //
                .hasMessageContaining("more than one");

        assertThat(new File(tempDir, "output.conllu")).doesNotExist();
    }

    /**
     * CoNLL-U has no escape syntax, so a tab in a value would add a column and corrupt the line.
     * The writer must reject such a value with a message naming the affected column.
     */
    @Test
    public void thatTabInFormIsRejected(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a\tb");
        new Sentence(jcas, 0, 3).addToIndexes();
        new Token(jcas, 0, 3).addToIndexes();

        assertWriterRejects(jcas, tempDir, "FORM");
    }

    /**
     * CoNLL-U has no escape syntax, so a line break in a value would split the line. The writer
     * must reject such a value with a message naming the affected column.
     */
    @Test
    public void thatLineBreakInLemmaIsRejected(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        Token token = new Token(jcas, 0, 1);
        token.addToIndexes();
        Lemma lemma = new Lemma(jcas, 0, 1);
        lemma.setValue("a\nb");
        lemma.addToIndexes();
        token.setLemma(lemma);

        assertWriterRejects(jcas, tempDir, "LEMMA");
    }

    /**
     * A line break in a comment value would end the comment and turn the remainder into an invalid
     * token line. The writer must reject such a value with a message naming the affected comment.
     */
    @Test
    public void thatLineBreakInSentenceIdIsRejected(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        Sentence sentence = new Sentence(jcas, 0, 1);
        sentence.setId("s1\ns2");
        sentence.addToIndexes();
        new Token(jcas, 0, 1).addToIndexes();

        assertWriterRejects(jcas, tempDir, "sent_id");
    }

    private static void assertWriterRejects(JCas aJCas, File aTempDir, String aExpectedMention)
        throws Exception
    {
        DocumentMetaData.create(aJCas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false);

        assertThatThrownBy(() -> writer.process(aJCas)) //
                .rootCause() //
                .isInstanceOf(IllegalStateException.class) //
                .hasMessageContaining(aExpectedMention);
    }

    /**
     * An empty node covers no text. If it carries no token form either, the FORM column must
     * contain the {@code _} placeholder instead of being left empty.
     */
    @Test
    public void thatEmptyNodeWithoutFormIsWrittenAsPlaceholder(@TempDir File tempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        new Token(jcas, 0, 1).addToIndexes();
        Token emptyNode = new Token(jcas, 1, 1);
        emptyNode.setId("1.1");
        emptyNode.addToIndexes();
        new Token(jcas, 2, 3).addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false);

        writer.process(jcas);

        String actual = readFileToString(new File(tempDir, "output.conllu"), "UTF-8");

        assertThat(actual.split("\n")) //
                .filteredOn(line -> line.startsWith("1.1\t")) //
                .singleElement() //
                .satisfies(line -> assertThat(line.split("\t", -1)[1]).isEqualTo("_"));
    }

    /**
     * A paragraph may start with whitespace (e.g. indentation) before its first sentence. The
     * sentence still starts the paragraph and must be marked accordingly.
     */
    @Test
    public void thatIndentedParagraphIsWritten(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("  Hello.");
        new Paragraph(jcas, 0, 8).addToIndexes();
        new Sentence(jcas, 2, 8).addToIndexes();
        new Token(jcas, 2, 7).addToIndexes();
        new Token(jcas, 7, 8).addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false);

        writer.process(jcas);

        String actual = readFileToString(new File(tempDir, "output.conllu"), "UTF-8");

        assertThat(actual.split("\n")).contains("# newpar");
    }

    /**
     * A file may contain several documents, each starting with its own {@code # newdoc id}. Each of
     * the document boundaries must be retained when writing the data back out.
     */
    @Test
    public void thatMultipleDocumentIdsRoundTrip(@TempDir File tempDir) throws Exception
    {
        File reference = new File(
                "src/test/resources/conll/u_v2/conllu-multiple_document_IDs.conllu");

        SimplePipeline.runPipeline(createReaderDescription(ConllUReader.class, //
                ConllUReader.PARAM_LANGUAGE, "en", //
                ConllUReader.PARAM_SOURCE_LOCATION, reference.getPath()),
                createEngineDescription(ConllUWriter.class, //
                        ConllUWriter.PARAM_TARGET_LOCATION, tempDir));

        String actual = readFileToString(
                new File(tempDir, "conllu-multiple_document_IDs.conllu.conllu"), "UTF-8");

        assertThat(newdocLines(actual)).containsExactly( //
                "# newdoc id = mf920901-001", //
                "# newdoc id = mf920901-002");
    }

    /**
     * The spec allows {@code # newdoc} without an ID. Such document boundaries must be retained
     * when writing the data back out.
     */
    @Test
    public void thatValuelessNewdocRoundTrips(@TempDir File tempDir) throws Exception
    {
        File source = new File(tempDir, "in.conllu");
        write(source, "# newdoc\n" //
                + "# sent_id = s1\n" //
                + "1\ta\ta\t_\t_\t_\t0\troot\t_\t_\n" //
                + "\n" //
                + "# newdoc\n" //
                + "# sent_id = s2\n" //
                + "1\tb\tb\t_\t_\t_\t0\troot\t_\t_\n", UTF_8);

        SimplePipeline.runPipeline(createReaderDescription(ConllUReader.class, //
                ConllUReader.PARAM_LANGUAGE, "en", //
                ConllUReader.PARAM_SOURCE_LOCATION, source.getPath()),
                createEngineDescription(ConllUWriter.class, //
                        ConllUWriter.PARAM_TARGET_LOCATION, tempDir, //
                        ConllUWriter.PARAM_OVERWRITE, true));

        String actual = readFileToString(new File(tempDir, "in.conllu.conllu"), "UTF-8");

        assertThat(newdocLines(actual)).containsExactly("# newdoc", "# newdoc");
    }

    /**
     * Sentences may directly follow each other without any whitespace in between. The writer marks
     * this with {@code SpaceAfter=No} on the last token of the sentence. Without paragraph
     * information, the reader places each sentence on its own line regardless, since older
     * treebanks use a sentence-final {@code SpaceAfter=No} where the original text had a line
     * break. Exact reconstruction would require support for {@code SpacesAfter}.
     */
    @Test
    public void thatSentencesWithoutSeparatorAreReadBackOnSeparateLines(@TempDir File tempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("Hello.World.");
        new Sentence(jcas, 0, 6).addToIndexes();
        new Sentence(jcas, 6, 12).addToIndexes();
        new Token(jcas, 0, 5).addToIndexes();
        new Token(jcas, 5, 6).addToIndexes();
        new Token(jcas, 6, 11).addToIndexes();
        new Token(jcas, 11, 12).addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false);

        writer.process(jcas);

        File output = new File(tempDir, "output.conllu");
        assertThat(readFileToString(output, "UTF-8").split("\n")) //
                .contains("2\t.\t_\t_\t_\t_\t_\t_\t_\tSpaceAfter=No");

        CollectionReaderDescription reader = createReaderDescription(ConllUReader.class, //
                ConllUReader.PARAM_LANGUAGE, "en", //
                ConllUReader.PARAM_SOURCE_LOCATION, output);

        JCas actual = new JCasIterable(reader).iterator().next();

        assertThat(actual.getDocumentText()).isEqualTo("Hello.\nWorld.");
    }

    /**
     * CoNLL-U uses {@code _} for unspecified values. Empty or missing values must therefore be
     * written as {@code _} instead of producing empty columns or the text {@code null}.
     */
    @Test
    public void thatMissingValuesAreWrittenAsPlaceholder(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b c");
        new Sentence(jcas, 0, 5).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        Token c = token(jcas, 4, 5);

        // Multi-word token without a surface string
        new SurfaceForm(jcas, 0, 3).addToIndexes();

        Lemma emptyLemma = new Lemma(jcas, 0, 1);
        emptyLemma.setValue("");
        emptyLemma.addToIndexes();
        a.setLemma(emptyLemma);

        Lemma missingLemma = new Lemma(jcas, 2, 3);
        missingLemma.addToIndexes();
        b.setLemma(missingLemma);

        POS emptyPos = new POS(jcas, 0, 1);
        emptyPos.setPosValue("");
        emptyPos.setCoarseValue("");
        emptyPos.addToIndexes();
        a.setPos(emptyPos);

        MorphologicalFeatures emptyMorph = new MorphologicalFeatures(jcas, 0, 1);
        emptyMorph.setValue("");
        emptyMorph.addToIndexes();
        a.setMorph(emptyMorph);

        MorphologicalFeatures missingMorph = new MorphologicalFeatures(jcas, 4, 5);
        missingMorph.addToIndexes();
        c.setMorph(missingMorph);

        assertThat(writeToString(jcas, tempDir)).isEqualToNormalizingNewlines(String.join("\n", //
                "1-2\t_\t_\t_\t_\t_\t_\t_\t_\t_", //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                "2\tb\t_\t_\t_\t_\t_\t_\t_\t_", //
                "3\tc\t_\t_\t_\t_\t_\t_\t_\t_"));
    }

    /**
     * A dependency relation is mandatory in CoNLL-U - a dependency without a type cannot be written
     * in the DEPREL or DEPS column.
     */
    @Test
    public void thatMissingRelationTypeIsRejected(@TempDir File tempDir) throws Exception
    {
        for (String flavor : List.of(DependencyFlavor.BASIC, DependencyFlavor.ENHANCED)) {
            for (String type : new String[] { null, "" }) {
                JCas jcas = JCasFactory.createText("a b");
                new Sentence(jcas, 0, 3).addToIndexes();
                Token a = token(jcas, 0, 1);
                Token b = token(jcas, 2, 3);
                dependency(jcas, a, b, type, flavor);

                assertWriterRejects(jcas, tempDir,
                        DependencyFlavor.BASIC.equals(flavor) ? "DEPREL" : "DEPS");
            }
        }
    }

    /**
     * The {@code |} character separates the items of the DEPS column, so it cannot be part of a
     * relation in that column. Writing it would produce a file that cannot be read back.
     */
    @Test
    public void thatPipeInDepsRelationIsRejected(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        dependency(jcas, a, b, "x|y", DependencyFlavor.ENHANCED);

        assertWriterRejects(jcas, tempDir, "DEPS");
    }

    /**
     * A dependency without a dependent cannot be written and must be rejected with a helpful
     * message instead of failing with a {@link NullPointerException}.
     */
    @Test
    public void thatDependencyWithoutDependentIsRejected(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        token(jcas, 2, 3);
        dependency(jcas, a, null, "dep", DependencyFlavor.BASIC);

        assertWriterRejects(jcas, tempDir, "dependent");
    }

    /**
     * Empty node IDs must have the form {@code i.k}, must refer to a word of the sentence and must
     * be unique.
     */
    @Test
    public void thatInvalidEmptyNodeIdsAreRejected(@TempDir File tempDir) throws Exception
    {
        for (List<String> ids : List.of(List.of("a.b"), List.of("1.x"), List.of("5.1"),
                List.of("1.1", "1.1"))) {
            JCas jcas = JCasFactory.createText("a b");
            new Sentence(jcas, 0, 3).addToIndexes();
            token(jcas, 0, 1);
            token(jcas, 2, 3);
            for (String id : ids) {
                Token emptyNode = new Token(jcas, 1, 1);
                emptyNode.setId(id);
                emptyNode.addToIndexes();
            }

            assertWriterRejects(jcas, tempDir, "[" + ids.get(0) + "]");
        }
    }

    /**
     * A multi-word token must cover complete words and must not overlap with another multi-word
     * token.
     */
    @Test
    public void thatInvalidSurfaceFormsAreRejected(@TempDir File tempDir) throws Exception
    {
        // Surface form ends within the word it starts at
        JCas partial = JCasFactory.createText("abc");
        new Sentence(partial, 0, 3).addToIndexes();
        token(partial, 0, 3);
        surfaceForm(partial, 0, 2, "ab");
        assertWriterRejects(partial, tempDir, "ab");

        // Overlapping surface forms
        JCas overlapping = JCasFactory.createText("a b c");
        new Sentence(overlapping, 0, 5).addToIndexes();
        token(overlapping, 0, 1);
        token(overlapping, 2, 3);
        token(overlapping, 4, 5);
        surfaceForm(overlapping, 0, 3, "ab");
        surfaceForm(overlapping, 2, 5, "bc");
        assertWriterRejects(overlapping, tempDir, "bc");
    }

    /**
     * A surface form covering only a single word is not a multi-word token. CoNLL-U cannot express
     * it - a range must span at least two words - so it must be rejected instead of producing an
     * invalid range such as {@code 1-1}.
     */
    @Test
    public void thatSingleWordSurfaceFormIsRejected(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("ab c");
        new Sentence(jcas, 0, 4).addToIndexes();
        token(jcas, 0, 2);
        token(jcas, 3, 4);
        surfaceForm(jcas, 0, 2, "AB");

        assertWriterRejects(jcas, tempDir, "AB");
    }

    /**
     * The rule assigning an empty node at the boundary of two touching sentences by its ID must
     * only be applied if the sentences actually touch. Otherwise, all empty nodes covered by a
     * sentence belong to it - also if they are not placed exactly where the reader would place them
     * (e.g. in a CAS built by another component).
     */
    @Test
    public void thatEmptyNodesAreKeptIfSentencesDoNotTouch(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText(" a b");
        new Sentence(jcas, 0, 4).addToIndexes();
        token(jcas, 1, 2);
        token(jcas, 3, 4);
        // `0.1` not at the begin of the sentence, `1.1` not at the end of word 1
        for (String[] node : new String[][] { { "0.1", "1" }, { "1.1", "0" } }) {
            int offset = Integer.parseInt(node[1]);
            Token emptyNode = new Token(jcas, offset, offset);
            emptyNode.setId(node[0]);
            emptyNode.addToIndexes();
        }

        assertThat(writeToString(jcas, tempDir).split("\n")) //
                .extracting(line -> line.split("\t")[0]) //
                .containsExactly("0.1", "1", "1.1", "2");
    }

    /**
     * The HEAD and DEPREL columns of an empty node must always be empty, so a basic dependency
     * whose dependent is an empty node cannot be expressed and must be rejected instead of being
     * dropped silently.
     */
    @Test
    public void thatBasicDependencyWithEmptyNodeAsDependentIsRejected(@TempDir File tempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        token(jcas, 2, 3);
        Token emptyNode = new Token(jcas, 1, 1);
        emptyNode.setId("1.1");
        emptyNode.addToIndexes();
        dependency(jcas, a, emptyNode, "dep", DependencyFlavor.BASIC);

        assertWriterRejects(jcas, tempDir, "[1.1]");
    }

    /**
     * If dependencies are not written, they must not be validated either - this allows writing data
     * whose dependency structure cannot be expressed in CoNLL-U.
     */
    @Test
    public void thatDependenciesAreIgnoredIfNotWritten(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b c");
        new Sentence(jcas, 0, 5).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        Token c = token(jcas, 4, 5);
        dependency(jcas, a, c, "dep", DependencyFlavor.BASIC);
        dependency(jcas, b, c, "dep", DependencyFlavor.BASIC);
        dependency(jcas, a, null, "dep", DependencyFlavor.BASIC);

        DocumentMetaData.create(jcas).setDocumentId("output");
        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false, //
                ConllUWriter.PARAM_WRITE_DEPENDENCY, false);
        writer.process(jcas);

        assertThat(readFileToString(new File(tempDir, "output.conllu"), "UTF-8").trim())
                .isEqualToNormalizingNewlines(String.join("\n", //
                        "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                        "2\tb\t_\t_\t_\t_\t_\t_\t_\t_", //
                        "3\tc\t_\t_\t_\t_\t_\t_\t_\t_"));
    }

    /**
     * {@code SpaceAfter} applies to tokens, not to the words of a multi-word token. If the words of
     * a multi-word token touch, this must not produce {@code SpaceAfter=No} on the words.
     */
    @Test
    public void thatWordsOfMultiwordTokenHaveNoSpaceAfter(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("del mar");
        new Sentence(jcas, 0, 7).addToIndexes();
        token(jcas, 0, 2);
        token(jcas, 2, 3);
        token(jcas, 4, 7);
        surfaceForm(jcas, 0, 3, "del");

        assertThat(writeToString(jcas, tempDir)).isEqualToNormalizingNewlines(String.join("\n", //
                "1-2\tdel\t_\t_\t_\t_\t_\t_\t_\t_", //
                "1\tde\t_\t_\t_\t_\t_\t_\t_\t_", //
                "2\tl\t_\t_\t_\t_\t_\t_\t_\t_", //
                "3\tmar\t_\t_\t_\t_\t_\t_\t_\t_"));
    }

    /**
     * The items of the DEPS column must be sorted by head and items with the same head by relation.
     */
    @Test
    public void thatDepsWithSameHeadAreSortedByRelation(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        dependency(jcas, a, b, "obj", DependencyFlavor.ENHANCED);
        dependency(jcas, a, b, "nsubj", DependencyFlavor.ENHANCED);

        assertThat(writeToString(jcas, tempDir).split("\n")) //
                .contains("2\tb\t_\t_\t_\t_\t_\t_\t1:nsubj|1:obj\t_");
    }

    /**
     * A multi-word token with an empty surface string is treated like one without a surface string
     * - its words are retained in the {@code # text} comment.
     */
    @Test
    public void thatEmptySurfaceStringDoesNotAffectTextComment(@TempDir File tempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("de el x");
        new Sentence(jcas, 0, 7).addToIndexes();
        token(jcas, 0, 2);
        token(jcas, 3, 5);
        token(jcas, 6, 7);
        surfaceForm(jcas, 0, 5, "");

        DocumentMetaData.create(jcas).setDocumentId("output");
        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir);
        writer.process(jcas);

        assertThat(readFileToString(new File(tempDir, "output.conllu"), "UTF-8").split("\n")) //
                .contains("# text = de el x", "1-2\t_\t_\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * Empty node IDs which are too large to be represented or which are not in canonical form (e.g.
     * with leading zeros) must be rejected with a helpful message.
     */
    @Test
    public void thatOversizedOrNonCanonicalEmptyNodeIdsAreRejected(@TempDir File tempDir)
        throws Exception
    {
        for (String id : List.of("99999999999.1", "1.99999999999", "01.1")) {
            JCas jcas = JCasFactory.createText("a b");
            new Sentence(jcas, 0, 3).addToIndexes();
            token(jcas, 0, 1);
            token(jcas, 2, 3);
            Token emptyNode = new Token(jcas, 1, 1);
            emptyNode.setId(id);
            emptyNode.addToIndexes();

            assertWriterRejects(jcas, tempDir, "[" + id + "]");
        }
    }

    /**
     * An empty node at the offset where two sentences touch must be written exactly once - also if
     * a zero-width sentence sits at the same offset.
     */
    @Test
    public void thatZeroWidthSentenceDoesNotDuplicateEmptyNodes(@TempDir File tempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("a bcd");
        new Sentence(jcas, 0, 3).addToIndexes();
        new Sentence(jcas, 3, 3).addToIndexes();
        new Sentence(jcas, 3, 5).addToIndexes();
        token(jcas, 0, 1);
        token(jcas, 2, 3);
        token(jcas, 3, 4);
        token(jcas, 4, 5);
        for (String id : List.of("2.1", "0.1")) {
            Token emptyNode = new Token(jcas, 3, 3);
            emptyNode.setId(id);
            emptyNode.addToIndexes();
        }

        List<String> lines = List.of(writeToString(jcas, tempDir).split("\n"));
        assertThat(lines).filteredOn(line -> line.startsWith("2.1\t")).hasSize(1);
        assertThat(lines).filteredOn(line -> line.startsWith("0.1\t")).hasSize(1);
    }

    /**
     * A dependency which is not covered by any sentence cannot be assigned to a sentence and must
     * be rejected instead of being dropped silently.
     */
    @Test
    public void thatDependencyNotCoveredBySentenceIsRejected(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 1).addToIndexes();
        new Sentence(jcas, 2, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        Dependency dep = new Dependency(jcas, 0, 3);
        dep.setGovernor(a);
        dep.setDependent(b);
        dep.setDependencyType("dep");
        dep.setFlavor(DependencyFlavor.BASIC);
        dep.addToIndexes();

        assertWriterRejects(jcas, tempDir, "dep");
    }

    /**
     * A dependency without a governor must be reported as such rather than as a governor which is
     * not part of the sentence.
     */
    @Test
    public void thatDependencyWithoutGovernorIsRejected(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        Token a = token(jcas, 0, 1);
        dependency(jcas, null, a, "root", DependencyFlavor.BASIC);

        assertWriterRejects(jcas, tempDir, "no governor");
    }

    /**
     * Paragraph and document markers are only written if enabled.
     */
    @Test
    public void thatParagraphsAndDocumentsCanBeOmitted(@TempDir File tempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        token(jcas, 0, 1);
        new Paragraph(jcas, 0, 1).addToIndexes();
        Document document = new Document(jcas, 0, 1);
        document.setId("d1");
        document.addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");
        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, tempDir, //
                ConllUWriter.PARAM_WRITE_PARAGRAPH, false, //
                ConllUWriter.PARAM_WRITE_DOCUMENTS, false);
        writer.process(jcas);

        assertThat(readFileToString(new File(tempDir, "output.conllu"), "UTF-8").split("\n")) //
                .noneMatch(line -> line.startsWith("# newpar") || line.startsWith("# newdoc"));
    }

    private static String writeToString(JCas aJCas, File aTempDir) throws Exception
    {
        DocumentMetaData.create(aJCas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false, //
                ConllUWriter.PARAM_OVERWRITE, true);
        writer.process(aJCas);

        return readFileToString(new File(aTempDir, "output.conllu"), "UTF-8").trim();
    }

    private static Token token(JCas aJCas, int aBegin, int aEnd)
    {
        Token token = new Token(aJCas, aBegin, aEnd);
        token.addToIndexes();
        return token;
    }

    private static void surfaceForm(JCas aJCas, int aBegin, int aEnd, String aValue)
    {
        SurfaceForm surfaceForm = new SurfaceForm(aJCas, aBegin, aEnd);
        surfaceForm.setValue(aValue);
        surfaceForm.addToIndexes();
    }

    private static void dependency(JCas aJCas, Token aGovernor, Token aDependent, String aType,
            String aFlavor)
    {
        Token anchor = aDependent != null ? aDependent : aGovernor;
        Dependency dep = new Dependency(aJCas, anchor.getBegin(), anchor.getEnd());
        dep.setGovernor(aGovernor);
        dep.setDependent(aDependent);
        dep.setDependencyType(aType);
        dep.setFlavor(aFlavor);
        dep.addToIndexes();
    }

    private static List<String> newdocLines(String aConllU)
    {
        return Stream.of(aConllU.split("\n")) //
                .filter(line -> line.startsWith("# newdoc")) //
                .collect(toList());
    }
}
