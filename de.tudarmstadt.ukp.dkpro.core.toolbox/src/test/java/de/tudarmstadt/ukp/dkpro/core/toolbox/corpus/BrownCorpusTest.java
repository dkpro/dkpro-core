/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Tag;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.TaggedToken;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Text;

public class BrownCorpusTest
{

    @Test
    public void brownTest() throws Exception {
        Corpus corpus = new BrownTEICorpus("src/test/resources/test_corpora/brown/");
        
        int nrOfTexts = 0;
        for (Text text : corpus.getTexts()) {
            // System.out.println(text);
            nrOfTexts++;
        }
        assertEquals(3, nrOfTexts);
        
        int nrOfSentences = 0;
        for (Sentence sentence : corpus.getSentences()) {
            // System.out.println(sentence);
            nrOfSentences++;
        }
        assertEquals(308, nrOfSentences);

        int nrOfTokens = 0;
        for (String token : corpus.getTokens()) {
            // System.out.println(token);
            nrOfTokens++;
        }
        assertEquals(6783, nrOfTokens);

        int nrOfTags = 0;
        for (Tag tag : corpus.getTags()) {
            // System.out.println(tag);
            nrOfTags++;
        }
        assertEquals(6783, nrOfTags);
        
        int nrOfTaggedTokens = 0;
        for (TaggedToken tt : corpus.getTaggedTokens()) {
            // System.out.println(tt);
            nrOfTaggedTokens++;
        }
        assertEquals(6783, nrOfTaggedTokens);
    }

    @Ignore
    @Test
    public void brownTest_DKPRO_HOME() throws Exception {
        CorpusBase corpus = new BrownTEICorpus();
        
        for (Text text : corpus.getTexts()) {
            System.out.println(text);
        }
    }
}
