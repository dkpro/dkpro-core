/*******************************************************************************
 * Copyright 2014
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

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertChunks;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertTagset;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class TreeTaggerChunkerTT4JTest
{
    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
    }
    
    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", null, "We need a very complicated example sentence, which " +
                "contains as many constituents and dependencies as possible .");

        String[] chunks = new String[] { 
                "[  0,  2]NC(NC) (We)", 
                "[  3,  7]VC(VC) (need)",
                "[  8, 44]NC(NC) (a very complicated example sentence,)",
                "[ 45, 50]NC(NC) (which)", 
                "[ 51, 59]VC(VC) (contains)", 
                "[ 60, 62]PC(PC) (as)",
                "[ 63, 97]NC(NC) (many constituents and dependencies)", 
                "[ 98,100]PC(PC) (as)",
                "[101,109]ADJC(ADJC) (possible)", 
                "[110,111]O(O) (.)" };

        String[] chunkTags = new String[] { "ADJC", "ADVC", "CONJC", "INTJ", "LST", "NC", "O",
                "PC", "PRT", "SBAR", "VC", "that" };

        // String[] unmappedChunk = new String[] { "#", "$", "''", "-LRB-", "-RRB-", "``" };

        assertChunks(chunks, select(jcas, Chunk.class));
        assertTagset(Chunk.class, "tt", chunkTags, jcas);
        // FIXME assertTagsetMapping(Chunk.class, "conll2000", unmappedChunk, jcas);
    }

    @Test
    public void testGerman()
        throws Exception
    {
        JCas jcas = runTest("de", null, "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] chunks = new String[] { 
                "[  0,  3]NC(NC) (Wir)",
                "[  4, 12]VC(VC) (brauchen)",
                "[ 13, 44]NC(NC) (ein sehr kompliziertes Beispiel)",
                "[ 45, 46]O(0) (,)",
                "[ 47, 54]NC(NC) (welches)",
                "[ 55, 64]O(0) (möglichst)",
                "[ 65, 84]NC(NC) (viele Konstituenten)",
                "[ 85, 88]O(0) (und)",
                "[ 89,100]NC(NC) (Dependenzen)",
                "[101,111]VC(VC) (beinhaltet)",
                "[112,113]O(0) (.)" };

        String[] chunkTags = new String[] { "0", "NC", "PC", "VC" };

        // String[] unmappedChunk = new String[] { "#", "$", "''", "-LRB-", "-RRB-", "``" };

        assertChunks(chunks, select(jcas, Chunk.class));
        assertTagset(Chunk.class, "tt", chunkTags, jcas);
        // FIXME assertTagsetMapping(Chunk.class, "conll2000", unmappedChunk, jcas);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText)
        throws Exception
    {
        AnalysisEngineDescription tagger = createEngineDescription(TreeTaggerPosLemmaTT4J.class);

        AnalysisEngineDescription chunker = createEngineDescription(TreeTaggerChunkerTT4J.class,
                TreeTaggerChunkerTT4J.PARAM_VARIANT, aVariant, 
                TreeTaggerChunkerTT4J.PARAM_PRINT_TAGSET, true);
        
        AnalysisEngineDescription aggregate = createEngineDescription(tagger, chunker);
        
        return TestRunner.runTest(aggregate, aLanguage, aText);
    }
    
	private JCas runTest(String aLanguage, String aText, String[] aLemmas, String[] aTags,
			String[] aTagClasses)
		throws Exception
	{
		AnalysisEngine tagger = createEngine(TreeTaggerPosLemmaTT4J.class);
        AnalysisEngine chunker = createEngine(TreeTaggerChunkerTT4J.class,
        		TreeTaggerPosLemmaTT4J.PARAM_PRINT_TAGSET, true);

        JCas aJCas = JCasFactory.createJCas();
        aJCas.setDocumentLanguage(aLanguage);

        TokenBuilder<Token, Annotation> tb = TokenBuilder.create(Token.class, Annotation.class);
        tb.buildTokens(aJCas, aText);

        tagger.process(aJCas);
        chunker.process(aJCas);

        // test Chunk annotations
        if (aTagClasses != null && aTags != null) {
	        int i = 0;
	        for (Chunk posAnnotation : select(aJCas, Chunk.class)) {
	        	System.out.println(posAnnotation.getChunkValue()+": ["+posAnnotation.getCoveredText()+"]");
	            assertEquals("In position "+i, aTagClasses[i], posAnnotation.getType().getShortName());
	            assertEquals("In position "+i, aTags[i], posAnnotation.getChunkValue());
	            i++;
	        }
	        assertEquals(aTags.length, i);
        }

        return aJCas;
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
