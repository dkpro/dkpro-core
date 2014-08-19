/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.api.io;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertChunks;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class IobDecoderTest
{
    @Test
    public void iobEncoderTest() 
        throws Exception
    {
        String[] input = new String[] {
                "O","O","O","B-NP","I-NP","I-NP","I-NP","O","O","O","O","O","B-NP","O","B-NP","O","O","O"
        };
        
        String[] chunks = new String[] {
                "[ 10, 43]Chunk(NP) (very complicated example sentence)",
                "[ 69, 81]Chunk(NP) (constituents)",
                "[ 86, 98]Chunk(NP) (dependencies)" };
        
        JCas jcas = getJCas();

        MappingProvider mappingProvider = new MappingProvider();
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, Chunk.class.getName());
        mappingProvider.setDefault(MappingProvider.LOCATION, "dummy");
        mappingProvider.configure(jcas.getCas());

        Type chunkType = JCasUtil.getType(jcas, Chunk.class);
        Feature chunkValue = chunkType.getFeatureByBaseName("chunkValue");
        IobDecoder decoder = new IobDecoder(jcas.getCas(), chunkValue, mappingProvider);

        List<Token> tokens = new ArrayList<Token>(JCasUtil.select(jcas, Token.class));
        decoder.decode(tokens, input);
        
        assertChunks(chunks, select(jcas, Chunk.class));
    }
    
    private JCas getJCas() 
        throws Exception
    {
        String text = "We need a very complicated example sentence , which " +
                "contains as many constituents and dependencies as possible .";

        JCas jcas = JCasFactory.createJCas();
        JCasBuilder cb = new JCasBuilder(jcas);
        for (String token : text.split(" ")) {
            cb.add(token, Token.class);
            cb.add(" ");
        }
        
        cb.close();
        
        return cb.getJCas();
    }
}