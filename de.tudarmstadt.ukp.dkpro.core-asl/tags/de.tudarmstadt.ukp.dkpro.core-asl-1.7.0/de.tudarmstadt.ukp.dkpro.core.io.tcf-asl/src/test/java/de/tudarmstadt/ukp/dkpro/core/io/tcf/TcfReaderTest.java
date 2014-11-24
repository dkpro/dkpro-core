/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.tcf;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.util.JCasUtil.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class TcfReaderTest
{
    @Test
    public void testCoreference()
        throws Exception
    {
        CollectionReader reader = createReader(TcfReader.class, 
                TcfReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TcfReader.PARAM_PATTERNS, "tcf-after.xml");
        
        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());

        String[][] ref = new String[][] {
                new String[] { "Sie", "die \" Wahren Finnen \"" }
        };
        
        AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
    }
    
    @Test
    public void testDependency()
        throws Exception
    {
        CollectionReader reader = createReader(TcfReader.class, 
                TcfReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TcfReader.PARAM_PATTERNS, "tcf04-karin-wl.xml");
        
        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());

        String[] ref1 = new String[] {
                "[  0,  5]Dependency(SB) D[0,5](Karin) G[6,12](fliegt)",
                "[  6, 12]Dependency(ROOT) D[6,12](fliegt) G[6,12](fliegt)",
                "[ 13, 17]Dependency(MO) D[13,17](nach) G[6,12](fliegt)",
                "[ 18, 21]Dependency(PNC) D[18,21](New) G[22,26](York)",
                "[ 22, 26]Dependency(NK) D[22,26](York) G[13,17](nach)",
                "[ 27, 28]Dependency(--) D[27,28](.) G[22,26](York)" };

        String[] ref2 = new String[] {
                "[ 29, 32]Dependency(SB) D[29,32](Sie) G[33,37](will)",
                "[ 33, 37]Dependency(ROOT) D[33,37](will) G[33,37](will)",
                "[ 38, 42]Dependency(MO) D[38,42](dort) G[50,56](machen)",
                "[ 43, 49]Dependency(OA) D[43,49](Urlaub) G[50,56](machen)",
                "[ 50, 56]Dependency(OC) D[50,56](machen) G[33,37](will)",
                "[ 57, 58]Dependency(--) D[57,58](.) G[50,56](machen)" };

        List<Sentence> sentences = new ArrayList<Sentence>(select(jcas, Sentence.class));
        assertEquals("Number of sentences", 2, sentences.size());
        Sentence s1 = sentences.get(0);
        Sentence s2 = sentences.get(1);
        
        AssertAnnotations.assertDependencies(ref1, selectCovered(Dependency.class, s1));
        AssertAnnotations.assertDependencies(ref2, selectCovered(Dependency.class, s2));
    }
}
