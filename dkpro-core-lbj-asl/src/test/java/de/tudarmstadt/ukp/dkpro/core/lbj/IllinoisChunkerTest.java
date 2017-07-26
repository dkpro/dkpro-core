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
package de.tudarmstadt.ukp.dkpro.core.lbj;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.*;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class IllinoisChunkerTest
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

    private JCas runTest(String aLanguage, String aVariant, String aText)
        throws Exception
    {
        AnalysisEngineDescription segmenter = createEngineDescription(IllinoisSegmenter.class);

        AnalysisEngineDescription tagger = createEngineDescription(IllinoisPosTagger.class);

        AnalysisEngineDescription chunker = createEngineDescription(IllinoisChunker.class,
                //IllinoisChunker.PARAM_VARIANT, aVariant,
                IllinoisChunker.PARAM_PRINT_TAGSET, true);

        AnalysisEngineDescription aggregate = createEngineDescription(segmenter, tagger, chunker);

        AnalysisEngine engine = createEngine(aggregate);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(aLanguage);
        jcas.setDocumentText(aText);
        engine.process(jcas);

        return jcas;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
