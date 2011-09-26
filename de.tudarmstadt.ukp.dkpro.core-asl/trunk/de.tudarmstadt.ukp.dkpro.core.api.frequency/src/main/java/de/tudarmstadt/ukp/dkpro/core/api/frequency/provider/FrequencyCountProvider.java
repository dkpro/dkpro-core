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
package de.tudarmstadt.ukp.dkpro.core.api.frequency.provider;

public interface FrequencyCountProvider
{

    /**
     * Get frequency for a phrase
     *
     * @param phrase
     *            phrase to search
     *            
     * @return frequency of the phrase. Returns 0 if the phrase does not exist in the corpus.
     * @throws Exception 
     */
    public long getFrequency(String phrase) throws Exception;

    /**
     * Get the probability (=normalized frequency) for a phrase.<br>
     * The frequency count is normalized by the number of phrases in the corpus.
     * 
     *
     * @param phrase
     *            phrase to search
     *            
     * @return probability (=normalized frequency) of the phrase. Returns null if the phrase does not exist in the corpus.
     * @throws Exception 
     */
    public double getProbability(String phrase) throws Exception;

    /**
     * Get the log probability for a phrase
     *
     * @param phrase
     *            phrase to search
     *            
     * @return The log probability of the. Returns 0 if the phrase does not exist in the corpus.
     * @throws Exception 
     */
    public double getLogProbability(String phrase) throws Exception;
    
    /**
     * @return The number of tokens in the corpus.
     */
    public long getNrOfTokens() throws Exception;
    
    /**
     * Returns the number of ngrams of a given size in the corpus.
     */
    public long getNrOfNgrams(int n) throws Exception;
}