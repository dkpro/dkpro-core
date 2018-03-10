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
import java.util.function.Predicate;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.util.Span;

public class CasNameSampleStream
    extends CasSampleStreamBase<NameSample>
{
    private Iterator<Sentence> sentences;

    private boolean clearAdaptiveData;

    private Predicate<NamedEntity> namedEntityFilter;

    public void setNamedEntityFilter(Predicate<NamedEntity> namedEntityFilter) {
        this.namedEntityFilter = namedEntityFilter;
    }

    @Override
    public void init(JCas aJCas)
    {
        sentences = select(aJCas, Sentence.class).iterator();
        // New document -> clear adaptive data
        clearAdaptiveData = true;
    }
    
    @Override
    public boolean isActive()
    {
        return sentences != null && sentences.hasNext();
    }
    
    @Override
    public NameSample produce(JCas aJCas)
    {
        // Process present sentences
        Sentence sentence = sentences.next();
        
        // Index tokens
        Int2ObjectMap<Token> idxTokenOffset = new Int2ObjectOpenHashMap<>();
        Object2IntMap<Token> idxToken = new Object2IntOpenHashMap<>();
        List<Token> tokens = selectCovered(Token.class, sentence);
        String[] words = new String[tokens.size()];
        int idx = 0;
        for (Token t : tokens) {
            idxTokenOffset.put(t.getBegin(), t);
            if (t.getEnd() > t.getBegin()) {
                idxTokenOffset.put(t.getEnd() - 1, t);
            }
            idxToken.put(t, idx);
            words[idx] = t.getText();
            idx++;
        }
                
        List<Span> names = new ArrayList<>();
        for (NamedEntity ne : selectCovered(NamedEntity.class, sentence)) {

            if (namedEntityFilter != null && !namedEntityFilter.test(ne)) {
                continue;
            }

            int begin = idxToken.get(idxTokenOffset.get(ne.getBegin()));
            int end = begin;
            if (ne.getEnd() > ne.getBegin()) {
                end = idxToken.get(idxTokenOffset.get(ne.getEnd() - 1));
            }
            names.add(new Span(begin, end + 1, ne.getValue()));
        }

        Span[] nameSpans = NameFinderME.dropOverlappingSpans(names.toArray(new Span[names.size()]));
        
        NameSample sample = new NameSample(words, nameSpans, clearAdaptiveData);
        
        // Adaptive data should be cleared when a new document is processed, so whenever have
        // processed a sample (in particular the first), we set this flag to false. init()
        // will re-set it to true
        clearAdaptiveData = false;
        
        // Block on next call to read
        if (!sentences.hasNext()) {
            documentComplete();
        }
        
        return sample;
    }
}
