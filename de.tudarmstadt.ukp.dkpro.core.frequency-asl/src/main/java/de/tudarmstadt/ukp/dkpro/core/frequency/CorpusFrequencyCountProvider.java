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
package de.tudarmstadt.ukp.dkpro.core.frequency;

import java.io.IOException;
import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyUtils;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringIterable;
import dkpro.toolbox.core.Sentence;
import dkpro.toolbox.corpus.Corpus;
import dkpro.toolbox.corpus.CorpusException;


public class CorpusFrequencyCountProvider
    extends FrequencyCountProviderBase
{

    private final ConditionalFrequencyDistribution<Integer, String> cfd;

    private Corpus corpus;

    public CorpusFrequencyCountProvider(Corpus corpus, int minN, int maxN)
        throws Exception
    {
        cfd = new ConditionalFrequencyDistribution<Integer, String>();
        this.corpus = corpus;

        if (minN > maxN) {
            throw new IllegalArgumentException("minN > maxN");
        }

        for (int i = minN; i <= maxN; i++) {
            for (Sentence s : corpus.getSentences()) {
                cfd.incAll(i, new NGramStringIterable(s.getTokens(), i, i));
            }
        }
    }

    @Override
    protected long getFrequencyFromProvider(String phrase)
    {
        int phraseLength = FrequencyUtils.getPhraseLength(phrase);

        if (cfd.hasCondition(phraseLength)) {
            return cfd.getCount(phraseLength, phrase);
        }
        else {
            return 0;
        }
    }

    @Override
    public double getProbability(String phrase)
        throws IOException
    {
        long count = getFrequency(phrase);

        long N = cfd.getN();

        if (N == 0) {
            return 0;
        }
        else {
            return (double) count / N;
        }
    }

    @Override
    public double getLogProbability(String phrase)
        throws IOException
    {
        return Math.log(getProbability(phrase));
    }

    @Override
    public long getNrOfTokens()
    {
        return cfd.getFrequencyDistribution(1).getN();
    }

    @Override
    public long getNrOfNgrams(int n)
    {
        // FIXME implement this
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public long getNrOfDistinctNgrams(int n)
    {
        // FIXME implement this
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Iterator<String> getNgramIterator(int n)
        throws IOException
    {
        try {
            return corpus.getTokens().iterator();
        }
        catch (CorpusException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String getLanguage()
    {
        return corpus.getLanguage();
    }
}