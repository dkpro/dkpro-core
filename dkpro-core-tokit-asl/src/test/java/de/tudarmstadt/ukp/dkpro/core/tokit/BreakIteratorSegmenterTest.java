/*
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.tokit;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;

public
class BreakIteratorSegmenterTest
{
	@Test
	public void run() throws Throwable
	{
		AnalysisEngineDescription aed = createEngineDescription(BreakIteratorSegmenter.class);

		SegmenterHarness.run(aed, "de.1", "de.4", "en.1", "en.2", "en.3", "en.6", "en.7", "en.9",
				"ar.1", "zh.1", "zh.2");
	}
	
    @Test
    public void testJapanese() throws Exception
    {
        JCas jcas = JCasFactory.createText("滧の べ滦榥榜ぶ 廤ま楺獣お 䨣みゅ騪", "ja");
        
        AnalysisEngine aed = createEngine(BreakIteratorSegmenter.class);
        aed.process(jcas);
        
        String[] tokens = { "滧", "の", "べ", "滦榥榜", "ぶ", "廤", "ま", "楺獣", "お", "䨣", "みゅ", "騪" };
        
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
	
    @Test
    public void testZoning() throws Exception
    {
        SegmenterHarness.testZoning(BreakIteratorSegmenter.class);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
