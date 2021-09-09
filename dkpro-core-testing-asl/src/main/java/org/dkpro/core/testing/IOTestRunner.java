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
package org.dkpro.core.testing;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.canParameterBeSet;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.getParameterSettings;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.setParameter;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.testing.dumper.CasDumpWriter;
import org.dkpro.core.testing.validation.CasValidator;
import org.dkpro.core.testing.validation.Message;

public class IOTestRunner
{
    private static final String RESOURCE_COLLECTION_READER_BASE = "org.dkpro.core.api.io.ResourceCollectionReaderBase";
    private static final String JCAS_FILE_WRITER_IMPL_BASE = "org.dkpro.core.api.io.JCasFileWriter_ImplBase";
        
    /**
     * @deprecated Use {@link ReaderAssert} instead.
     */
    @Deprecated
    public static void testRoundTrip(Class<? extends CollectionReader> aReader,
            Class<? extends AnalysisComponent> aWriter, String aFile)
        throws Exception
    {
        ReaderAssert.assertThat(aReader)
                .readingFrom("src/test/resources/" + aFile)
                .usingWriter(aWriter,
                        // Need 'overwrite' if we are going to both
                        // read and write to the same file
                        "overwrite", true)
                .outputAsString(FilenameUtils.getName(aFile))
                .satisfies(output -> assertThat(output.trim()).isEqualToNormalizingNewlines(
                        contentOf(new File("src/test/resources/" + aFile), UTF_8).trim()));
    }

    public static void testRoundTrip(Class<? extends CollectionReader> aReader,
            Class<? extends AnalysisComponent> aWriter, String aFile, TestOptions aOptions)
        throws Exception
    {
        testOneWay(createReaderDescription(aReader), createEngineDescription(aWriter), aFile,
                aFile, aOptions);
    }

    public static void testRoundTrip(CollectionReaderDescription aReader,
            AnalysisEngineDescription aWriter, String aFile, TestOptions aOptions)
        throws Exception
    {
        testOneWay(aReader, aWriter, aFile, aFile, aOptions);
    }

    public static void testRoundTrip(CollectionReaderDescription aReader,
            AnalysisEngineDescription aWriter, String aFile)
        throws Exception
    {
//        ReaderAssert.assertThat(aReader)
//                .readingFrom("src/test/resources/" + aFile)
//                .usingWriter(aWriter)
//                .asString()
//                .isEqualToNormalizingNewlines(
//                        contentOf(new File("src/test/resources/" + aFile), UTF_8));
        testOneWay(aReader, aWriter, aFile, aFile);
    }

    public static void testOneWay(Class<? extends CollectionReader> aReader, String aExpectedFile,
            String aFile)
        throws Exception
    {
        testOneWay(createReaderDescription(aReader), aExpectedFile, aFile);
    }

    public static void testOneWay(CollectionReaderDescription aReader, String aExpectedFile,
            String aFile)
        throws Exception
    {
        testOneWay(aReader, aExpectedFile, aFile, null);
    }
    
    /**
     * One-way test reading a file and writing to the same format but comparing against a reference
     * file instead of the original file.
     * 
     * @param aReader
     *            reader to read the data.
     * @param aExpectedFile
     *            expected file.
     * @param aFile
     *            input file.
     * @param aOptions
     *            test options.
     * @throws Exception
     *             if there was an error.
     */
    public static void testOneWay(CollectionReaderDescription aReader, String aExpectedFile,
            String aFile, TestOptions aOptions)
        throws Exception
    {
        String outputFolder = StringUtils.substringAfterLast(aReader.getImplementationName(), ".")
                + "-" + FilenameUtils.getBaseName(aFile);
        if (DkproTestContext.get() != null) {
            outputFolder = DkproTestContext.get().getTestWorkspaceFolderName();
        }
        File output = new File("target/test-output/" + outputFolder + "/dump.txt");

        AnalysisEngineDescription writer = createEngineDescription(
                CasDumpWriter.class, CasDumpWriter.PARAM_TARGET_LOCATION, output,
                CasDumpWriter.PARAM_SORT, true);

        testOneWay2(aReader, writer, aExpectedFile, "dump.txt", aFile, aOptions);
    }
    
    public static void testOneWay(Class<? extends CollectionReader> aReader,
            Class<? extends AnalysisComponent> aWriter, String aExpectedFile, String aFile)
        throws Exception
    {
        testOneWay(aReader, aWriter, aExpectedFile, aFile, null);
    }

    public static void testOneWay(Class<? extends CollectionReader> aReader,
            Class<? extends AnalysisComponent> aWriter, String aExpectedFile, String aFile,
            TestOptions aOptions)
        throws Exception
    {
        Class<?> dkproReaderBase = Class.forName(RESOURCE_COLLECTION_READER_BASE);
        if (!dkproReaderBase.isAssignableFrom(aReader)) {
            throw new IllegalArgumentException("Reader must be a subclass of ["
                    + RESOURCE_COLLECTION_READER_BASE + "]");
        }

        Class<?> dkproWriterBase = Class.forName(JCAS_FILE_WRITER_IMPL_BASE);
        if (!dkproWriterBase.isAssignableFrom(aWriter)) {
            throw new IllegalArgumentException("writer must be a subclass of ["
                    + JCAS_FILE_WRITER_IMPL_BASE + "]");
        }
        
        // We assume that the writer is creating a file with the same extension as is provided as
        // the expected file
        String extension = FilenameUtils.getExtension(aExpectedFile);
        String name = FilenameUtils.getBaseName(aFile);
        Object[] aExtraParams = {};

        testOneWay2(createReaderDescription(aReader, aExtraParams),
                createEngineDescription(aWriter, aExtraParams), aExpectedFile, name + "."
                        + extension, aFile, aOptions);
    }

    public static void testOneWay(CollectionReaderDescription aReader,
            AnalysisEngineDescription aWriter, String aExpectedFile, String aFile)
        throws Exception
    {
        testOneWay(aReader, aWriter, aExpectedFile, aFile, null);
    }

    public static void testOneWay(CollectionReaderDescription aReader,
            AnalysisEngineDescription aWriter, String aExpectedFile, String aFile,
            TestOptions aOptions)
        throws Exception
    {
        Class<?> dkproReaderBase = Class.forName(RESOURCE_COLLECTION_READER_BASE);
        if (!dkproReaderBase.isAssignableFrom(Class.forName(aReader.getImplementationName()))) {
            throw new IllegalArgumentException("Reader must be a subclass of ["
                    + RESOURCE_COLLECTION_READER_BASE + "]");
        }

        Class<?> dkproWriterBase = Class.forName(JCAS_FILE_WRITER_IMPL_BASE);
        if (!dkproWriterBase
                .isAssignableFrom(Class.forName(aWriter.getAnnotatorImplementationName()))) {
            throw new IllegalArgumentException("writer must be a subclass of ["
                    + JCAS_FILE_WRITER_IMPL_BASE + "]");
        }
        
        // We assume that the writer is creating a file with the same extension as is provided as
        // the expected file
        String extension = FilenameUtils.getExtension(aExpectedFile);
        String name = FilenameUtils.getBaseName(aFile);

        testOneWay2(aReader, aWriter, aExpectedFile, name + "." + extension, aFile, aOptions);
    }
    
    @Deprecated
    public static void testOneWay2(Class<? extends CollectionReader> aReader,
            Class<? extends AnalysisComponent> aWriter, String aExpectedFile, String aOutputFile,
            String aFile, Object... aExtraParams)
        throws Exception
    {
        testOneWay2(createReaderDescription(aReader, aExtraParams),
                createEngineDescription(aWriter, aExtraParams),
                aExpectedFile, aOutputFile, aFile, null);
    }
    
    public static void testOneWay2(CollectionReaderDescription aReader,
            AnalysisEngineDescription aWriter, String aExpectedFile, String aOutputFile,
            String aInputFile, TestOptions aOptions)
        throws Exception
    {
        String outputFolder = StringUtils.substringAfterLast(aReader.getImplementationName(), ".")
                + "-" + FilenameUtils.getBaseName(aInputFile);
        if (DkproTestContext.get() != null) {
            outputFolder = DkproTestContext.get().getTestWorkspaceFolderName();
        }
        
        File reference = new File("src/test/resources/" + aExpectedFile);
        File input = new File("src/test/resources/" + aInputFile);
        File output = DkproTestContext.get().getTestWorkspace(false);

        setParameter(aReader, ComponentParameters.PARAM_SOURCE_LOCATION, input);

        if (canParameterBeSet(aWriter, ComponentParameters.PARAM_STRIP_EXTENSION)) {
            setParameter(aWriter, ComponentParameters.PARAM_STRIP_EXTENSION, true);
        }

//        if (canParameterBeSet(aWriter, "overwrite")) {
//            setParameter(aWriter, "overwrite", true);
//        }

        if (!getParameterSettings(aWriter).containsKey(ComponentParameters.PARAM_TARGET_LOCATION)) {
            setParameter(aWriter, ComponentParameters.PARAM_TARGET_LOCATION, output);
        }

        AnalysisEngineDescription metadataStripper = createEngineDescription(
                DocumentMetaDataStripper.class);

        AnalysisEngineDescription validator = createEngineDescription(
                Validator.class);

        Validator.options = aOptions != null ? aOptions : new TestOptions();
        
        runPipeline(aReader, validator, metadataStripper, aWriter);

        AssertAnnotations.assertValid(Validator.messages);
        
        if (aOptions == null || aOptions.resultAssertor == null) {
            String expected = FileUtils.readFileToString(reference, "UTF-8");
            String actual = FileUtils.readFileToString(new File(output, aOutputFile), "UTF-8");
            expected = EOLUtils.normalizeLineEndings(expected);
            actual = EOLUtils.normalizeLineEndings(actual);
            assertEquals(expected.trim(), actual.trim());
        }
        else {
            aOptions.resultAssertor.accept(reference, new File(output, aOutputFile));
        }
    }
    
    public static class Validator
        extends JCasAnnotator_ImplBase
    {
        public static List<Message> messages;
        
        public static TestOptions options;
        
        @Override
        public void initialize(UimaContext aContext)
            throws ResourceInitializationException
        {
            super.initialize(aContext);
            
            messages = new ArrayList<>();
        }
        
        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            CasValidator validator = CasValidator.createWithAllChecks();
            options.skippedChecks.forEach(check -> validator.removeCheck(check));
            messages = validator.analyze(aJCas);
        }
    }
}
