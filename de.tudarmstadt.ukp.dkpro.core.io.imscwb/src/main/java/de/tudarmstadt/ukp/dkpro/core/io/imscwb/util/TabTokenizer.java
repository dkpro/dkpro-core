/*******************************************************************************
 * Copyright 2011
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit√§t Darmstadt
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