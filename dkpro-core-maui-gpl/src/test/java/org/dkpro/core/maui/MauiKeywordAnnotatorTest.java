package org.dkpro.core.maui;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.pdf.PdfReader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class MauiKeywordAnnotatorTest
{
    @Test
    public void testVocabThesoz() throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                PdfReader.class,
                PdfReader.PARAM_SOURCE_LOCATION, "src/test/resources/pdf/*.pdf",
                PdfReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription annotator = createEngineDescription(
                MauiKeywordAnnotator.class,
                MauiKeywordAnnotator.PARAM_MODEL_LOCATION, "src/test/resources/model-vocab-thesoz.ser",
                MauiKeywordAnnotator.PARAM_LANGUAGE, "en",
                MauiKeywordAnnotator.PARAM_VOCABULARY_LOCATION, "src/test/resources/thesoz-komplett.rdf.gz",
                MauiKeywordAnnotator.PARAM_VOCABULARY_FORMAT, "skos"
                //MauiKeywordAnnotator.PARAM_VOCABULARY_ENCODING, "UTF-8"
                );
        
//        AnalysisEngineDescription textWriter = createEngineDescription(
//                TextWriter.class,
//                TextWriter.PARAM_TARGET_LOCATION, "target/pdf2text"
//                );
        
        runPipeline(reader, annotator);
    }
    
    @Test
    public void testVocabNone() throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                PdfReader.class,
                PdfReader.PARAM_SOURCE_LOCATION, "src/test/resources/pdf/*.pdf",
                PdfReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription annotator = createEngineDescription(
                MauiKeywordAnnotator.class,
                MauiKeywordAnnotator.PARAM_MODEL_LOCATION, "src/test/resources/model-vocab-none.ser",
                MauiKeywordAnnotator.PARAM_LANGUAGE, "en"
//                MauiKeywordAnnotator.PARAM_VOCABULARY_LOCATION, "none"
                //MauiKeywordAnnotator.PARAM_VOCABULARY_ENCODING, "UTF-8"
                );
        
//        AnalysisEngineDescription textWriter = createEngineDescription(
//                TextWriter.class,
//                TextWriter.PARAM_TARGET_LOCATION, "target/pdf2text"
//                );
        
        runPipeline(reader, annotator);
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
