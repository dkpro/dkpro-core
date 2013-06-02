package de.tudarmstadt.ukp.dkpro.core.langdect;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregate;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
import de.tudarmstadt.ukp.dkpro.core.frequency.resources.Web1TFrequencyCountResource;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class LanguageDetectorTest
{

    @Ignore
    @Test
    public void languageDetectorTest()
        throws Exception
    {
        String web1TBaseDir = new DKProContext().getWorkspace("web1t").getAbsolutePath();
        
        AnalysisEngine engine = createAggregate(
            createAggregateDescription(
                createPrimitiveDescription(
                        BreakIteratorSegmenter.class
                ),
                createPrimitiveDescription(
                    LanguageDetector.class,
                    LanguageDetector.PARAM_WEB1T_RESOURCES, 
                    Arrays.asList(
                            createExternalResourceDescription(
                                    Web1TFrequencyCountResource.class,
                                    Web1TFrequencyCountResource.PARAM_INDEX_PATH, web1TBaseDir + "/en",
                                    Web1TFrequencyCountResource.PARAM_MIN_NGRAM_LEVEL, "1",
                                    Web1TFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, "3"
                            ),
                            createExternalResourceDescription(
                                    Web1TFrequencyCountResource.class,
                                    Web1TFrequencyCountResource.PARAM_INDEX_PATH, web1TBaseDir + "/de",
                                    Web1TFrequencyCountResource.PARAM_MIN_NGRAM_LEVEL, "1",
                                    Web1TFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, "3"
                            )
                     )
                )
            )
        );
        
        for (String line : FileUtils.readLines(new File("src/test/resources/langdect/test.txt"))) {
            String[] parts = line.split("\t");
            String text = parts[0];
            String language = parts[1];
            
            JCas aJCas = engine.newJCas();
            aJCas.setDocumentText(text);
            engine.process(aJCas);
            
            String[] languageParts = aJCas.getDocumentLanguage().split("/");
            String casLanguage = languageParts[languageParts.length-1];

            assertEquals(language, casLanguage);
        }
    }
    
//    @Test
//    public void languageDetectorTest()
//        throws Exception
//    {
//        String web1TBaseDir = new DKProContext().getWorkspace("web1t").getAbsolutePath();
//        
//        AnalysisEngine engine = createAggregate(
//            createAggregateDescription(
//                createPrimitiveDescription(
//                        BreakIteratorSegmenter.class
//                ),
//                createPrimitiveDescription(
//                    LanguageDetector.class,
//                    LanguageDetector.PARAM_WEB1T_SOURCES, 
//                    new String[] {
//                        web1TBaseDir + "/en",
//                        web1TBaseDir + "/de"
//                    }
//                )
//            )
//        );
//        
//        for (String line : FileUtils.readLines(new File("src/main/resources/language_detection_sample.csv"))) {
//            String[] parts = line.split("\t");
//            String text = parts[1];
//            
//            JCas aJCas = engine.newJCas();
//            aJCas.setDocumentText(text);
//
//            engine.process(aJCas);
//            System.out.println(parts[0].substring(1,parts[0].length()-1) + "\t" + aJCas.getDocumentLanguage());
//        }
//    }
}
