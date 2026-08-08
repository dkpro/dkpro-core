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

package org.dkpro.core.decompounding.web1t;

/**
 * N-gram model class.
 * 
 * This is only a data container for the n-grams.
 */
public class NGramModel
{
    private String gram;
    private int freq;

    public NGramModel(String aGram, int aFreq)
    {
        gram = aGram;
        freq = aFreq;
    }

    public String getGram()
    {
        return gram;
    }

    public void setGram(String aGram)
    {
        gram = aGram;
    }

    public int getFreq()
    {
        return freq;
    }

    public void setFreq(int aFreq)
    {
        freq = aFreq;
    }

    public int getN()
    {
        return gram.split(" ").length;
    }

    @Override
    public String toString()
    {
        return "[" + gram + "] (freq=" + freq + ")";
    }
}
