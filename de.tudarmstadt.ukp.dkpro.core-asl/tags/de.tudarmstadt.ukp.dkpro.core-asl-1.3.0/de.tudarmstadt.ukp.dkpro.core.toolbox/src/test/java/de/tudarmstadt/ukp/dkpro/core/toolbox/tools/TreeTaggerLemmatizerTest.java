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
package de.tudarmstadt.ukp.dkpro.core.toolbox.tools;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

public class TreeTaggerLemmatizerTest
{

    @Test
    public void treeTaggerAnnotatorEnglishTest()
        throws Exception
    {
        runTest("en", "This is a test .",
                new String[] { "this", "be",  "a",   "test", "."    }
        );

        runTest("en", "A neural net .",
                new String[] { "a",   "neural", "net", "."    }
        );

        runTest("en", "John is purchasing oranges .",
                new String[] { "John", "be",  "purchase", "orange", "."    }
        );
    }
    
    private void runTest(String language, String testDocument, String[] lemmas)
        throws Exception
    {
        TreeTaggerLemmatizer lemmatizer = new TreeTaggerLemmatizer();
        
        // test Lemma annotations
        if (lemmas != null) {
            checkLemma(lemmas,  lemmatizer.lemmatize(testDocument, "en"));
        }
    }

    private void checkLemma(String[] expected, Collection<String> actual)
    {
        int i = 0;
        for (String lemma : actual) {
            assertEquals("In position "+i, expected[i], lemma);
            i++;
        }
    }
}