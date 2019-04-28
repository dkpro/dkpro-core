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
package org.dkpro.core.api.transform;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;
import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.JCasIterator;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Div;

public class JCasTransformerChangeBasedTest
{
    @Test
    public void test()
        throws Exception
    {
        String inputText = "aXa";
        String normalizedText = "aYYYa";

        JCas input = JCasFactory.createJCas();
        input.setDocumentText(inputText);
        new Div(input, 1,2).addToIndexes();
        
        AnalysisEngineDescription aae = createEngineDescription(
                createEngineDescription(TestTransformer.class,
                        TestTransformer.PARAM_TYPES_TO_COPY, Div.class));
        aae.getAnalysisEngineMetaData().getOperationalProperties().setOutputsNewCASes(true);
        AnalysisEngine normalizer = createEngine(aae);
        
        JCasIterator iterator = normalizer.processAndOutputNewCASes(input);
        JCas output = iterator.next();
        
        assertEquals(normalizedText, output.getDocumentText());
        assertEquals(1, selectSingle(output, Div.class).getBegin());
        assertEquals(4, selectSingle(output, Div.class).getEnd());
    }
    
    public static class TestTransformer
        extends JCasTransformerChangeBased_ImplBase
    {
        @Override
        public void process(JCas aInput, JCas aOutput)
            throws AnalysisEngineProcessException
        {
            Pattern p = Pattern.compile("(?<LEFT>.*)X(?<RIGHT>.*)");
            Matcher m = p.matcher(aInput.getDocumentText());
            while (m.find()) {
                replace(m.end("LEFT"), m.start("RIGHT"), "YYY");
            }
        }
    }
}
