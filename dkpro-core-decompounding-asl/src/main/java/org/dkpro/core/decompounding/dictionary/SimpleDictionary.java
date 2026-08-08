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

package org.dkpro.core.decompounding.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The simple dictionary reads a file in which each line is a new word.
 * 
 * This can be used to create your own dictionary from a corpus
 * 
 */
public class SimpleDictionary
    implements Dictionary
{
    private Set<String> words;

    /**
     * Constructor for a simple dictionary
     * 
     * @param aDict
     *            The file with all words
     */
    public SimpleDictionary(File aDict, String aEncoding) throws IOException
    {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(aDict), aEncoding))) {
            words = readFileToSet(in);
        }
    }

    /**
     * Create a simple dictionary from a list of string. This can be used for testing
     * 
     * @param aWords
     *            A list of words
     */
    public SimpleDictionary(String... aWords)
    {
        words = new HashSet<String>();
        for (String word : aWords) {
            words.add(word.toLowerCase());
        }
    }

    public SimpleDictionary(InputStream aDictStream, String aEncoding) throws IOException
    {
        words = readFileToSet(new BufferedReader(new InputStreamReader(aDictStream, aEncoding)));
    }

    @Override
    public boolean contains(String aWord)
    {
        return words.contains(aWord);
    }

    /**
     * Reads the dictionary to set
     * 
     * @param aReader
     *            a reader,
     * @return A set of words
     * @throws IOException
     *             if an I/O error occurs.
     */
    protected Set<String> readFileToSet(BufferedReader aReader) throws IOException
    {
        Set<String> set = new HashSet<String>();
        String line;
        while ((line = aReader.readLine()) != null) {
            set.add(line.toLowerCase());
        }

        return set;
    }

    @Override
    public List<String> getAll()
    {
        return new ArrayList<String>(words);
    }

    public void setWords(Set<String> aWords)
    {
        words = aWords;
    }
}
