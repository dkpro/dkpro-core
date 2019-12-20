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

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import org.dkpro.core.io.brat.BratReader;
import org.dkpro.core.io.brat.BratWriter;
import org.dkpro.core.io.conll.Conll2009Reader;
import org.dkpro.core.io.conll.Conll2012Reader;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.EOLUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.json.ReaderBasedJsonParser;

//NOTE: This file contains Asciidoc markers for partial inclusion of this file in the documentation
//Do not remove these tags!
public class BratReaderWriterTest
{
     
    @Test
    public void test__SingleDocument__ProvideTxtFile()
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
        
        File bratOrigDir = new File("src/test/resources/brat/");
        File txtFileRef = new File(bratOrigDir, "document1.txt");
        File tempDir = copyBratFilesToTempLocation(bratOrigDir);
        File txtFile = new File(BratReader.stripProtocol(new File(tempDir, "document1.txt")));
        
        testReadWrite(
                createReader(BratReader.class,
                        BratReader.PARAM_SOURCE_LOCATION, txtFile.toString(),
                        BratReader.PARAM_MAPPING, mapping), 
                createEngine(BratWriter.class,
                        BratReader.PARAM_SOURCE_LOCATION, txtFile.toString(),
                        BratWriter.PARAM_RELATION_TYPES, asList(
                                "de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value")),
                txtFileRef, txtFile);    
    }
    
    @Test
    public void test__BratDirectory()
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
                "    },",            
                "    {",
                "      'from': 'Token',",
                "      'to': 'de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token'",
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
        
        File bratOrigDir = new File("src/test/resources/brat");
        File annFileRef = bratOrigDir;
        File tempDir = copyBratFilesToTempLocation(bratOrigDir);
        File annFile = tempDir;
        testReadWrite(
                createReader(BratReader.class,
                        BratReader.PARAM_SOURCE_LOCATION, tempDir.toString(),
                        BratReader.PARAM_MAPPING, mapping), 
                createEngine(BratWriter.class,
                        BratReader.PARAM_SOURCE_LOCATION, tempDir.toString(),
                        BratWriter.PARAM_RELATION_TYPES, asList(
                                "de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value")),
                annFileRef, annFile);    
    }    
        
    @Test
    public void testConll2009()
        throws Exception
    {
// tag::testOneWay[]
        testOneWay(
                createReaderDescription(Conll2009Reader.class), // the reader
                createEngineDescription(BratWriter.class, // the writer
                        BratWriter.PARAM_WRITE_RELATION_ATTRIBUTES, true),
                "conll/2009/en-ref.ann", // the reference file for the output
                "conll/2009/en-orig.conll"); // the input file for the test
// end::testOneWay[]
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
                "brat/document0b.ann");
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
        testRoundTrip(createReaderDescription(BratReader.class,
                BratReader.PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS,
                asList("Token -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                        "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization",
                        "Location -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location")),
                createEngineDescription(BratWriter.class, BratWriter.PARAM_ENABLE_TYPE_MAPPINGS,
                        true),
                "brat/document0c.ann");
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
        
        JCasCollector.readJCases = new ArrayList<JCas>();
        AnalysisEngine collector = createEngine(JCasCollector.class); 
                
        SimplePipeline.runPipeline(reader, new AnalysisEngine[] {collector, writer});
        
        boolean isSingleFile = ((BratReader)reader).sourceLocationIsSingleFile();
        int expNumRead = 1;
        if (isSingleFile) {
            assertFilesHaveSameContent(expAnnFile, gotAnnFile);
        }  else {
            String pattern = new File(gotAnnFile, "*.ann").toString();
            expNumRead = FileGlob.listFiles(pattern).length;
        }
        assertEquals("Number of documents read was not as expected", 
                expNumRead, JCasCollector.readJCases.size());

    }    
    
    private File copyBratFilesToTempLocation(File bratDir) 
                    throws IOException { 
        
        Path tempDir = null;        
        tempDir = Files.createTempDirectory("dkpro", new FileAttribute[0]);
        FileCopy.copyFolder(bratDir, tempDir.toFile());
        
        // Delete the -ref files from the inputs dir
        String pattern = new File(tempDir.toFile(), "*-ref*").toString();
        FileGlob.deleteFiles(pattern);
        
        return tempDir.toFile();
    }
    
    private void assertFilesHaveSameContent(File expFile, File actualFile) throws IOException {
        String expContent = FileUtils.readFileToString(expFile, "UTF-8");
        String actualContent = FileUtils.readFileToString(actualFile, "UTF-8");
        expContent = EOLUtils.normalizeLineEndings(expContent);
        actualContent = EOLUtils.normalizeLineEndings(actualContent);
        assertEquals(expContent.trim(), actualContent.trim());
    }
}
