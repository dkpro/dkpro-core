package de.tudarmstadt.ukp.dkpro.core.io.imscwb.util;

import java.util.Iterator;

public class TabTokenizer
    implements Iterable<String>, Iterator<String>
{
    String delim = "\t";
    String s;
    int curIndex;
    int nextIndex;
    boolean nextIsLastToken;
    
    public TabTokenizer(String s)
    {
        this.s = s;
        this.curIndex = 0;
        this.nextIndex = 0;
        this.nextIsLastToken = false;
    }
    
    @Override
    public Iterator<String> iterator()
    {
        return this;
    }
    
    @Override
    public boolean hasNext()
    {
        nextIndex = s.indexOf(delim, curIndex);
    
        if (nextIsLastToken) {
            return false;
    
        }
    
        if (nextIndex == -1) {
            nextIsLastToken = true;
        }
    
        return true;
    }
    
    @Override
    public String next()
    {
        if (nextIndex == -1) {
            nextIndex = s.length();
        }
    
        String token = s.substring(curIndex, nextIndex);
        curIndex = nextIndex + 1;
    
        return token;
    }
    
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}