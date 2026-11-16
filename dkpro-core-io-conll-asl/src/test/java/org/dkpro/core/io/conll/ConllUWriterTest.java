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
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Document;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.SurfaceForm;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;

public class ConllUWriterTest
{
    @Test
    public void thatLineBreaksDoNotBreakTheFormat(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("Test\ntest.");
        new Sentence(jcas, 0, 10).addToIndexes();
        new Token(jcas, 0, 4).addToIndexes();
        new Token(jcas, 5, 9).addToIndexes();
        new Token(jcas, 9, 10).addToIndexes();

        DocumentMetaData dmd = DocumentMetaData.create(jcas);
        dmd.setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, ConllUWriter.PARAM_TARGET_LOCATION,
                aTempDir);

        writer.process(jcas);

        String reference = readFileToString(
                new File("src/test/resources/conll/u_v2/conllu-linebreaks.conllu"), UTF_8).trim();
        String actual = readFileToString(new File(aTempDir, "output.conllu"), UTF_8).trim();

        assertThat(actual).isEqualToNormalizingNewlines(reference);
    }

    /**
     * Empty nodes are modelled as zero-width tokens and must be written back out with their
     * original decimal ID and form. This cannot be covered by a regular round-trip test because
     * {@code NoZeroSizeTokensAndSentencesCheck} - which {@code IOTestRunner} applies to all
     * round-trip tests - rejects zero-width tokens.
     */
    @Test
    public void thatEmptyNodesRoundTrip(@TempDir File aTempDir) throws Exception
    {
        File reference = new File("src/test/resources/conll/u_v2/conllu-empty_nodes.conllu");

        SimplePipeline.runPipeline(
                createReaderDescription(ConllUReader.class, ConllUReader.PARAM_LANGUAGE, "en",
                        ConllUReader.PARAM_SOURCE_LOCATION, reference.getPath(),
                        ConllUReader.PARAM_READ_EMPTY_NODES, true),
                createEngineDescription(ConllUWriter.class, ConllUWriter.PARAM_TARGET_LOCATION,
                        aTempDir, ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false,
                        ConllUWriter.PARAM_OVERWRITE, true));

        String actual = readFileToString(new File(aTempDir, "conllu-empty_nodes.conllu.conllu"),
                UTF_8).trim();

        assertThat(actual).isEqualToNormalizingNewlines(readFileToString(reference, UTF_8).trim());
    }

    /**
     * The enhanced dependency graph may refer to empty nodes by their decimal ID.
     */
    @Test
    public void thatEnhancedDependenciesOnEmptyNodesRoundTrip(@TempDir File aTempDir)
        throws Exception
    {
        File source = new File(aTempDir, "in.conllu");
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
                        aTempDir, ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false,
                        ConllUWriter.PARAM_OVERWRITE, true));

        String actual = readFileToString(new File(aTempDir, "in.conllu.conllu"), UTF_8).trim();

        assertThat(actual).isEqualToNormalizingNewlines(conllu.trim());
    }

    /**
     * The HEAD column can only refer to regular tokens. A basic dependency governed by an empty
     * node cannot be expressed and must be rejected instead of producing an invalid HEAD value.
     */
    @Test
    public void thatBasicDependencyGovernedByEmptyNodeIsRejected(@TempDir File aTempDir)
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
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir);

        assertThatThrownBy(() -> writer.process(jcas)) //
                .rootCause() //
                .isInstanceOf(IllegalStateException.class) //
                .hasMessageContaining("[1.1]");
    }

    /**
     * The HEAD and DEPREL columns can only hold a single basic dependency per token. A token which
     * is the dependent of more than one basic dependency cannot be expressed and must be rejected
     * instead of silently dropping all but one of the relations. No output file is created in this
     * case.
     */
    @Test
    public void thatTokenWithMultipleBasicDependenciesIsRejected(@TempDir File aTempDir)
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
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir);

        assertThatThrownBy(() -> writer.process(jcas)) //
                .rootCause() //
                .isInstanceOf(IllegalStateException.class) //
                .hasMessageContaining("more than one");

        // No empty or partial output file must be left behind, which could be mistaken for a
        // successful conversion.
        assertThat(new File(aTempDir, "output.conllu")).doesNotExist();
    }

    /**
     * Sub-document spans are written as {@code # newdoc} comments in front of the first sentence
     * they contain - by default, and also if the span starts before that sentence (e.g. due to
     * indentation).
     */
    @Test
    public void thatDocumentsAreWrittenAsNewdoc(@TempDir File aTempDir) throws Exception
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
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false);

        writer.process(jcas);

        String actual = readFileToString(new File(aTempDir, "output.conllu"), UTF_8).trim();

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
    public void thatNoNewdocIsWrittenWithoutDocuments(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        new Token(jcas, 0, 1).addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir);

        writer.process(jcas);

        String actual = readFileToString(new File(aTempDir, "output.conllu"), UTF_8);

        assertThat(newdocLines(actual)).isEmpty();
    }

    /**
     * CoNLL-U addresses heads by their index within the sentence, so a dependency whose governor is
     * in a different sentence cannot be expressed and must be rejected.
     */
    @Test
    public void thatGovernorInOtherSentenceIsRejected(@TempDir File aTempDir) throws Exception
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
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir);

        assertThatThrownBy(() -> writer.process(jcas)) //
                .rootCause() //
                .isInstanceOf(IllegalStateException.class) //
                .hasMessageContaining("governor which is not part of the sentence");
    }

    /**
     * CoNLL-U has no escape syntax, so a tab in a value would add a column and corrupt the line.
     * The writer must reject such a value with a message naming the affected column.
     */
    @Test
    public void thatTabInFormIsRejected(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a\tb");
        new Sentence(jcas, 0, 3).addToIndexes();
        new Token(jcas, 0, 3).addToIndexes();

        assertWriterRejects(jcas, aTempDir, "FORM");
    }

    /**
     * CoNLL-U has no escape syntax, so a line break in a value would split the line. The writer
     * must reject such a value with a message naming the affected column.
     */
    @Test
    public void thatLineBreakInLemmaIsRejected(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        Token token = new Token(jcas, 0, 1);
        token.addToIndexes();
        Lemma lemma = new Lemma(jcas, 0, 1);
        lemma.setValue("a\nb");
        lemma.addToIndexes();
        token.setLemma(lemma);

        assertWriterRejects(jcas, aTempDir, "LEMMA");
    }

    /**
     * A line break in a comment value would end the comment and turn the remainder into an invalid
     * token line. The writer must reject such a value with a message naming the affected comment.
     */
    @Test
    public void thatLineBreakInSentenceIdIsRejected(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        Sentence sentence = new Sentence(jcas, 0, 1);
        sentence.setId("s1\ns2");
        sentence.addToIndexes();
        new Token(jcas, 0, 1).addToIndexes();

        assertWriterRejects(jcas, aTempDir, "sent_id");
    }

    private static void assertWriterRejects(JCas aJCas, File aTempDir, String aExpectedMention)
        throws Exception
    {
        if (!JCasUtil.exists(aJCas, DocumentMetaData.class)) {
            DocumentMetaData.create(aJCas).setDocumentId("output");
        }

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
    public void thatEmptyNodeWithoutFormIsWrittenAsPlaceholder(@TempDir File aTempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        new Token(jcas, 0, 1).addToIndexes();
        Token emptyNode = new Token(jcas, 1, 1);
        emptyNode.setId("1.1");
        emptyNode.addToIndexes();
        enhancedRoot(jcas, emptyNode);
        new Token(jcas, 2, 3).addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false);

        writer.process(jcas);

        String actual = readFileToString(new File(aTempDir, "output.conllu"), UTF_8);

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
    public void thatIndentedParagraphIsWritten(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("  Hello.");
        new Paragraph(jcas, 0, 8).addToIndexes();
        new Sentence(jcas, 2, 8).addToIndexes();
        new Token(jcas, 2, 7).addToIndexes();
        new Token(jcas, 7, 8).addToIndexes();

        DocumentMetaData.create(jcas).setDocumentId("output");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false);

        writer.process(jcas);

        String actual = readFileToString(new File(aTempDir, "output.conllu"), UTF_8);

        assertThat(actual.split("\n")).contains("# newpar");
    }

    /**
     * A file may contain several documents, each starting with its own {@code # newdoc id}. Each of
     * the document boundaries must be retained when writing the data back out.
     */
    @Test
    public void thatMultipleDocumentIdsRoundTrip(@TempDir File aTempDir) throws Exception
    {
        File reference = new File(
                "src/test/resources/conll/u_v2/conllu-multiple_document_IDs.conllu");

        SimplePipeline.runPipeline(createReaderDescription(ConllUReader.class, //
                ConllUReader.PARAM_LANGUAGE, "en", //
                ConllUReader.PARAM_SOURCE_LOCATION, reference.getPath()),
                createEngineDescription(ConllUWriter.class, //
                        ConllUWriter.PARAM_TARGET_LOCATION, aTempDir));

        String actual = readFileToString(
                new File(aTempDir, "conllu-multiple_document_IDs.conllu.conllu"), UTF_8);

        assertThat(newdocLines(actual)).containsExactly( //
                "# newdoc id = mf920901-001", //
                "# newdoc id = mf920901-002");
    }

    /**
     * The spec allows {@code # newdoc} without an ID. Such document boundaries must be retained
     * when writing the data back out.
     */
    @Test
    public void thatValuelessNewdocRoundTrips(@TempDir File aTempDir) throws Exception
    {
        File source = new File(aTempDir, "in.conllu");
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
                        ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                        ConllUWriter.PARAM_OVERWRITE, true));

        String actual = readFileToString(new File(aTempDir, "in.conllu.conllu"), UTF_8);

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
    public void thatSentencesWithoutSeparatorAreReadBackOnSeparateLines(@TempDir File aTempDir)
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
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false);

        writer.process(jcas);

        File output = new File(aTempDir, "output.conllu");
        assertThat(readFileToString(output, UTF_8).split("\n")) //
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
    public void thatMissingValuesAreWrittenAsPlaceholder(@TempDir File aTempDir) throws Exception
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

        assertThat(writeToString(jcas, aTempDir)).isEqualToNormalizingNewlines(String.join("\n", //
                "1-2\t_\t_\t_\t_\t_\t_\t_\t_\t_", //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                "2\tb\t_\t_\t_\t_\t_\t_\t_\t_", //
                "3\tc\t_\t_\t_\t_\t_\t_\t_\t_"));
    }

    /**
     * An enhanced dependency without a relation type cannot be written in the DEPS column, which
     * has no notation for an unspecified relation.
     */
    @Test
    public void thatEnhancedDependencyWithoutRelationTypeIsRejected(@TempDir File aTempDir)
        throws Exception
    {
        for (String type : new String[] { null, "" }) {
            JCas jcas = JCasFactory.createText("a b");
            new Sentence(jcas, 0, 3).addToIndexes();
            Token a = token(jcas, 0, 1);
            Token b = token(jcas, 2, 3);
            dependency(jcas, a, b, type, DependencyFlavor.ENHANCED);

            assertWriterRejects(jcas, aTempDir, "DEPS");
        }
    }

    /**
     * A basic dependency without a relation type (e.g. an arc not yet labeled during annotation) is
     * written with its HEAD and an unspecified DEPREL.
     */
    @Test
    public void thatUnlabeledBasicDependencyIsWrittenWithUnspecifiedDeprel(@TempDir File aTempDir)
        throws Exception
    {
        for (String type : new String[] { null, "" }) {
            JCas jcas = JCasFactory.createText("a b");
            new Sentence(jcas, 0, 3).addToIndexes();
            Token a = token(jcas, 0, 1);
            Token b = token(jcas, 2, 3);
            dependency(jcas, a, b, type, DependencyFlavor.BASIC);

            assertThat(writeToString(jcas, aTempDir).split("\n")) //
                    .contains("2\tb\t_\t_\t_\t_\t1\t_\t_\t_");
        }
    }

    /**
     * The {@code |} character separates the items of the DEPS column, so it cannot be part of a
     * relation in that column. Writing it would produce a file that cannot be read back.
     */
    @Test
    public void thatPipeInDepsRelationIsRejected(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        dependency(jcas, a, b, "x|y", DependencyFlavor.ENHANCED);

        assertWriterRejects(jcas, aTempDir, "DEPS");
    }

    /**
     * A dependency without a dependent cannot be written and must be rejected with a helpful
     * message instead of failing with a {@link NullPointerException}.
     */
    @Test
    public void thatDependencyWithoutDependentIsRejected(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        token(jcas, 2, 3);
        dependency(jcas, a, null, "dep", DependencyFlavor.BASIC);

        assertWriterRejects(jcas, aTempDir, "dependent");
    }

    /**
     * Empty node IDs must have the form {@code i.k}, must refer to a word of the sentence and must
     * be unique.
     */
    @Test
    public void thatInvalidEmptyNodeIdsAreRejected(@TempDir File aTempDir) throws Exception
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

            assertWriterRejects(jcas, aTempDir, "[" + ids.get(0) + "]");
        }
    }

    /**
     * A multi-word token must cover complete words and must not overlap with another multi-word
     * token.
     */
    @Test
    public void thatInvalidSurfaceFormsAreRejected(@TempDir File aTempDir) throws Exception
    {
        // Surface form ends within the word it starts at
        JCas partial = JCasFactory.createText("abc");
        new Sentence(partial, 0, 3).addToIndexes();
        token(partial, 0, 3);
        surfaceForm(partial, 0, 2, "ab");
        assertWriterRejects(partial, aTempDir, "ab");

        // Overlapping surface forms
        JCas overlapping = JCasFactory.createText("a b c");
        new Sentence(overlapping, 0, 5).addToIndexes();
        token(overlapping, 0, 1);
        token(overlapping, 2, 3);
        token(overlapping, 4, 5);
        surfaceForm(overlapping, 0, 3, "ab");
        surfaceForm(overlapping, 2, 5, "bc");
        assertWriterRejects(overlapping, aTempDir, "bc");
    }

    /**
     * A surface form covering only a single word is not a multi-word token. CoNLL-U cannot express
     * it - a range must span at least two words - so it must be rejected instead of producing an
     * invalid range such as {@code 1-1}.
     */
    @Test
    public void thatSingleWordSurfaceFormIsRejected(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("ab c");
        new Sentence(jcas, 0, 4).addToIndexes();
        token(jcas, 0, 2);
        token(jcas, 3, 4);
        surfaceForm(jcas, 0, 2, "AB");

        assertWriterRejects(jcas, aTempDir, "AB");
    }

    /**
     * The rule assigning an empty node at the boundary of two touching sentences by its ID must
     * only be applied if the sentences actually touch. Otherwise, all empty nodes covered by a
     * sentence belong to it - also if they are not placed exactly where the reader would place them
     * (e.g. in a CAS built by another component).
     */
    @Test
    public void thatEmptyNodesAreKeptIfSentencesDoNotTouch(@TempDir File aTempDir) throws Exception
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
            enhancedRoot(jcas, emptyNode);
        }

        assertThat(writeToString(jcas, aTempDir).split("\n")) //
                .extracting(line -> line.split("\t")[0]) //
                .containsExactly("0.1", "1", "1.1", "2");
    }

    /**
     * The HEAD and DEPREL columns of an empty node must always be empty, so a basic dependency
     * whose dependent is an empty node cannot be expressed and must be rejected instead of being
     * dropped silently.
     */
    @Test
    public void thatBasicDependencyWithEmptyNodeAsDependentIsRejected(@TempDir File aTempDir)
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

        assertWriterRejects(jcas, aTempDir, "[1.1]");
    }

    /**
     * If dependencies are not written, they must not be validated either - this allows writing data
     * whose dependency structure cannot be expressed in CoNLL-U.
     */
    @Test
    public void thatDependenciesAreIgnoredIfNotWritten(@TempDir File aTempDir) throws Exception
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
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false, //
                ConllUWriter.PARAM_WRITE_DEPENDENCY, false);
        writer.process(jcas);

        assertThat(readFileToString(new File(aTempDir, "output.conllu"), UTF_8).trim())
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
    public void thatWordsOfMultiwordTokenHaveNoSpaceAfter(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("del mar");
        new Sentence(jcas, 0, 7).addToIndexes();
        token(jcas, 0, 2);
        token(jcas, 2, 3);
        token(jcas, 4, 7);
        surfaceForm(jcas, 0, 3, "del");

        assertThat(writeToString(jcas, aTempDir)).isEqualToNormalizingNewlines(String.join("\n", //
                "1-2\tdel\t_\t_\t_\t_\t_\t_\t_\t_", //
                "1\tde\t_\t_\t_\t_\t_\t_\t_\t_", //
                "2\tl\t_\t_\t_\t_\t_\t_\t_\t_", //
                "3\tmar\t_\t_\t_\t_\t_\t_\t_\t_"));
    }

    /**
     * The items of the DEPS column must be sorted by head and items with the same head by relation.
     */
    @Test
    public void thatDepsWithSameHeadAreSortedByRelation(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        dependency(jcas, a, b, "obj", DependencyFlavor.ENHANCED);
        dependency(jcas, a, b, "nsubj", DependencyFlavor.ENHANCED);

        assertThat(writeToString(jcas, aTempDir).split("\n")) //
                .contains("2\tb\t_\t_\t_\t_\t_\t_\t1:nsubj|1:obj\t_");
    }

    /**
     * A multi-word token with an empty surface string is treated like one without a surface string
     * - its words are retained in the {@code # text} comment.
     */
    @Test
    public void thatEmptySurfaceStringDoesNotAffectTextComment(@TempDir File aTempDir)
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
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir);
        writer.process(jcas);

        assertThat(readFileToString(new File(aTempDir, "output.conllu"), UTF_8).split("\n")) //
                .contains("# text = de el x", "1-2\t_\t_\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * Empty node IDs which are too large to be represented or which are not in canonical form (e.g.
     * with leading zeros) must be rejected with a helpful message.
     */
    @Test
    public void thatOversizedOrNonCanonicalEmptyNodeIdsAreRejected(@TempDir File aTempDir)
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

            assertWriterRejects(jcas, aTempDir, "[" + id + "]");
        }
    }

    /**
     * An empty node at the offset where two sentences touch must be written exactly once - also if
     * a zero-width sentence sits at the same offset.
     */
    @Test
    public void thatZeroWidthSentenceDoesNotDuplicateEmptyNodes(@TempDir File aTempDir)
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
            enhancedRoot(jcas, emptyNode);
        }

        List<String> lines = List.of(writeToString(jcas, aTempDir).split("\n"));
        assertThat(lines).filteredOn(line -> line.startsWith("2.1\t")).hasSize(1);
        assertThat(lines).filteredOn(line -> line.startsWith("0.1\t")).hasSize(1);
    }

    /**
     * A dependency whose dependent is not covered by any sentence cannot be assigned to a sentence.
     * In strict mode, the uncovered dependent itself already makes the writer fail - in lenient
     * mode, both the token and the dependency are skipped and reported.
     */
    @Test
    public void thatDependenciesOutsideSentencesAreSkippedInLenientMode(@TempDir File aTempDir)
        throws Throwable
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 1).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        dependency(jcas, a, b, "dep", DependencyFlavor.BASIC);

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")).containsExactly( //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * The range line of a multi-word token must be written exactly once - also if several tokens
     * start at the begin offset of the multi-word token (e.g. overlapping or zero-width tokens).
     */
    @Test
    public void thatMultiwordTokenRangeIsWrittenOnlyOnce(@TempDir File aTempDir) throws Exception
    {
        JCas overlapping = JCasFactory.createText("del");
        new Sentence(overlapping, 0, 3).addToIndexes();
        token(overlapping, 0, 3);
        token(overlapping, 0, 2);
        token(overlapping, 2, 3);
        surfaceForm(overlapping, 0, 3, "del");
        assertThat(writeToString(overlapping, aTempDir).split("\n")) //
                .filteredOn(line -> line.matches("\\d+-\\d+\t.*")) //
                .containsExactly("1-3\tdel\t_\t_\t_\t_\t_\t_\t_\t_");

        // A zero-width word cannot be written - in lenient mode, it is skipped, but the
        // multi-word token is still written over the remaining words.
        JCas zeroWidth = JCasFactory.createText("del");
        new Sentence(zeroWidth, 0, 3).addToIndexes();
        token(zeroWidth, 0, 2);
        token(zeroWidth, 2, 3);
        token(zeroWidth, 0, 0);
        surfaceForm(zeroWidth, 0, 3, "del");
        writeLeniently(zeroWidth, aTempDir);
        assertThat(readOutput(aTempDir).split("\n")) //
                .filteredOn(line -> line.matches("\\d+-\\d+\t.*")) //
                .containsExactly("1-2\tdel\t_\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * In lenient mode, empty nodes which cannot be written - because their ID is malformed, refers
     * to a word which does not exist (e.g. because a token was deleted in an annotation tool) or is
     * used twice - are skipped and reported. Dependencies involving them are skipped along with
     * them without being reported again.
     */
    @Test
    public void thatInvalidEmptyNodesAreSkippedInLenientMode(@TempDir File aTempDir)
        throws Throwable
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        Token first = emptyNode(jcas, 1, "1.1", "x");
        Token duplicate = emptyNode(jcas, 1, "1.1", "x");
        Token z = emptyNode(jcas, 1, "1.3", "z");
        dependency(jcas, a, z, "y", DependencyFlavor.ENHANCED);
        Token dangling = emptyNode(jcas, 3, "5.1", "w");
        emptyNode(jcas, 1, "a.b", "v");
        dependency(jcas, a, first, "x", DependencyFlavor.ENHANCED);
        dependency(jcas, a, duplicate, "x", DependencyFlavor.ENHANCED);
        dependency(jcas, dangling, b, "dep", DependencyFlavor.ENHANCED);

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")).containsExactly( //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                "1.1\tx\t_\t_\t_\t_\t_\t_\t1:x\t_", //
                "1.2\tz\t_\t_\t_\t_\t_\t_\t1:y\t_", //
                "2\tb\t_\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * The empty nodes following a word are renumbered {@code i.1}, {@code i.2}, ... so that the
     * output is valid also if some empty nodes have been removed. References to them are updated
     * accordingly.
     */
    @Test
    public void thatEmptyNodesAreRenumbered(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        Token x = emptyNode(jcas, 1, "1.2", "x");
        Token y = emptyNode(jcas, 1, "1.5", "y");
        enhancedRoot(jcas, x);
        enhancedRoot(jcas, y);
        dependency(jcas, y, b, "dep", DependencyFlavor.ENHANCED);

        assertThat(writeToString(jcas, aTempDir).split("\n")).containsExactly( //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                "1.1\tx\t_\t_\t_\t_\t_\t_\t0:root\t_", //
                "1.2\ty\t_\t_\t_\t_\t_\t_\t0:root\t_", //
                "2\tb\t_\t_\t_\t_\t_\t_\t1.2:dep\t_");
    }

    /**
     * Errors must name the document - the message itself at most identifies the sentence, which is
     * not sufficient to locate the problem in a corpus of many documents.
     */
    @Test
    public void thatErrorsNameTheDocument(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 1).addToIndexes();
        token(jcas, 0, 1);
        token(jcas, 2, 3);
        DocumentMetaData dmd = DocumentMetaData.create(jcas);
        dmd.setDocumentId("output");
        dmd.setDocumentUri("file:/corpus/doc-17.txt");

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir);

        assertThatThrownBy(() -> writer.process(jcas)) //
                .hasStackTraceContaining("Unable to write [file:/corpus/doc-17.txt]") //
                .rootCause() //
                .hasMessageContaining("not covered by any sentence");
    }

    /**
     * A dependency without a governor must be reported as such rather than as a governor which is
     * not part of the sentence.
     */
    @Test
    public void thatDependencyWithoutGovernorIsRejected(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        Token a = token(jcas, 0, 1);
        dependency(jcas, null, a, "root", DependencyFlavor.BASIC);

        assertWriterRejects(jcas, aTempDir, "no governor");
    }

    /**
     * Paragraph and document markers are only written if enabled.
     */
    @Test
    public void thatParagraphsAndDocumentsCanBeOmitted(@TempDir File aTempDir) throws Exception
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
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_PARAGRAPH, false, //
                ConllUWriter.PARAM_WRITE_DOCUMENTS, false);
        writer.process(jcas);

        assertThat(readFileToString(new File(aTempDir, "output.conllu"), UTF_8).split("\n")) //
                .noneMatch(line -> line.startsWith("# newpar") || line.startsWith("# newdoc"));
    }

    /**
     * In lenient mode, of several basic dependencies on a token only the first one is written and a
     * warning is logged instead of failing.
     */
    @Test
    public void thatLenientModeKeepsFirstOfMultipleBasicDependencies(@TempDir File aTempDir)
        throws Throwable
    {
        JCas jcas = JCasFactory.createText("a b c");
        new Sentence(jcas, 0, 5).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        Token c = token(jcas, 4, 5);
        dependency(jcas, a, c, "first", DependencyFlavor.BASIC);
        dependency(jcas, b, c, "second", DependencyFlavor.BASIC);

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")).contains("3\tc\t_\t_\t_\t_\t1\tfirst\t_\t_");
    }

    /**
     * In lenient mode, a surface form which cannot be written as a multi-word token is skipped and
     * a warning is logged instead of failing.
     */
    @Test
    public void thatLenientModeSkipsInvalidSurfaceForms(@TempDir File aTempDir) throws Throwable
    {
        JCas jcas = JCasFactory.createText("ab c");
        new Sentence(jcas, 0, 4).addToIndexes();
        token(jcas, 0, 2);
        token(jcas, 3, 4);
        surfaceForm(jcas, 0, 2, "AB");

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")) //
                .noneMatch(line -> line.matches("^\\d+-\\d+\t.*"));
    }

    /**
     * In lenient mode, dependencies which cannot be expressed in CoNLL-U are skipped and a warning
     * is logged instead of failing.
     */
    @Test
    public void thatLenientModeSkipsInexpressibleDependencies(@TempDir File aTempDir)
        throws Throwable
    {
        JCas jcas = JCasFactory.createText("a b c");
        new Sentence(jcas, 0, 3).addToIndexes();
        new Sentence(jcas, 4, 5).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        Token c = token(jcas, 4, 5);
        // Governor in another sentence
        dependency(jcas, a, c, "cross", DependencyFlavor.BASIC);
        // Enhanced dependency without relation type
        dependency(jcas, a, b, null, DependencyFlavor.ENHANCED);
        // Relation containing the DEPS separator
        dependency(jcas, b, a, "x|y", DependencyFlavor.ENHANCED);

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir)).isEqualToNormalizingNewlines(String.join("\n", //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                "2\tb\t_\t_\t_\t_\t_\t_\t_\t_", //
                "", //
                "1\tc\t_\t_\t_\t_\t_\t_\t_\t_"));
    }

    /**
     * In lenient mode, tabs and line breaks in values are replaced with spaces and a warning is
     * logged instead of failing.
     */
    @Test
    public void thatLenientModeReplacesTabsAndLineBreaksInValues(@TempDir File aTempDir)
        throws Throwable
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        Token token = token(jcas, 0, 1);
        Lemma lemma = new Lemma(jcas, 0, 1);
        lemma.setValue("x\ny\tz");
        lemma.addToIndexes();
        token.setLemma(lemma);

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")).contains("1\ta\tx y z\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * Tokens outside any sentence cannot be written. By default, this fails - in lenient mode, they
     * are skipped and a warning is logged.
     */
    @Test
    public void thatTokensOutsideSentencesAreRejectedOrSkipped(@TempDir File aTempDir)
        throws Throwable
    {
        JCas strict = JCasFactory.createText("a b");
        new Sentence(strict, 0, 1).addToIndexes();
        token(strict, 0, 1);
        token(strict, 2, 3);
        assertWriterRejects(strict, aTempDir, "not covered by any sentence");

        JCas lenient = JCasFactory.createText("a b");
        new Sentence(lenient, 0, 1).addToIndexes();
        token(lenient, 0, 1);
        token(lenient, 2, 3);
        writeLeniently(lenient, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")).containsExactly( //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * A sentence without tokens cannot be written - an empty sentence block is invalid CoNLL-U and
     * cannot be read back. Such sentences are skipped and a warning is logged.
     */
    @Test
    public void thatSentencesWithoutTokensAreSkipped(@TempDir File aTempDir) throws Throwable
    {
        JCas jcas = JCasFactory.createText("a bcd e");
        new Sentence(jcas, 0, 1).addToIndexes();
        new Sentence(jcas, 2, 5).addToIndexes();
        new Sentence(jcas, 6, 7).addToIndexes();
        token(jcas, 0, 1);
        token(jcas, 6, 7);

        DocumentMetaData.create(jcas).setDocumentId("output");
        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir);
        writer.process(jcas);

        assertThat(readOutput(aTempDir)).isEqualToNormalizingNewlines(String.join("\n", //
                "# text = a", //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                "", //
                "# text = e", //
                "1\te\t_\t_\t_\t_\t_\t_\t_\t_"));
    }

    /**
     * A sentence must contain at least one word - a sentence consisting only of empty nodes would
     * be rejected when reading the file back. Such sentences are skipped along with their empty
     * nodes and the dependencies involving them, and a warning is logged. This applies also in
     * strict mode - but a dependency of a word in another sentence on such an empty node cannot be
     * written, so it is a problem.
     */
    @Test
    public void thatSentencesWithOnlyEmptyNodesAreSkipped(@TempDir File aTempDir) throws Throwable
    {
        JCas jcas = JCasFactory.createText("a bcd e");
        new Sentence(jcas, 0, 1).addToIndexes();
        new Sentence(jcas, 2, 5).addToIndexes();
        new Sentence(jcas, 6, 7).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token e = token(jcas, 6, 7);
        Token emptyNode = emptyNode(jcas, 2, "0.1", "x");
        dependency(jcas, a, emptyNode, "dep", DependencyFlavor.ENHANCED);
        dependency(jcas, emptyNode, e, "dep", DependencyFlavor.ENHANCED);

        assertWriterRejects(jcas, aTempDir, "governor in a sentence which is skipped");

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")).containsExactly( //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                "", //
                "1\te\t_\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * Of several basic dependencies on a token, the first valid one is written in lenient mode - an
     * invalid dependency (e.g. one governed by an empty node) must not displace a valid one.
     */
    @Test
    public void thatInvalidBasicDependencyDoesNotDisplaceValidOne(@TempDir File aTempDir)
        throws Throwable
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        Token emptyNode = emptyNode(jcas, 1, "1.1", "x");
        dependency(jcas, emptyNode, b, "invalid", DependencyFlavor.BASIC);
        dependency(jcas, a, b, "valid", DependencyFlavor.BASIC);

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\\n")).containsExactly( //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                "1.1\tx\t_\t_\t_\t_\t_\t_\t_\t_", //
                "2\tb\t_\t_\t_\t_\t1\tvalid\t_\t_");
    }

    /**
     * IDs must be written with ASCII digits regardless of the default locale - in some locales
     * (e.g. Arabic), numbers are otherwise formatted with other digits which no reader can parse.
     */
    @Test
    public void thatOutputDoesNotDependOnDefaultLocale(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("del");
        new Sentence(jcas, 0, 3).addToIndexes();
        token(jcas, 0, 2);
        token(jcas, 2, 3);
        surfaceForm(jcas, 0, 3, "del");

        // PrintWriter.printf uses the general default locale, not the one of the FORMAT category
        Locale defaultLocale = Locale.getDefault();
        Locale formatLocale = Locale.getDefault(Locale.Category.FORMAT);
        Locale displayLocale = Locale.getDefault(Locale.Category.DISPLAY);
        Locale.setDefault(Locale.forLanguageTag("ar-EG"));
        String output;
        try {
            output = writeToString(jcas, aTempDir);
        }
        finally {
            Locale.setDefault(defaultLocale);
            Locale.setDefault(Locale.Category.FORMAT, formatLocale);
            Locale.setDefault(Locale.Category.DISPLAY, displayLocale);
        }

        assertThat(output.split("\n")).containsExactly( //
                "1-2\tdel\t_\t_\t_\t_\t_\t_\t_\t_", //
                "1\tde\t_\t_\t_\t_\t_\t_\t_\t_", //
                "2\tl\t_\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * CoNLL-U requires the features to be sorted alphabetically by name (case-insensitive). Data
     * from sources other than CoNLL-U may not be sorted.
     */
    @Test
    public void thatFeaturesAreSorted(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        Token a = token(jcas, 0, 1);
        MorphologicalFeatures morph = new MorphologicalFeatures(jcas, 0, 1);
        morph.setValue("Number=Sing|Case=Nom|abbr=Yes");
        morph.addToIndexes();
        a.setMorph(morph);

        assertThat(writeToString(jcas, aTempDir))
                .isEqualTo("1\ta\t_\t_\t_\tabbr=Yes|Case=Nom|Number=Sing\t_\t_\t_\t_");
    }

    /**
     * Writer errors must point at the sentence containing the problem - by its ID if it has one,
     * otherwise by its offsets.
     */
    @Test
    public void thatWriterErrorsNameTheSentence(@TempDir File aTempDir) throws Exception
    {
        for (String id : new String[] { "s1", null }) {
            JCas jcas = JCasFactory.createText("x a b c");
            Sentence sentence = new Sentence(jcas, 2, 7);
            sentence.setId(id);
            sentence.addToIndexes();
            Token a = token(jcas, 2, 3);
            Token b = token(jcas, 4, 5);
            Token c = token(jcas, 6, 7);
            dependency(jcas, a, c, "dep", DependencyFlavor.BASIC);
            dependency(jcas, b, c, "dep", DependencyFlavor.BASIC);

            assertWriterRejects(jcas, aTempDir, id != null ? "[s1]" : "[2-7]");
        }
    }

    /**
     * A sentence extending beyond the end of the document text is an invalid CAS and must be
     * rejected with a helpful message.
     */
    @Test
    public void thatSentenceBeyondDocumentTextIsRejected(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("abc");
        new Sentence(jcas, 0, 10).addToIndexes();
        token(jcas, 0, 3);

        assertWriterRejects(jcas, aTempDir, "beyond");
    }

    /**
     * A dependency belongs to the sentence of its dependent - regardless of the offsets of the
     * dependency annotation itself.
     */
    @Test
    public void thatDependencyIsAssignedToSentenceOfDependent(@TempDir File aTempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("a b c");
        new Sentence(jcas, 0, 1).addToIndexes();
        new Sentence(jcas, 2, 5).addToIndexes();
        token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        Token c = token(jcas, 4, 5);
        Dependency dep = new Dependency(jcas, 0, 0);
        dep.setGovernor(b);
        dep.setDependent(c);
        dep.setDependencyType("dep");
        dep.setFlavor(DependencyFlavor.BASIC);
        dep.addToIndexes();

        assertThat(writeToString(jcas, aTempDir).split("\n")) //
                .contains("2\tc\t_\t_\t_\t_\t1\tdep\t_\t_");
    }

    /**
     * A multi-word token cannot span sentences.
     */
    @Test
    public void thatMultiwordTokenSpanningSentencesIsRejected(@TempDir File aTempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        new Sentence(jcas, 0, 1).addToIndexes();
        new Sentence(jcas, 2, 3).addToIndexes();
        token(jcas, 0, 1);
        token(jcas, 2, 3);
        surfaceForm(jcas, 0, 3, "ab");

        assertWriterRejects(jcas, aTempDir, "spans multiple sentences");
    }

    /**
     * Tags, features and relations must not contain whitespace, and forms and lemmas must not start
     * or end with whitespace - such values would not survive being read back.
     */
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "FORM", "LEMMA", "UPOS", "XPOS", "FEATS", "DEPREL" })
    public void thatWhitespaceInValuesIsRejected(String aColumn, @TempDir File aTempDir)
        throws Exception
    {
        assertWriterRejects(casWithWhitespaceIn(aColumn), aTempDir, aColumn);
    }

    /**
     * In lenient mode, values containing whitespace are written as they are and a warning is
     * logged.
     */
    @Test
    public void thatLenientModeKeepsWhitespaceInValues(@TempDir File aTempDir) throws Throwable
    {
        JCas jcas = casWithWhitespaceIn("XPOS");

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")) //
                .anyMatch(line -> line.contains("\tDOD *\t"));
    }

    /**
     * Create a CAS with whitespace in the given column - inner whitespace for tag columns, leading
     * or trailing whitespace for FORM and LEMMA.
     */
    private static JCas casWithWhitespaceIn(String aColumn) throws Exception
    {
        // "a", an ideographic space, a regular space and "b"
        JCas jcas = JCasFactory.createText("a\u3000 b");
        new Sentence(jcas, 0, 4).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 3, 4);
        if ("FORM".equals(aColumn)) {
            // A token starting with an ideographic space
            token(jcas, 1, 4);
        }
        if ("LEMMA".equals(aColumn)) {
            Lemma lemma = new Lemma(jcas, 0, 1);
            lemma.setValue(" a");
            lemma.addToIndexes();
            a.setLemma(lemma);
        }
        if ("UPOS".equals(aColumn) || "XPOS".equals(aColumn)) {
            POS pos = new POS(jcas, 0, 1);
            if ("UPOS".equals(aColumn)) {
                pos.setCoarseValue("NO UN");
            }
            else {
                pos.setPosValue("DOD *");
            }
            pos.addToIndexes();
            a.setPos(pos);
        }
        if ("FEATS".equals(aColumn)) {
            MorphologicalFeatures morph = new MorphologicalFeatures(jcas, 0, 1);
            morph.setValue("N Nom Sg");
            morph.addToIndexes();
            a.setMorph(morph);
        }
        if ("DEPREL".equals(aColumn)) {
            dependency(jcas, a, b, "a b", DependencyFlavor.BASIC);
        }
        return jcas;
    }

    private static void writeLeniently(JCas aJCas, File aTempDir) throws Exception
    {
        if (!JCasUtil.exists(aJCas, DocumentMetaData.class)) {
            DocumentMetaData.create(aJCas).setDocumentId("output");
        }
        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false, //
                ConllUWriter.PARAM_LENIENT, true, //
                ConllUWriter.PARAM_OVERWRITE, true);
        writer.process(aJCas);
    }

    private static String readOutput(File aTempDir) throws Exception
    {
        return readFileToString(new File(aTempDir, "output.conllu"), UTF_8).trim();
    }

    private static String writeToString(JCas aJCas, File aTempDir) throws Exception
    {
        if (!JCasUtil.exists(aJCas, DocumentMetaData.class)) {
            DocumentMetaData.create(aJCas).setDocumentId("output");
        }

        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false, //
                ConllUWriter.PARAM_OVERWRITE, true);
        writer.process(aJCas);

        return readFileToString(new File(aTempDir, "output.conllu"), UTF_8).trim();
    }

    /**
     * CoNLL-U requires empty nodes to have enhanced dependencies. In lenient mode, an empty node
     * without them is written nevertheless - other nodes may refer to it, and the lenient reader
     * accepts it.
     */
    @Test
    public void thatEmptyNodeWithoutEnhancedDependenciesIsRejected(@TempDir File aTempDir)
        throws Throwable
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        token(jcas, 0, 1);
        emptyNode(jcas, 1, "1.1", "x");

        assertWriterRejects(jcas, aTempDir, "[1.1]");

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")).containsExactly( //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_", //
                "1.1\tx\t_\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * Empty nodes only exist as part of the enhanced dependency graph - if dependencies are not
     * written, empty nodes are not written either.
     */
    @Test
    public void thatEmptyNodesAreNotWrittenWithoutDependencies(@TempDir File aTempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token x = emptyNode(jcas, 1, "1.1", "x");
        dependency(jcas, a, x, "dep", DependencyFlavor.ENHANCED);

        DocumentMetaData.create(jcas).setDocumentId("output");
        AnalysisEngine writer = createEngine(ConllUWriter.class, //
                ConllUWriter.PARAM_TARGET_LOCATION, aTempDir, //
                ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false, //
                ConllUWriter.PARAM_WRITE_DEPENDENCY, false);
        writer.process(jcas);

        assertThat(readOutput(aTempDir).split("\n")).containsExactly( //
                "1\ta\t_\t_\t_\t_\t_\t_\t_\t_");
    }

    /**
     * A blank document or paragraph ID cannot be written - {@code # newpar id = } is invalid - so
     * the boundary is written without an ID.
     */
    @Test
    public void thatBlankBoundaryIdsAreOmitted(@TempDir File aTempDir) throws Exception
    {
        JCas jcas = JCasFactory.createText("a");
        new Sentence(jcas, 0, 1).addToIndexes();
        token(jcas, 0, 1);
        Document document = new Document(jcas, 0, 1);
        document.setId("");
        document.addToIndexes();
        Paragraph paragraph = new Paragraph(jcas, 0, 1);
        paragraph.setId(" ");
        paragraph.addToIndexes();

        assertThat(writeToString(jcas, aTempDir).split("\n")) //
                .startsWith("# newdoc", "# newpar");
    }

    /**
     * CoNLL-U requires every word to have a form - a token which covers no text or only whitespace
     * cannot be written ({@code _} would be read back as a literal underscore). In lenient mode, it
     * is skipped together with the dependencies involving it, and the following words and empty
     * nodes are renumbered.
     */
    @Test
    public void thatTokensWithoutFormAreSkippedInLenientMode(@TempDir File aTempDir)
        throws Throwable
    {
        JCas jcas = JCasFactory.createText("a \u3000 b");
        new Sentence(jcas, 0, 5).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token blank = token(jcas, 2, 3);
        Token b = token(jcas, 4, 5);
        Token zeroWidth = token(jcas, 5, 5);
        Token x = emptyNode(jcas, 5, "3.1", "x");
        dependency(jcas, a, a, "root", DependencyFlavor.BASIC);
        dependency(jcas, a, b, "dep", DependencyFlavor.BASIC);
        dependency(jcas, a, blank, "dep", DependencyFlavor.BASIC);
        dependency(jcas, zeroWidth, b, "dep", DependencyFlavor.ENHANCED);
        dependency(jcas, b, x, "dep", DependencyFlavor.ENHANCED);

        assertWriterRejects(jcas, aTempDir, "[2-3]");

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")).containsExactly( //
                "1\ta\t_\t_\t_\t_\t0\troot\t_\t_", //
                "2\tb\t_\t_\t_\t_\t1\tdep\t_\t_", //
                "2.1\tx\t_\t_\t_\t_\t_\t_\t2:dep\t_");
    }

    /**
     * If a token without form is skipped, the empty nodes following it join the empty nodes of the
     * preceding word - or move to the start of the sentence - and are renumbered in order.
     */
    @Test
    public void thatEmptyNodesOfSkippedTokensAreRenumbered(@TempDir File aTempDir) throws Throwable
    {
        JCas jcas = JCasFactory.createText("\u3000 a \u3000 b");
        new Sentence(jcas, 0, 7).addToIndexes();
        token(jcas, 0, 1);
        token(jcas, 2, 3);
        token(jcas, 4, 5);
        token(jcas, 6, 7);
        // Word 1 and 3 are skipped
        for (String[] node : new String[][] { { "0.1", "0", "p" }, { "1.1", "1", "q" },
                { "2.1", "3", "r" }, { "3.1", "5", "s" }, { "3.2", "5", "t" },
                { "4.1", "7", "u" } }) {
            enhancedRoot(jcas, emptyNode(jcas, Integer.parseInt(node[1]), node[0], node[2]));
        }

        writeLeniently(jcas, aTempDir);

        assertThat(readOutput(aTempDir).split("\n")) //
                .extracting(line -> line.split("\t")[0] + " " + line.split("\t")[1]) //
                .containsExactly("0.1 p", "0.2 q", "1 a", "1.1 r", "1.2 s", "1.3 t", "2 b",
                        "2.1 u");
    }

    /**
     * The paragraph marker of a skipped sentence is carried over to the next sentence written - but
     * only if that sentence is still part of the paragraph.
     */
    @Test
    public void thatParagraphMarkerIsNotCarriedOutOfParagraph(@TempDir File aTempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("x a");
        // The first sentence contains no words and is skipped
        new Sentence(jcas, 0, 1).addToIndexes();
        new Sentence(jcas, 2, 3).addToIndexes();
        token(jcas, 2, 3);
        Paragraph paragraph = new Paragraph(jcas, 0, 1);
        paragraph.setId("p1");
        paragraph.addToIndexes();

        assertThat(writeToString(jcas, aTempDir).split("\n")) //
                .noneMatch(line -> line.startsWith("# newpar"));
    }

    private static Token token(JCas aJCas, int aBegin, int aEnd)
    {
        Token token = new Token(aJCas, aBegin, aEnd);
        token.addToIndexes();
        return token;
    }

    private static Token emptyNode(JCas aJCas, int aOffset, String aId, String aForm)
    {
        Token emptyNode = new Token(aJCas, aOffset, aOffset);
        emptyNode.setId(aId);
        emptyNode.setText(aForm);
        emptyNode.addToIndexes();
        return emptyNode;
    }

    /**
     * Make the given node a root of the enhanced graph - every empty node needs at least one
     * enhanced dependency.
     */
    private static void enhancedRoot(JCas aJCas, Token aNode)
    {
        dependency(aJCas, aNode, aNode, "root", DependencyFlavor.ENHANCED);
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
