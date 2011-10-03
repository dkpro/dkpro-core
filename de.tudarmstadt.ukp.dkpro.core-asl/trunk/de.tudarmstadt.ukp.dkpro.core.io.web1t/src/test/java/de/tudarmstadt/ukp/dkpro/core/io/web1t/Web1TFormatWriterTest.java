package de.tudarmstadt.ukp.dkpro.core.io.web1t;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.junit.Test;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class Web1TFormatWriterTest {

    @Test
    public void web1TFormatTest() throws Exception {     
        CollectionReader reader = createCollectionReader(
                TextReader.class,
                ResourceCollectionReaderBase.PARAM_PATH, "src/test/resources/",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                        ResourceCollectionReaderBase.INCLUDE_PREFIX + "**/*.txt" }
        );
 
        AnalysisEngineDescription segmenter = createPrimitiveDescription(
                BreakIteratorSegmenter.class
        );
 
        AnalysisEngineDescription ngramWriter = createPrimitiveDescription(
                Web1TFormatWriter.class,
                Web1TFormatWriter.PARAM_OUTPUT_PATH, "target/",
                Web1TFormatWriter.PARAM_MIN_NGRAM_LENGTH, 1,
                Web1TFormatWriter.PARAM_MAX_NGRAM_LENGTH, 3
        );
        
        SimplePipeline.runPipeline(
                reader,
                segmenter,
                ngramWriter
        );
    }
}
