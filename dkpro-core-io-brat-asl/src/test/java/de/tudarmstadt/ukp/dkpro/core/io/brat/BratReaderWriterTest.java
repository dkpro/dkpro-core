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
package de.tudarmstadt.ukp.dkpro.core.io.brat;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;
import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2009Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2012Reader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

//NOTE: This file contains Asciidoc markers for partial inclusion of this file in the documentation
//Do not remove these tags!
public class BratReaderWriterTest
{
    @Test
    public void testConll2009() throws Exception
    {
        // tag::testOneWay[]
        testOneWay(createReaderDescription(Conll2009Reader.class), // the reader
                createEngineDescription(BratWriter.class, // the writer
                        BratWriter.PARAM_WRITE_RELATION_ATTRIBUTES, true),
                "conll/2009/en-ref.ann", // the reference file for the output
                "conll/2009/en-orig.conll"); // the input file for the test
        // end::testOneWay[]
    }

    @Test
    public void testConll2009_2() throws Exception
    {
        testRoundTrip(createReaderDescription(BratReader.class),
                createEngineDescription(BratWriter.class,
                        BratWriter.PARAM_WRITE_RELATION_ATTRIBUTES, true),
                "conll/2009/en-ref.ann");
    }

    @Test
    public void testConll2012Html() throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2012Reader.class,
                        Conll2012Reader.PARAM_USE_HEADER_METADATA, false),
                createEngineDescription(BratWriter.class, BratWriter.PARAM_FILENAME_EXTENSION,
                        ".html"),
                "conll/2012/en-ref.html", "conll/2012/en-orig.conll");
    }

    @Test
    public void testConll2012Json() throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2012Reader.class,
                        Conll2012Reader.PARAM_USE_HEADER_METADATA, false),
                createEngineDescription(BratWriter.class, BratWriter.PARAM_FILENAME_EXTENSION,
                        ".json"),
                "conll/2012/en-ref.json", "conll/2012/en-orig.conll");
    }

    @Test
    public void testConll2012() throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2012Reader.class,
                        Conll2012Reader.PARAM_USE_HEADER_METADATA, false),
                createEngineDescription(BratWriter.class), "conll/2012/en-ref.ann",
                "conll/2012/en-orig.conll");
    }

    @Ignore("Test largely ok but due to same spans for constituents not stable, thus ignoring")
    @Test
    public void testConll2012_2() throws Exception
    {
        testRoundTrip(createReaderDescription(BratReader.class),
                createEngineDescription(BratWriter.class), "conll/2012/en-ref.ann");
    }

    @Test
    public void testConll2012_3() throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2012Reader.class, Conll2012Reader.PARAM_READ_LEMMA,
                        false, Conll2012Reader.PARAM_READ_NAMED_ENTITY, false,
                        Conll2012Reader.PARAM_READ_SEMANTIC_PREDICATE, false,
                        Conll2012Reader.PARAM_READ_COREFERENCE, false,
                        Conll2012Reader.PARAM_USE_HEADER_METADATA, false),
                createEngineDescription(BratWriter.class), "conll/2012/en-ref-min.ann",
                "conll/2012/en-orig.conll");
    }

    @Test
    public void testWithShortNames() throws Exception
    {
        testRoundTrip(createReaderDescription(BratReader.class,
                BratReader.PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS,
                asList("Token -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                        "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization",
                        "Location -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location")),
                createEngineDescription(BratWriter.class, BratWriter.PARAM_ENABLE_TYPE_MAPPINGS,
                        true),
                "brat/document0a.ann");
    }

    @Test
    public void testWithLongNames() throws Exception
    {
        testRoundTrip(createReaderDescription(BratReader.class),
                createEngineDescription(BratWriter.class, BratWriter.PARAM_ENABLE_TYPE_MAPPINGS,
                        false),
                "brat/document0b.ann");
    }

    @Test
    public void testBratWithNewlines() throws Exception
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
    public void testBratWithDiscontinousTwo() throws Exception
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
    
    @Test
    public void test1() throws Exception
    {
        testOneWay(createReaderDescription(BratReader.class,
                BratReader.PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS,
                asList("Country -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location",
                        "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization",
                        "MERGE-ORG -> de.tudarmstadt.ukp.dkpro.core.io.brat.type.MergeOrg"),
                BratReader.PARAM_RELATION_TYPE_MAPPINGS,
                asList("Origin -> de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation"),
                BratReader.PARAM_RELATION_TYPES,
                asList("de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value")),
                createEngineDescription(BratWriter.class, BratWriter.PARAM_RELATION_TYPES, asList(
                        "de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value")),
                "brat/document1-ref.ann", "brat/document1.ann");
    }

    @Test
    public void testTextAnnotationWithSubcategorization() throws Exception
    {
        testOneWay(createReaderDescription(BratReader.class, BratReader.PARAM_TEXT_ANNOTATION_TYPES,
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity:value",
                BratReader.PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS,
                asList("Country -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                        "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                        "MERGE-ORG -> de.tudarmstadt.ukp.dkpro.core.io.brat.type.MergeOrg"),
                BratReader.PARAM_RELATION_TYPE_MAPPINGS,
                asList("Origin -> de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation"),
                BratReader.PARAM_RELATION_TYPES,
                asList("de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value")),
                createEngineDescription(BratWriter.class, BratWriter.PARAM_RELATION_TYPES, asList(
                        "de.tudarmstadt.ukp.dkpro.core.io.brat.type.AnnotationRelation:source:target{A}:value")),
                "brat/document1-ref-sub.ann", "brat/document1.ann");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
