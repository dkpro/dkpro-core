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
package de.tudarmstadt.ukp.dkpro.core.api.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextUtils
{
    /**
     * Read a file containing stopwords (one per line).
     * <p>
     * Empty lines and lines starting with ("#") are filtered out.
     *
     * @param file      input file
     * @param lowercase if true, lowercase everything
     * @return a collection of unique stopwords
     * @throws IOException if the file cannot be read
     */
    public static Set<String> readStopwordsFile(File file, boolean lowercase)
            throws IOException
    {
        return readStopwordsPath(file.toPath(), lowercase);
    }

    /**
     * Read a file containing stopwords (one per line).
     * <p>
     * Empty lines and lines starting with ("#") are filtered out.
     *
     * @param location  input file location
     * @param lowercase if true, lowercase everything
     * @return a collection of unique stopwords
     * @throws IOException if the file cannot be read
     */
    public static Set<String> readStopwordsFile(String location, boolean lowercase)
            throws IOException
    {
        return readStopwordsFile(new File(location), lowercase);
    }

    /**
     * Read an {@link InputStream} containing stopwords (one per line).
     * <p>
     * Empty lines and lines starting with ("#") are filtered out.
     *
     * @param inputStream input stream
     * @param lowercase   if true, lowercase everything
     * @return a collection of unique stopwords
     */
    public static Set<String> readStopwordsInputStream(InputStream inputStream, boolean lowercase)
    {
        return readStream(new BufferedReader(new InputStreamReader(inputStream)).lines(),
                lowercase);
    }

    /**
     * Read a file containing stopwords (one per line).
     * <p>
     * Empty lines and lines starting with ("#") are filtered out.
     *
     * @param path      input file {@link Path}
     * @param lowercase if true, lowercase everything
     * @return a collection of unique stopwords
     * @throws IOException if the file cannot be read
     */
    public static Set<String> readStopwordsPath(Path path, boolean lowercase)
            throws IOException
    {
        return readStopwordsInputStream(Files.newInputStream(path), lowercase);
    }

    public static Set<String> readStopwordsURL(URL url, boolean lowercase)
            throws IOException
    {
        return readStopwordsInputStream(url.openStream(), lowercase);
    }

    private static Set<String> readStream(Stream<String> s, boolean lowercase)
    {
        return s.map(String::trim)
                .filter(l -> !l.isEmpty())
                .filter(l -> !l.startsWith("#"))
                .map(l -> lowercase ? l.toLowerCase() : l)
                .collect(Collectors.toSet());
    }
}
