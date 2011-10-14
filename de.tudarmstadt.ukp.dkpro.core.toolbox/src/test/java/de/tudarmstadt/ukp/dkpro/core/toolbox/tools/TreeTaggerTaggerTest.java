package de.tudarmstadt.ukp.dkpro.core.toolbox.tools;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Tag;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.TaggedToken;

public class TreeTaggerTaggerTest
{

    @Test
    public void treeTaggerAnnotatorEnglishTest()
        throws Exception
    {
        runTest("en", "This is a test .",
                new TaggedToken[] {
                    new TaggedToken(
                            "This",
                            new Tag("DT", "en")
                    ),
                    new TaggedToken(
                            "is",
                            new Tag("VBZ", "en")
                    ),
                    new TaggedToken(
                            "a",
                            new Tag("DT", "en")
                    ),
                    new TaggedToken(
                            "test",
                            new Tag("NN", "en")
                    ),
                    new TaggedToken(
                            ".",
                            new Tag("SENT", "en")
                    ),
                }
        );
    }
    
    private void runTest(String language, String testDocument, TaggedToken[] taggedTokens)
        throws Exception
    {
        TreeTaggerPosTagger tagger = new TreeTaggerPosTagger();
        
        // test Lemma annotations
        if (taggedTokens != null) {
            checkTaggedTokens(taggedTokens,  tagger.tag(testDocument, language));
        }
    }

    private void checkTaggedTokens(TaggedToken[] expected, Collection<TaggedToken> actual)
    {
        int i = 0;
        for (TaggedToken taggedToken : actual) {
            assertEquals("In position "+i, expected[i].getToken(), taggedToken.getToken());
            assertEquals("In position "+i, expected[i].getPos().getTag(), taggedToken.getPos().getTag());
            i++;
        }
    }
}