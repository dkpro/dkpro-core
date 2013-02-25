package de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.*;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.util.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.model.DfModel;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.util.TfidfUtils;

/**
 * Unit test of {@link TfidfConsumer} and {@link TfidfAnnotator}.
 *
 * @author zesch, parzonka
 *
 */
public class TfidfConsumerTest {

    private final static String CONSUMER_TEST_DATA_PATH = "src/test/resources/consumer/";
    private final static String OUTPUT_PATH = CONSUMER_TEST_DATA_PATH + "output/df.model";

    @Test
    public void RawScoresTest()
	    throws Exception {

        CollectionReader reader = createCollectionReader(
                TextReader.class,
                PARAM_PATH, CONSUMER_TEST_DATA_PATH,
                PARAM_PATTERNS, new String[] { INCLUDE_PREFIX+"*.txt" });

        AnalysisEngineDescription aggregate = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class),
                createPrimitiveDescription(TfidfConsumer.class,
                    TfidfConsumer.PARAM_FEATURE_PATH, de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token.class.getName(),
                    TfidfConsumer.PARAM_OUTPUT_PATH,  OUTPUT_PATH
                )
        );

        // now create the tf and df files
        SimplePipeline.runPipeline(reader, aggregate);

        // check whether they were really created and contain the correct values
	DfModel dfModel = TfidfUtils.getDfModel(OUTPUT_PATH);

        assertEquals(2, dfModel.getDf("example"));
	assertEquals(2, dfModel.getDf("sentence"));
	assertEquals(1, dfModel.getDf("funny"));
    }

    @AfterClass
    public static void cleanUp() {
		FileUtils.deleteRecursive(new File(OUTPUT_PATH));
    }
}
