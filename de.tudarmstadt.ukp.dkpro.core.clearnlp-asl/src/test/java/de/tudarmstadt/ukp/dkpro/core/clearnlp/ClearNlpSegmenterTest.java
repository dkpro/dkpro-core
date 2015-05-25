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
package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;

public class ClearNlpSegmenterTest
{
	@Test
	public void run() throws Throwable
	{
		AnalysisEngineDescription aed = createEngineDescription(ClearNlpSegmenter.class);

        SegmenterHarness.run(aed, "de.1", "de.2", "de.3", "de.4", "en.1", "en.7", "en.9", "ar.1",
                "zh.1", "zh.2");
	}
	
	/**
	 * We had a bug where the token offsets were assigned wrong when one word was a suffix of the
	 * previous word.
	 */
    @Test
    public void testSuffix() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("this is is this is is");
        
        AnalysisEngine aed = createEngine(ClearNlpSegmenter.class);
        aed.process(jcas);
        
        
        List<Token> tokens = new ArrayList<>(select(jcas, Token.class));
        assertEquals(5, tokens.get(1).getBegin());
        assertEquals(7, tokens.get(1).getEnd());
        
        for (Token t : tokens) {
            System.out.printf("%d %d %s%n", t.getBegin(), t.getEnd(), t.getCoveredText());
        }

    }

    @Test
    public void testZoning() throws Exception
    {
        SegmenterHarness.testZoning(ClearNlpSegmenter.class);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
