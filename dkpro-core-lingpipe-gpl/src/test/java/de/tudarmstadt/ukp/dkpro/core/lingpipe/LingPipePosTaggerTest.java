/**
 * Copyright 2007-2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.lingpipe;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class LingPipePosTaggerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", null, "This is a test .",
                new String[] { "DT", "BEZ", "AT", "NN", "." },
                new String[] { "DET", "VERB", "DET", "NOUN", "PUNCT" });

        runTest("en", null, "A neural net .", 
                new String[] { "AT", "JJ", "NN", "." }, 
                new String[] { "DET", "ADJ", "NOUN", "PUNCT" });

        runTest("en", null, "John is purchasing oranges .",
                new String[] { "NP", "BEZ", "VBG", "NNS", "." },
                new String[] { "PROPN", "VERB", "VERB", "NOUN", "PUNCT" });
        
        // This is WRONG tagging. "jumps" is tagged as "NNS"
        JCas jcas = runTest("en", "general-brown", "The quick brown fox jumps over the lazy dog . \n",
                new String[] { "AT", "JJ", "JJ", "NN", "NNS", "IN", "AT", "JJ", "NN", "." },
                new String[] { "DET", "ADJ", "ADJ", "NOUN", "NOUN", "ADP", "DET", "ADJ", "NOUN",
                        "PUNCT" });

        String[] brownTags = { "'", "''", "(", ")", "*", ",", "--", ".", ":", "ABL", "ABN", "ABX",
                "AP", "AP$", "AT", "BE", "BED", "BEDZ", "BEG", "BEM", "BEN", "BER", "BEZ", "CC",
                "CD", "CD$", "CS", "DO", "DOD", "DOZ", "DT", "DT$", "DTI", "DTS", "DTX", "EX",
                "HV", "HVD", "HVG", "HVN", "HVZ", "IN", "JJ", "JJ$", "JJR", "JJS", "JJT", "MD",
                "NIL", "NN", "NN$", "NNS", "NNS$", "NP", "NP$", "NPS", "NPS$", "NR", "NR$", "NRS",
                "OD", "PN", "PN$", "PP$", "PP$$", "PPL", "PPLS", "PPO", "PPS", "PPSS", "QL", "QLP",
                "RB", "RB$", "RBR", "RBT", "RN", "RP", "TL", "TO", "UH", "VB", "VBD", "VBG", "VBN",
                "VBZ", "WDT", "WP$", "WPO", "WPS", "WQL", "WRB", "``" };

        String[] unmappedBrown = { "'", "''", "*", "--", "AP$", "DT$", "JJ$", "NIL", "``" };
        
        AssertAnnotations.assertTagset(POS.class, "brown", brownTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "brown", unmappedBrown, jcas);

        
        jcas = runTest("en", "bio-genia", "The quick brown fox jumps over the lazy dog . \n",
                new String[] { "DT", "RB", "VBN", "NN", "NNS", "IN", "DT", "NN", "NN", "." },                
                new String[] { "DET", "ADV", "VERB", "NOUN", "NOUN", "ADP", "DET", "NOUN", "NOUN",
                        "PUNCT" });

        String[] ptbTags = { "", "''", "(", ")", ",", "-", ".", ":", "CC", "CD", "CT", "DT", "EX",
                "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "N", "NN", "NNP", "NNPS", "NNS", "PDT",
                "POS", "PP", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "VB", "VBD",
                "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "XT", "``" };

        String[] unmappedPtb = { "", "CT", "N", "XT" };

        AssertAnnotations.assertTagset(POS.class, "ptb", ptbTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPtb, jcas);

        
        jcas = runTest("en", "bio-medpost", "The quick brown fox jumps over the lazy dog . \n",
                new String[] { "DD", "NN", "JJ", "NN", "NNS", "II", "DD", "NN", "NN", "." },                
                new String[] { "DET", "NOUN", "ADJ", "NOUN", "NOUN", "ADP", "DET", "NOUN", "NOUN",
                        "PUNCT" });
        
        String[] medpostTags = { "''", "(", ")", ",", ".", ":", "CC", "CC+", "CS", "CS+", "CSN",
                "CST", "DB", "DD", "EX", "GE", "II", "II+", "JJ", "JJ+", "JJR", "JJT", "MC", "NN",
                "NN+", "NNP", "NNS", "PN", "PND", "PNG", "PNR", "RR", "RR+", "RRR", "RRT", "SYM",
                "TO", "VBB", "VBD", "VBG", "VBI", "VBN", "VBZ", "VDB", "VDD", "VDN", "VDZ", "VHB",
                "VHD", "VHG", "VHI", "VHZ", "VM", "VVB", "VVD", "VVG", "VVGJ", "VVGN", "VVI",
                "VVN", "VVNJ", "VVZ", "``" };

        String[] unmappedMedpost = { "CC+", "CS+", "II+", "JJ+", "NN+", "RR+" };

        AssertAnnotations.assertTagset(POS.class, "medpost", medpostTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "medpost", unmappedMedpost, jcas);
    }

    private JCas runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AnalysisEngine engine = createEngine(LingPipePosTagger.class,
                LingPipePosTagger.PARAM_VARIANT, variant,
                LingPipePosTagger.PARAM_PRINT_TAGSET, true);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
        
        return jcas;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
