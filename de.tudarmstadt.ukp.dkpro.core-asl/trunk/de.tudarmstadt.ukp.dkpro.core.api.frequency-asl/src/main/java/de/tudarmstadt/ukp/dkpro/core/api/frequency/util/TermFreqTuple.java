package de.tudarmstadt.ukp.dkpro.core.api.frequency.util;

public class TermFreqTuple<T>
    implements Comparable<TermFreqTuple<T>>
{
    private T key;
    private long freq;
    
    public TermFreqTuple(T key, long freq)
    {
        super();
        this.key = key;
        this.freq = freq;
    }
    
    public T getKey()
    {
        return key;
    }
    public void setKey(T key)
    {
        this.key = key;
    }
    public long getFreq()
    {
        return freq;
    }
    public void setFreq(long freq)
    {
        this.freq = freq;
    }

    @Override
    public String toString()
    {
        return key + " - " + freq;
    }

    @Override
    public int compareTo(TermFreqTuple<T> arg0)
    {
        if (this.freq < arg0.freq) {
            return 1;
        }
        else if (this.freq > arg0.freq) {
            return -1;    
        }
        else {
            return 0;
        }
    }   
}