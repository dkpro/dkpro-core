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
package de.tudarmstadt.ukp.dkpro.core.io.tei;

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
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TEIReaderTest
{
    @Test
    public void brownReaderTest()
        throws Exception
    {

        CollectionReader reader = createCollectionReader(
                TEIReader.class,
                TEIReader.PARAM_PATH, "src/test/resources/brown_tei/",
                TEIReader.PARAM_PATTERNS, new String[] {
                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.xml"
                }
        );

        String firstSentence = "The Fulton County Grand Jury said Friday an investigation of Atlanta's recent primary election produced `` no evidence '' that any irregularities took place . ";

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            if (i == 0) {
                assertEquals(2239, JCasUtil.select(jcas, Token.class).size());
                assertEquals(2239, JCasUtil.select(jcas, POS.class).size());
                assertEquals(98, JCasUtil.select(jcas, Sentence.class).size());
                
                assertEquals(firstSentence, JCasUtil.select(jcas, Sentence.class).iterator().next().getCoveredText());
            }
            i++;
        }
        
        assertEquals(3, i);
    }

    @Test
    public void brownReaderTest_noSentences()
        throws Exception
    {
        
        CollectionReader reader = createCollectionReader(
                TEIReader.class,
                TEIReader.PARAM_PATH, "src/test/resources/brown_tei/",
                TEIReader.PARAM_PATTERNS, new String[] {
                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.xml"
                },
                TEIReader.PARAM_WRITE_SENTENCES, false
        );

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            if (i == 0) {
                assertEquals(2239, JCasUtil.select(jcas, Token.class).size());
                assertEquals(2239, JCasUtil.select(jcas, POS.class).size());
                assertEquals(0, JCasUtil.select(jcas, Sentence.class).size());
            }
            i++;
        }
        
        assertEquals(3, i);
    }

    @Test
    public void brownReaderTest_noToken_noPOS()
        throws Exception
    {
        
        CollectionReader reader = createCollectionReader(
                TEIReader.class,
                TEIReader.PARAM_PATH, "src/test/resources/brown_tei/",
                TEIReader.PARAM_PATTERNS, new String[] {
                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.xml"
                },
                TEIReader.PARAM_WRITE_TOKENS, false,
                TEIReader.PARAM_WRITE_POS, false
        );

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            if (i == 0) {
                assertEquals(0, JCasUtil.select(jcas, Token.class).size());
                assertEquals(0, JCasUtil.select(jcas, POS.class).size());
                assertEquals(98, JCasUtil.select(jcas, Sentence.class).size());
            }
            i++;
        }
        
        assertEquals(3, i);
    }

    @Test(expected=ResourceInitializationException.class)
    public void brownReaderTest_expectedException()
        throws Exception
    {
        
        CollectionReader reader = createCollectionReader(
                TEIReader.class,
                TEIReader.PARAM_PATH, "src/test/resources/brown_tei/",
                TEIReader.PARAM_PATTERNS, new String[] {
                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.xml"
                },
                TEIReader.PARAM_WRITE_POS, true,
                TEIReader.PARAM_WRITE_TOKENS, false
        );

        for (JCas jcas : new JCasIterable(reader)) {
            // should never get here
            System.out.println(jcas.getDocumentText());
        }
    }
}
