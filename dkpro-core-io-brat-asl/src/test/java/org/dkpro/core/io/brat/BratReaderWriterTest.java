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
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.resources.FileCopy;
import org.dkpro.core.api.resources.FileGlob;
import org.dkpro.core.io.brat.BratReader;
import org.dkpro.core.io.brat.BratWriter;
import org.dkpro.core.io.conll.Conll2009Reader;
import org.dkpro.core.io.conll.Conll2012Reader;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.EOLUtils;
import org.dkpro.core.testing.ReaderAssert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class BratReaderWriterTest
{
    private static final String customReaderMappings = String.join("\n",
            "{",
            "  'textTypeMapppings': [",
            "    {",
            "      'from': 'Country',",
            "      'to': 'de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location'",
            "    },",
            "    {",
            "      'from': 'MergeOrg',",
            "      'to': 'de.tudarmstadt.ukp.dkpro.core.io.brat.type.MergeOrg'",
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
            "      'type': 'de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation',",
            "      'feature': 'comment'",
            "    },",
            "    {",
            "      'type': 'de.tudarmstadt.ukp.dkpro.core.io.brat.type.MergeOrg',",
            "      'feature': 'comment'",
            "    }",
            "  ]",
            "}");
    
    private static final String[] writerCustomMappings = new String[] {
            "de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location -> Country",
            "de.tudarmstadt.ukp.dkpro.core.io.brat.type.MergeOrg -> MERGE-ORG",
        };

    
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
        .usingWriter(BratWriter.class)
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
    public void test__SingleTxtFileWithoutAnAnnFile__AssumesEmptyAnnFiles() throws Exception
        {
            ReaderAssert
                    .assertThat(BratReader.class)
                    .readingFrom("src/test/resources/text-only/document0a.txt")
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
                    .containsExactlyInAnyOrder("annotation.conf", 
                            "document0a.ann", "document0a.txt",
                            "visual.conf");
        }
    
    @Test
    public void test__SingleAnnFile() throws Exception
    {
        ReaderAssert
                .assertThat(BratReader.class)
                .readingFrom("src/test/resources/brat-basic/document0a.ann", true)
                .usingWriter(BratWriter.class)
                .asFiles()
                .allSatisfy(file -> {
                    if (!file.getName().endsWith(".conf")) {
                        assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                                contentOf(new File("src/test/resources/brat-basic", 
                                        file.getName())));
                    }
                })
                .extracting(File::getName)
                .containsExactlyInAnyOrder("annotation.conf", "document0a.ann", "document0a.txt",
                        "visual.conf");
    }    
    
    @Test
    public void test__SingleDir__ThatDoesContainAnnFiles__AssumesEmptyAnnFiles() throws Exception
    {
        
        ReaderAssert
                .assertThat(BratReader.class)
                .readingFrom("src/test/resources/brat-basic", true)
                .usingWriter(BratWriter.class)
                .asFiles()
                .allSatisfy(file -> {
                    if (!file.getName().endsWith(".conf")) {
                        assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                                contentOf(new File("src/test/resources/brat-basic", 
                                        file.getName())));
                    }
                })
                .extracting(File::getName)
                .containsExactlyInAnyOrder("annotation.conf", "document0a.ann", "document0a.txt", "visual.conf");
    }
    
    
    @Test
    public void test__SingleDir__ThatDoesNotContainsAnnFiles__AssumesEmptyAnnFiles() throws Exception
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
    public void test__SingleAnnFileContainingCustomTypes() throws Exception
    {
        ReaderAssert
                .assertThat(BratReader.class,
                        BratReader.PARAM_MAPPING, customReaderMappings)
                .readingFrom("src/test/resources/brat-custom-types/merger.ann")
                .usingWriter(BratWriter.class,
                        BratWriter.PARAM_TYPE_MAPPINGS, writerCustomMappings,
                        BratWriter.PARAM_CHECK_CONFLICTING_MAPPINGS, false)
                .asFiles()
                .allSatisfy(file -> {
                    if (!file.getName().endsWith(".conf")) {
                        assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                                contentOf(new File("src/test/resources/brat-custom-types/", 
                                        file.getName())));
                    }
                })
                .extracting(File::getName)
                .containsExactlyInAnyOrder("annotation.conf", "merger.txt", "merger.ann", "visual.conf");
    }    
    

    @Test
    public void test__SingleAnnFileWithCustomMapping() throws Exception
    {
        ReaderAssert
                .assertThat(BratReader.class,
                        BratReader.PARAM_MAPPING, customReaderMappings)
                .readingFrom("src/test/resources/brat/document2.ann", true)
                .usingWriter(BratWriter.class,
                        BratWriter.PARAM_TYPE_MAPPINGS, writerCustomMappings,
                        BratWriter.PARAM_CHECK_CONFLICTING_MAPPINGS, false)
                .asFiles()
                .allSatisfy(file -> {
                    if (!file.getName().endsWith(".conf")) {
                        assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                                contentOf(new File("src/test/resources/brat", 
                                        file.getName().replaceAll("\\.ann", ".ann"))));
                    }
                })
                .extracting(File::getName)
                .containsExactlyInAnyOrder("annotation.conf", "document2.ann", "document2.txt",
                        "visual.conf");
    }    
    

    @Test
    public void test__AnnotationOfUnknownType__andAnnotationHasNoAttributes() throws Exception
    {
        ReaderAssert
                .assertThat(BratReader.class)
                .readingFrom("src/test/resources/brat-unknown-labels/hurricane.ann")
                .usingWriter(BratWriter.class)
                .asFiles()
                .allSatisfy(file -> {
                    if (!file.getName().endsWith(".conf")) {
                        assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                                contentOf(new File("src/test/resources/brat-unknown-labels/", 
                                        file.getName())));
                    }
                })
                .extracting(File::getName)
                .containsExactlyInAnyOrder("annotation.conf", "hurricane.txt", "hurricane.ann", "visual.conf");
    }    

    // TODO-AD: At the moment, if an .ann file contains an annotation 
    //   whose brat label that is not mapped, AND the annotation has 
    //   some attributes, then an exception will be raised.
    //
    //   The reason is that there does not seem to be an easy way to 
    //   store the annotation's attributes into a generic BratTag, at 
    //   least not in a way that will allow the BratWriter to write the 
    //   annotation back in the exact way it was read.
    //
    //   Note that if the annotation has no attributes, then no exception
    //   is raised, and the system is able to capture that annotation and 
    //   write it back the way it was read.
    //
    @Test(expected=IllegalStateException.class)
    public void test__AnnotationOfUnknownType__andAnnotationHasSomeAttributes() throws Throwable
    {
        try {
            ReaderAssert
                    .assertThat(BratReader.class)
                    .readingFrom("src/test/resources/brat-unknown-labels/cars.ann")
                    .usingWriter(BratWriter.class)
                    .asFiles()
                    .allSatisfy(file -> {
                        if (!file.getName().endsWith(".conf")) {
                            assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                                    contentOf(new File("src/test/resources/brat-unknown-labels/", 
                                            file.getName())));
                        }
                    })
                    .extracting(File::getName)
                    .containsExactlyInAnyOrder("annotation.conf", "hurricane.txt", "hurricane.ann", "visual.conf");
        } catch (AssertionError e) {
            Throwable cause = e.getCause();
            throw cause;
        }
    }        

    @Test
    public void testConll2009()
        throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2009Reader.class), // the reader
                createEngineDescription(BratWriter.class, // the writer
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false,                        
                        BratWriter.PARAM_WRITE_RELATION_ATTRIBUTES, true),
                "conll/2009/en-ref.ann", // the reference file for the output
                "conll/2009/en-orig.conll"); // the input file for the test
    }
    
    @Test
    public void test__ReaderCanAlwaysRecognizeFullyQualifiedClassName() throws ResourceInitializationException {
        ReaderAssert
        .assertThat(BratReader.class)
        .readingFrom("src/test/resources/brat-no-mappings/merger-no-mapping.ann", true)
        .usingWriter(BratWriter.class)
        .asFiles()
        .allSatisfy(file -> {
            if (!file.getName().endsWith(".conf")) {
                assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                        contentOf(new File("src/test/resources/brat-no-mappings", 
                                file.getName().replaceAll("\\.ann", ".withMappings.ann"))));
            }
        })
        .extracting(File::getName)
        .containsExactlyInAnyOrder("annotation.conf", "merger-no-mapping.ann", "merger-no-mapping.txt",
                "visual.conf");        
    }    
    
    @Test(expected=IllegalStateException.class)
    public void test__ConflictingReaderMappings__RaisesException() throws Throwable {
        
        String customMappings = String.join("\n",
                "{",
                "  'textTypeMapppings': [",
                "    {",
                "      'from': 'Location',",
                "      'to': 'com.acme.Location'",
                "    }",
                "  ],",
                "  'relationTypeMapppings': [",
                "    {",
                "      'from': 'Origin',",
                "      'to': 'de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation'",
                "    }",
                "  ]",
                "}");
        
        try {
            ReaderAssert
            .assertThat(BratReader.class,
                    BratReader.PARAM_MAPPING, customMappings)
            .readingFrom("src/test/resources/brat-basic/document0a.ann")
            .usingWriter(BratWriter.class)
            .asFiles(); 
        } catch (AssertionError e) {
            throw e.getCause();
        }
    }
    
    @Test(expected=ResourceInitializationException.class)
    public void test__ConflictingWriterMappings__RaisesException() throws Throwable {
        String[] mappings = new String[] {
                  "de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location -> City",
                  "de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location -> Country"                  
        };  
        try {
            ReaderAssert
            .assertThat(BratReader.class)
            .readingFrom("src/test/resources/brat-basic/document0a.ann")
            .usingWriter(BratWriter.class,
                    BratWriter.PARAM_TYPE_MAPPINGS, mappings)
            .asFiles();
        } catch (AssertionError e) {
            throw e.getCause();
        }
            
    }

    @Test
    public void testConll2009_2()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(BratReader.class), 
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false,                        
                        BratWriter.PARAM_WRITE_RELATION_ATTRIBUTES, true),
                "conll/2009/en-ref.ann");
    }

    @Test
    public void testConll2012Html()
        throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2012Reader.class,
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false,                        
                        Conll2012Reader.PARAM_USE_HEADER_METADATA, false), 
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false,                        
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
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false,                        
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
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false,                        
                        Conll2012Reader.PARAM_USE_HEADER_METADATA, false), 
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false), 
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
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false), 
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
                createEngineDescription(BratWriter.class), 
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
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false,                        
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
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false,                        
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
                        BratReader.PARAM_CHECK_CONFLICTING_MAPPINGS, false,
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
                        BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false,
                        BratWriter.PARAM_CHECK_CONFLICTING_MAPPINGS, false,
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
            .usingWriter(BratWriter.class)
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
    
    public File getTestBratOutputsDir() throws IOException {
        return DkproTestContext.get().getTestOutputFolder();
    }

    public File getTestBratInputsDir() throws IOException {
        return DkproTestContext.get().getTestInputFolder();
    }
}
