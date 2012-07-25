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

import java.util.Set;

import bak.pcj.map.ObjectKeyLongMap;
import bak.pcj.map.ObjectKeyLongOpenHashMap;

// TODO already store sorted!
/**
 * It is basically a mapping from samples (keys) to long values (counts).
 * 
 * <p>
 * Suppose we want to record the number of occurrences of each word in a sentence, then this class
 * can be used as follows:
 * 
 * <p><blockquote><pre>
 * {@code
 * FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
 * for (String word : "foo bar baz foo".split(" ")) {
 *     fd.inc(word);
 * }
 * System.out.println(fd.getCount("foo"));
 * }
 * </pre></blockquote>
 * 
 * The last call to {@link FrequencyDistribution#getCount} will yield 2, because the
 * word "foo" has appeared twice in the given sequence of words.
 * 
 * <p>
 * This class was inspired by NLTK's <a
 * href="http://nltk.googlecode.com/svn/trunk/doc/api/nltk.probability.FreqDist-class.html">
 * FreqDist</a>.
 * 
 * @param <T>
 *            the type of the samples
 * @see ConditionalFrequencyDistribution
 * @author zesch
 */
public class FrequencyDistribution<T>
{

    private ObjectKeyLongMap freqDist;

    /** The total number of samples (accumulated count). */
    private long n;

    /**
     * Creates a new empty {@link FrequencyDistribution}.
     */
    public FrequencyDistribution()
    {
        freqDist = new ObjectKeyLongOpenHashMap();
        n = 0;
    }

    /**
     * Creates a new {@link FrequencyDistribution} prefilled with samples from an {@link Iterable}.
     * The count for each sample in the iterable is cumulatively increased by 1.
     * 
     * @param iterable
     *            the {@link Iterable} used to fill the {@link FrequencyDistribution}
     */
    public FrequencyDistribution(Iterable<T> iterable)
    {
        this();
        incAll(iterable);
    }

    /**
     * Indicates whether this distribution contains outcomes for a given <code>sample</code>.
     * 
     * @param sample
     *            the sample to look up
     * @return true if samples exist
     */
    public boolean contains(T sample)
    {
        return this.freqDist.containsKey(sample);
    }

    /**
     * Increments the count for a given <code>sample</code>.
     * 
     * @param sample
     *            the sample to increment the count for
     */
    public void inc(T sample)
    {
        addSample(sample, 1);
    }

    /**
     * Increments the count for each sample in a given {@link Iterable}.
     * 
     * @param iterable
     *            the samples used to increment the counts
     */

    public void incAll(Iterable<T> iterable)
    {
        for (T o : iterable) {
            addSample(o, 1);
        }
    }

    /**
     * Returns the total number of sample outcomes that have been recorded by this frequency
     * distribution. This is equal to the accumulated count of all samples (duplicates included).
     * 
     * @return the total number of sample outcomes
     */
    public long getN()
    {
        return n;
    }

    /**
     * Returns the total number of sample values (or bins) that have counts greater than zero. This
     * is equal to the accumulated counts of all distinct samples (duplicates excluded).
     * 
     * @return the total number of bins
     */
    public long getB()
    {
        return this.freqDist.size();
    }

    /**
     * Returns the count for a given <code>sample</code>. If no such samples have been recorded yet,
     * <code>0</code> will be returned.
     * 
     * @param sample
     *            the sample to get the count for
     * @return the count for a given sample
     */
    public long getCount(T sample)
    {
        if (freqDist.containsKey(sample)) {
            return freqDist.get(sample);
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the {@link Set} of sample values (or bins) for which counts have been recorded.
     * 
     * @return the set of bins
     */
    @SuppressWarnings("unchecked")
    public Set<T> getKeys()
    {
        return this.freqDist.keySet();
    }

    /**
     * Increases the count for a given <code>sample</code>.
     * 
     * @param sample
     *            the sample to increase the count for
     * @param number
     *            the number to increase by
     */
    public void addSample(T sample, long number)
    {
        this.n = this.n + number;
        if (freqDist.containsKey(sample)) {
            freqDist.put(sample, freqDist.get(sample) + number);
        }
        else {
            freqDist.put(sample, number);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        // this.freqDist = sortByValue(this.freqDist);
        for (Object o : freqDist.keySet()) {
            sb.append((T) o.toString());
            sb.append(" - ");
            sb.append(freqDist.get(o));
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }
    //
    // /**
    // * Sorts a Map by its values.
    // *
    // * @return the sorted Map
    // */
    // @SuppressWarnings("unchecked")
    // private ObjectKeyIntOpenHashMap sortByValue(ObjectKeyIntOpenHashMap map)
    // {
    // List list = new LinkedList();
    // ObjectKeyIntMapIterator entryIter = map.entries();
    // while (entryIter.hasNext()) {
    // entryIter.next();
    // list.add();
    // }
    // Collections.sort(list, new Comparator()
    // {
    // @Override
    // public int compare(Object arg0, Object arg1)
    // {
    // return ((Comparable) ((Map.Entry) (arg1)).getValue()).compareTo(((Map.Entry)
    // (arg0)).getValue());
    // }
    //
    // });
    //
    // Map result = new LinkedHashMap();
    // for (Iterator iter = list.iterator(); iter.hasNext();) {
    // Map.Entry entry = (Map.Entry) iter.next();
    // result.put(entry.getKey(), entry.getValue());
    // }
    // return result;
    // }
}