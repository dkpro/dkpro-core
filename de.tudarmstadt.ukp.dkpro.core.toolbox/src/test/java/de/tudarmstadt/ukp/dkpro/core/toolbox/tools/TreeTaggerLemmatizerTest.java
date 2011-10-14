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