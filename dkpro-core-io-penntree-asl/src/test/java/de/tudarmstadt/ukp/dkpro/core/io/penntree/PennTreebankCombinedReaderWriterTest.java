/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.canParameterBeSet;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.setParameter;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.EOLUtils;

public class PennTreebankCombinedReaderWriterTest
{
    @Test
    public void testTreeWithRoot()
        throws Exception
    {
        testRoundTrip("tree_with_ROOT.mrg", 
                PennTreebankCombinedWriter.PARAM_NO_ROOT_LABEL, false);
    }

    @Test
    public void testTreeWithTraceRemoved()
        throws Exception
    {
        testOneWay("tree_with_trace_filtered.mrg", "tree_with_trace.mrg",
                PennTreebankCombinedWriter.PARAM_EMPTY_ROOT_LABEL, true);
    }

    @Test
    public void testTreeWithTrace()
        throws Exception
    {
        testRoundTrip("tree_with_trace.mrg",
                PennTreebankCombinedReader.PARAM_REMOVE_TRACES, false,
                PennTreebankCombinedReader.PARAM_WRITE_TRACES_TO_TEXT, true,
                PennTreebankCombinedWriter.PARAM_EMPTY_ROOT_LABEL, true);
    }

    @Test
    public void testTreeWithParentheses()
        throws Exception
    {
        testRoundTrip("tree_with_parentheses.mrg",
                PennTreebankCombinedWriter.PARAM_EMPTY_ROOT_LABEL, true);
    }

    @Test
    public void testTreeWithDirectSpeech()
        throws Exception
    {
        testRoundTrip("tree_with_direct_speech.mrg",
                PennTreebankCombinedWriter.PARAM_EMPTY_ROOT_LABEL, true);
    }

    @Ignore("This file contains trees in different variations of formatting that are not "
            + "performed verbatim. Other tests check for individual tree structures.")
    @Test
    public void testAll()
        throws Exception
    {
        testRoundTrip("stanford-english-trees.mrg",
                PennTreebankCombinedWriter.PARAM_EMPTY_ROOT_LABEL, true);
    }

    public void testOneWay(String aExpectedFile, String aFile, Object... aExtraParams)
        throws Exception
    {
        File reference = new File("src/test/resources/stanfordPennTrees/" + aExpectedFile);
        File input = new File("src/test/resources/stanfordPennTrees/" + aFile);
        File output = testContext.getTestOutputFolder();

        List<Object> extraReaderParams = new ArrayList<>();
        extraReaderParams.add(PennTreebankCombinedReader.PARAM_SOURCE_LOCATION);
        extraReaderParams.add(input);
        extraReaderParams.addAll(asList(aExtraParams));

        CollectionReaderDescription reader = createReaderDescription(
                PennTreebankCombinedReader.class, extraReaderParams.toArray());

        List<Object> extraWriterParams = new ArrayList<>();
        extraWriterParams.add(PennTreebankCombinedWriter.PARAM_TARGET_LOCATION);
        extraWriterParams.add(output);
        extraWriterParams.add(PennTreebankCombinedWriter.PARAM_STRIP_EXTENSION);
        extraWriterParams.add(true);
        extraWriterParams.addAll(asList(aExtraParams));

        AnalysisEngineDescription writer = createEngineDescription(PennTreebankCombinedWriter.class,
                extraWriterParams.toArray());

        if (canParameterBeSet(writer, "overwrite")) {
            setParameter(writer, "overwrite", true);
        }

        runPipeline(reader, writer);

        String expected = FileUtils.readFileToString(reference, "UTF-8");
        String actual = FileUtils.readFileToString(
                new File(output, FilenameUtils.getBaseName(input.toString()) + ".mrg"), "UTF-8");
        expected = EOLUtils.normalizeLineEndings(expected);
        actual = EOLUtils.normalizeLineEndings(actual);
        assertEquals(expected.trim(), actual.trim());
    }

    

    public void testRoundTrip(String aFile, Object... aExtraParams)
        throws Exception
    {
        testOneWay(aFile, aFile, aExtraParams);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
