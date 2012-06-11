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

import static org.apache.commons.lang.StringUtils.repeat;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;

import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.uimafit.factory.JCasBuilder;
import org.uimafit.testing.util.HideOutput;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public
class TreeTaggerPosLemmaTT4JTest
{
	@Before
	public void initTrace()
	{
		// TreeTaggerWrapper.TRACE = true;
	}

	@Test
	public void treeTaggerAnnotatorEnglishTest()
		throws Exception
	{
        runTest("en", "This is a test .",
				new String[] { "this", "be",  "a",   "test", "."    },
				new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" },
				new String[] { "ART",  "V",   "ART", "NN",   "PUNC" });

        runTest("en", "A neural net .",
        		new String[] { "a",   "neural", "net", "."    },
        		new String[] { "DT",  "JJ",     "NN",  "SENT" },
        		new String[] { "ART", "ADJ",    "NN",  "PUNC" });

        runTest("en", "John is purchasing oranges .",
        		new String[] { "John", "be",  "purchase", "orange", "."    },
        		new String[] { "NP",   "VBZ", "VVG",      "NNS",    "SENT" },
        		new String[] { "NP",   "V",   "V",        "NN",     "PUNC" });

        // TT4J per default runs TreeTagger with the -sgml option, so XML tags are not tagged
        runTest("en", "My homepage is <url> http://null.dummy </url> .",
        		new String[] { "my", "homepage", "be", "http://null.dummy", "." },
        		new String[] { "PP$", "NN", "VBZ", "JJ", "SENT" },
        		new String[] { "PR", "NN", "V", "ADJ", "PUNC" });
	}

	@Test
	public void treeTaggerAnnotatorGermanTest()
		throws Exception
    {
        runTest("de", "Das ist ein Test .",
        		new String[] { "die", "sein",  "eine", "Test", "."   },
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."   },
        		new String[] { "PR",  "V",     "ART", "NN",   "PUNC" });
    }

	@Test
	public void treeTaggerAnnotatorSloveneTest()
		throws Exception
    {
        runTest("sl", "To je test .",
        		new String[] { "ta",          "biti",      "test",  "." },
        		new String[] { "zk-sei----s", "gvpste--n", "somei", "SENT" },
        		new String[] { "O",           "O",         "O",     "O" });

        runTest("sl", "Gremo na Češko za kosilo .",
        		new String[] { "iti",             "na",   "Češko", "za",   "kosilo", "." },
        		new String[] { "gppspm--n-----d", "dpet", "slmei", "dpet", "soset",  "SENT" },
        		new String[] { "O",               "O",    "O",     "O",    "O",      "O" });
    }

    @Test
    public
    void treeTaggerAnnotatorChineseTest()
    	throws Exception
    {
        runTest("zh", "尾 舵 常 处于 风轮 后面 的 尾流 区里 .",
        		new String[] { "_",  "_", "_",  "_", "风轮", "_", "_", "_", "_",  "_" },
        		new String[] { "ng", "n", "d",  "v", "n",   "f", "u", "n", "nl", "w" },
        		new String[] { "O",  "O", "O",  "O", "O",   "O", "O", "O", "O",  "O" } );
    }

	@Test
//	@Ignore("Platform specific")
	public void testOddCharacters()
		throws Exception
    {
        runTest("en", "² § ¶ § °",
        		new String[] { "²",  "§",    "¶",  "§",    "°"   },
        		new String[] { "NN", "SYM",  "NN", "SYM",  "SYM" },
        		new String[] { "NN", "PUNC", "NN", "PUNC", "PUNC"   });
    }

	/**
	 * Generate a very large document and test it.
	 */
	@Test
	public void hugeDocumentTest()
		throws Exception
	{
		// Start Java with -Xmx512m
		boolean run = Runtime.getRuntime().maxMemory() > (500000000);
		if (!run) {
			System.out.println("Test requires more heap than available, skipping");
		}
		Assume.assumeTrue(run);

		// Disable trace as this significantly slows down the test
		TreeTaggerWrapper.TRACE = false;

		String text = "This is a test .";
		int reps = 4000000 / text.length();
        String testString = repeat(text, " ", reps);

        JCas jcas = runTest("en", testString, null, null, null);
    	List<POS> actualTags = new ArrayList<POS>(select(jcas, POS.class));
        assertEquals(reps * 5, actualTags.size());

        // test POS annotations
		String[] expectedTags = new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" };
		String[] expectedTagClasses = new String[] { "ART",  "V",   "ART", "NN",   "PUNC" };

		for (int i = 0; i < actualTags.size(); i++) {
            POS posAnnotation = actualTags.get(i);
            assertEquals("In position "+i, expectedTagClasses[i%5], posAnnotation.getType().getShortName());
            assertEquals("In position "+i, expectedTags[i%5], posAnnotation.getPosValue());
		}

        System.out.println("Successfully tagged document with " + testString.length() +
        		" characters");
    }

	/**
	 * Test using the same AnalysisEngine multiple times.
	 */
	@Test
	public void multiDocumentTest()
		throws Exception
	{
    	checkModelsAndBinary("en");

		String testDocument = "This is a test .";
		String[] lemmas     = new String[] { "this", "be",  "a",   "test", "."    };
		String[] tags       = new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" };
		String[] tagClasses = new String[] { "ART",  "V",   "ART", "NN",   "PUNC" };

        AnalysisEngine engine = createPrimitive(TreeTaggerPosLemmaTT4J.class);

        HideOutput hideOut = new HideOutput();
		try {
			
			for (int n = 0; n < 100; n++) {
		        JCas aJCas = TestRunner.runTest(engine, "en", testDocument);
		        
		        AssertAnnotations.assertPOS(tagClasses, tags, select(aJCas, POS.class));
		        AssertAnnotations.assertLemma(lemmas, select(aJCas, Lemma.class));
			}
		}
		finally {
			engine.destroy();
			hideOut.restoreOutput();
		}
    }

    /**
     * Run the {@link #hugeDocumentTest()} 100 times.
     */
    @Test
    @Ignore("This test takes a very long time. Only include it if you need to "+
    		"test the stability of the annotator")
	public void loadTest()
		throws Exception
	{
		for (int i = 0; i < 100; i++) {
			System.out.println("Load test iteration " + i);
			hugeDocumentTest();
		}
	}

	private void checkModelsAndBinary(String lang)
	{
		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/lib/" + lang
						+ "-tagger-little-endian.par") != null);

		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/bin/LICENSE.txt") != null);
	}

	private JCas runTest(String language, String testDocument, String[] lemmas, String[] tags,
			String[] tagClasses)
		throws Exception
	{
		checkModelsAndBinary(language);
		
        AnalysisEngine engine = createPrimitive(TreeTaggerPosLemmaTT4J.class);

        JCas aJCas = TestRunner.runTest(engine, language, testDocument);
        
        AssertAnnotations.assertLemma(lemmas, select(aJCas, Lemma.class));
        AssertAnnotations.assertPOS(tagClasses, tags, select(aJCas, POS.class));

        return aJCas;
    }

	/**
	 * Test using the same AnalysisEngine multiple times.
	 */
	@Test
	public void longTokenTest()
		throws Exception
	{
    	checkModelsAndBinary("en");

        AnalysisEngine engine = createPrimitive(TreeTaggerPosLemmaTT4J.class);
        JCas jcas = engine.newJCas();

		try {
			for (int n = 99990; n < 100000; n ++) {
				System.out.println(n);
		        jcas.setDocumentLanguage("en");
		        JCasBuilder builder = new JCasBuilder(jcas);
		        builder.add("Start", Token.class);
		        builder.add("with", Token.class);
		        builder.add("good", Token.class);
		        builder.add("tokens", Token.class);
		        builder.add(".", Token.class);
		        builder.add(StringUtils.repeat("b", n), Token.class);
		        builder.add("End", Token.class);
		        builder.add("with", Token.class);
		        builder.add("some", Token.class);
		        builder.add("good", Token.class);
		        builder.add("tokens", Token.class);
		        builder.add(".", Token.class);
		        builder.close();
		        engine.process(jcas);
		        jcas.reset();
			}
		}
		finally {
			engine.destroy();
		}
    }

    /**
     * Runs a small pipeline on a text containing quite odd characters such as
     * Unicode LEFT-TO-RIGHT-MARKs. The BreakIteratorSegmenter creates tokens from these
     * which are send to TreeTagger as tokens containing line breaks or only
     * whitespace. TreeTaggerPosLemmaTT4J has to filter these tokens before
     * they reach the TreeTaggerWrapper.
     */
//    @Test
//    public
//    void testStrangeDocument()
//    throws Exception
//    {
//		CollectionReader reader = createCollectionReader(
//				FileSystemReader.class,
//				createTypeSystemDescription(),
//				FileSystemReader.PARAM_INPUTDIR, getTestResource(
//						"test_files/annotator/TreeTaggerPosLemmaTT4J/strange"));
//
//		AnalysisEngine sentenceSplitter = createPrimitive(
//				BreakIteratorSegmenter.class,
//				tsd);
//
//		AnalysisEngine tt = createPrimitive(TreeTaggerPosLemmaTT4J.class, tsd,
//				TreeTaggerTT4JBase.PARAM_LANGUAGE_CODE, "en");
//
//		runPipeline(reader, sentenceSplitter, tt);
//    }

//    @Test
//    @Ignore("This test should fail, however - due to fixes in the Tokenizer, " +
//    		"we can currently not provokate a failure with the given 'strange' " +
//    		"document.")
//    public
//    void testStrangeDocumentFail()
//    throws Exception
//    {
//		CollectionReader reader = createCollectionReader(
//				FileSystemReader.class,
//				createTypeSystemDescription(),
//				FileSystemReader.PARAM_INPUTDIR, getTestResource(
//						"test_files/annotator/TreeTaggerPosLemmaTT4J/strange"));
//
//		AnalysisEngine sentenceSplitter = createPrimitive(
//				BreakIteratorSegmenter.class,
//				tsd);
//
//		AnalysisEngine tt = createPrimitive(TreeTaggerPosLemmaTT4J.class, tsd,
//				TreeTaggerTT4JBase.PARAM_LANGUAGE_CODE, "en",
//				TreeTaggerTT4JBase.PARAM_PERFORMANCE_MODE, true);
//
//		runPipeline(
//				reader,
//				sentenceSplitter,
//				tt);
//    }

    /**
     * When running this test, check manually if TreeTagger is restarted
     * between the documents. If you jank up the log levels, that should be
     * visible on the console. Unfortunately we cannot easily access the
     * restartCount of the TreeTaggerWrapper.
     */
//    @Test
//    public
//    void testRealMultiDocument()
//    throws Exception
//    {
//		CollectionReader reader = createCollectionReader(
//				FileSystemReader.class,
//				createTypeSystemDescription(),
//				FileSystemReader.PARAM_INPUTDIR, getTestResource(
//						"test_files/annotator/TreeTaggerPosLemmaTT4J/multiDoc"));
//
//		AnalysisEngine sentenceSplitter = createPrimitive(
//				BreakIteratorSegmenter.class,
//				tsd);
//
//		AnalysisEngine tt = createPrimitive(TreeTaggerPosLemmaTT4J.class, tsd,
//				TreeTaggerTT4JBase.PARAM_LANGUAGE_CODE, "en");
//
//		runPipeline(
//				reader,
//				sentenceSplitter,
//				tt);
//    }

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
