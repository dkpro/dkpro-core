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

import com.googlecode.jweb1t.JWeb1TSearcherInMemory;

public class Web1TInMemoryProvider
    extends Web1TProviderBase
{

    public Web1TInMemoryProvider(String language, String ngramLocation, int maxNgramLevel)
        throws IOException
    {
        searcher = new JWeb1TSearcherInMemory(ngramLocation, maxNgramLevel);
        basePath = ngramLocation;
        this.language = language;
    }
}
