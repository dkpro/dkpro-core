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
package org.dkpro.core.io.tiger;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.conll.Conll2012Writer;
import org.dkpro.core.io.tiger.TigerXmlReader;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.ReaderAssert;
import org.dkpro.core.testing.WriterAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;

public class TigerXmlReaderTest
{
    @Before
    public void setUp() throws IOException {
        DkproTestContext.get().initializeTestWorkspace();
    }    
    
    @Test
    public void test()
        throws Exception
    {
        CollectionReader reader = createReader(TigerXmlReader.class,
                TigerXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TigerXmlReader.PARAM_PATTERNS, "[+]tiger-sample.xml",
                TigerXmlReader.PARAM_LANGUAGE, "de",
                TigerXmlReader.PARAM_READ_PENN_TREE, true);

        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());

        String pennTree = "(VROOT ($( ``) (S (PN-SB (NE Ross) (NE Perot)) (VAFIN wäre) "
                + "(ADV vielleicht) (NP-PD (ART ein) (ADJA prächtiger) (NN Diktator))) ($( ''))";

        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
    }

    @Test(expected = IllegalStateException.class)
    public void test2()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(TigerXmlReader.class,
                TigerXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TigerXmlReader.PARAM_PATTERNS, "[+]simple-broken-sentence.xml",
                TigerXmlReader.PARAM_LANGUAGE, "de",
                TigerXmlReader.PARAM_READ_PENN_TREE, true);

        for (JCas cas : iteratePipeline(reader, new AnalysisEngineDescription[] {})) {
            System.out.printf("%s %n", DocumentMetaData.get(cas).getDocumentId());
        }
    }

    @Test
    public void tigerSampleTest()
        throws Exception
    {       
        ReaderAssert
            .assertThat(TigerXmlReader.class,
                    TigerXmlReader.PARAM_LANGUAGE, "de",
                    TigerXmlReader.PARAM_READ_PENN_TREE, true)
            .readingFrom("src/test/resources/tiger-sample.xml")
            .usingWriter(WriterAssert.simpleJCasDumper(new File("tiger-sample.xml")))
            .asFiles()
            .allSatisfy(file -> {
                assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                        contentOf(new File("src/test/resources/", file.getName() + ".dump")));
            })
            .extracting(File::getName)
            .containsExactlyInAnyOrder("tiger-sample.xml");
        
    }

    @Test
    public void semevalSampleTest()
        throws Exception
    {
        ReaderAssert
            .assertThat(TigerXmlReader.class,
                  TigerXmlReader.PARAM_LANGUAGE, "en",
                  TigerXmlReader.PARAM_READ_PENN_TREE, true)
            .readingFrom("src/test/resources/semeval1010-en-sample.xml")
            .usingWriter(WriterAssert.simpleJCasDumper(new File("semeval1010-en-sample.xml")))
            .asFiles()
            .allSatisfy(file -> {
                assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                        contentOf(new File("src/test/resources/", file.getName() + ".dump")));
            })
            .extracting(File::getName)
            .containsExactlyInAnyOrder("semeval1010-en-sample.xml");
    }

    @Test
    public void semevalSampleTest2()
        throws Exception
    {
        testOneWay(
                createReaderDescription(TigerXmlReader.class,
                        TigerXmlReader.PARAM_LANGUAGE, "en",
                        TigerXmlReader.PARAM_READ_PENN_TREE, true),
                createEngineDescription(Conll2012Writer.class),
                "semeval1010-en-sample.conll",
                "semeval1010-en-sample.xml");
    }

    @Test
    public void testNoncontiguousFrameTarget()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(TigerXmlReader.class,
                TigerXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TigerXmlReader.PARAM_PATTERNS, "[+]tiger-sample-noncontiguousframe.xml",
                TigerXmlReader.PARAM_LANGUAGE, "de",
                TigerXmlReader.PARAM_READ_PENN_TREE, true);

        int[][] frameRanges = new int[][] { { 4, 11 }, { 33, 47 }, { 71, 74 }, { 112, 138 },
                { 143, 147 }, { 246, 255 } };

        for (JCas cas : iteratePipeline(reader, new AnalysisEngineDescription[] {})) {
            for (Sentence sentence : select(cas, Sentence.class)) {
                for (SemPred frame : selectCovered(SemPred.class, sentence)) {
                    System.out
                            .println("frame boundary " + frame.getBegin() + " : " + frame.getEnd());
                    boolean found = false;
                    for (int[] element : frameRanges) {
                        if (element[0] == frame.getBegin() && element[1] == frame.getEnd()) {
                            found = true;
                            break;
                        }
                    }
                    assertEquals(true, found);
                }
            }
        }
    }

    @Test
    public void testFrameTargetHavingMultipleChildren()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(TigerXmlReader.class,
                TigerXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TigerXmlReader.PARAM_PATTERNS, "[+]tiger-sample-complexframe.xml",
                TigerXmlReader.PARAM_LANGUAGE, "de",
                TigerXmlReader.PARAM_READ_PENN_TREE, true);

        int[][] frameRanges = new int[][] { { 26, 41 }, { 54, 61 }, { 64, 85 }, { 97, 104 },
                { 120, 130 }, { 135, 151 }, { 152, 169 } };
        /* Frame targets:
         * Glaubwürdigkeit
         * wichtig
         * ein Zeichen zu setzen
         * gewillt
         * Erreichung
         * Millenniumsziele
         * <eine aktive Rolle> ... <übernehmen> (Noncontiguous frame target)
         * **/

        for (JCas cas : iteratePipeline(reader, new AnalysisEngineDescription[] {})) {
            for (Sentence sentence : select(cas, Sentence.class)) {
                for (SemPred frame : selectCovered(SemPred.class, sentence)) {
                    System.out.println("frame target text [" + frame.getCoveredText()
                            + "], frame boundary " + frame.getBegin() + " : " + frame.getEnd());
                    boolean found = false;
                    for (int[] element : frameRanges) {
                        if (element[0] == frame.getBegin() && element[1] == frame.getEnd()) {
                            found = true;
                            break;
                        }
                    }
                    assertEquals(true, found);
                }
            }
        }
    }

    @Test
    public void testContiguousFrameTarget()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(TigerXmlReader.class,
                TigerXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TigerXmlReader.PARAM_PATTERNS, "[+]tiger-sample-contiguousframe.xml",
                TigerXmlReader.PARAM_LANGUAGE, "de",
                TigerXmlReader.PARAM_READ_PENN_TREE, true);


        /**
         * first element is contiguous
         * it spans over 2 tokens "schlage" and "mit", so the boundary should be
         * schlage.begin and mit.end ==> (4, 15)
         */
        int[][] frameRanges = new int[][] { { 4, 15 }, { 33, 47 }, { 71, 74 }, { 112, 138 },
                { 143, 147 }, { 246, 255 } };

        for (JCas cas : iteratePipeline(reader, new AnalysisEngineDescription[] {})) {
            for (Sentence sentence : select(cas, Sentence.class)) {
                for (SemPred frame : selectCovered(SemPred.class, sentence)) {
                    System.out
                            .println("frame boundary " + frame.getBegin() + " : " + frame.getEnd());
                    boolean found = false;
                    for (int[] element : frameRanges) {
                        if (element[0] == frame.getBegin() && element[1] == frame.getEnd()) {
                            found = true;
                            break;
                        }
                    }
                    assertEquals(true, found);
                }
            }
        }
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
