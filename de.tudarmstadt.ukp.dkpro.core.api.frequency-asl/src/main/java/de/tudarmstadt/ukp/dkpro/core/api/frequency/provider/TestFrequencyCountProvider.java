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

import java.io.IOException;
import java.util.Iterator;

/**
 *  Frequency provider to be used in unit tests.
 *  The returned frequency is based on the number of characters.
 */
public class TestFrequencyCountProvider
    extends FrequencyCountProviderBase
{

    @Override
    protected long getFrequencyFromProvider(String phrase)
        throws IOException
    {
        return new Double(Math.floor(
                            Math.pow(
                                    10.0,
                                    Integer.valueOf(phrase.length()).doubleValue())
                            )
                         ).longValue();
    }

    @Override
    public long getNrOfNgrams(int n) {
        return 100l * n;
    }
    
    @Override
    public long getNrOfDistinctNgrams(int n)
        throws Exception
    {
        return 10l * n;
    }
    
    @Override
    public long getNrOfTokens() {
        return 100l;
    }

    @Override
    public Iterator<String> getNgramIterator(int n)
        throws Exception
    {
        throw new UnsupportedOperationException("This method is currently not supported.");
    }

    @Override
    public String getLanguage()
        throws Exception
    {
        return "__test";
    }

    @Override
    public String getID()
        throws Exception
    {
        return "1.5.0";
    }
}