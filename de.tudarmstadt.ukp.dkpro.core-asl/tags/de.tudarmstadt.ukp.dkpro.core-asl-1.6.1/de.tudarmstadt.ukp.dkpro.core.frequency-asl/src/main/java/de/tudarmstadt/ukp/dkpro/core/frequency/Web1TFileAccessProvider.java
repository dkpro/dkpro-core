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

public class Web1TFileAccessProvider
    extends Web1TProviderBase
{
    public Web1TFileAccessProvider(String... indexFiles)
        throws IOException
    {
        searcher = new JWeb1TSearcher(indexFiles);
        basePath = new File(indexFiles[0]).getParent();
    }

    /**
     * Try to deduce the index files from the given path.
     * 
     * @param indexPath
     *            The path in which the ngram index files are located.
     * @param minN
     *            The minimum ngram length.
     * @param maxN
     *            The maximum ngram length.
     */
    public Web1TFileAccessProvider(File indexPath, int minN, int maxN)
        throws IOException
    {
        searcher = new JWeb1TSearcher(indexPath, minN, maxN);
        basePath = indexPath.getAbsolutePath();
    }
}