/*
 * Copyright 2016
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
package org.dkpro.core.api.embeddings.text;

import org.dkpro.core.api.embeddings.Vectorizer;
import org.dkpro.core.api.embeddings.VectorizerUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * A {@link Vectorizer} backed by a {@code Map<String, float[]}.
 * <p>
 * Use {@link #load(File)} to initialize.
 */
public class TextFormatVectorizer
        implements Vectorizer
{
    private Map<String, float[]> embeddings;
    private float[] unknownVector;
    private int dimensions;
    private boolean caseless;

    private TextFormatVectorizer(Map<String, float[]> embeddings)
    {
        assert !embeddings.isEmpty();
        this.embeddings = embeddings;
        dimensions = embeddings.values().iterator().next().length;
        unknownVector = VectorizerUtils.randomVector(dimensions);
        caseless = embeddings.keySet().stream()
                .allMatch(token -> token.equals(token.toLowerCase()));
    }

    /**
     * Load a text-format embeddings file (assuming no header line).
     *
     * @param f the {@link File} containing the embeddings in text format
     * @return a new {@link TextFormatVectorizer}
     * @throws IOException if an I/O error occurs
     */
    public static Vectorizer load(File f)
            throws IOException
    {
        return load(f, false);
    }

    /**
     * Load a text-format embeddings file.
     *
     * @param f             the {@link File} containing the embeddings in text format
     * @param hasHeaderLine if true, the first line in the file is expected to be a header line
     * @return a new {@link TextFormatVectorizer}
     * @throws IOException if an I/O error occurs
     */
    public static Vectorizer load(File f, boolean hasHeaderLine)
            throws IOException
    {
        return new TextFormatVectorizer(
                TextFormatEmbeddingsUtils.readEmbeddingFileTxt(f, hasHeaderLine));
    }

    @Override public float[] vectorize(String token)
    {
        if (caseless) {
            token = token.toLowerCase();
        }
        float[] vector = contains(token) ? embeddings.get(token) : unknownVector();
        assert vector.length == dimensions();
        return vector;
    }

    @Override public boolean contains(String token)
    {
        return embeddings.containsKey(token);
    }

    @Override public float[] unknownVector()
    {
        return unknownVector;
    }

    @Override public int dimensions()
    {
        return dimensions;
    }

    @Override public int size()
    {
        return embeddings.size();
    }

    @Override public boolean isCaseless()
    {
        return caseless;
    }
}
