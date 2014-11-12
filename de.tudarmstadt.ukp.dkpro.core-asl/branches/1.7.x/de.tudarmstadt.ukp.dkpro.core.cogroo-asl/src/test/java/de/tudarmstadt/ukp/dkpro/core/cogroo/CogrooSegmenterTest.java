/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.cogroo;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class CogrooSegmenterTest
{
    @Test
    public void testPortuguese() throws Exception
    {
        final String text = "Este é um teste. E mais um.";
        final String[] sentences = new String[] { "Este é um teste.", "E mais um." };
        final String[] tokens = new String[] { "Este", "é", "um", "teste", ".", "E", "mais",
                "um", "." };
        
        JCas jcas = runTest("pt-BR", text);
        
        assertSentence(sentences, select(jcas, Sentence.class));
        assertToken(tokens, select(jcas, Token.class));
    }

    private JCas runTest(String aLanguage, String aDocument)
        throws Exception
    {
        AnalysisEngine engine = createEngine(CogrooSegmenter.class);
        
        JCas jcas = engine.newJCas();
        jcas.setDocumentText(aDocument);
        jcas.setDocumentLanguage(aLanguage);
        
        engine.process(jcas);
        
        return jcas;
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
