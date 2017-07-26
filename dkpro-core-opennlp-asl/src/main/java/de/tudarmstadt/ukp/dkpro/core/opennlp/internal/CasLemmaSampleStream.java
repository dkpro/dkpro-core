/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.opennlp.internal;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import opennlp.tools.lemmatizer.LemmaSample;
import opennlp.tools.util.StringUtil;

public class CasLemmaSampleStream
    extends CasSampleStreamBase<LemmaSample>
{
    private Iterator<Sentence> sentences;

    @Override
    public void init(JCas aJCas)
    {
        sentences = select(aJCas, Sentence.class).iterator();
    }
    
    @Override
    public boolean isActive()
    {
        return sentences != null && sentences.hasNext();
    }
    
    @Override
    public LemmaSample produce(JCas aJCas)
    {
        // Process present sentences
        Sentence sentence = sentences.next();
        
        // Block on next call to read
        if (!sentences.hasNext()) {
            documentComplete();
        }
        
        List<String> words = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        List<String> lemmas = new ArrayList<>();
        
        for (Token t : selectCovered(Token.class, sentence)) {
            words.add(t.getCoveredText());
            
            if (t.getPos() == null) {
                throw new IllegalStateException("Token ["+t.getCoveredText()+"] has no POS");
            }
            tags.add(t.getPos().getPosValue());
            
            if (t.getLemma() == null) {
                throw new IllegalStateException("Token ["+t.getCoveredText()+"] has no lemma");
            }
            lemmas.add(t.getLemma().getValue());
        }
        
        return new LemmaSample(words, tags, lemmas);
    }
}
