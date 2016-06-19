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
package de.tudarmstadt.ukp.dkpro.core.arktools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.*;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class ArktweetTaggerTest
{
    @Test
    public void arktweetTaggerTest()
        throws Exception
    {
        runTest("en",
                "@Gunservatively obozo will go nuts when PA elects a Republican Governor next Tue. Can you say redistricting?",
                new String[] { "@Gunservatively", "obozo", "will", "go", "nuts", "when", "PA",
                        "elects", "a", "Republican", "Governor", "next", "Tue", ".", "Can", "you",
                        "say", "redistricting", "?" },
                new String[] { "@", "^", "V", "V", "A", "R", "^", "V", "D", "A", "N", "P", "^", ",",
                        "V", "O", "V", "V", "," },
                new String[] { "AT", "PROPN", "VERB", "VERB", "ADJ", "ADV", "PROPN", "VERB", "DET",
                        "ADJ", "NOUN", "ADP", "PROPN", "PUNCT", "VERB", "PRON", "VERB", "VERB",
                        "PUNCT" }
        );

        runTest("en",
                "Spending the day withhh mommma !",
                new String[] { "Spending", "the", "day", "withhh", "mommma", "!" },
                new String[] { "V", "D", "N", "P", "N", "," },
                new String[] { "VERB", "DET", "NOUN", "ADP", "NOUN", "PUNCT" }
        );

        runTest("en",
                "lmao ... s/o to the cool ass asian officer 4 #1 not runnin my license and #2 not takin dru boo to jail . Thank u God . #amen",
                new String[] { "lmao", "...", "s/o", "to", "the", "cool", "ass", "asian", "officer",
                        "4", "#1", "not", "runnin", "my", "license", "and", "#2", "not", "takin",
                        "dru", "boo", "to", "jail", ".", "Thank", "u", "God", ".", "#amen" },
                new String[] { "!", ",", "V", "P", "D", "A", "N", "A", "N", "P", "$", "R", "V", "D",
                        "N", "&", "$", "R", "V", "N", "N", "P", "N", ",", "V", "O", "^", ",", "#" },
                new String[] { "INT", "PUNCT", "VERB", "ADP", "DET", "ADJ", "NOUN", "ADJ", "NOUN",
                        "ADP", "NUM", "ADV", "VERB", "DET", "NOUN", "CONJ", "NUM", "ADV", "VERB",
                        "NOUN", "NOUN", "ADP", "NOUN", "PUNCT", "VERB", "PRON", "PROPN", "PUNCT",
                        "HASH" }
        );

        runTest("en",
                "Different smiley styles :) :-) (^_^) ^o #smiley",
                new String[] { "Different", "smiley", "styles", ":)", ":-)", "(^_^)", "^o",
                        "#smiley" },
                new String[] { "A", "A", "N", "E", "E", "E", "E", "#" },
                new String[] { "ADJ", "ADJ", "NOUN", "EMO", "EMO", "EMO", "EMO", "HASH" }
        );
    }

//    // Test for issue 335
//    @Test
//    public void bugTest() throws Exception {
//        runTest("en",
//                "company&#039;s mo",
//                new String[] { "company&#039;s", "mo" },
//                new String[] { "S",              "N", },
//                new String[] { "NN",             "NN" }
//        );
//    }
    
    private JCas runTest(String language, String testDocument, String[] tokens, String[] tags,
            String[] tagClasses)
                throws Exception
    {
        AnalysisEngine tokenizer = createEngine(
                ArktweetTokenizer.class
        );
        
        AnalysisEngine tagger = createEngine(
                ArktweetPosTagger.class,
                ArktweetPosTagger.PARAM_VARIANT, "default"
        );

        JCas aJCas = tagger.newJCas();
        aJCas.setDocumentLanguage(language);
        aJCas.setDocumentText(testDocument);

        tokenizer.process(aJCas);
        tagger.process(aJCas);

        assertToken(tokens, select(aJCas, Token.class));
        assertPOS(tagClasses, tags, select(aJCas, POS.class));
        
        return aJCas;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
