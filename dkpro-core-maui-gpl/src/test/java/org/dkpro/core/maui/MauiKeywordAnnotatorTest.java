package org.dkpro.core.maui;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class MauiKeywordAnnotatorTest
{
    @Test
    public void testVocabThesoz() throws Exception
    {
        File ouputFolder = testContext.getTestOutputFolder();
        
        CollectionReaderDescription reader = createReaderDescription(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/text/*.txt",
                TextReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription annotator = createEngineDescription(
                MauiKeywordAnnotator.class,
                MauiKeywordAnnotator.PARAM_VARIANT, "socialscience_thesoz");
        
        AnalysisEngineDescription writer = createEngineDescription(
                XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, ouputFolder,
                XmiWriter.PARAM_OVERWRITE, true);
        
        runPipeline(reader, annotator, writer);
    }    
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
