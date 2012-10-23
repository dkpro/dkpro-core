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
package de.tudarmstadt.ukp.dkpro.core.tokit;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;



/**
 * @author Judith Eckle-Kohler
 * 
 */
public class GermanSeparatedParticleAnnotatorTest
{
	
	@Test
	public void testGermanSeparatedParticles()
		throws Exception
	{
        runTest("de", "Wir schlagen ein Treffen nächste Woche vor .",
        		new String[] { "wir", "vorschlagen", "eine", "Treffen", "nah", "Woche", "vor", "."    });
 
        runTest("de", "Wir können gleich anfangen .",
        		new String[] { "wir", "können", "gleich", "anfangen", "."    });

        runTest("de", "Fangen wir jetzt an ?",
        		new String[] { "anfangen", "wir", "jetzt", "an", "?"    });
        
	}

	private void runTest(String language, String testDocument, String[] lemmatizedDocument)
		throws Exception
	{
		
		AnalysisEngineDescription processor = createAggregateDescription(
				
				createPrimitiveDescription(TreeTaggerPosLemmaTT4J.class,
						TreeTaggerPosLemmaTT4J.PARAM_LANGUAGE_CODE, "de"),
						
				createPrimitiveDescription(GermanSeparatedParticleAnnotator.class)
		);
			
		AnalysisEngine engine = createPrimitive(processor);

		JCas jcas = TestRunner.runTest(engine, language, testDocument);

		AssertAnnotations.assertLemma(lemmatizedDocument, select(jcas, Lemma.class));
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}


}
