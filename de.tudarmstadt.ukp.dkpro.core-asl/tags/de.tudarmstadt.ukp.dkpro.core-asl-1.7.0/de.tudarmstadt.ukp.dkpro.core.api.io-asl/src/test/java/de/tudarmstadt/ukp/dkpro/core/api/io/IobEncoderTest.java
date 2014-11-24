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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.NC;

public class IobEncoderTest
{

    @Test
    public void iobEncoderTest() 
        throws Exception
    {
        String[] expected = new String[] {
                "O","O","O","B-NP","I-NP","I-NP","I-NP","O","O","O","O","O","B-NP","O","B-NP","O","O","O"
        };
        
        JCas jcas = getJCas();

        Type chunkType = JCasUtil.getType(jcas, Chunk.class);
        Feature chunkValue = chunkType.getFeatureByBaseName("chunkValue");

        IobEncoder encoder = new IobEncoder(jcas.getCas(), chunkType, chunkValue);
        
        int i=0;
        for (Token token : JCasUtil.select(jcas, Token.class)) {
            assertEquals(expected[i], encoder.encode(token));
            i++;
        }
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
        }
        
        List<Token> tokens = new ArrayList<Token>(JCasUtil.select(jcas, Token.class));
        NC nc1 = new NC(jcas, tokens.get(3).getBegin(), tokens.get(6).getEnd());
        nc1.setChunkValue("NP");
        nc1.addToIndexes();
        
        NC nc2 = new NC(jcas, tokens.get(12).getBegin(), tokens.get(12).getEnd());
        nc2.setChunkValue("NP");
        nc2.addToIndexes();
        
        NC nc3 = new NC(jcas, tokens.get(14).getBegin(), tokens.get(14).getEnd());
        nc3.setChunkValue("NP");
        nc3.addToIndexes();
        
        return cb.getJCas();
    }
}