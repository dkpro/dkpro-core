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
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;
import static org.uimafit.util.JCasUtil.iterate;

import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public
class TreeTaggerPosLemmaTT4JTest
{
	{
		System.setProperty(TreeTaggerWrapper.class.getName()+".TRACE", "true");
	}

    @Test
    public
    void treeTaggerAnnotatorEnglishTest()
    throws Exception
    {
        runEnglishTest();
        runEnglishTest2();
    }

    @Test
    public
    void treeTaggerAnnotatorGermanTest()
    throws Exception
    {
        runGermanTest();
    }

    private
    void runEnglishTest()
    throws Exception
    {
        runTest(
        		"This is a test .",
        		new String[] { "this", "be",  "a",   "test", "."    },
        		new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" },
        		new String[] { "ART",  "V",   "ART", "NN",   "PUNC" },
        		"en");
    }

    /**
     * Generate a very large document and test it.
     */
    @Test
    @Ignore("This test requires at least 1 GB memory. Start Java with -Xmx1024m")
    public
    void hugeDocumentTest()
    throws Exception
    {
        String testString = "This is a test . ";
        int reps = 5000000 / testString.length();
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < reps; i++) {
    		sb.append(testString);
    	}

        JCas jcas = runTest(sb.toString(), null, null, null, "en");

        FSIndex posIndex = jcas.getAnnotationIndex(POS.type);
        assertEquals(reps * 5, posIndex.size());

        // test POS annotations
		String[] tags = new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" };
		String[] tagClasses = new String[] { "ART",  "V",   "ART", "NN",   "PUNC" };

        FSIterator posIter = posIndex.iterator();
        int i = 0;
        while (posIter.hasNext()) {
            POS posAnnotation = (POS) posIter.next();
            assertEquals("In position "+i, tagClasses[i%5], posAnnotation.getType().getShortName());
            assertEquals("In position "+i, tags[i%5], posAnnotation.getPosValue());
            i++;
        }

        System.out.println("Successfully tagged document of size: "+sb.length());
    }

    /**
     * Test using the same AnalysisEngine multiple times.
     */
    @Test
//    @Ignore("This test fails with the non-TT4J annotator")
    public
    void multiDocumentTest()
    throws Exception
    {
    	checkModelsAndBinary("en");

		String testDocument = "This is a test .";
		String[] lemmas     = new String[] { "this", "be",  "a",   "test", "."    };
		String[] tags       = new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" };
		String[] tagClasses = new String[] { "ART",  "V",   "ART", "NN",   "PUNC" };

        AnalysisEngine engine = createPrimitive(TreeTaggerPosLemmaTT4J.class,
        		createTypeSystemDescription());

		try {
			for (int n = 0; n < 100; n++) {
		        JCas aJCas = engine.newJCas();
		        aJCas.setDocumentLanguage("en");
		        TokenBuilder<Token, Annotation> tb = new TokenBuilder<Token, Annotation>(Token.class, Annotation.class);
		        tb.buildTokens(aJCas, testDocument);
		        engine.process(aJCas);

		        // test POS annotations
		        if (tagClasses != null && tags != null) {
			        FSIndex posIndex = aJCas.getAnnotationIndex(POS.type);
			        FSIterator posIter = posIndex.iterator();
			        int i = 0;
			        while (posIter.hasNext()) {
			            POS posAnnotation = (POS) posIter.next();
			            assertEquals("In position "+i, tagClasses[i], posAnnotation.getType().getShortName());
			            assertEquals("In position "+i, tags[i], posAnnotation.getPosValue());
			            i++;
			        }
		        }

		        // test Lemma annotations
		        if (lemmas != null) {
			        FSIndex lemmaIndex = aJCas.getAnnotationIndex(Lemma.type);
			        FSIterator lemmaIter = lemmaIndex.iterator();
			        int j = 0;
			        while (lemmaIter.hasNext()) {
			            Lemma lemmaAnnotation = (Lemma) lemmaIter.next();
			            assertEquals("In position "+j, lemmas[j], lemmaAnnotation.getValue());
			            j++;
			        }
		        }
			}
		}
		finally {
			engine.destroy();
		}
    }

    /**
     * Run the {@link #hugeDocumentTest()} 100 times.
     */
    @Test
    @Ignore("This test takes a very long time. Only include it if you need to "+
    		"test the stability of the annotator")
    public
    void loadTest()
    throws Exception
    {
    	for (int i = 0; i < 100; i++) {
    		System.out.println("Load test iteration "+i);
    		hugeDocumentTest();
    	}
    }

    @Test
    @Ignore("Platform specific")
    public
    void testOddCharacters()
    throws Exception
    {
        runTest(
        		"² § ¶ § °",
        		new String[] { "²",  "§",   "¶",  "§",   "°"   },
        		new String[] { "NN", "SYM", "NN", "SYM", "SYM" },
        		new String[] { "NN", "O",   "NN", "O",   "O"   },
        		"en");
    }

    private
    void runEnglishTest2()
    throws Exception
    {
        runTest(
        		"A neural net .",
        		new String[] { "a",   "neural", "net", "."    },
        		new String[] { "DT",  "JJ",     "NN",  "SENT" },
        		new String[] { "ART", "ADJ",    "NN",  "PUNC" },
        		"en");
    }

    private
    void runGermanTest()
    throws Exception
    {
        runTest(
        		"Das ist ein Test .",
        		new String[] { "d",   "sein",  "ein", "Test", "."    },
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."    },
        		new String[] { "PR",  "V",     "ART", "NN",   "PUNC" },
        		"de");
    }

    private
    void checkModelsAndBinary(String lang)
    {
		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/lib/" + lang + "-tagger-little-endian.par") != null);

		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/bin/LICENSE.txt") != null);
    }

    private
    JCas runTest(
    		String testDocument,
    		String[] lemmas,
    		String[] tags,
    		String[] tagClasses,
    		String language)
    throws Exception
    {
    	checkModelsAndBinary(language);

        AnalysisEngine engine = createPrimitive(TreeTaggerPosLemmaTT4J.class,
        		createTypeSystemDescription());

        JCas aJCas = engine.newJCas();
        aJCas.setDocumentLanguage(language);

        TokenBuilder<Token, Annotation> tb = new TokenBuilder<Token, Annotation>(Token.class, Annotation.class);
        tb.buildTokens(aJCas, testDocument);

        engine.process(aJCas);

        // test POS annotations
        if (tagClasses != null && tags != null) {
	        int i = 0;
	        for (POS posAnnotation : iterate(aJCas, POS.class)) {
	            assertEquals("In position "+i, tagClasses[i], posAnnotation.getType().getShortName());
	            assertEquals("In position "+i, tags[i], posAnnotation.getPosValue());
	            i++;
	        }
        }

        // test Lemma annotations
        if (lemmas != null) {
	        FSIndex lemmaIndex = aJCas.getAnnotationIndex(Lemma.type);
	        FSIterator lemmaIter = lemmaIndex.iterator();
	        int j = 0;
	        while (lemmaIter.hasNext()) {
	            Lemma lemmaAnnotation = (Lemma) lemmaIter.next();
	            assertEquals("In position "+j, lemmas[j], lemmaAnnotation.getValue());
	            j++;
	        }
        }

        return aJCas;
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
}
