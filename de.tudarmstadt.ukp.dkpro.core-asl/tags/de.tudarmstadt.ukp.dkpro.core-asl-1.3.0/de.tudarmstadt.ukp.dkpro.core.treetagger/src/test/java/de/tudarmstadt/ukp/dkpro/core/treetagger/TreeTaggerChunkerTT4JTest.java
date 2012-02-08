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
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Assume;
import org.junit.Test;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public
class TreeTaggerChunkerTT4JTest
{
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
        		new String[] { "NC",   "VC",  "NC",          "O" },
        		new String[] { "NC",   "VC",  "NC",          "O" },
        		"en");
    }

    private
    void runEnglishTest2()
    throws Exception
    {
        runTest(
        		"A neural-net .",
        		new String[] { "a",   "neural", "-", "net", "." },
        		new String[] { "NC",                        "O" },
        		new String[] { "NC",                        "O" },
        		"en");
    }

    private
    void runGermanTest()
    throws Exception
    {
        runTest(
        		"Das ist ein Test .",
        		new String[] { "d",   "sein",  "ein", "Test", "." },
        		new String[] { "NC",  "VC",    "NC",          "0" },
        		new String[] { "NC",  "VC",    "NC",          "O" },
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

	private JCas runTest(String testDocument, String[] lemmas, String[] tags, String[] tagClasses,
			String language)
		throws Exception
	{
    	checkModelsAndBinary(language);

		AnalysisEngine tagger = createPrimitive(TreeTaggerPosLemmaTT4J.class);
        AnalysisEngine chunker = createPrimitive(TreeTaggerChunkerTT4J.class);

        JCas aJCas = CasCreationUtils.createCas(createTypeSystemDescription(), null, null).getJCas();
        aJCas.setDocumentLanguage(language);

        TokenBuilder<Token, Annotation> tb = TokenBuilder.create(Token.class, Annotation.class);
        tb.buildTokens(aJCas, testDocument);

        tagger.process(aJCas);
        chunker.process(aJCas);

        // test Chunk annotations
        if (tagClasses != null && tags != null) {
	        int i = 0;
	        for (Chunk posAnnotation : select(aJCas, Chunk.class)) {
	        	System.out.println(posAnnotation.getChunkValue()+": ["+posAnnotation.getCoveredText()+"]");
	            assertEquals("In position "+i, tagClasses[i], posAnnotation.getType().getShortName());
	            assertEquals("In position "+i, tags[i], posAnnotation.getChunkValue());
	            i++;
	        }
	        assertEquals(tags.length, i);
        }

        return aJCas;
    }
}
