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

import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;

public class SegmenterTest
{

    @Test
    public void segmenterTest()
        throws Exception
    {
        String text = "This is a test. A test with two sentences.";
        
        Segmenter segmenter = new Segmenter();
        
        Collection<String> tokens = segmenter.tokenize(text, "en");
        
        assertEquals(11, tokens.size());
        
        
        Collection<Sentence> sentences = segmenter.sentenceSplit(text, "en");
        
        assertEquals(2, sentences.size());
        
        
    }
}