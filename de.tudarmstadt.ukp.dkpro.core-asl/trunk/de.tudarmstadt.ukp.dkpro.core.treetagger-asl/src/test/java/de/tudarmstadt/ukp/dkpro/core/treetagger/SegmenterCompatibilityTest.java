/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.treetagger;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public
class SegmenterCompatibilityTest
{
	@Before
	public void initTrace()
	{
		// TreeTaggerWrapper.TRACE = true;
	}

	@Test
	public void segmenterCompatibilityTest() throws Exception {
	
	    AnalysisEngineDescription desc = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class),
                createPrimitiveDescription(TreeTaggerPosLemmaTT4J.class)
        );
        AnalysisEngine engine = createPrimitive(desc);

        JCas aJCas = engine.newJCas();
        aJCas.setDocumentLanguage("en");
        aJCas.setDocumentText("Two cats sat on two mats.");
        engine.process(aJCas);

        checkLemma(new String[] { "Two", "cat", "sit", "on", "two", "mat", "." },  select(aJCas, Lemma.class));
    }

	private void checkLemma(String[] expected, Collection<Lemma> actual)
	{
        int i = 0;
        for (Lemma lemmaAnnotation : actual) {
            assertEquals("In position "+i, expected[i], lemmaAnnotation.getValue());
            i++;
        }
	}
}
