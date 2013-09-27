/**
 * Copyright 2013
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.core.arktools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ArktweetTaggerTest
{

    @Test
    public void arktweetTaggerTest()
        throws Exception
    {
        runTest("en",
                "@Gunservatively obozo will go nuts when PA elects a Republican Governor next Tue. Can you say redistricting?",
                new String[] { "@Gunservatively", "obozo", "will", "go", "nuts", "when", "PA", "elects", "a", "Republican", "Governor", "next", "Tue", ".", "Can", "you", "say", "redistricting", "?" },
                new String[] { "@", "^", "V", "V", "A", "R", "^", "V", "D", "A", "N", "P", "^", ",", "V", "O", "V", "V", "," },
                new String[] { "AT", "NP", "V", "V", "ADJ", "ADV", "NP", "V", "ART", "ADJ", "NN", "PP", "NP", "PUNC", "V", "PR", "V", "V", "PUNC" }
        );

        runTest("en",
                "Spending the day withhh mommma !",
                new String[] { "Spending", "the", "day", "withhh", "mommma", "!"    },
                new String[] { "V",        "D",   "N",   "P",      "N",      ","    },
                new String[] { "V",        "ART", "NN",  "PP",     "NN",     "PUNC" }
        );

        runTest("en",
                "lmao ... s/o to the cool ass asian officer 4 #1 not runnin my license and #2 not takin dru boo to jail . Thank u God . #amen",
                new String[] { "lmao", "...",  "s/o", "to", "the", "cool", "ass", "asian", "officer", "4",  "#1",   "not", "runnin", "my",  "license", "and",  "#2",   "not", "takin", "dru", "boo", "to", "jail", ".",    "Thank", "u",  "God", ".",    "#amen" },
                new String[] { "!",    ",",    "V",   "P",  "D",   "A",    "N",   "A",     "N",       "P",  "$",    "R",   "V",      "D",   "N",       "&",    "$",    "R",   "V",     "N",   "N",   "P",  "N",    ",",    "V",     "O",  "^",   ",",    "#"     },
                new String[] { "INT",  "PUNC", "V",   "PP", "ART", "ADJ",  "NN",  "ADJ",   "NN",      "PP", "CARD", "ADV", "V",      "ART", "NN",      "CONJ", "CARD", "ADV", "V",     "NN",  "NN",  "PP", "NN",   "PUNC", "V",     "PR", "NP",  "PUNC", "HASH"  }
        );

        runTest("en",
                "Different smiley styles :) :-) (^_^) ^o #smiley",
                new String[] { "Different", "smiley",  "styles", ":)", ":-)", "(^_^)", "^o", "#smiley"},
                new String[] { "A",         "A",       "N",      "E",  "E",   "E",     "E",   "#"},
                new String[] { "ADJ",       "ADJ",     "NN",     "EMO","EMO", "EMO",   "EMO", "HASH"}
        );
    }

    private JCas runTest(
            String language,
            String testDocument,
            String[] tokens,
            String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AnalysisEngine engine = createEngine(
                ArktweetTagger.class,
                ArktweetTagger.PARAM_VARIANT, "default"
        );

        JCas aJCas = engine.newJCas();
        aJCas.setDocumentLanguage(language);
        aJCas.setDocumentText(testDocument);

        engine.process(aJCas);

        // test tokens
        checkTokens(tokens, select(aJCas, Token.class));

        // test POS annotations
        if (tagClasses != null && tags != null) {
            checkTags(tagClasses, tags, select(aJCas, POS.class));
        }

        return aJCas;
    }

    private void checkTokens(String[] expected, Collection<Token> actual)
    {
        int i = 0;
        for (Token tokenAnnotation : actual) {
            assertEquals("In position "+i, expected[i], tokenAnnotation.getCoveredText());
            i++;
        }
    }

    private void checkTags(String[] tagClasses, String[] tags, Collection<POS> actual)
    {
        int i = 0;
        for (POS posAnnotation : actual) {
            assertEquals("In position "+i, tagClasses[i], posAnnotation.getType().getShortName());
            assertEquals("In position "+i, tags[i],       posAnnotation.getPosValue());
            i++;
        }
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }

}
