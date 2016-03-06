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
package de.tudarmstadt.ukp.dkpro.core.io.ditop;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.mallet.lda.LdaTopicModelEstimator;
import de.tudarmstadt.ukp.dkpro.core.mallet.lda.LdaTopicModelInferencer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiTopWriterTest
{
    private static final String DITOP_CORPUSNAME = "test";
    private static final String TARGET_DITOP = "target/ditop";
    private static final File MODEL_FILE = new File("target/mallet/model");
    private static final String CAS_DIR = "src/test/resources/txt";
    private static final String CAS_FILE_PATTERN = "[+]*.txt";

    private static final int N_TOPICS = 10;
    private static final int N_ITERATIONS = 50;
    private static final boolean USE_LEMMAS = false;
    private static final String LANGUAGE = "en";

    @Before
    public void setUp()
        throws Exception
    {
        /* Generate model */
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription estimator = createEngineDescription(
                LdaTopicModelEstimator.class,
                LdaTopicModelEstimator.PARAM_TARGET_LOCATION, MODEL_FILE,
                LdaTopicModelEstimator.PARAM_N_ITERATIONS, N_ITERATIONS,
                LdaTopicModelEstimator.PARAM_N_TOPICS, N_TOPICS,
                LdaTopicModelEstimator.PARAM_USE_LEMMA, USE_LEMMAS);
        SimplePipeline.runPipeline(reader, segmenter, estimator);

        MODEL_FILE.deleteOnExit();
    }

    @Test
    public void testSimple()
        throws UIMAException, IOException
    {
        int expectedNDocuments = 2;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription inferencer = createEngineDescription(
                LdaTopicModelInferencer.class,
                LdaTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE);
        AnalysisEngineDescription ditopwriter = createEngineDescription(DiTopWriter.class,
                DiTopWriter.PARAM_TARGET_LOCATION, TARGET_DITOP,
                DiTopWriter.PARAM_MODEL_LOCATION, MODEL_FILE,
                DiTopWriter.PARAM_CORPUS_NAME, DITOP_CORPUSNAME);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, ditopwriter);

        /* test whether target files and dirs exist */
        File contentDir = new File(TARGET_DITOP, DITOP_CORPUSNAME + "_" + N_TOPICS);
        File topicsFile = new File(contentDir, "topics.csv");
        File topicTermT15File = new File(contentDir, "topicTerm-T15.txt");
        File topicTermFile = new File(contentDir, "topicTerm.txt");
        File topicTermMatrixFile = new File(contentDir, "topicTermMatrix.txt");

        assertTrue(new File(TARGET_DITOP, "config.all").exists());
        assertTrue(contentDir.isDirectory());
        assertTrue(topicTermT15File.exists());
        assertTrue(topicTermFile.exists());
        assertTrue(topicTermMatrixFile.exists());
        assertTrue(topicsFile.exists());

        /* check that file lengths are correct */
        assertEquals(expectedNDocuments + 1, FileUtils.readLines(topicsFile).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermT15File).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermFile).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermMatrixFile).size());
        MODEL_FILE.delete();
    }

    @Test
    public void testCollectionValuesExact()
        throws UIMAException, IOException
    {
        int expectedNDocuments = 2;
        String exactName = new File(CAS_DIR).toURI().toString();
        String[] collectionValues = new String[] { exactName };
        boolean exactMatch = true;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription inferencer = createEngineDescription(
                LdaTopicModelInferencer.class,
                LdaTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE);
        AnalysisEngineDescription ditopwriter = createEngineDescription(DiTopWriter.class,
                DiTopWriter.PARAM_TARGET_LOCATION, TARGET_DITOP,
                DiTopWriter.PARAM_MODEL_LOCATION, MODEL_FILE,
                DiTopWriter.PARAM_CORPUS_NAME, DITOP_CORPUSNAME,
                DiTopWriter.PARAM_COLLECTION_VALUES, collectionValues,
                DiTopWriter.PARAM_COLLECTION_VALUES_EXACT_MATCH, exactMatch);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, ditopwriter);

        /* test whether target files and dirs exist */
        File contentDir = new File(TARGET_DITOP, DITOP_CORPUSNAME + "_" + N_TOPICS);
        File topicsFile = new File(contentDir, "topics.csv");
        File topicTermT15File = new File(contentDir, "topicTerm-T15.txt");
        File topicTermFile = new File(contentDir, "topicTerm.txt");
        File topicTermMatrixFile = new File(contentDir, "topicTermMatrix.txt");

        assertTrue(new File(TARGET_DITOP, "config.all").exists());
        assertTrue(contentDir.isDirectory());
        assertTrue(topicTermT15File.exists());
        assertTrue(topicTermFile.exists());
        assertTrue(topicTermMatrixFile.exists());
        assertTrue(topicsFile.exists());

        /* check that file lengths are correct */
        assertEquals(expectedNDocuments + 1, FileUtils.readLines(topicsFile).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermT15File).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermFile).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermMatrixFile).size());
        MODEL_FILE.delete();
    }

    @Test
    public void testCollectionValuesExactNoMatch()
        throws UIMAException, IOException
    {
        int expectedNDocuments = 0;
        String[] collectionValues = new String[] { "file:/home/schnober/workspace/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.io.ditop/src/test/resources/" };
        boolean exactMatch = true;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription inferencer = createEngineDescription(
                LdaTopicModelInferencer.class,
                LdaTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE);
        AnalysisEngineDescription ditopwriter = createEngineDescription(DiTopWriter.class,
                DiTopWriter.PARAM_TARGET_LOCATION, TARGET_DITOP,
                DiTopWriter.PARAM_MODEL_LOCATION, MODEL_FILE,
                DiTopWriter.PARAM_CORPUS_NAME, DITOP_CORPUSNAME,
                DiTopWriter.PARAM_COLLECTION_VALUES, collectionValues,
                DiTopWriter.PARAM_COLLECTION_VALUES_EXACT_MATCH, exactMatch);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, ditopwriter);

        /* test whether target files and dirs exist */
        File contentDir = new File(TARGET_DITOP, DITOP_CORPUSNAME + "_" + N_TOPICS);
        File topicsFile = new File(contentDir, "topics.csv");
        File topicTermT15File = new File(contentDir, "topicTerm-T15.txt");
        File topicTermFile = new File(contentDir, "topicTerm.txt");
        File topicTermMatrixFile = new File(contentDir, "topicTermMatrix.txt");

        assertTrue(new File(TARGET_DITOP, "config.all").exists());
        assertTrue(contentDir.isDirectory());
        assertTrue(topicTermT15File.exists());
        assertTrue(topicTermFile.exists());
        assertTrue(topicTermMatrixFile.exists());
        assertTrue(topicsFile.exists());

        /* check that file lengths are correct */
        assertEquals(expectedNDocuments + 1, FileUtils.readLines(topicsFile).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermT15File).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermFile).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermMatrixFile).size());
        MODEL_FILE.delete();
    }

    @Test
    public void testCollectionValuesNotExact()
        throws UIMAException, IOException
    {
        int expectedNDocuments = 2;
        String[] collectionValues = new String[] { "txt" };
        boolean exactMatch = false;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription inferencer = createEngineDescription(
                LdaTopicModelInferencer.class,
                LdaTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE);
        AnalysisEngineDescription ditopwriter = createEngineDescription(DiTopWriter.class,
                DiTopWriter.PARAM_TARGET_LOCATION, TARGET_DITOP,
                DiTopWriter.PARAM_MODEL_LOCATION, MODEL_FILE,
                DiTopWriter.PARAM_CORPUS_NAME, DITOP_CORPUSNAME,
                DiTopWriter.PARAM_COLLECTION_VALUES, collectionValues,
                DiTopWriter.PARAM_COLLECTION_VALUES_EXACT_MATCH, exactMatch);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, ditopwriter);

        /* test whether target files and dirs exist */
        File contentDir = new File(TARGET_DITOP, DITOP_CORPUSNAME + "_" + N_TOPICS);
        File topicsFile = new File(contentDir, "topics.csv");
        File topicTermT15File = new File(contentDir, "topicTerm-T15.txt");
        File topicTermFile = new File(contentDir, "topicTerm.txt");
        File topicTermMatrixFile = new File(contentDir, "topicTermMatrix.txt");

        assertTrue(new File(TARGET_DITOP, "config.all").exists());
        assertTrue(contentDir.isDirectory());
        assertTrue(topicTermT15File.exists());
        assertTrue(topicTermFile.exists());
        assertTrue(topicTermMatrixFile.exists());
        assertTrue(topicsFile.exists());

        /* check that file lengths are correct */
        assertEquals(expectedNDocuments + 1, FileUtils.readLines(topicsFile).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermT15File).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermFile).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermMatrixFile).size());
        MODEL_FILE.delete();
    }

    @Test
    public void testCollectionValuesNotExactNoMatch()
        throws UIMAException, IOException
    {
        int expectedNDocuments = 0;
        String[] collectionValues = new String[] { "abcd" };
        boolean exactMatch = false;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription inferencer = createEngineDescription(
                LdaTopicModelInferencer.class,
                LdaTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE);
        AnalysisEngineDescription ditopwriter = createEngineDescription(DiTopWriter.class,
                DiTopWriter.PARAM_TARGET_LOCATION, TARGET_DITOP,
                DiTopWriter.PARAM_MODEL_LOCATION, MODEL_FILE,
                DiTopWriter.PARAM_CORPUS_NAME, DITOP_CORPUSNAME,
                DiTopWriter.PARAM_COLLECTION_VALUES, collectionValues,
                DiTopWriter.PARAM_COLLECTION_VALUES_EXACT_MATCH, exactMatch);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, ditopwriter);

        /* test whether target files and dirs exist */
        File contentDir = new File(TARGET_DITOP, DITOP_CORPUSNAME + "_" + N_TOPICS);
        File topicsFile = new File(contentDir, "topics.csv");
        File topicTermT15File = new File(contentDir, "topicTerm-T15.txt");
        File topicTermFile = new File(contentDir, "topicTerm.txt");
        File topicTermMatrixFile = new File(contentDir, "topicTermMatrix.txt");

        assertTrue(new File(TARGET_DITOP, "config.all").exists());
        assertTrue(contentDir.isDirectory());
        assertTrue(topicTermT15File.exists());
        assertTrue(topicTermFile.exists());
        assertTrue(topicTermMatrixFile.exists());
        assertTrue(topicsFile.exists());

        /* check that file lengths are correct */
        assertEquals(expectedNDocuments + 1, FileUtils.readLines(topicsFile).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermT15File).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermFile).size());
        assertEquals(N_TOPICS, FileUtils.readLines(topicTermMatrixFile).size());
        MODEL_FILE.delete();
    }
}
