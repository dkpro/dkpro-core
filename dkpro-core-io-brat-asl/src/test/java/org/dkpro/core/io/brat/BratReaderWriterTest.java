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
package org.dkpro.core.io.brat;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.resources.FileCopy;
import org.dkpro.core.api.resources.FileGlob;
import org.dkpro.core.io.brat.BratReader;
import org.dkpro.core.io.brat.BratWriter;
import org.dkpro.core.io.conll.Conll2009Reader;
import org.dkpro.core.io.conll.Conll2012Reader;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.EOLUtils;
import org.dkpro.core.testing.ReaderAssert;
import org.dkpro.core.testing.assertions.AssertFile;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class BratReaderWriterTest
{
    @Before
    public void setUp() throws IOException {
        DkproTestContext.get().initializeTestWorkspace();
    }
    
    @Test
    public void test__SingleTxtFile()
        throws Exception
    {
        ReaderAssert
        .assertThat(BratReader.class)
        .readingFrom("src/test/resources/brat/document0a.txt")
        .usingWriter(BratWriter.class,
                BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, true)
        .asFiles()
        .allSatisfy(file -> {
            if (!file.getName().endsWith(".conf")) {
                assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                        contentOf(new File("src/test/resources/brat", 
                                file.getName())));
            }
        })
        .extracting(File::getName)
        .containsExactlyInAnyOrder("annotation.conf", "document0a.ann", "document0a.txt",
                "visual.conf");
    }

    @Test
    public void test__SingleTxtFileWithoutAnAnnFile__AssumesEmptyAnnFiles() throws Exception {
        boolean deleteAnnFiles = true;
        File tempInputsDir = copyBratFilesToTestInputsDir(new File("src/test/resources/brat/"),
                deleteAnnFiles);
        File tempInputTxtFile = new File(tempInputsDir, "document0a.txt");                
        
        Map<String,Object> readerParams = new HashMap<String,Object>();
        {
            readerParams.put(BratReader.PARAM_SOURCE_LOCATION, tempInputTxtFile);
        }
        Map<String,Object> writerParams = new HashMap<String,Object>();
        {
            writerParams.put(BratWriter.PARAM_TARGET_LOCATION, getTestBratOutputsDir());
        };
        
        testOneWaySimple(readerParams, writerParams);
    }    
    
    @Test
    public void test__SingleAnnFile() throws Exception
    {
        ReaderAssert
                .assertThat(BratReader.class)
                .readingFrom("src/test/resources/text-only/document0a.ann")
                .usingWriter(BratWriter.class)
                .asFiles()
                .allSatisfy(file -> {
                    // The ".ann" files have been freshly generated and are empty
                    if (file.getName().endsWith(".ann")) {
                        assertThat(contentOf(file)).isEmpty();
                    }
                    // The ".text" files should match the originals
                    if (file.getName().endsWith(".txt")) {
                        assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                                contentOf(new File("src/test/resources/text-only", 
                                        file.getName())));
                    }
                })
                .extracting(File::getName)
                .containsExactlyInAnyOrder("annotation.conf", "document0a.ann", "document0a.txt",
                        "visual.conf");
    }    
        
    @Test
    public void test__SingleDirWithoutAnnFiles__AssumesEmptyAnnFiles() throws Exception
    {
        ReaderAssert
                .assertThat(BratReader.class)
                .readingFrom("src/test/resources/text-only")
                .usingWriter(BratWriter.class)
                .asFiles()
                .allSatisfy(file -> {
                    // The ".ann" files have been freshly generated and are empty
                    if (file.getName().endsWith(".ann")) {
                        assertThat(contentOf(file)).isEmpty();
                    }
                    // The ".text" files should match the originals
                    if (file.getName().endsWith(".txt")) {
                        assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                                contentOf(new File("src/test/resources/text-only", 
                                        file.getName())));
                    }
                })
                .extracting(File::getName)
                .containsExactlyInAnyOrder("annotation.conf", "document0a.ann", "document0a.txt",
                        "document0b.ann", "document0b.txt", "document0c.ann", "document0c.txt",
                        "document0d.ann", "document0d.txt", "visual.conf");
    }

    
    @Test
    public void testConll2009()
        throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2009Reader.class), // the reader
                createEngineDescription(BratWriter.class, // the writer
                        BratWriter.PARAM_WRITE_RELATION_ATTRIBUTES, true),
                "conll/2009/en-ref.ann", // the reference file for the output
                "conll/2009/en-orig.conll"); // the input file for the test
    }

    @Test
    public void testConll2009_2()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(BratReader.class), 
                createEngineDescription(BratWriter.class, 
                        BratWriter.PARAM_WRITE_RELATION_ATTRIBUTES, true),
                "conll/2009/en-ref.ann");
    }

    @Test
    public void testConll2012Html()
        throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2012Reader.class,
                        Conll2012Reader.PARAM_USE_HEADER_METADATA, false), 
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_FILENAME_EXTENSION, ".html"), 
                "conll/2012/en-ref.html",
                "conll/2012/en-orig.conll");
    }

    @Test
    public void testConll2012Json()
        throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2012Reader.class,
                        Conll2012Reader.PARAM_USE_HEADER_METADATA, false), 
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_FILENAME_EXTENSION, ".json"), 
                "conll/2012/en-ref.json",
                "conll/2012/en-orig.conll");
    }

    @Test
    public void testConll2012()
        throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2012Reader.class,
                        Conll2012Reader.PARAM_USE_HEADER_METADATA, false), 
                createEngineDescription(BratWriter.class), 
                "conll/2012/en-ref.ann",
                "conll/2012/en-orig.conll");
    }

    @Ignore("Test largely ok but due to same spans for constituents not stable, thus ignoring")
    @Test
    public void testConll2012_2()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(BratReader.class), 
                createEngineDescription(BratWriter.class), 
                "conll/2012/en-ref.ann");
    }

    @Test
    public void testConll2012_3()
        throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2012Reader.class,
                        Conll2012Reader.PARAM_READ_LEMMA, false,
                        Conll2012Reader.PARAM_READ_NAMED_ENTITY, false,
                        Conll2012Reader.PARAM_READ_SEMANTIC_PREDICATE, false,
                        Conll2012Reader.PARAM_READ_COREFERENCE, false, 
                        Conll2012Reader.PARAM_USE_HEADER_METADATA, false), 
                createEngineDescription(BratWriter.class), 
                "conll/2012/en-ref-min.ann",
                "conll/2012/en-orig.conll");
    }

    @Test
    public void testWithShortNames()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(BratReader.class,
                        BratReader.PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS, asList(
                                "Token -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                                "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization",
                                "Location -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location")),
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, true), 
                "brat/document0a.ann");
    }

    @Test
    public void testWithLongNames()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(BratReader.class), 
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false), 
                "with-long-names/document0a.ann");
    }    

    @Test
    public void test1legacy()
        throws Exception
    {
        testOneWay(
                createReaderDescription(BratReader.class,
                        BratReader.PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS, asList(
                                "Country -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location",
                                "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization",
                                "MERGE-ORG -> de.tudarmstadt.ukp.dkpro.core.io.brat.type.MergeOrg"),
                        BratReader.PARAM_RELATION_TYPE_MAPPINGS, asList(
                                "Origin -> de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation"),
                        BratReader.PARAM_RELATION_TYPES, asList(
                                "de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value"),
                        BratReader.PARAM_NOTE_MAPPINGS, asList(
                                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization:value",
                                "de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:comment",
                                "de.tudarmstadt.ukp.dkpro.core.io.brat.type.MergeOrg:comment")), 
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_RELATION_TYPES, asList(
                                "de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value")),
                "brat/document1-ref.ann", 
                "brat/document1.ann");
    }
    
    @Test
    public void test1mapping()
        throws Exception
    {
        String mapping = String.join("\n",
                "{",
                "  'textTypeMapppings': [",
                "    {",
                "      'from': 'Country',",
                "      'to': 'de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location'",
                "    },",
                "    {",
                "      'from': 'Organization',",
                "      'to': 'de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization'",
                "    },",
                "    {",
                "      'from': 'MERGE-ORG',",
                "      'to': 'de.tudarmstadt.ukp.dkpro.core.io.brat.type.MergeOrg'",
                "    }",
                "  ],",
                "  'relationTypeMapppings': [",
                "    {",
                "      'from': 'Origin',",
                "      'to': 'de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation'",
                "    }",
                "  ],",
                "  'spans': [",
                "    {",
                "      'type': 'de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location',",
                "      'defaultFeatureValues': {",
                "        'value': 'LOC'",
                "      }",
                "    }",
                "  ],",
                "  'relations': [",
                "    {",
                "      'type': 'de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation',",
                "      'arg1': 'source',",
                "      'arg2': 'target',",
                "      'flags2': 'A',",
                "      'subCatFeature': 'value'",
                "    }",
                "  ],",
                "  'comments': [",
                "    {",
                "      'type': 'de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization',",
                "      'feature': 'value'",
                "    },",
                "    {",
                "      'type': 'de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation',",
                "      'feature': 'comment'",
                "    },",
                "    {",
                "      'type': 'de.tudarmstadt.ukp.dkpro.core.io.brat.type.MergeOrg',",
                "      'feature': 'comment'",
                "    }",
                "  ]",
                "}");
        
        testOneWay(
                createReaderDescription(BratReader.class,
                        BratReader.PARAM_MAPPING, mapping), 
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_RELATION_TYPES, asList(
                                "de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value")),
                "brat/document1-ref-mapping.ann", 
                "brat/document1.ann");    
    }

    @Test
    public void testTextAnnotationWithSubcategorization()
        throws Exception
    {
        testOneWay(
                createReaderDescription(BratReader.class,
                        BratReader.PARAM_TEXT_ANNOTATION_TYPES, 
                                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity:value",
                        BratReader.PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS, asList(
                                "Country -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                                "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                                "MERGE-ORG -> de.tudarmstadt.ukp.dkpro.core.io.brat.type.MergeOrg"),
                        BratReader.PARAM_RELATION_TYPE_MAPPINGS, asList(
                                "Origin -> de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation"),
                        BratReader.PARAM_RELATION_TYPES, asList(
                                "de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value")),
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_RELATION_TYPES, asList(
                                "de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value")),
                "brat/document1-ref-sub.ann", 
                "brat/document1.ann");
    }
    
    @Test
    public void testBratWithDiscontinuousFragmentNear() 
        throws Exception
    {
        ReaderAssert.assertThat(BratReader.class,
                BratReader.PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS,
                asList("Token -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                        "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization",
                        "Location -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location"))
            .readingFrom("src/test/resources/brat/document0c.ann")
            .usingWriter(BratWriter.class, 
                    BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, true)
            .outputAsString("document0c.ann")
            .isEqualToNormalizingNewlines(
                    contentOf(new File("src/test/resources/brat/document0c.ann"), UTF_8));        
    }
    
    @Test
    public void testBratWithDiscontinuousFragmentFar() 
        throws Exception
    {
        testOneWay(createReaderDescription(BratReader.class,
                BratReader.PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS,
                asList("Token -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                        "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization",
                        "Location -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location")),
                createEngineDescription(BratWriter.class, BratWriter.PARAM_ENABLE_TYPE_MAPPINGS,
                        true),
                "brat/document0d-ref.ann",
                "brat/document0d.ann");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
    
    ////////////////////////////////////////////////////////////
    // Alain Desilets
    //
    //  To test my improvements, I could not use testOneWay
    //  and testRoundtrip.
    //
    //  The reason is that these methods do WAY too much and in
    //  particular, they "patch" the reader description to avoid
    //  problems caused by the very situations I am trying to 
    //  deal with. In other words, if I use testOneWay to test 
    //  a situation like "forgetting to add *.ann at the end of
    //  a directory", the test will succeed because testOneWay() 
    //  adds *.ann if not present.
    //
    //  So I created a simpler runner for tests called 
    //  testReadWrite().
    ////////////////////////////////////////////////////////////
    
    public static class JCasCollector extends JCasAnnotator_ImplBase {

        public static List<JCas> readJCases = new ArrayList<JCas>();
        
        @Override
        public void process(JCas aJCas) throws AnalysisEngineProcessException {
            readJCases.add(aJCas);
        }
    }
    
    private void testReadWrite(CollectionReader reader,
            AnalysisEngine writer, File expAnnFile, File gotAnnFile) 
                    throws UIMAException, IOException {
        testReadWrite(reader, writer, expAnnFile, gotAnnFile, null);
    }
    
    
    private void testReadWrite(CollectionReader reader,
            AnalysisEngine writer, File expAnnFile, File gotAnnFile,
            Boolean expectEmptyAnnFiles) 
                    throws UIMAException, IOException {
        
        if (expectEmptyAnnFiles == null) {
            expectEmptyAnnFiles = false;
        }
        
        JCasCollector.readJCases = new ArrayList<JCas>();
        AnalysisEngine collector = createEngine(JCasCollector.class); 
                
        SimplePipeline.runPipeline(reader, new AnalysisEngine[] {collector, writer});
        
        boolean isSingleFile = ((BratReader)reader).sourceLocationIsSingleFile();
        int expNumRead = 1;
        if (isSingleFile) {
        }  else {
            String pattern = new File(gotAnnFile, "*.ann").toString();
            expNumRead = FileGlob.listFiles(pattern).length;
        }
        assertEquals("Number of documents read was not as expected", 
                expNumRead, JCasCollector.readJCases.size());
        assertFilesHaveSameContent(expAnnFile, gotAnnFile, expectEmptyAnnFiles);
    }    
    
    private File copyBratFilesToTestInputsDir(File bratDir) 
            throws IOException { 
        return copyBratFilesToTestInputsDir(bratDir, null);
    }

    private File copyBratFilesToTestInputsDir(File bratDir, Boolean deleteAnnFiles)
        throws IOException
    {
//        File testWorkspace = testContext.getTestWorkspace();

        if (deleteAnnFiles == null) {
            deleteAnnFiles = false;
        }

//        File testInputsDir = getTestBratInputsDir();
        File testInputsDir = DkproTestContext.get().getTestInputFolder();
        FileCopy.copyFolder(bratDir, testInputsDir);

        // Delete the -ref files from the inputs dir
        String pattern = new File(testInputsDir, "*-ref*").toString();
        FileGlob.deleteFiles(pattern);

        if (deleteAnnFiles) {
            pattern = new File(testInputsDir, "*.ann").toString();
            FileGlob.deleteFiles(pattern);
        }

        return testInputsDir;
    }
    
    private void assertFilesHaveSameContent(File expFileOrDir, File actualFileOrDir,
            Boolean expectEmptyAnnFiles)
        throws IOException
    {
        if (expectEmptyAnnFiles == null) {
            expectEmptyAnnFiles = false;
        }
        
        if (!actualFileOrDir.isDirectory()) {
            String expContent = "";
            if (! expectEmptyAnnFiles || expFileOrDir.toString().endsWith(".txt")) {
                expContent = FileUtils.readFileToString(expFileOrDir, "UTF-8");
            }
            String actualContent = FileUtils.readFileToString(actualFileOrDir, "UTF-8");
            expContent = EOLUtils.normalizeLineEndings(expContent);
            actualContent = EOLUtils.normalizeLineEndings(actualContent);
            assertEquals(expContent.trim(), actualContent.trim());            
        } else {
            String pattern = new File(actualFileOrDir, "*.*").toString();
            for (File anActualFile: FileGlob.listFiles(pattern)) {
                File anExpFile = new File(expFileOrDir, anActualFile.getName());
                assertFilesHaveSameContent(anExpFile, anActualFile, expectEmptyAnnFiles);
            }
        }
    }
    
    private void testOneWaySimple(Map<String,Object> readerParams, Map<String,Object> writerParams) 
                 throws Exception {
        
        Object[] readerParamsArray = paramsMap2Arr(readerParams);
        Object[] writerParamsArray = paramsMap2Arr(writerParams);
        
        CollectionReader reader = createReader(BratReader.class, readerParamsArray);
        AnalysisEngine writer = createEngine(BratWriter.class, writerParamsArray);

        SimplePipeline.runPipeline(reader, new AnalysisEngine[] {writer});
        
        boolean isSingleFile = ((BratReader)reader).sourceLocationIsSingleFile();
        if (isSingleFile) {
            assertSingleBratFileOK(readerParams, writerParams);
        }  else {
//            isSingleFile = FileGlob.listFiles(pattern).length;
        }
        
    }

    private void assertSingleBratFileOK(Map<String, Object> readerParams,
            Map<String, Object> writerParams)
        throws Exception
    {
        File sourceLocation = (File) readerParams.get(BratReader.PARAM_SOURCE_LOCATION);
        File targetLocation = (File) writerParams.get(BratWriter.PARAM_TARGET_LOCATION);
        
        File sourceTxt = new File(sourceLocation.toString().replaceAll("\\.ann$", ".txt"));
        File sourceAnn = new File(sourceLocation.toString().replaceAll("\\.txt$", ".ann"));
        
        String sourceFileName = sourceTxt.getName().replaceAll("\\.txt$", "");
        File targetTxt = new File(targetLocation, sourceFileName + ".txt");
        File targetAnn = new File(targetLocation, sourceFileName + ".ann");
        
        AssertFile.assertFilesHaveSameContent("Outputed .txt file not same as input one", 
                sourceTxt, targetTxt);
        AssertFile.assertFilesHaveSameContent("Outputed .ann file not same as input one", 
                sourceAnn, targetAnn);
        
    }

    private Object[] paramsMap2Arr(Map<String, Object> paramsMap)
    {
        Object[] paramsArr = new Object[2 * paramsMap.keySet().size()];
        int pos = 0;
        for (String paramName: paramsMap.keySet()) {
            paramsArr[pos] = paramName;
            paramsArr[pos + 1] = paramsMap.get(paramName);
            pos += 2;
        }
        
        return paramsArr;
    }

    private File copyBratFilesToTempLocation(File bratDir) throws IOException
    {
        return copyBratFilesToTempLocation(bratDir, null);
    }

    private File copyBratFilesToTempLocation(File bratDir, Boolean deleteAnnFiles)
        throws IOException
    {
        if (deleteAnnFiles == null) {
            deleteAnnFiles = false;
        }
        
        Path tempDir = null;        
        tempDir = Files.createTempDirectory("dkpro", new FileAttribute[0]);
        FileCopy.copyFolder(bratDir, tempDir.toFile());
        
        // Delete the -ref files from the inputs dir
        String pattern = new File(tempDir.toFile(), "*-ref*").toString();
        FileGlob.deleteFiles(pattern);
        
        if (deleteAnnFiles) {
            pattern = new File(tempDir.toFile(), "*.ann").toString();
            FileGlob.deleteFiles(pattern);
        }
        
        return tempDir.toFile();
    }
   
   public File getTestBratOutputsDir() throws IOException {
       return DkproTestContext.get().getTestOutputFolder();
   }

   public File getTestBratInputsDir() throws IOException {
       return DkproTestContext.get().getTestInputFolder();
   }
}
