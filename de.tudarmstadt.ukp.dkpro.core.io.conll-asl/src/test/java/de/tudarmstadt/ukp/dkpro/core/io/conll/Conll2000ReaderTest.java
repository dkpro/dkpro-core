/*******************************************************************************
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
 ******************************************************************************/
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
                Conll2000Reader.PARAM_PATTERNS, "chunk2000_test.txt"
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
                "[  0, 10]Chunk(NP) (Confidence)",
                "[ 11, 13]Chunk(PP) (in)",
                "[ 14, 23]Chunk(NP) (the pound)",
                "[ 24, 50]Chunk(VP) (is widely expected to take)",
                "[ 51, 69]Chunk(NP) (another sharp dive)",
                "[ 70, 72]Chunk(SBAR) (if)",
                "[ 73, 86]Chunk(NP) (trade figures)",
                "[ 87, 90]Chunk(PP) (for)",
                "[ 91,100]Chunk(NP) (September)",
                "[103,106]Chunk(ADJP) (due)",
                "[107,110]Chunk(PP) (for)",
                "[111,118]Chunk(NP) (release)",
                "[119,127]Chunk(NP) (tomorrow)",
                "[130,142]Chunk(VP) (fail to show)",
                "[143,168]Chunk(NP) (a substantial improvement)",
                "[169,173]Chunk(PP) (from)",
                "[174,189]Chunk(NP) (July and August)",
                "[190,213]Chunk(NP) ('s near-record deficits)",
                "[228,230]Chunk(PP) (of)",
                "[231,244]Chunk(NP) (the Exchequer)",
                "[245,257]Chunk(NP) (Nigel Lawson)",
                "[258,280]Chunk(NP) ('s restated commitment)",
                "[281,283]Chunk(PP) (to)",
                "[284,306]Chunk(NP) (a firm monetary policy)",
                "[307,328]Chunk(VP) (has helped to prevent)",
                "[329,339]Chunk(NP) (a freefall)",
                "[340,342]Chunk(PP) (in)",
                "[343,351]Chunk(NP) (sterling)",
                "[352,356]Chunk(PP) (over)",
                "[357,370]Chunk(NP) (the past week)",
                "[378,386]Chunk(NP) (analysts)",
                "[387,393]Chunk(VP) (reckon)",
                "[394,412]Chunk(NP) (underlying support)",
                "[413,416]Chunk(PP) (for)",
                "[417,425]Chunk(NP) (sterling)",
                "[426,441]Chunk(VP) (has been eroded)",
                "[442,444]Chunk(PP) (by)",
                "[445,459]Chunk(NP) (the chancellor)",
                "[460,470]Chunk(NP) ('s failure)",
                "[471,482]Chunk(VP) (to announce)",
                "[483,506]Chunk(NP) (any new policy measures)",
                "[507,509]Chunk(PP) (in)",
                "[510,534]Chunk(NP) (his Mansion House speech)",
                "[535,548]Chunk(NP) (last Thursday)" };

        String[] posMapped = new String[] { "NN", "PP", "ART", "NN", "V", "ADV", "V", "PP", "V",
                "ART", "ADJ", "NN", "PP", "NN", "NN", "PP", "NP", "PUNC", "ADJ", "PP", "NN", "NN",
                "PUNC", "V", "PP", "V", "ART", "ADJ", "NN", "PP", "NP", "CONJ", "NP", "O", "ADJ",
                "NN", "PUNC", "NP", "PP", "ART", "NP", "NP", "NP", "O", "V", "NN", "PP", "ART",
                "NN", "ADJ", "NN", "V", "V", "PP", "V", "ART", "NN", "PP", "NN", "PP", "ART",
                "ADJ", "NN", "PUNC", "CONJ", "NN", "V", "V", "NN", "PP", "NN", "V", "V", "V", "PP",
                "ART", "NN", "O", "NN", "PP", "V", "ART", "ADJ", "NN", "NN", "PP", "PR", "NP",
                "NP", "NN", "ADJ", "NP", "PUNC" };

        String[] posOriginal = new String[] { "NN", "IN", "DT", "NN", "VBZ", "RB", "VBN", "TO",
                "VB", "DT", "JJ", "NN", "IN", "NN", "NNS", "IN", "NNP", ",", "JJ", "IN", "NN",
                "NN", ",", "VB", "TO", "VB", "DT", "JJ", "NN", "IN", "NNP", "CC", "NNP", "POS",
                "JJ", "NNS", ".", "NNP", "IN", "DT", "NNP", "NNP", "NNP", "POS", "VBN", "NN", "TO",
                "DT", "NN", "JJ", "NN", "VBZ", "VBN", "TO", "VB", "DT", "NN", "IN", "NN", "IN",
                "DT", "JJ", "NN", ".", "CC", "NNS", "VBP", "VBG", "NN", "IN", "NN", "VBZ", "VBN",
                "VBN", "IN", "DT", "NN", "POS", "NN", "TO", "VB", "DT", "JJ", "NN", "NNS", "IN",
                "PRP$", "NNP", "NNP", "NN", "JJ", "NNP", "." };

        assertSentence(sentences, select(jcas, Sentence.class));
        assertChunks(chunks, select(jcas, Chunk.class));
        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
    }
}
