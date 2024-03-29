/*
 * Copyright 2017
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
 */
package org.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.AssertAnnotations.assertChunks;
import static org.dkpro.core.testing.AssertAnnotations.assertTagset;
import static org.dkpro.core.testing.AssertAnnotations.assertTagsetMapping;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssumeResource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class OpenNlpChunkerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", null, "We need a very complicated example sentence, which " +
                "contains as many constituents and dependencies as possible.");

        String[] chunks = { 
                "[  0,  2]NC(NP) (We)",
                "[  3,  7]VC(VP) (need)",
                "[  8, 43]NC(NP) (a very complicated example sentence)",
                "[ 45, 50]NC(NP) (which)",
                "[ 51, 59]VC(VP) (contains)",
                "[ 60, 62]PC(PP) (as)",
                "[ 63, 97]NC(NP) (many constituents and dependencies)",
                "[ 98,100]PC(PP) (as)",
                "[101,109]ADJC(ADJP) (possible)" };

        String[] chunkTags = { "ADJP", "ADVP", "CONJP", "INTJ", "LST", "NP", "PP", "PRT", "SBAR",
                "UCP", "VP" };

        String[] unmappedChunk = {};

        assertChunks(chunks, select(jcas, Chunk.class));
        assertTagset(Chunk.class, "conll2000", chunkTags, jcas);
        assertTagsetMapping(Chunk.class, "conll2000", unmappedChunk, jcas);
    }

    @Test
    public void testEnglishIxa()
        throws Exception
    {
        JCas jcas = runTest("en", "perceptron-ixa", "We need a very complicated example sentence, "
                + "which contains as many constituents and dependencies as possible.");

        String[] chunks = { 
                "[  0,  2]NC(NP) (We)",
                "[  3,  7]VC(VP) (need)",
                "[  8, 43]NC(NP) (a very complicated example sentence)",
                "[ 45, 50]NC(NP) (which)",
                "[ 51, 59]VC(VP) (contains)",
                "[ 60, 62]O(SBAR) (as)",
                "[ 63, 97]NC(NP) (many constituents and dependencies)",
                "[ 98,100]PC(PP) (as)",
                "[101,109]ADJC(ADJP) (possible)" };

        String[] chunkTags = { "ADJP", "ADVP", "CONJP", "INTJ", "LST", "NP", "PP", "PRT", "SBAR",
                "UCP", "VP" };

        String[] unmappedChunk = {};

        assertChunks(chunks, select(jcas, Chunk.class));
        assertTagset(Chunk.class, "conll2000", chunkTags, jcas);
        assertTagsetMapping(Chunk.class, "conll2000", unmappedChunk, jcas);
    }    
    @Disabled("We don't have these models integrated yet")
    @Test
    public void testPortuguese()
        throws Exception
    {
        JCas jcas = runTest("pt", "cogroo", "Precisamos de uma frase exemplo muito complicado, que "
                + "contém o maior número de eleitores e dependências possível.");

        String[] chunks = new String[] { 
                "[  0, 43]Chunk(NP) (We need a very complicated example sentence)",
                "[ 43, 44]Chunk(O) (,)",
                "[ 45,109]Chunk(NP) (which contains as many constituents and dependencies as possible)",
                "[109,110]Chunk(O) (.)" };

        String[] chunkTags = new String[] { "ADJP", "ADVP", "CONJP", "INTJ", "LST", "NP", "O",
                "PP", "PRT", "SBAR", "UCP", "VP" };

        // String[] unmappedChunk = new String[] { "#", "$", "''", "-LRB-", "-RRB-", "``" };

        assertChunks(chunks, select(jcas, Chunk.class));
        assertTagset(Chunk.class, "conll2000", chunkTags, jcas);
        // FIXME assertTagsetMapping(Chunk.class, "conll2000", unmappedChunk, jcas);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText)
        throws Exception
    {
        String variant = aVariant != null ? aVariant : "default";
        
        AssumeResource.assumeResource(OpenNlpChunker.class, "chunker", aLanguage, variant);

        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class);

        AnalysisEngineDescription tagger = createEngineDescription(OpenNlpPosTagger.class);

        AnalysisEngineDescription chunker = createEngineDescription(OpenNlpChunker.class,
                OpenNlpChunker.PARAM_VARIANT, aVariant,
                OpenNlpChunker.PARAM_PRINT_TAGSET, true);

        AnalysisEngineDescription aggregate = createEngineDescription(segmenter, tagger, chunker);

        AnalysisEngine engine = createEngine(aggregate);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(aLanguage);
        jcas.setDocumentText(aText);
        engine.process(jcas);

        return jcas;
    }
}
