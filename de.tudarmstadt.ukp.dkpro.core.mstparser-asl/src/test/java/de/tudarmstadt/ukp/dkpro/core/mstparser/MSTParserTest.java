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
		        "[  0,  2]Dependency(NP-SBJ) D[0,2](We) G[3,7](need)",
		        "[  3,  7]Dependency(ROOT) D[3,7](need) G[3,7](need)",
		        "[  8,  9]Dependency(DEP) D[8,9](a) G[35,43](sentence)",
		        "[ 10, 14]Dependency(DEP) D[10,14](very) G[15,26](complicated)",
		        "[ 15, 26]Dependency(DEP) D[15,26](complicated) G[35,43](sentence)",
		        "[ 27, 34]Dependency(DEP) D[27,34](example) G[35,43](sentence)",
		        "[ 35, 43]Dependency(NP-OBJ) D[35,43](sentence) G[3,7](need)",
		        "[ 44, 45]Dependency(DEP) D[44,45](,) G[3,7](need)",
		        "[ 46, 51]Dependency(SBAR) D[46,51](which) G[3,7](need)",
		        "[ 52, 60]Dependency(S) D[52,60](contains) G[46,51](which)",
		        "[ 61, 63]Dependency(PP) D[61,63](as) G[52,60](contains)",
		        "[ 64, 68]Dependency(DEP) D[64,68](many) G[69,81](constituents)",
		        "[ 69, 81]Dependency(NP) D[69,81](constituents) G[61,63](as)",
		        "[ 82, 85]Dependency(DEP) D[82,85](and) G[86,98](dependencies)",
		        "[ 86, 98]Dependency(NP) D[86,98](dependencies) G[61,63](as)",
		        "[ 99,101]Dependency(PP) D[99,101](as) G[86,98](dependencies)",
		        "[102,110]Dependency(ADJP) D[102,110](possible) G[99,101](as)",
		        "[111,112]Dependency(DEP) D[111,112](.) G[3,7](need)"};

        String[] depTags = new String[] { "<no-type>", "ADJP", "ADVP", "CONJP", "DEP", "FRAG",
                "NAC", "NP", "NP-OBJ", "NP-PRD", "NP-SBJ", "NX", "PP", "PRN", "PRT", "QP", "ROOT",
                "S", "SBAR", "SINV", "SQ", "UCP", "VP", "WHNP" };

		AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(Dependency.class, null, depTags, jcas);
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