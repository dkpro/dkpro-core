/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.dl4j.internal;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.jcas.JCas;
import org.deeplearning4j.models.sequencevectors.sequence.Sequence;
import org.deeplearning4j.models.sequencevectors.transformers.SequenceTransformer;
import org.deeplearning4j.models.word2vec.VocabWord;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class SentenceTransformer
    implements SequenceTransformer<VocabWord, Sentence>, Iterable<Sequence<VocabWord>>
{
    private JCas jcas;
    private Iterator<Sentence> iterator;
    protected boolean readOnly = false;
    protected AtomicInteger sentenceCounter = new AtomicInteger(0);

    public SentenceTransformer(JCas aJCas)
    {
        jcas = aJCas;
    }

    @Override
    public Sequence<VocabWord> transformToSequence(Sentence aSentence)
    {
        Sequence<VocabWord> sequence = new Sequence<>();

        for (Token token : selectCovered(Token.class, aSentence)) {
            String text = token.getCoveredText();
            if (StringUtils.isBlank(text)) {
                continue;
            }

            VocabWord word = new VocabWord(1.0, text);
            sequence.addElement(word);
        }

        sequence.setSequenceId(sentenceCounter.getAndIncrement());
        return sequence;
    }

    @Override
    public Iterator<Sequence<VocabWord>> iterator()
    {
        iterator = select(jcas, Sentence.class).iterator();

        return new Iterator<Sequence<VocabWord>>()
        {
            @Override
            public boolean hasNext()
            {
                return SentenceTransformer.this.iterator.hasNext();
            }

            @Override
            public Sequence<VocabWord> next()
            {
                Sequence<VocabWord> sequence = SentenceTransformer.this
                        .transformToSequence(iterator.next());
                return sequence;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}
