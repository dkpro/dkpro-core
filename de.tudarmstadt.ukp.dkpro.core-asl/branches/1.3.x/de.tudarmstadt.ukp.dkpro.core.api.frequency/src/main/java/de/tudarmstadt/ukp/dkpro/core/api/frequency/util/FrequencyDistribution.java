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
 * Inspired by nltk.probability.FreqDist
 * 
 * @author zesch
 */
public class FrequencyDistribution<T> {
    
	private ObjectKeyLongMap freqDist;
	
	private long n;
	
	public FrequencyDistribution() {
		freqDist = new ObjectKeyLongOpenHashMap();
		n = 0;
	}
	
	public FrequencyDistribution(Iterable<T> iterable) {
		this();
		for (T o : iterable) {
			addSample(o, 1);
		}
	}
	
	public boolean contains(T o) {
	    return this.freqDist.containsKey(o);
	}
	
	public void inc(T o) {
		addSample(o, 1);
	}
	
	public void incAll(Iterable<T> iterable ) {
		for (T o : iterable) {
			addSample(o, 1);
		}	
	}
	
	/**
	 * @return The total number of sample outcomes that have been recorded by this FreqDist.
	 */
	public long getN() {
		return n;
	}

	/**
	 * @return The total number of sample values (or bins) that have counts greater than zero.
	 */
	public long getB() {
	    return this.freqDist.size();
	}
	
	public long getCount(T o) {
		if (freqDist.containsKey(o)) {
			return freqDist.get(o);
		}
		else {
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
    public Set<T> getKeys() {
//	    this.freqDist = sortByValue(this.freqDist);
	    return this.freqDist.keySet();
	}
	
	public void addSample(T o, long number) {
		this.n = this.n + number;
		if (freqDist.containsKey(o)) {
			freqDist.put(o, freqDist.get(o) + number);
		}
		else {
			freqDist.put(o, number);
		}
	}
	
    @SuppressWarnings("unchecked")
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
//        this.freqDist = sortByValue(this.freqDist);
        for (Object o : freqDist.keySet()) {
            sb.append((T) o.toString());
            sb.append(" - ");
            sb.append(freqDist.get(o));
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }
//
//    /**
//     * Sorts a Map by its values.
//     * 
//     * @return the sorted Map
//     */
//    @SuppressWarnings("unchecked")
//    private ObjectKeyIntOpenHashMap sortByValue(ObjectKeyIntOpenHashMap map)
//    {
//        List list = new LinkedList();
//        ObjectKeyIntMapIterator entryIter = map.entries();
//        while (entryIter.hasNext()) {
//            entryIter.next();
//            list.add();
//        }
//        Collections.sort(list, new Comparator()
//        {
//            @Override
//            public int compare(Object arg0, Object arg1)
//            {
//                return ((Comparable) ((Map.Entry) (arg1)).getValue()).compareTo(((Map.Entry) (arg0)).getValue());
//            }
//
//        });
//
//        Map result = new LinkedHashMap();
//        for (Iterator iter = list.iterator(); iter.hasNext();) {
//            Map.Entry entry = (Map.Entry) iter.next();
//            result.put(entry.getKey(), entry.getValue());
//        }
//        return result;
//    }
}