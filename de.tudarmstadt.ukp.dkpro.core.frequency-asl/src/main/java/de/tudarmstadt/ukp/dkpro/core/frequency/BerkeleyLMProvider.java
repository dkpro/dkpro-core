/*******************************************************************************
 * Copyright 2010
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

import java.util.Arrays;
import java.util.Iterator;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;

/**
 * Wrapper for the Berkeley LM package.
 *
 * nGram index files are looked up in DKPRO_HOME directory.
 */
public class BerkeleyLMProvider
	implements FrequencyCountProvider
{

    private final NgramLanguageModel<String> lm;
    private String language;
    
	public BerkeleyLMProvider(String binaryFile, String language)
	    throws Exception
	{
	    lm = LmReaders.readLmBinary(binaryFile);
	    this.language = language;
	}

	// FIXME how to obtain phrase count from logProb
    @Override
    public long getFrequency(String phrase)
		throws Exception
	{
        throw new Exception("Not implemented yet.");
//        return getProbnew Float(Math.exp(logProb)).longValue();
	}

    @Override
    public double getProbability(String phrase)
        throws Exception
    {
        return Math.exp(getLogProbability(phrase));
    }

    @Override
    public double getLogProbability(String phrase)
        throws Exception
    {
        return lm.getLogProb(Arrays.asList(phrase.split(" ")));
    }

    @Override
    public long getNrOfTokens()
        throws Exception
    {
        return lm.getWordIndexer().numWords();
    }

    @Override
    public long getNrOfNgrams(int n)
        throws Exception
    {
        throw new Exception("Not implemented yet.");
    }

    @Override
    public long getNrOfDistinctNgrams(int n)
        throws Exception
    {
        throw new Exception("Not implemented yet.");
    }

    @Override
    public Iterator<String> getNgramIterator(int n)
        throws Exception
    {
        throw new Exception("Not implemented yet.");
    }

    @Override
    public String getLanguage()
        throws Exception
    {
        return this.language;
    }
    
    @Override
    public String getID()
        throws Exception
    {
        return this.getClass().getSimpleName();
    }
}