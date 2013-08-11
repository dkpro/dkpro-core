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
package de.tudarmstadt.ukp.dkpro.core.mstparser;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * @author beinborn
 * @author zesch
 */
public class MSTParserTest
{
	static final String documentEnglish = "This is an easy example sentence";

	/**
	 * This method runs the MSTParser for an example sentence and checks if it returns the correct
	 * annotations. An annotation consists of: dependency type, begin of dependency, end of
	 * dependency, begin of the head, end of the head
	 *
	 * @throws Exception
	 */
	@Test
	public void testEnglish()
		throws Exception
	{
		JCas jcas = runTestEnglish(documentEnglish);

		String[] dependencies = new String[] { "NP-SBJ 5,7,0,4", "ROOT 5,7,5,7", "DEP 24,32,8,10",
				"DEP 24,32,11,15", "DEP 24,32,16,23", "NP-PRD 5,7,24,32" };

		AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));

	}

	/**
	 * Generates a JCas from the input text and annotates it with dependencies.
	 *
	 * @param aText
	 * @return jcas annotated with dependency relations
	 * @throws Exception
	 */
	private JCas runTestEnglish(String aText)
		throws Exception
	{
		AnalysisEngineDescription aggregate = createAggregateDescription(
				createEngineDescription(BreakIteratorSegmenter.class),
				createEngineDescription(OpenNlpPosTagger.class),
				createEngineDescription(MSTParser.class, MSTParser.PARAM_PRINT_TAGSET, true));

		AnalysisEngine engine = createPrimitive(aggregate);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText(aText);
		engine.process(jcas);

		return jcas;
	}
}