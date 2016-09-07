/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.lxf;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertDependencies;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertPOS;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertSentence;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertToken;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.lxf.LxfReader;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class LxfReaderTest
{
    @Test
    public void testText()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                LxfReader.class, 
                LxfReader.PARAM_SOURCE_LOCATION, "src/test/resources/lxf/text/orig.lxf");
        
        JCas jcas = new JCasIterable(reader).iterator().next();
    
        assertEquals("This is a test. And here is another one.\n", jcas.getDocumentText());
    }

    @Test
    public void testTokenizerRepp()
            throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                LxfReader.class, 
                LxfReader.PARAM_SOURCE_LOCATION, "src/test/resources/lxf/tokenizer-repp/orig.lxf");
        
        JCas jcas = new JCasIterable(reader).iterator().next();
    
        String[] sentences = {
                "This is a test.",
                "And here is another one." };
    
        String[] tokens = { "This", "is", "a", "test", ".", "And", "here", "is", "another", "one",
                "." };
        
        assertSentence(sentences, select(jcas, Sentence.class));
        assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void testTokenizerReppHunpos()
            throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                LxfReader.class, 
                LxfReader.PARAM_SOURCE_LOCATION, "src/test/resources/lxf/tokenizer-repp-hunpos/orig.lxf");
        
        JCas jcas = new JCasIterable(reader).iterator().next();
    
        String[] sentences = {
                "This is a test.",
                "And here is another one." };
    
        String[] tokens = { "This", "is", "a", "test", ".", "And", "here", "is", "another", "one",
                "." };

        String[] pos = { "DT", "VBZ", "DT", "NN", ".", "CC", "RB", "VBZ", "DT", "NN", "." };
        
        String[] posMapped = { "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS",
                "POS" };

        assertSentence(sentences, select(jcas, Sentence.class));
        assertToken(tokens, select(jcas, Token.class));
        assertPOS(posMapped, pos, select(jcas, POS.class));
    }

    
    @Test
    public void testTokenizerReppHunposBn()
            throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                LxfReader.class, 
                LxfReader.PARAM_SOURCE_LOCATION, "src/test/resources/lxf/tokenizer-repp-hunpos-bn/orig.lxf");
        
        JCas jcas = new JCasIterable(reader).iterator().next();
    
        String[] sentences = {
                "This is a test.",
                "And here is another one." };
    
        String[] tokens = { "This", "is", "a", "test", ".", "And", "here", "is", "another", "one",
                "." };

        // FIXME Actually there should only be one set of POS here - for some reason the reference
        // file contains POS from hunpos AND from the bn parser
        String[] pos = { "DT", "DT", "VBZ", "VBZ", "DT", "DT", "NN", "NN", ".", ".", "CC", "CC",
                "RB", "RB", "VBZ", "VBZ", "DT", "DT", "NN", "NN", ".", "." };
        
        String[] posMapped = { "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS",
                "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS", "POS",
                "POS" };

        String[] dependencies = { 
                "[  0,  4]Dependency(SBJ) D[0,4](This) G[5,7](is)",
                "[  5,  7]ROOT(ROOT) D[5,7](is) G[5,7](is)",
                "[  8,  9]Dependency(NMOD) D[8,9](a) G[10,14](test)",
                "[ 10, 14]Dependency(OBJ) D[10,14](test) G[5,7](is)",
                "[ 14, 15]Dependency(P) D[14,15](.) G[5,7](is)",
                "[ 16, 19]Dependency(DEP) D[16,19](And) G[25,27](is)",
                "[ 20, 24]Dependency(LOC) D[20,24](here) G[25,27](is)",
                "[ 25, 27]ROOT(ROOT) D[25,27](is) G[25,27](is)",
                "[ 28, 35]Dependency(NMOD) D[28,35](another) G[36,39](one)",
                "[ 36, 39]Dependency(SBJ) D[36,39](one) G[25,27](is)",
                "[ 39, 40]Dependency(P) D[39,40](.) G[25,27](is)" };

        assertSentence(sentences, select(jcas, Sentence.class));
        assertToken(tokens, select(jcas, Token.class));
        assertPOS(posMapped, pos, select(jcas, POS.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
    }
}
