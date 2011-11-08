package de.tudarmstadt.ukp.dkpro.core.api.frequency.util;

import gnu.trove.TObjectIntHashMap;

/**
 * Inspired by nltk.probability.FreqDist
 * 
 * @author zesch
 */
public class TroveFrequencyDistribution<T> {
    
	private TObjectIntHashMap<T> freqDist;
	
	private long n;
	
	public TroveFrequencyDistribution() {
		freqDist = new TObjectIntHashMap<T>();
		n = 0;
	}
	
	public TroveFrequencyDistribution(Iterable<T> iterable) {
		this();
		for (T o : iterable) {
			addObject(o);
		}
	}
	
	public boolean contains(T o) {
	    return this.freqDist.containsKey(o);
	}
	
	public void inc(T o) {
		addObject(o);
	}
	
	public void incAll(Iterable<T> iterable ) {
		for (T o : iterable) {
			addObject(o);
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

    public Object[] getKeys() {
	    return freqDist.keys();
	}
	
	private void addObject(T o) {
		this.n++;
		if (freqDist.containsKey(o)) {
			freqDist.put(o, freqDist.get(o) + 1);
		}
		else {
			freqDist.put(o, 1);
		}
	}
	
    @SuppressWarnings("unchecked")
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Object o : getKeys()) {
            sb.append((T) o.toString());
            sb.append(" - ");
            sb.append(freqDist.get((T) o));
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }
}