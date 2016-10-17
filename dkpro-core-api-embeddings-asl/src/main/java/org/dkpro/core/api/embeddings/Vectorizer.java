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
package org.dkpro.core.api.embeddings;

import java.io.IOException;

/**
 * An interface for vectorizers mapping tokens to embedding vectors.
 */
public interface Vectorizer
{
    /**
     * Get the vector for a token. If the token is unknown, implementing classes should return the
     * {@link #unknownVector()}.
     *
     * @param token
     *            a token String
     * @return a float array
     * @throws IOException
     *             if there was an error accessing the vector file.
     */
    float[] vectorize(String token)
            throws IOException;

    /**
     * True if the token is known by the vectorizer.
     *
     * @param token a token String
     * @return true if the token is known
     */
    boolean contains(String token);

    /**
     * The vector for unknown tokens.
     *
     * @return a float array
     */
    float[] unknownVector();

    /**
     * The dimensionality of the embeddings
     *
     * @return an int
     */
    int dimensions();

    /**
     * The total number of known tokens.
     *
     * @return an int
     */
    int size();

    boolean isCaseless();
}
