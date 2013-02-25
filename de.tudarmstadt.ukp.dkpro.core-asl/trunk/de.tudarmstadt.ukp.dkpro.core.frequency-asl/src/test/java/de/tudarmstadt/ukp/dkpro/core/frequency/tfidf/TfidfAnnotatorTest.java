package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static org.uimafit.util.JCasUtil.iterate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.TfidfAnnotator;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.TfidfConsumer;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.TfidfAnnotator.WeightingModeIdf;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.TfidfAnnotator.WeightingModeTf;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf;

/**
 *
 * @author erbs, zesch
 *
 */
public class TfidfAnnotatorTest
{
    // assertEquals on doubles needs an epsilon
    protected static final double EPSILON = 0.000001;

    private final static String CONSUMER_TEST_DATA_PATH = "src/test/resources/consumer/";
    private final static String OUTPUT_PATH = CONSUMER_TEST_DATA_PATH + "output/df.model";

    @Before
    public void buildModel() throws Exception {
        // write the model
        CollectionReader reader = createCollectionReader(
                TextReader.class,
                PARAM_PATH, CONSUMER_TEST_DATA_PATH,
                PARAM_PATTERNS, new String[] { INCLUDE_PREFIX+"*.txt" });

        AnalysisEngineDescription aggregate = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class),
                createPrimitiveDescription(
                        TfidfConsumer.class,
                        TfidfConsumer.PARAM_FEATURE_PATH, Token.class.getName(),
                        TfidfConsumer.PARAM_OUTPUT_PATH,  OUTPUT_PATH));

        SimplePipeline.runPipeline(reader, aggregate);
    }

    @After
    public void cleanUp() {
		FileUtils.deleteRecursive(new File(OUTPUT_PATH));
    }

    @Test
    public void tfidfTest_normal_constantOne() throws Exception {

        AnalysisEngine tfidfAnnotator = createPrimitive(
                TfidfAnnotator.class,
                TfidfAnnotator.PARAM_FEATURE_PATH, Token.class.getName(),
                TfidfAnnotator.PARAM_TFDF_PATH,    OUTPUT_PATH,
                TfidfAnnotator.PARAM_TF_MODE,      WeightingModeTf.NORMAL.toString(),
                TfidfAnnotator.PARAM_IDF_MODE,     WeightingModeIdf.CONSTANT_ONE.toString()
        );

        Map<String, Double> expectedDoc1 = new HashMap<String, Double>();
        expectedDoc1.put("example", 1.0);
        expectedDoc1.put("sentence", 1.0);
        expectedDoc1.put("funny", 1.0);

        Map<String, Double> expectedDoc2 = new HashMap<String, Double>();
        expectedDoc2.put("example", 2.0);
        expectedDoc2.put("sentence", 1.0);

        for (JCas jcas : new JCasIterable(getReader(), getTokenizer(), tfidfAnnotator)) {
            testIt(jcas, expectedDoc1, expectedDoc2);
        }
    }

    @Test
    public void tfidfTest_binary_binary() throws Exception {

        AnalysisEngine tfidfAnnotator = createPrimitive(
                TfidfAnnotator.class,
                TfidfAnnotator.PARAM_FEATURE_PATH, Token.class.getName(),
                TfidfAnnotator.PARAM_TFDF_PATH,    OUTPUT_PATH,
                TfidfAnnotator.PARAM_TF_MODE,      WeightingModeTf.BINARY.toString(),
                TfidfAnnotator.PARAM_IDF_MODE,     WeightingModeIdf.BINARY.toString()
        );

        Map<String, Double> expectedDoc1 = new HashMap<String, Double>();
        expectedDoc1.put("example", 1.0);
        expectedDoc1.put("sentence", 1.0);
        expectedDoc1.put("funny", 1.0);

        Map<String, Double> expectedDoc2 = new HashMap<String, Double>();
        expectedDoc2.put("example", 1.0);
        expectedDoc2.put("sentence", 1.0);

        for (JCas jcas : new JCasIterable(getReader(), getTokenizer(), tfidfAnnotator)) {
            testIt(jcas, expectedDoc1, expectedDoc2);
        }
    }

    @Test
    public void tfidfTest_normal_log() throws Exception {

        AnalysisEngine tfidfAnnotator = createPrimitive(
                TfidfAnnotator.class,
                TfidfAnnotator.PARAM_FEATURE_PATH, Token.class.getName(),
                TfidfAnnotator.PARAM_TFDF_PATH,    OUTPUT_PATH,
                TfidfAnnotator.PARAM_TF_MODE,      WeightingModeTf.NORMAL.toString(),
                TfidfAnnotator.PARAM_IDF_MODE,     WeightingModeIdf.LOG.toString()
        );

        Map<String, Double> expectedDoc1 = new HashMap<String, Double>();
        expectedDoc1.put("example", 0.0);
        expectedDoc1.put("sentence", 0.0);
        expectedDoc1.put("funny", Math.log(2));

        Map<String, Double> expectedDoc2 = new HashMap<String, Double>();
        expectedDoc2.put("example", 0.0);
        expectedDoc2.put("sentence", 0.0);

        for (JCas jcas : new JCasIterable(getReader(), getTokenizer(), tfidfAnnotator)) {
            testIt(jcas, expectedDoc1, expectedDoc2);
        }
    }

    private void testIt(JCas jcas, Map<String, Double> expectedDoc1, Map<String, Double> expectedDoc2) {
        if (DocumentMetaData.get(jcas).getDocumentTitle().equals("test1.txt")) {
            int i = 0;
            for (Tfidf tfidf : iterate(jcas, Tfidf.class)) {
                assertEquals(tfidf.getTerm(), expectedDoc1.get(tfidf.getTerm()).doubleValue(), tfidf.getTfidfValue(), EPSILON);
                i++;
            }
            assertEquals(3, i);
        }
        else if (DocumentMetaData.get(jcas).getDocumentTitle().equals("test2.txt")) {
            int i = 0;
            for (Tfidf tfidf : iterate(jcas, Tfidf.class)) {
                assertEquals(tfidf.getTerm(), expectedDoc2.get(tfidf.getTerm()).doubleValue(), tfidf.getTfidfValue(), EPSILON);
                i++;
            }
            assertEquals(3, i);
        }
        else {
            fail("There should be no other documents in that directory.");
        }
    }

    private CollectionReader getReader() throws ResourceInitializationException {
            return createCollectionReader(
                TextReader.class,
                PARAM_PATH, CONSUMER_TEST_DATA_PATH,
                PARAM_PATTERNS, new String[] { INCLUDE_PREFIX+"*.txt" });
    }

    private AnalysisEngine getTokenizer() throws ResourceInitializationException {
        return createPrimitive(BreakIteratorSegmenter.class);
    }
}