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

import java.io.IOException;
import java.util.Iterator;

import com.googlecode.jweb1t.JWeb1TIterator;
import com.googlecode.jweb1t.Searcher;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProviderBase;

public abstract class Web1TProviderBase
    extends FrequencyCountProviderBase
{
    public static final String BOS = "<S>";
    public static final String EOS = "</S>";

    protected Searcher searcher;
    protected String basePath;

    @Override
    public long getNrOfNgrams(int n)
    {
        return searcher.getNrOfNgrams(n);
    }

    @Override
	public long getNrOfTokens()
    {
        return searcher.getNrOfNgrams(1);
    }

    @Override
    public long getNrOfDistinctNgrams(int n)
        throws Exception
    {
        return searcher.getNrOfDistinctNgrams(n);
    }
    
    @Override
    protected long getFrequencyFromProvider(String phrase)
        throws IOException
    {
        return searcher.getFrequency(phrase);
    }
    
    @Override
    public Iterator<String> getNgramIterator(int n)
        throws Exception
    {
        return new JWeb1TIterator(basePath, n).getIterator();
    }    
}