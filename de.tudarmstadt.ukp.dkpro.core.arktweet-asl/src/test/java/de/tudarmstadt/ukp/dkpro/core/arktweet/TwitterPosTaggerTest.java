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
package de.tudarmstadt.ukp.dkpro.core.arktweet;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TwitterPosTaggerTest
{

    @Test
    public void treeTaggerAnnotatorEnglishTest()
        throws Exception
    {
        runTest("en",
                "@Gunservatively obozo will go nuts when PA elects a Republican Governor next Tue. Can you say redistricting?",
                new String[] { "@Gunservatively", "obozo", "will", "go", "nuts", "when", "PA", "elects", "a", "Republican", "Governor", "next", "Tue", ".", "Can", "you", "say", "redistricting", "?" },
                new String[] { "@", "^", "V", "V", "N", "R", "^", "V", "D", "A", "N", "P", "^", ",", "V", "O", "V", "V", "," },
                new String[] { "AT", "NP", "V", "V", "NN", "ADV", "NP", "V", "ART", "ADJ", "NN", "PP", "NP", "PUNC", "V", "PR", "V", "V", "PUNC" }
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
        AnalysisEngine engine = createPrimitive(TwitterPosTagger.class);

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
