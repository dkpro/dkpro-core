/*******************************************************************************
 * Copyright 2014
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.testing;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

public class IOTestRunner
{
    private static final String RESOURCE_COLLECTION_READER_BASE = "de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase";
    private static final String JCAS_FILE_WRITER_IMPL_BASE = "de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase";
    
    public static void testRoundTrip(Class<? extends CollectionReader> aReader,
            Class<? extends AnalysisComponent> aWriter, String aFile, Object... aExtraParams)
        throws Exception
    {
        testOneWay(aReader, aWriter, aFile, aFile, aExtraParams);
    }
    
    public static void testOneWay(Class<? extends CollectionReader> aReader,
            Class<? extends AnalysisComponent> aWriter, String aExpectedFile, String aFile,
            Object... aExtraParams)
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
        
        String outputFolder = aReader.getSimpleName() + "-" + FilenameUtils.getBaseName(aFile);
        if (DkproTestContext.get() != null) {
            outputFolder = DkproTestContext.get().getTestOutputFolderName();
        }
        
        File reference = new File("src/test/resources/" + aExpectedFile);
        File input = new File("src/test/resources/" + aFile);
        File output = new File("target/test-output/" + outputFolder);

        List<Object> extraReaderParams = new ArrayList<>();
        extraReaderParams.add(ComponentParameters.PARAM_SOURCE_LOCATION);
        extraReaderParams.add(input);
        extraReaderParams.addAll(asList(aExtraParams));

        CollectionReaderDescription reader = createReaderDescription(aReader,
                extraReaderParams.toArray());

        List<Object> extraWriterParams = new ArrayList<>();
        extraWriterParams.add(ComponentParameters.PARAM_TARGET_LOCATION);
        extraWriterParams.add(output);
        extraWriterParams.add(ComponentParameters.PARAM_STRIP_EXTENSION);
        extraWriterParams.add(true);
        extraWriterParams.addAll(asList(aExtraParams));

        AnalysisEngineDescription writer = createEngineDescription(aWriter,
                extraWriterParams.toArray());

        runPipeline(reader, writer);

        // We assume that the writer is creating a file with the same extension as is provided as
        // the expected file
        String extension = FilenameUtils.getExtension(aExpectedFile);
        
        String expected = FileUtils.readFileToString(reference, "UTF-8");
        String actual = FileUtils.readFileToString(
                new File(output, FilenameUtils.getBaseName(input.toString()) + '.' + extension),
                "UTF-8");
        assertEquals(expected.trim(), actual.trim());
    }
}
