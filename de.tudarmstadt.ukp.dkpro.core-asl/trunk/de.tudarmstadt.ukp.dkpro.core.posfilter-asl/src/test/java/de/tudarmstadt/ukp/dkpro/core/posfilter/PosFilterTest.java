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
package de.tudarmstadt.ukp.dkpro.core.posfilter;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class PosFilterTest
{
	@Test
	public void testEnglish1()
		throws Exception
	{
		String testDocument = "This is a not so long test sentence . This is a longer second " +
				"test sentence . More sentences are necessary for the tests .";

		String[] tokens = new String[] { "long", "test", "sentence", "second", "test", "sentence",
				"More", "sentences", "necessary", "tests" };

		runTest("en", testDocument, tokens, null, null, null, null, PosFilter.PARAM_TYPE_TO_REMOVE, Token.class.getName(), PosFilter.PARAM_ADJ, true, PosFilter.PARAM_N, true);
	}

    @Test
    public void testEnglish2()
        throws Exception
    {
        String testDocument = "This is a not so long test sentence . This is a longer second " +
                "test sentence . More sentences are necessary for the tests .";

        String[] tokens = new String[] { "is", "long", "test", "sentence", "is", "second", "test",
                "sentence", "More", "sentences", "are", "necessary", "tests" };

        runTest("en", testDocument, tokens, null, null, null, null, PosFilter.PARAM_TYPE_TO_REMOVE, Token.class.getName(), PosFilter.PARAM_ADJ, true, PosFilter.PARAM_N, true,
                PosFilter.PARAM_V, true);
    }

    @Test
    public void testEnglish3()
        throws Exception
    {
        String testDocument = "This is a not so long test sentence .";

        String[] tokens = new String[] { "This", "is", "a", "not", "so", "long", "test", "sentence", "." };
        String[] lemmas = new String[] { "be", "long", "test", "sentence" };

        runTest("en", testDocument, tokens, lemmas, null, null, null, PosFilter.PARAM_TYPE_TO_REMOVE, Lemma.class.getName(), PosFilter.PARAM_ADJ, true, PosFilter.PARAM_N, true,
                PosFilter.PARAM_V, true);
    }

    @Test
    public void testEnglish4()
        throws Exception
    {
        String testDocument = "This is a not so long test sentence .";

        String[] tokens = new String[] { "This", "is", "a", "not", "so", "long", "test", "sentence", "." };
        String[] pos = new String[] { "VBZ", "JJ", "NN", "NN" };
        String[] originalPos = new String[] { "V", "ADJ", "NN", "NN" };

        runTest("en", testDocument, tokens, null, null, pos, originalPos, PosFilter.PARAM_TYPE_TO_REMOVE, POS.class.getName(), PosFilter.PARAM_ADJ, true, PosFilter.PARAM_N, true,
                PosFilter.PARAM_V, true);
    }

	private void runTest(String language, String testDocument,
	        String[] aTokens, String[] aLemmas, String[] aStems, String[] aPOSs, String[] aOrigPOSs, Object... aExtraParams)
		throws Exception
	{
		List<Object> posFilterParams = new ArrayList<Object>();
		posFilterParams.addAll(asList(aExtraParams));

		AnalysisEngineDescription aggregate = createEngineDescription(
		        createEngineDescription(OpenNlpPosTagger.class),
		        createEngineDescription(SnowballStemmer.class),
		        createEngineDescription(MateLemmatizer.class),
				createEngineDescription(PosFilter.class,
						posFilterParams.toArray(new Object[posFilterParams.size()])));

		JCas jcas = TestRunner.runTest(aggregate, language, testDocument);

        Type stemType = jcas.getCas().getTypeSystem().getType(Stem.class.getCanonicalName());
        Type lemmaType = jcas.getCas().getTypeSystem().getType(Lemma.class.getCanonicalName());
        Type posType = jcas.getCas().getTypeSystem().getType(POS.class.getCanonicalName());
		Collection<Token> tokens = select(jcas, Token.class);
        AssertAnnotations.assertToken(aTokens, tokens);
        if (aLemmas != null) {
            AssertAnnotations.assertLemma(aLemmas, select(jcas, Lemma.class));
            for (Token t : tokens) {
                Assert.assertEquals(t.getLemma(), getAnnotation(lemmaType, t));
            }
        }
        if (aStems != null) {
            AssertAnnotations.assertStem(aStems, select(jcas, Stem.class));
            for (Token t : tokens) {
                Assert.assertEquals(t.getStem(), getAnnotation(stemType, t));
            }
        }
        if (aPOSs != null) {
            AssertAnnotations.assertPOS(aOrigPOSs, aPOSs, select(jcas, POS.class));
            for (Token t : tokens) {
                Assert.assertEquals(t.getPos(), getAnnotation(posType, t));
            }
        }
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}

    private AnnotationFS getAnnotation(Type type, AnnotationFS annotation)
    {
        List<AnnotationFS> annotations = CasUtil.selectCovered(type, annotation);

        if (annotations.size() != 1) {
            return null;
        }

        return annotations.get(0);
    }
}
