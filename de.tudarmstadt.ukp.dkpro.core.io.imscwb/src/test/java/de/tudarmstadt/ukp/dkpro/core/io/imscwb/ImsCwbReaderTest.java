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
package de.tudarmstadt.ukp.dkpro.core.io.imscwb;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ImsCwbReaderTest
{
    @Test
    public void wackyTest()
        throws Exception
    {
        
        CollectionReader reader = createCollectionReader(
                ImsCwbReader.class,
                ImsCwbReader.PARAM_PATH, "src/test/resources/wacky/",
                ImsCwbReader.PARAM_TAGGER_TAGSET, "classpath:stts.map",
                ImsCwbReader.PARAM_ENCODING, "ISO-8859-15",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.txt" }
        );

        String firstSentence = "Nikita ( La Femme Nikita ) Dieser Episodenf\u00FChrer wurde von September 1998 bis Mai 1999 von Konstantin C.W. Volkmann geschrieben und im Mai 2000 von Stefan B\u00F6rzel \u00FCbernommen . ";
        
        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            System.out.println(jcas.getDocumentText());
            if (i == 0) {
                assertEquals(11406, JCasUtil.select(jcas, Token.class).size());
                assertEquals(11406, JCasUtil.select(jcas, Lemma.class).size());
                assertEquals(11406, JCasUtil.select(jcas, POS.class).size());
                assertEquals(717, JCasUtil.select(jcas, Sentence.class).size());

                assertEquals(firstSentence, JCasUtil.select(jcas, Sentence.class).iterator().next().getCoveredText());
                
                assertEquals("\"http://www.epguides.de/nikita.htm\"", DocumentMetaData.get(jcas).getDocumentTitle());
            }
            i++;
        }
        
        assertEquals(4, i);

    }
    
    @Test
    public void wackyTest_noAnnotations()
        throws Exception
    {
        
        CollectionReader reader = createCollectionReader(
                ImsCwbReader.class,
                ImsCwbReader.PARAM_PATH, "src/test/resources/wacky/",
                ImsCwbReader.PARAM_TAGGER_TAGSET, "src/test/resources/stts.map",
                ImsCwbReader.PARAM_ENCODING, "ISO-8859-15",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.txt" },
                ImsCwbReader.PARAM_WRITE_TOKENS, false,
                ImsCwbReader.PARAM_WRITE_LEMMAS, false,
                ImsCwbReader.PARAM_WRITE_POS, false,
                ImsCwbReader.PARAM_WRITE_SENTENCES, false
        );

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            if (i == 0) {
                assertEquals(0, JCasUtil.select(jcas, Token.class).size());
                assertEquals(0, JCasUtil.select(jcas, POS.class).size());
                assertEquals(0, JCasUtil.select(jcas, Sentence.class).size());
            }
            i++;
        }
        
        assertEquals(4, i);
    }

    @Test(expected=ResourceInitializationException.class)
    public void wackyTest__expectedException()
        throws Exception
    {
        
        CollectionReader reader = createCollectionReader(
                ImsCwbReader.class,
                ImsCwbReader.PARAM_PATH, "src/test/resources/wacky",
                ImsCwbReader.PARAM_TAGGER_TAGSET, "src/test/resources/stts.map",
                ImsCwbReader.PARAM_ENCODING, "ISO-8859-15",
                ImsCwbReader.PARAM_WRITE_TOKENS, false,
                ImsCwbReader.PARAM_WRITE_LEMMAS, true,
                ImsCwbReader.PARAM_WRITE_POS, false,
                ImsCwbReader.PARAM_WRITE_SENTENCES, false
        );

        for (JCas jcas : new JCasIterable(reader)) {
            // should never get here
            System.out.println(jcas.getDocumentText());
        }
    }
}