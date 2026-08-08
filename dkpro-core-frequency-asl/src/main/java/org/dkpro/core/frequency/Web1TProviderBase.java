/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.frequency;

import java.io.IOException;
import java.util.Iterator;

import org.dkpro.core.api.frequency.provider.FrequencyCountProviderBase;

import com.googlecode.jweb1t.JWeb1TIterator;
import com.googlecode.jweb1t.Searcher;

public abstract class Web1TProviderBase
    extends FrequencyCountProviderBase
{
    public static final String BOS = "<S>";
    public static final String EOS = "</S>";

    protected Searcher searcher;
    protected String basePath;
    protected String language;

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
    {
        return searcher.getNrOfDistinctNgrams(n);
    }

    @Override
    protected long getFrequencyFromProvider(String phrase) throws IOException
    {
        return searcher.getFrequency(phrase);
    }

    @Override
    public Iterator<String> getNgramIterator(int n) throws IOException
    {
        return new JWeb1TIterator(basePath, n).getIterator();
    }

    @Override
    public String getLanguage()
    {
        return this.language;
    }
}
