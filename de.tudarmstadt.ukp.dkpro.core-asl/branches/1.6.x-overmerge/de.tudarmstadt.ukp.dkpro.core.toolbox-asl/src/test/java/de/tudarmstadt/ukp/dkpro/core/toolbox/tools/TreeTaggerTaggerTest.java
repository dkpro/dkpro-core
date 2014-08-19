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

import org.junit.Assume;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Tag;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.TaggedToken;

public class TreeTaggerTaggerTest
{

    @Test
    public void treeTaggerAnnotatorEnglishTest()
        throws Exception
    {
    	checkModelsAndBinary("en");
    	
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
    
	private void checkModelsAndBinary(String lang)
	{
		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/lib/tagger-" + lang
						+ "-little-endian.par") != null);

		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/bin/LICENSE.txt") != null);
	}
}