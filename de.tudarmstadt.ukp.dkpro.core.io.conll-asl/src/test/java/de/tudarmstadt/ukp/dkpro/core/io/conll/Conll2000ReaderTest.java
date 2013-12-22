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
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.IOB;

public class Conll2000ReaderTest
{

    @Test
    public void conll2000test()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                Conll2000Reader.class, 
                Conll2000Reader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/2000/", 
                Conll2000Reader.PARAM_PATTERNS, "chunk2000_test.txt"
        );
        
        int i=0;
        int nrOfSentences = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {                
                if (nrOfSentences == 0) {
                    assertEquals("Confidence in the pound is widely expected to take " +
                    		"another sharp dive if trade figures for September , due for" +
                    		" release tomorrow , fail to show a substantial improvement " +
                    		"from July and August 's near-record deficits .", sentence.getCoveredText());
                    for (Token token : JCasUtil.select(jcas, Token.class)) {
                        POS pos = token.getPos();
                        Collection<IOB> iobs = JCasUtil.selectCovered(jcas, IOB.class, token);
                        assertEquals(1, iobs.size());
                        IOB iob = iobs.iterator().next();
                        
                        System.out.println(token.getCoveredText() + "\t" + pos.getClass().getSimpleName() + "\t" + iob.getValue());
                    }
                }
                nrOfSentences++;
            }
            i++;
        }
        
        // 1 big document
        assertEquals(1, i);
        
        assertEquals(3, nrOfSentences);
    }
}
