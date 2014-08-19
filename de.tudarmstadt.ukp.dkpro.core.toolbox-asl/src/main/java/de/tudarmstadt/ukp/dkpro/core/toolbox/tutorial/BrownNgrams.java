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
package de.tudarmstadt.ukp.dkpro.core.toolbox.tutorial;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringIterable;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.BrownTeiCorpus;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.Corpus;

public class BrownNgrams {

	public static void main(String[] args) throws Exception
    {
        Corpus brownCorpus = new BrownTeiCorpus();
        
        // disregarding sentence boundaries
        FrequencyDistribution<String> ngramFreqDist = new FrequencyDistribution<String>(
        		new NGramStringIterable(brownCorpus.getTokens(), 1, 2)
        );
        
        // regarding sentence boundaries
        FrequencyDistribution<String> ngramFreqDist2 = new FrequencyDistribution<String>();
        for (Sentence s : brownCorpus.getSentences()) {
        	ngramFreqDist2.incAll(
        		new NGramStringIterable(s.getTokens(), 1, 5)
        	);
        }
        
        System.out.println(ngramFreqDist.getCount("the"));
        System.out.println(ngramFreqDist.getCount("the old man"));
        System.out.println();
        System.out.println(ngramFreqDist2.getCount("the"));
        System.out.println(ngramFreqDist2.getCount("the old man"));
        
    }
}
