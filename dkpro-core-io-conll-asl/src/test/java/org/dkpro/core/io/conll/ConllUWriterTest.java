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

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
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
    public void thatBasicDependencyOnEmptyNodeIsRejected(@TempDir File tempDir) throws Exception
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
                .hasRootCauseInstanceOf(IllegalStateException.class);
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
                        ConllUWriter.PARAM_TARGET_LOCATION, tempDir, //
                        ConllUWriter.PARAM_WRITE_DOCUMENT_ID, true));

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
                        ConllUWriter.PARAM_WRITE_DOCUMENT_ID, true, //
                        ConllUWriter.PARAM_OVERWRITE, true));

        String actual = readFileToString(new File(tempDir, "in.conllu.conllu"), "UTF-8");

        assertThat(newdocLines(actual)).containsExactly("# newdoc", "# newdoc");
    }

    /**
     * Sentences may directly follow each other without any whitespace in between. Writing such a
     * document and reading it back in must preserve the text.
     */
    @Test
    public void thatSentencesWithoutSeparatorRoundTrip(@TempDir File tempDir) throws Exception
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

        CollectionReaderDescription reader = createReaderDescription(ConllUReader.class, //
                ConllUReader.PARAM_LANGUAGE, "en", //
                ConllUReader.PARAM_SOURCE_LOCATION, new File(tempDir, "output.conllu"));

        JCas actual = new JCasIterable(reader).iterator().next();

        assertThat(actual.getDocumentText()).isEqualTo("Hello.World.");
    }

    private static List<String> newdocLines(String aConllU)
    {
        return Stream.of(aConllU.split("\n")) //
                .filter(line -> line.startsWith("# newdoc")) //
                .collect(toList());
    }
}
