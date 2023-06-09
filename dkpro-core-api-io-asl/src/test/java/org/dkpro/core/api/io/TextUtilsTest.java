/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.api.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TextUtilsTest
{
    private static final String STOPWORDS_LOCATION = "src/test/resources/stopwords.txt";
    private Set<String> EXPECTED_STOPWORDS = new HashSet<>();

    @BeforeEach
    public void setUp()
    {
        EXPECTED_STOPWORDS.add("Token1");
    }

    @Test
    public void testReadStopwordsFileString()
            throws Exception
    {
        String stopwordsFile = STOPWORDS_LOCATION;
        Set<String> stopwords = TextUtils.readStopwordsFile(stopwordsFile, false);
        assertEquals(EXPECTED_STOPWORDS, stopwords);
    }

    @Test
    public void testReadStopwordsFile()
            throws Exception
    {
        File stopwordsFile = new File(STOPWORDS_LOCATION);
        Set<String> stopwords = TextUtils.readStopwordsFile(stopwordsFile, false);
        assertEquals(EXPECTED_STOPWORDS, stopwords);
    }

    @Test
    public void testReadStopwordsInputStream()
            throws Exception
    {
        InputStream stopwordsFile = Files.newInputStream(new File(STOPWORDS_LOCATION).toPath());
        Set<String> stopwords = TextUtils.readStopwordsInputStream(stopwordsFile, false);
        assertEquals(EXPECTED_STOPWORDS, stopwords);
    }

    @Test
    public void testReadStopwordsFile3()
            throws Exception
    {
        Path stopwordsFile = new File(STOPWORDS_LOCATION).toPath();
        Set<String> stopwords = TextUtils.readStopwordsPath(stopwordsFile, false);
        assertEquals(EXPECTED_STOPWORDS, stopwords);
    }

    @Test
    public void testReadStopwordsURL()
            throws Exception
    {
        URL stopwordsURL = getClass().getResource("/stopwords.txt");
        Set<String> stopwords = TextUtils.readStopwordsURL(stopwordsURL, false);
        assertEquals(EXPECTED_STOPWORDS, stopwords);
    }
}
