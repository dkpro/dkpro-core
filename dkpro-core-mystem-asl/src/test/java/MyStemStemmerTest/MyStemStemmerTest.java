package MyStemStemmerTest;
/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.jcas.JCas;
import org.dkpro.core.mystem.MyStemStemmer;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class MyStemStemmerTest {
	@Test
	public void testRussian() throws Exception {
		runTest("ru", "Не печатать исходные словоформы, только леммы и граммемы.", 
				new String[] { "не", "печатать", "исходный", "словоформа", "только", "лемма", "и", "граммема" });

	}

	private JCas runTest(String aLanguage, String aText, String[] aStems, Object... aParams) throws Exception {
		JCas result = TestRunner.runTest(createEngineDescription(MyStemStemmer.class, aParams), aLanguage, aText);

		AssertAnnotations.assertStem(aStems, select(result, Stem.class));

		return result;
	}

	@Rule
	public DkproTestContext testContext = new DkproTestContext();
}
