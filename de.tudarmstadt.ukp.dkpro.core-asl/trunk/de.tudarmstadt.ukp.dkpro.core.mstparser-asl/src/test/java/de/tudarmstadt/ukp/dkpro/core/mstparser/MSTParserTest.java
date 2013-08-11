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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
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
	static final String documentEnglish = "We need a very complicated example sentence , which " +
            "contains as many constituents and dependencies as possible .";

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

		String[] dependencies = new String[] { 
		        "Dependency(ADJP)[102,110] D(possible)[102,110] G(as)[99,101]",
		        "Dependency(DEP)[10,14] D(very)[10,14] G(complicated)[15,26]",
		        "Dependency(DEP)[111,112] D(.)[111,112] G(need)[3,7]",
		        "Dependency(DEP)[15,26] D(complicated)[15,26] G(sentence)[35,43]",
		        "Dependency(DEP)[27,34] D(example)[27,34] G(sentence)[35,43]",
		        "Dependency(DEP)[44,45] D(,)[44,45] G(need)[3,7]",
		        "Dependency(DEP)[64,68] D(many)[64,68] G(constituents)[69,81]",
		        "Dependency(DEP)[8,9] D(a)[8,9] G(sentence)[35,43]",
		        "Dependency(DEP)[82,85] D(and)[82,85] G(dependencies)[86,98]",
		        "Dependency(NP)[69,81] D(constituents)[69,81] G(as)[61,63]",
		        "Dependency(NP)[86,98] D(dependencies)[86,98] G(as)[61,63]",
		        "Dependency(NP-OBJ)[35,43] D(sentence)[35,43] G(need)[3,7]",
		        "Dependency(NP-SBJ)[0,2] D(We)[0,2] G(need)[3,7]",
		        "Dependency(PP)[61,63] D(as)[61,63] G(contains)[52,60]",
		        "Dependency(PP)[99,101] D(as)[99,101] G(dependencies)[86,98]",
		        "Dependency(ROOT)[3,7] D(need)[3,7] G(need)[3,7]",
		        "Dependency(S)[52,60] D(contains)[52,60] G(which)[46,51]",
		        "Dependency(SBAR)[46,51] D(which)[46,51] G(need)[3,7]"};

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
		AnalysisEngineDescription aggregate = createEngineDescription(
				createEngineDescription(BreakIteratorSegmenter.class),
				createEngineDescription(OpenNlpPosTagger.class),
				createEngineDescription(MSTParser.class, MSTParser.PARAM_PRINT_TAGSET, true));

		AnalysisEngine engine = createEngine(aggregate);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText(aText);
		engine.process(jcas);

		return jcas;
	}
}