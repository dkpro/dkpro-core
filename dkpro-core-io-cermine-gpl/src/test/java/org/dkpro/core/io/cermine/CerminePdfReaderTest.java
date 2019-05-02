/*
 * Copyright 2007-2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/ .
 */
package org.dkpro.core.io.cermine;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.dumper.CasDumpWriter;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;


public class CerminePdfReaderTest
{
    @Test
    public void test() throws Exception
    {
        File outputFile = new File(testContext.getTestOutputFolder(), "dump-output.txt");

        CollectionReader reader = createReader(CerminePdfReader.class,
                CerminePdfReader.PARAM_SOURCE_LOCATION, "src/test/resources/data",
                CerminePdfReader.PARAM_PATTERNS, "[+]**/*.pdf");

        AnalysisEngine writer = createEngine(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, outputFile);

        SimplePipeline.runPipeline(reader, writer);

        assertTrue(FileUtils.contentEqualsIgnoreEOL(
                new File("src/test/resources/reference/test.dump"),
                outputFile, 
                "UTF-8"));
    }

    @Test
    public void testMetadata() throws Exception
    {
        List<String> expectedTitles = new ArrayList<>();
        expectedTitles.add("Out-of-domain FrameNet Semantic Role Labeling");

        CollectionReaderDescription reader = createReaderDescription(CerminePdfReader.class,
                CerminePdfReader.PARAM_SOURCE_LOCATION, "src/test/resources/data",
                CerminePdfReader.PARAM_PATTERNS, "[+]**/*.pdf");

        List<String> actualTitles = new ArrayList<>();
        for (JCas jcas : SimplePipeline.iteratePipeline(reader)) {
            DocumentMetaData metadata = DocumentMetaData.get(jcas);
            actualTitles.add(metadata.getDocumentTitle());
        }

        for (String expectedTitle : expectedTitles) {
            assertTrue(actualTitles.contains(expectedTitle));
            actualTitles.remove(expectedTitle);
        }
    }
    
    @Test
    public void testNormalizeText() throws Exception
    {
        File outputFile = new File(testContext.getTestOutputFolder(), "dump-output.txt");

        CollectionReader reader = createReader(CerminePdfReader.class,
                CerminePdfReader.PARAM_SOURCE_LOCATION, "src/test/resources/data",
                CerminePdfReader.PARAM_PATTERNS, "[+]**/*.pdf",
                CerminePdfReader.PARAM_NORMALIZE_TEXT, true);

        AnalysisEngine writer = createEngine(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, outputFile);

        SimplePipeline.runPipeline(reader, writer);

        assertTrue(FileUtils.contentEqualsIgnoreEOL(
                new File("src/test/resources/reference/test-normalized.dump"),
                outputFile, 
                "UTF-8"));
    }
    
    @Test
    public void testIgnoreCitations() throws Exception
    {
        File outputFile = new File(testContext.getTestOutputFolder(), "dump-output.txt");

        CollectionReader reader = createReader(CerminePdfReader.class,
                CerminePdfReader.PARAM_SOURCE_LOCATION, "src/test/resources/data",
                CerminePdfReader.PARAM_PATTERNS, "[+]**/*.pdf",
                CerminePdfReader.PARAM_NORMALIZE_TEXT, true, 
                CerminePdfReader.PARAM_IGNORE_CITATIONS, true);

        AnalysisEngine writer = createEngine(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, outputFile);

        SimplePipeline.runPipeline(reader, writer);

        assertTrue(FileUtils.contentEqualsIgnoreEOL(
                new File("src/test/resources/reference/test-normalized-nocitations.dump"),
                outputFile, 
                "UTF-8"));
    }
    
    @Test
    public void testIgnoreReferencesSection() throws Exception
    {
        File outputFile = new File(testContext.getTestOutputFolder(), "dump-output.txt");

        CollectionReader reader = createReader(CerminePdfReader.class,
                CerminePdfReader.PARAM_SOURCE_LOCATION, "src/test/resources/data",
                CerminePdfReader.PARAM_PATTERNS, "[+]**/*.pdf",
                CerminePdfReader.PARAM_NORMALIZE_TEXT, true,
                CerminePdfReader.PARAM_IGNORE_CITATIONS, true,
                CerminePdfReader.PARAM_IGNORE_REFERENCES_SECTION, true);

        AnalysisEngine writer = createEngine(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, outputFile);

        SimplePipeline.runPipeline(reader, writer);

        assertTrue(FileUtils.contentEqualsIgnoreEOL(
                new File("src/test/resources/reference/test-normalized-nocitations-noRefSection.dump"),
                outputFile, 
                "UTF-8"));
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
