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
 * Inspired by nltk.probability.ConditionalFreqDist
 * 
 * @author zesch
 */
public class ConditionalFrequencyDistribution<C,V> {
    
	private Map<C,FrequencyDistribution<V>> cfd;
	
	private long n;
	
	public ConditionalFrequencyDistribution() {
		cfd = new HashMap<C,FrequencyDistribution<V>>();
		n = 0;
	}
	
	public ConditionalFrequencyDistribution(Map<C,Iterable<V>> samples) {
		this();
		for (Map.Entry<C, Iterable<V>> entry : samples.entrySet()) {
		    addSamples(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * @return The total number of sample outcomes that have been recorded by this ConditionalFreqDist.
	 */
	public long getN() {
		return n;
	}

	public long getCount(C t, V u) {
		if (cfd.containsKey(t)) {
			return cfd.get(t).getCount(u);
		}
		else {
			return 0;
		}
	}

	public FrequencyDistribution<V> getFrequencyDistribution(C c) {
	    return cfd.get(c);
	}
	
	public Set<C> getConditions() {
	    return this.cfd.keySet();
	}
	
	public boolean hasCondition(C t) {
	    return this.cfd.containsKey(t);
	}
	
    public void addSample(C t, V sample) {
        List<V> samples = new ArrayList<V>();
        samples.add(sample);
        
        addSamples(t, samples);
    }

    public void addSamples(C t, Iterable<V> samples) {
	    FrequencyDistribution<V> freqDist = null;

	    if (cfd.containsKey(t)) {
	        freqDist = cfd.get(t);
	    }
	    else {
	        freqDist = new FrequencyDistribution<V>();
	        cfd.put(t, freqDist);
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