/*
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertChunks;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertPOS;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertSentence;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class Conll2000ReaderTest
{

    @Test
    public void conll2000test()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                Conll2000Reader.class, 
                Conll2000Reader.PARAM_LANGUAGE, "en",
                Conll2000Reader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/2000/", 
                Conll2000Reader.PARAM_PATTERNS, "chunk2000_test.conll",
                Conll2000Reader.PARAM_CHUNK_TAG_SET, "conll2000"
        );
        
        JCas jcas = new JCasIterable(reader).iterator().next();

        String[] sentences = new String[] {
                "Confidence in the pound is widely expected to take another sharp dive if trade "
                + "figures for September , due for release tomorrow , fail to show a substantial "
                + "improvement from July and August 's near-record deficits .",
                "Chancellor of the Exchequer Nigel Lawson 's restated commitment to a firm "
                + "monetary policy has helped to prevent a freefall in sterling over the past "
                + "week .",
                "But analysts reckon underlying support for sterling has been eroded by the "
                + "chancellor 's failure to announce any new policy measures in his Mansion "
                + "House speech last Thursday ." };

        String[] chunks = new String[] { 
                "[  0, 10]NC(NP) (Confidence)",
                "[ 11, 13]PC(PP) (in)",
                "[ 14, 23]NC(NP) (the pound)",
                "[ 24, 50]VC(VP) (is widely expected to take)",
                "[ 51, 69]NC(NP) (another sharp dive)",
                "[ 70, 72]O(SBAR) (if)",
                "[ 73, 86]NC(NP) (trade figures)",
                "[ 87, 90]PC(PP) (for)",
                "[ 91,100]NC(NP) (September)",
                "[103,106]ADJC(ADJP) (due)",
                "[107,110]PC(PP) (for)",
                "[111,118]NC(NP) (release)",
                "[119,127]NC(NP) (tomorrow)",
                "[130,142]VC(VP) (fail to show)",
                "[143,168]NC(NP) (a substantial improvement)",
                "[169,173]PC(PP) (from)",
                "[174,189]NC(NP) (July and August)",
                "[190,213]NC(NP) ('s near-record deficits)",
                "[228,230]PC(PP) (of)",
                "[231,244]NC(NP) (the Exchequer)",
                "[245,257]NC(NP) (Nigel Lawson)",
                "[258,280]NC(NP) ('s restated commitment)",
                "[281,283]PC(PP) (to)",
                "[284,306]NC(NP) (a firm monetary policy)",
                "[307,328]VC(VP) (has helped to prevent)",
                "[329,339]NC(NP) (a freefall)",
                "[340,342]PC(PP) (in)",
                "[343,351]NC(NP) (sterling)",
                "[352,356]PC(PP) (over)",
                "[357,370]NC(NP) (the past week)",
                "[378,386]NC(NP) (analysts)",
                "[387,393]VC(VP) (reckon)",
                "[394,412]NC(NP) (underlying support)",
                "[413,416]PC(PP) (for)",
                "[417,425]NC(NP) (sterling)",
                "[426,441]VC(VP) (has been eroded)",
                "[442,444]PC(PP) (by)",
                "[445,459]NC(NP) (the chancellor)",
                "[460,470]NC(NP) ('s failure)",
                "[471,482]VC(VP) (to announce)",
                "[483,506]NC(NP) (any new policy measures)",
                "[507,509]PC(PP) (in)",
                "[510,534]NC(NP) (his Mansion House speech)",
                "[535,548]NC(NP) (last Thursday)" };

        String[] posMapped = { "POS_NOUN", "POS_ADP", "POS_DET", "POS_NOUN", "POS_VERB", "POS_ADV", "POS_VERB", "POS_ADP", "POS_VERB",
                "POS_DET", "POS_ADJ", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_NOUN", "POS_ADP", "POS_PROPN", "POS_PUNCT", "POS_ADJ", "POS_ADP",
                "POS_NOUN", "POS_NOUN", "POS_PUNCT", "POS_VERB", "POS_ADP", "POS_VERB", "POS_DET", "POS_ADJ", "POS_NOUN", "POS_ADP",
                "POS_PROPN", "POS_CONJ", "POS_PROPN", "POS_X", "POS_ADJ", "POS_NOUN", "POS_PUNCT", "POS_PROPN", "POS_ADP", "POS_DET",
                "POS_PROPN", "POS_PROPN", "POS_PROPN", "POS_X", "POS_VERB", "POS_NOUN", "POS_ADP", "POS_DET", "POS_NOUN", "POS_ADJ", "POS_NOUN",
                "POS_VERB", "POS_VERB", "POS_ADP", "POS_VERB", "POS_DET", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_ADP", "POS_DET", "POS_ADJ",
                "POS_NOUN", "POS_PUNCT", "POS_CONJ", "POS_NOUN", "POS_VERB", "POS_VERB", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_VERB",
                "POS_VERB", "POS_VERB", "POS_ADP", "POS_DET", "POS_NOUN", "POS_X", "POS_NOUN", "POS_ADP", "POS_VERB", "POS_DET", "POS_ADJ",
                "POS_NOUN", "POS_NOUN", "POS_ADP", "POS_PRON", "POS_PROPN", "POS_PROPN", "POS_NOUN", "POS_ADJ", "POS_PROPN", "POS_PUNCT" };

        String[] posOriginal = { "NN", "IN", "DT", "NN", "VBZ", "RB", "VBN", "TO", "VB", "DT", "JJ",
                "NN", "IN", "NN", "NNS", "IN", "NNP", ",", "JJ", "IN", "NN", "NN", ",", "VB", "TO",
                "VB", "DT", "JJ", "NN", "IN", "NNP", "CC", "NNP", "POS", "JJ", "NNS", ".", "NNP",
                "IN", "DT", "NNP", "NNP", "NNP", "POS", "VBN", "NN", "TO", "DT", "NN", "JJ", "NN",
                "VBZ", "VBN", "TO", "VB", "DT", "NN", "IN", "NN", "IN", "DT", "JJ", "NN", ".", "CC",
                "NNS", "VBP", "VBG", "NN", "IN", "NN", "VBZ", "VBN", "VBN", "IN", "DT", "NN", "POS",
                "NN", "TO", "VB", "DT", "JJ", "NN", "NNS", "IN", "PRP$", "NNP", "NNP", "NN", "JJ",
                "NNP", "." };

        assertSentence(sentences, select(jcas, Sentence.class));
        assertChunks(chunks, select(jcas, Chunk.class));
        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
    }
}
