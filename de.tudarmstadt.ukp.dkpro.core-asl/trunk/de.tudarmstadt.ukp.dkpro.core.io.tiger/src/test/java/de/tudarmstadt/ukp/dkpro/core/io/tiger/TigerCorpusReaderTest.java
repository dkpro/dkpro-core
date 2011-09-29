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
package de.tudarmstadt.ukp.dkpro.core.io.tiger;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TigerCorpusReaderTest
{
    @Test
    public void tigerTest()
        throws Exception
    {
        
        CollectionReader reader = createCollectionReader(
                TigerCorpusReader.class,
                TigerCorpusReader.PARAM_FILE, "src/test/resources/tiger.txt"
        );

        String firstSentence = "`` Ross Perot wäre vielleicht ein prächtiger Diktator '' ";
        
        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            if (i == 0) {
                assertEquals(9, JCasUtil.select(jcas, Token.class).size());
                assertEquals(9, JCasUtil.select(jcas, Lemma.class).size());
                assertEquals(9, JCasUtil.select(jcas, POS.class).size());
                assertEquals(1, JCasUtil.select(jcas, Sentence.class).size());

                assertEquals(firstSentence, JCasUtil.select(jcas, Sentence.class).iterator().next().getCoveredText());
                
                assertEquals("Sentence 1", DocumentMetaData.get(jcas).getDocumentTitle());
            }
            i++;
        }
        
        assertEquals(20, i);
    }
}