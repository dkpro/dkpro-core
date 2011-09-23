package de.tudarmstadt.ukp.dkpro.core.frequency.web1t;

import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.frequency.web1t.jweb1t.CreateFileMap;

/**
 * This class provides a method to create the indexes to access the web1t corpus
 * via jWeb1T (wrapped in {@link Web1TFrequencyCountProvider}).
 * 
 * @author zesch, Mateusz Parzonka
 * 
 */
public class JWeb1TIndexer
{

    private final String ngramLocation;
    private final int maxNgramSize;
    
    public JWeb1TIndexer(String ngramLocation, int maxNgramSize) {
        this.ngramLocation = ngramLocation;
        this.maxNgramSize = maxNgramSize;
    }

    /**
     * Run this method to create the indexes. The corpus must be installed in
     * the the folder "...DKPRO_HOME/web1t"
     * 
     * @param args
     * @throws Exception
     */
    public void create()
        throws Exception
    {

        for (int i = 1; i <= maxNgramSize; i++) {
            createIndex(i);
        }
    }

    private void createIndex(int n)
        throws Exception
    {
        String[] args = {
                ngramLocation,
                new Integer(n).toString()
        };
        
        CreateFileMap.main(args);

    }
}