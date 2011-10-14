package de.tudarmstadt.ukp.dkpro.core.toolbox.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for temporary storing extracted texts before adding to the CAS.
 * 
 * @author zesch
 *
 */
public class CorpusSentence {
    private final List<String> tokens;
    private final List<String> lemmas;
    private final List<String> posList;

    public CorpusSentence()
    {
        tokens  = new ArrayList<String>();
        lemmas  = new ArrayList<String>();
        posList = new ArrayList<String>();
    }
    
    public void addToken(String token) {
        tokens.add(token);
    }            

    public void addLemma(String lemma) {
        lemmas.add(lemma);
    }            
    
    public void addPOS(String pos) {
        posList.add(pos);
    }

    public void addToken(List<String> tokenList) {
        tokens.addAll(tokenList);
    }            

    public void addLemma(List<String> lemmaList) {
        lemmas.addAll(lemmaList);
    }            
    
    public void addPOS(List<String> posList) {
        posList.addAll(posList);
    }

    public List<String> getTokens()
    {
        return tokens;
    }

    public List<String> getLemmas()
    {
        return lemmas;
    }

    public List<String> getPOS()
    {
        return posList;
    }            
}
