package de.tudarmstadt.ukp.dkpro.core.readability;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures.Measures;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore;

public class ReadabilityAnnotatorTest
{
    private final static double EPSILON = 0.1; 
    
	static HashMap<String, Double>  correctResult = new HashMap<String, Double>();
	static
	{
		correctResult.put("kincaid", 7.6);
		correctResult.put("ari",  9.1);
		correctResult.put("coleman_liau", 11.6);
		correctResult.put("flesch", 70.6);
		correctResult.put("lix", 5.0);
		correctResult.put("smog", 9.9);
		correctResult.put("fog", 10.6);
	}
	
    @Test
    public void readabilityAnnotatorTest()
        throws Exception
    {
        String text = FileUtils.readFileToString(
                new File("src/test/resources/readability/test_document_en.txt")
          );
        
        Map<String, Boolean> measureMap = new HashMap<String,Boolean>();
          for (Measures measure : Measures.values()) {
        	  measureMap.put(measure.name(), true);
          }
        

        AnalysisEngineDescription segmenter = createEngineDescription(
                BreakIteratorSegmenter.class
        );

        AnalysisEngineDescription readability = createEngineDescription(
                ReadabilityAnnotator.class
        );

        AnalysisEngineDescription aggregate = createEngineDescription(
                segmenter,
                readability
        );
        
        AnalysisEngine ae = createEngine(aggregate); 
        JCas jcas = ae.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText(text);
        ae.process(jcas);

        int i = 0;
        for (ReadabilityScore score : JCasUtil.select(jcas, ReadabilityScore.class)) {
        	String strMeasureName = score.getMeasureName();
        	double dScore = score.getScore();
        	System.out.println(strMeasureName + " : " + score.getScore());
        	assertTrue(measureMap.containsKey(strMeasureName));
        	assertEquals(correctResult.get(strMeasureName), dScore, EPSILON);
            i++;
        }
        assertEquals(7, i);
    }
}
