/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.testing.dumper;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.uima.fit.factory.JCasFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

class CasToComparableTextWriterTest
{
    @Test
    void writesCsvOutput(@TempDir File tempDir) throws Exception
    {
        var outputFile = new File(tempDir, "out.csv");

        var jcas = JCasFactory.createJCas();
        jcas.setDocumentText("Hello world.");

        var writer = createEngine( //
                createEngineDescription(CasToComparableTextWriter.class, //
                        CasToComparableTextWriter.PARAM_TARGET_LOCATION, outputFile, //
                        CasToComparableTextWriter.PARAM_FORMAT, "CSV"));
        writer.process(jcas);
        writer.collectionProcessComplete();

        var content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
        assertThat(content).contains("======== CAS 0 ========");
        assertThat(content).contains("uima.tcas.DocumentAnnotation");
        assertThat(content).contains("Hello world.");
    }

    @Test
    void writesHtmlOutput(@TempDir File tempDir) throws Exception
    {
        var outputFile = new File(tempDir, "out.html");

        var jcas = JCasFactory.createJCas();
        jcas.setDocumentText("Hello world.");

        var writer = createEngine( //
                createEngineDescription(CasToComparableTextWriter.class, //
                        CasToComparableTextWriter.PARAM_TARGET_LOCATION, outputFile, //
                        CasToComparableTextWriter.PARAM_FORMAT, "HTML"));
        writer.process(jcas);
        writer.collectionProcessComplete();

        var content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
        assertThat(content).contains("<html>");
        assertThat(content).contains("uima.tcas.DocumentAnnotation");
    }

    @Test
    void concatenatesMultipleCases(@TempDir File tempDir) throws Exception
    {
        var outputFile = new File(tempDir, "multi.csv");

        var writer = createEngine( //
                createEngineDescription(CasToComparableTextWriter.class, //
                        CasToComparableTextWriter.PARAM_TARGET_LOCATION, outputFile));

        var jcas1 = JCasFactory.createJCas();
        jcas1.setDocumentText("First.");
        writer.process(jcas1);

        var jcas2 = JCasFactory.createJCas();
        jcas2.setDocumentText("Second.");
        writer.process(jcas2);

        writer.collectionProcessComplete();

        var content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
        assertThat(content).contains("======== CAS 0 ========");
        assertThat(content).contains("======== CAS 1 ========");
        assertThat(content).contains("First.");
        assertThat(content).contains("Second.");
    }

    @Test
    void singleViewOmitsViewHeader(@TempDir File tempDir) throws Exception
    {
        var outputFile = new File(tempDir, "single-view.csv");

        var jcas = JCasFactory.createJCas();
        jcas.setDocumentText("Single view.");

        var writer = createEngine( //
                createEngineDescription(CasToComparableTextWriter.class, //
                        CasToComparableTextWriter.PARAM_TARGET_LOCATION, outputFile));
        writer.process(jcas);
        writer.collectionProcessComplete();

        var content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
        assertThat(content).contains("======== CAS 0 ========");
        assertThat(content).doesNotContain("-------- View ");
    }

    @Test
    void multipleViewsIncludeViewHeaders(@TempDir File tempDir) throws Exception
    {
        var outputFile = new File(tempDir, "multi-view.csv");

        var jcas = JCasFactory.createJCas();
        jcas.setDocumentText("Default view text.");
        var secondView = jcas.createView("SecondView");
        secondView.setDocumentText("Second view text.");

        var writer = createEngine( //
                createEngineDescription(CasToComparableTextWriter.class, //
                        CasToComparableTextWriter.PARAM_TARGET_LOCATION, outputFile));
        writer.process(jcas);
        writer.collectionProcessComplete();

        var content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
        assertThat(content).contains("-------- View _InitialView --------");
        assertThat(content).contains("-------- View SecondView --------");
        assertThat(content).contains("Default view text.");
        assertThat(content).contains("Second view text.");
    }

    @Test
    void defaultsExcludeUriFeatures(@TempDir File tempDir) throws Exception
    {
        var outputFile = new File(tempDir, "default-excludes.csv");

        var jcas = JCasFactory.createJCas();
        jcas.setDocumentText("Doc.");
        var dmd = DocumentMetaData.create(jcas);
        dmd.setDocumentUri("file:/tmp/doc.txt");
        dmd.setCollectionId("file:/tmp/");
        dmd.setDocumentBaseUri("file:/tmp/");
        dmd.setDocumentId("doc.txt");

        var writer = createEngine( //
                createEngineDescription(CasToComparableTextWriter.class, //
                        CasToComparableTextWriter.PARAM_TARGET_LOCATION, outputFile));
        writer.process(jcas);
        writer.collectionProcessComplete();

        var content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
        // Path-leaking features are filtered out by default
        assertThat(content).doesNotContain("documentUri");
        assertThat(content).doesNotContain("documentBaseUri");
        assertThat(content).doesNotContain("collectionId");
        assertThat(content).doesNotContain("file:/tmp");
        // Non-URI metadata still appears
        assertThat(content).contains("documentId");
        assertThat(content).contains("doc.txt");
    }

    @Test
    void viewPatternsCanIncludeOnlySelectedView(@TempDir File tempDir) throws Exception
    {
        var outputFile = new File(tempDir, "include-view.csv");

        var jcas = JCasFactory.createJCas();
        jcas.setDocumentText("Default view text.");
        var secondView = jcas.createView("SecondView");
        secondView.setDocumentText("Second view text.");

        var writer = createEngine( //
                createEngineDescription(CasToComparableTextWriter.class, //
                        CasToComparableTextWriter.PARAM_TARGET_LOCATION, outputFile, //
                        CasToComparableTextWriter.PARAM_VIEW_PATTERNS,
                        new String[] { CasToComparableTextWriter.INCLUDE_PREFIX + "SecondView" }));
        writer.process(jcas);
        writer.collectionProcessComplete();

        var content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
        // Only SecondView selected -> no view separator (single view after filtering)
        assertThat(content).doesNotContain("-------- View ");
        assertThat(content).contains("Second view text.");
        assertThat(content).doesNotContain("Default view text.");
    }

    @Test
    void viewPatternsCanExcludeView(@TempDir File tempDir) throws Exception
    {
        var outputFile = new File(tempDir, "exclude-view.csv");

        var jcas = JCasFactory.createJCas();
        jcas.setDocumentText("Default view text.");
        var secondView = jcas.createView("SecondView");
        secondView.setDocumentText("Second view text.");

        var writer = createEngine( //
                createEngineDescription(CasToComparableTextWriter.class, //
                        CasToComparableTextWriter.PARAM_TARGET_LOCATION, outputFile, //
                        CasToComparableTextWriter.PARAM_VIEW_PATTERNS,
                        new String[] { CasToComparableTextWriter.INCLUDE_PREFIX + ".*",
                                CasToComparableTextWriter.EXCLUDE_PREFIX + "SecondView" }));
        writer.process(jcas);
        writer.collectionProcessComplete();

        var content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
        assertThat(content).contains("Default view text.");
        assertThat(content).doesNotContain("Second view text.");
        // Only one view left after exclusion -> no separator
        assertThat(content).doesNotContain("-------- View ");
    }

    @Test
    void customExclusionsOverrideDefaults(@TempDir File tempDir) throws Exception
    {
        var outputFile = new File(tempDir, "custom-excludes.csv");

        var jcas = JCasFactory.createJCas();
        jcas.setDocumentText("Doc.");
        var dmd = DocumentMetaData.create(jcas);
        dmd.setDocumentUri("file:/tmp/doc.txt");
        dmd.setDocumentId("doc.txt");

        var writer = createEngine( //
                createEngineDescription(CasToComparableTextWriter.class, //
                        CasToComparableTextWriter.PARAM_TARGET_LOCATION, outputFile, //
                        CasToComparableTextWriter.PARAM_EXCLUDE_FEATURE_PATTERNS,
                        new String[] { ".*:documentId" }));
        writer.process(jcas);
        writer.collectionProcessComplete();

        var content = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
        // Custom override replaces defaults: documentId is now excluded
        assertThat(content).doesNotContain("documentId");
        // ... and documentUri is no longer excluded
        assertThat(content).contains("documentUri");
        assertThat(content).contains("file:/tmp/doc.txt");
    }
}
