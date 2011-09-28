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

import java.io.File;
import java.io.IOException;

import com.googlecode.jweb1t.JWeb1TSearcher;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyUtils;

public class Web1TFrequencyCountProvider
    extends FrequencyCountProviderBase
{

    JWeb1TSearcher searcher;
    
    public Web1TFrequencyCountProvider(String ... indexFiles)
        throws IOException
    {
        searcher = new JWeb1TSearcher(indexFiles);
    }
    
    /**
     * Try to deduce the index files from the given path.
     * @param indexPath The path in which the ngram index files are located.
     * @param minN The minimum ngram length.
     * @param maxN The maximum ngram length.
     * @throws IOException
     */
    public Web1TFrequencyCountProvider(File indexPath, int minN, int maxN)
        throws IOException
    {
        searcher = new JWeb1TSearcher(indexPath, minN, maxN);
    }
    
    @Override
    public long getFrequency(String phrase)
        throws IOException
    {
        return searcher.getFrequency(phrase);
    }

    // FIXME fixed numbers are only correct for English Google Web1T
    // as this can also be used with other data files now it needs to be changed
    /*
     * <p>
     * Number of tokens: 1,024,908,267,229 <br>
     * Number of sentences: 95,119,665,584 <br>
     * Number of unigrams: 13,588,391 <br>
     * Number of bigrams: 314,843,401 <br>
     * Number of trigrams: 977,069,902 <br>
     * Number of fourgrams: 1,313,818,354 <br>
     * Number of fivegrams: 1,176,470,663
     * 
     */
    @Override
    public long getNrOfNgrams(int n) {
        switch (n) {
            case 1:
                return 13588391;
            case 2:
                return 314843401;
            case 3:
                return 977069902;
            case 4:
                return 1313818354;
            case 5:
                return 1176470663;
            default:
                return 0;
        }
    }
    
    // FIXME fixed number is only correct for English Google Web1T
    // as this can also be used with other data files now it needs to be changed
    public long getNrOfTokens() {
        return 1024908267229l;
    }
 
    @Override
    public double getLogLikelihood(int termFrequency, int sizeOfCorpus, String term) throws Exception {
        return FrequencyUtils.loglikelihood(
                termFrequency,
                sizeOfCorpus,
                getFrequency(term),
                getNrOfTokens()
        );
    }
}