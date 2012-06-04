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
package de.tudarmstadt.ukp.dkpro.core.api.frequency.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Inspired by nltk.probability.ConditionalFreqDist Maps a condition to a
 * {@link FrequencyDistribution}.
 * 
 * <p>
 * This class could be used to learn how frequently a word collocates with another word. Suppose we
 * want to know how frequently the word "of" appears after the word "because", then
 * {@link ConditionalFrequencyDistribution} can be used as follows:
 * 
 * <p>
 * <blockquote>
 * 
 * <pre>
 * {
 *     &#064;code
 *     ConditionalFrequencyDistribution&lt;String, String&gt; cfd = new ConditionalFrequencyDistribution&lt;String, String&gt;();
 * 
 *     cfd.addSample(&quot;because&quot;, &quot;in&quot;);
 *     cfd.addSample(&quot;because&quot;, &quot;of&quot;);
 *     cfd.addSample(&quot;despite&quot;, &quot;in&quot;);
 *     cfd.addSample(&quot;because&quot;, &quot;of&quot;);
 * 
 *     System.out.println(cfd.getCount(&quot;despite&quot;, &quot;of&quot;));
 * }
 * </pre>
 * 
 * </blockquote> The last call to {@link ConditionalFrequencyDistribution#getCount} will yield 2,
 * because given the <code>condition</code> that the first word in a two-word sequence is "because",
 * the word "of" has appeared twice.
 * 
 * <p>
 * This class was inspired by NLTK's <a href=
 * "http://nltk.googlecode.com/svn/trunk/doc/api/nltk.probability.ConditionalFreqDist-class.html">
 * FreqDist</a>.
 * 
 * @param <C>
 *            the type of the conditions
 * @param <V>
 *            the type of the samples
 * @see FrequencyDistribution
 * 
 * @author zesch
 */
public class ConditionalFrequencyDistribution<C, V>
{

    private Map<C, FrequencyDistribution<V>> cfd;

    /** The total number of samples of all FrequencyDistributions. */
    private long n;

    /**
     * Creates a new empty {@link ConditionalFrequencyDistribution}.
     */
    public ConditionalFrequencyDistribution()
    {
        cfd = new HashMap<C, FrequencyDistribution<V>>();
        n = 0;
    }

    /**
     * Creates a new {@link ConditionalFrequencyDistribution} and fills it with samples from a map.
     * 
     * @param samples
     *            the {@link Iterable} to fill from
     */
    public ConditionalFrequencyDistribution(Map<C, Iterable<V>> samples)
    {
        this();
        for (Map.Entry<C, Iterable<V>> entry : samples.entrySet()) {
            addSamples(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @return The total number of sample outcomes that have been recorded by this
     *         ConditionalFreqDist.
     */
    public long getN()
    {
        return n;
    }

    /**
     * Returns the total number of samples which equal a given <code>sample</code> under a given
     * <code>condition</code>.
     * 
     * <p>
     * If there are no samples for the condition in question on record, <code>0</code> will be
     * returned.
     * 
     * @param condition
     *            the condition
     * @param sample
     *            the sample under a given condition
     * @return the number of all samples which equal <code>sample</code>
     */
    public long getCount(C condition, V sample)
    {
        if (cfd.containsKey(condition)) {
            return cfd.get(condition).getCount(sample);
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the {@link FrequencyDistribution} under a given <code>condition</code>, or
     * <code>null</code> if this distribution contains no such {@link FrequencyDistribution} for
     * this <code>condition</code>.
     * 
     * @param condition
     *            the condition
     * @return the distribution the condition maps to
     */
    public FrequencyDistribution<V> getFrequencyDistribution(C condition)
    {
        return cfd.get(condition);
    }

    /**
     * Returns all conditions for which samples have been recorded.
     * 
     * @return a {@link Set} of all recorded conditions
     */
    public Set<C> getConditions()
    {
        return this.cfd.keySet();
    }

    /**
     * Indicates whether samples have been recorded under a given <code>condition</code>.
     * 
     * @param condition
     *            the condition in question
     * @return true if samples for <code>condition</code> exist
     */
    public boolean hasCondition(C condition)
    {
        return this.cfd.containsKey(condition);
    }

    /**
     * Adds a sample under a given <code>condition</code>.
     * 
     * @param condition
     *            the condition for this sample
     * @param sample
     *            the sample to add
     */
    public void addSample(C condition, V sample)
    {
        List<V> samples = new ArrayList<V>();
        samples.add(sample);

        addSamples(condition, samples);
    }

    /**
     * Adds multiple samples under a given <code>condition</code>.
     * 
     * <p>
     * If there is no {@link FrequencyDistribution} present for the given <code>condition</code>, a
     * new empty one will be created and populated from the given <code>samples</code>.
     * 
     * @param condition
     *            the condition for the samples
     * @param samples
     *            the samples to add
     */

    public void addSamples(C condition, Iterable<V> samples)
    {
        FrequencyDistribution<V> freqDist = null;

        if (cfd.containsKey(condition)) {
            freqDist = cfd.get(condition);
        }
        else {
            freqDist = new FrequencyDistribution<V>();
            cfd.put(condition, freqDist);
        }

        long countBefore = freqDist.getN();
        freqDist.incAll(samples);
        this.n = n + (freqDist.getN() - countBefore);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (C t : cfd.keySet()) {
            sb.append(t.toString());
            sb.append(System.getProperty("line.separator"));
            sb.append(cfd.get(t).toString());
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }
}