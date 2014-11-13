/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.textnormalizer;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator;
import de.tudarmstadt.ukp.dkpro.core.jazzy.JazzyChecker;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class SpellCheckerNormalizerTest
{
    @Test
    public void testSpellCheckerNormalizer() throws Exception
    {
        test("ich tesde meiine neue Rechtschreibbeprüfung.",
                "ich teste meine neue Rechtschreibprüfung.");

        // Doesn't work
        // test("LauFEN SCHIEßEN BaföG ÜBERlegen","laufen schießen BAföG überlegen");
    }
    
    public void test(String input, String output) throws Exception
    {
    	AggregateBuilder builder = new AggregateBuilder();
    	builder.add(createEngineDescription(BreakIteratorSegmenter.class));
    	builder.add(createEngineDescription(JazzyChecker.class, 
    	        JazzyChecker.PARAM_MODEL_LOCATION, "src/test/resources/dictionary/ngerman"));
    	builder.add(createEngineDescription(SpellCheckerNormalizer.class));
    	builder.add(createEngineDescription(ApplyChangesAnnotator.class), 
    	        ApplyChangesAnnotator.VIEW_SOURCE, CAS.NAME_DEFAULT_SOFA, 
    	        ApplyChangesAnnotator.VIEW_TARGET, "spell_checked");
    
    	AnalysisEngine engine = builder.createAggregate();
    
    	String text = input;
    	JCas jcas = engine.newJCas();
    	jcas.setDocumentText(text);
    	DocumentMetaData.create(jcas);
    
    	engine.process(jcas);
    
    	JCas view0 = jcas.getView(CAS.NAME_DEFAULT_SOFA);
    	JCas view1 = jcas.getView("spell_checked");	
    	
    	System.out.println("View0: " + view0.getDocumentText());
    	System.out.println("View1: " + view1.getDocumentText());
    	
    	assertEquals(output, view1.getDocumentText());
    }
}
