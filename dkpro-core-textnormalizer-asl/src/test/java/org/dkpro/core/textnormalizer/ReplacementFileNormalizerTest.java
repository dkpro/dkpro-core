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
package org.dkpro.core.textnormalizer;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.castransformation.ApplyChangesAnnotator;
import org.dkpro.core.textnormalizer.ReplacementFileNormalizer.SrcSurroundings;
import org.dkpro.core.textnormalizer.ReplacementFileNormalizer.TargetSurroundings;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class ReplacementFileNormalizerTest 
{
    @Test
    public void testReplacementNormalizer() throws Exception
    {
        testEmoticonReplacement(":-):-(", " lächeln  traurig ");
        //    testInternetslangReplacement("AKA hdl AKA.", "Also known as Hab' dich lieb Also known as.");
    }

    public void testEmoticonReplacement(String input, String output) throws Exception 
    {
        AnalysisEngineDescription replace = createEngineDescription(
            ReplacementFileNormalizer.class,
            ReplacementFileNormalizer.PARAM_MODEL_LOCATION, "src/main/resources/replaceLists/emoticons_de.txt",
            ReplacementFileNormalizer.PARAM_TARGET_SURROUNDINGS, TargetSurroundings.WHITESPACE);
    
        AggregateBuilder ab = new AggregateBuilder();
        ab.add(replace);
        ab.add(createEngineDescription(ApplyChangesAnnotator.class),
                ApplyChangesAnnotator.VIEW_SOURCE, CAS.NAME_DEFAULT_SOFA, 
                ApplyChangesAnnotator.VIEW_TARGET, "view1");
    
        AnalysisEngine engine = ab.createAggregate();
        JCas jcas = engine.newJCas();
        jcas.setDocumentText(input);    
        DocumentMetaData.create(jcas);
        engine.process(jcas);
    
        JCas view0 = jcas.getView(CAS.NAME_DEFAULT_SOFA);
        JCas view1 = jcas.getView("view1");
    
        System.out.println(view0.getDocumentText());
        System.out.println(view1.getDocumentText());
    
        assertEquals(output, view1.getDocumentText());
    }

    public void testInternetslangReplacement(String input, String output) throws Exception
    {
        AnalysisEngineDescription replace = createEngineDescription(
            ReplacementFileNormalizer.class,
            ReplacementFileNormalizer.PARAM_MODEL_LOCATION, "src/main/resources/replaceLists/internetslang.txt",
            ReplacementFileNormalizer.PARAM_SRC_SURROUNDINGS, SrcSurroundings.ONLY_ALPHANIMERIC);
    
        AggregateBuilder ab = new AggregateBuilder();
        ab.add(createEngineDescription(BreakIteratorSegmenter.class), 
                CAS.NAME_DEFAULT_SOFA, CAS.NAME_DEFAULT_SOFA);
        ab.add(replace);
        ab.add(createEngineDescription(    ApplyChangesAnnotator.class), 
            ApplyChangesAnnotator.VIEW_SOURCE, CAS.NAME_DEFAULT_SOFA, 
            ApplyChangesAnnotator.VIEW_TARGET, "view1");
    
        AnalysisEngine engine = ab.createAggregate();
        JCas jcas = engine.newJCas();
        jcas.setDocumentText(input);    
        DocumentMetaData.create(jcas);
        engine.process(jcas);
    
        JCas view0 = jcas.getView(CAS.NAME_DEFAULT_SOFA);
        JCas view1 = jcas.getView("view1");
    
        System.out.println(view0.getDocumentText());
        System.out.println(view1.getDocumentText());
    
        assertEquals(output, view1.getDocumentText());
    }
}
