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
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class CorpusReaderTest
{
    @Ignore
    @Test
    public void corpusReaderTest() throws Exception {

        CollectionReaderDescription reader = createReaderDescription(
                CorpusReader.class,
                CorpusReader.PARAM_CORPUS, BrownTeiCorpus.class.getName()
        );

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            if (i == 0) {
                int sentenceCount = 0;
                int tokenCount = 0;

                for (Sentence s : JCasUtil.select(jcas, Sentence.class)) {
                    if (sentenceCount == 0) {
                        System.out.println(s.getCoveredText());
                        assertEquals(s.getBegin(), 0);
                        assertEquals(s.getEnd(), 123);
                    }
                    if (sentenceCount < 10) {
                        System.out.println(s.getCoveredText());
                    }
                    sentenceCount++;
                }

                for (Token t : JCasUtil.select(jcas, Token.class)) {
                    if (tokenCount == 0) {
                        System.out.println(t.getCoveredText());
                        assertEquals(t.getBegin(), 0);
                        assertEquals(t.getEnd(), 2);
                    }
                    if (tokenCount < 100) {
                        System.out.println(t.getCoveredText());
                    }
                    tokenCount++;
                }
            }

            i++;
        }

        assertEquals(501,i);

    }
}
