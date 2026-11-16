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
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Data-driven round-trip tests for CoNLL-U. Each folder in {@code src/test/resources/conllu-suite}
 * is one test case and contains:
 * <ul>
 * <li>{@code data.conllu} - the input which is read by the {@link ConllUReader}</li>
 * <li>{@code reference.conllu} (optional) - the expected output of the {@link ConllUWriter}. If it
 * is absent, the output is expected to be identical to the input.</li>
 * <li>{@code reader.properties} (optional) - additional reader parameters</li>
 * <li>{@code writer.properties} (optional) - additional writer parameters</li>
 * </ul>
 * Boolean parameter values ({@code true}/{@code false}) are passed as booleans, all others as
 * strings.
 */
public class ConllUReaderWriterSuiteTest
{
    private static final String DATA_CONLLU = "data.conllu";
    private static final String REFERENCE_CONLLU = "reference.conllu";
    private static final String READER_PROPERTIES = "reader.properties";
    private static final String WRITER_PROPERTIES = "writer.properties";

    public static Iterable<File> testSuiteFiles()
    {
        File[] folders = new File("src/test/resources/conllu-suite/") //
                .listFiles(File::isDirectory);
        if (folders == null) {
            throw new IllegalStateException("Test suite folder [src/test/resources/conllu-suite/] "
                    + "not found - the test must run in the module directory.");
        }
        Arrays.sort(folders);
        return asList(folders);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("testSuiteFiles")
    public void runTest(File aReferenceFolder) throws Exception
    {
        File targetFolder = new File("target/test-output/conllu-suite/",
                aReferenceFolder.getName());

        List<Object> readerParams = new ArrayList<>(asList( //
                ConllUReader.PARAM_LANGUAGE, "en", //
                ConllUReader.PARAM_SOURCE_LOCATION, aReferenceFolder, //
                ConllUReader.PARAM_PATTERNS, DATA_CONLLU));
        readerParams.addAll(loadParameters(new File(aReferenceFolder, READER_PROPERTIES)));

        List<Object> writerParams = new ArrayList<>(asList( //
                ConllUWriter.PARAM_TARGET_LOCATION, targetFolder, //
                ConllUWriter.PARAM_STRIP_EXTENSION, true, //
                ConllUWriter.PARAM_OVERWRITE, true));
        writerParams.addAll(loadParameters(new File(aReferenceFolder, WRITER_PROPERTIES)));

        SimplePipeline.runPipeline( //
                createReaderDescription(ConllUReader.class, readerParams.toArray()), //
                createEngineDescription(ConllUWriter.class, writerParams.toArray()));

        File reference = new File(aReferenceFolder, REFERENCE_CONLLU);
        if (!reference.exists()) {
            reference = new File(aReferenceFolder, DATA_CONLLU);
        }

        String expected = readFileToString(reference, UTF_8).trim();
        String actual = readFileToString(new File(targetFolder, DATA_CONLLU), UTF_8).trim();

        assertThat(actual).isEqualToNormalizingNewlines(expected);
    }

    private static List<Object> loadParameters(File aFile) throws IOException
    {
        List<Object> params = new ArrayList<>();
        if (!aFile.exists()) {
            return params;
        }

        Properties props = new Properties();
        try (Reader r = Files.newBufferedReader(aFile.toPath(), UTF_8)) {
            props.load(r);
        }

        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key).trim();
            params.add(key);
            if ("true".equals(value) || "false".equals(value)) {
                params.add(Boolean.valueOf(value));
            }
            else {
                params.add(value);
            }
        }
        return params;
    }
}
