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

import java.util.Random;

/**
 * Helper methods for {@link Vectorizer}s.
 */
public class VectorizerUtils
{
    private static final int RANDOM_SEED = 12345;

    /**
     * Create a random vector. Values are between -0.5 and 0.5,
     *
     * @param aSize the dimensionality of the vector
     * @param seed  The random seed
     * @return a float[] of the specified size
     */
    public static float[] randomVector(int aSize, long seed)
    {
        Random rand = new Random(seed);
        float[] unk = new float[aSize];
        for (int i = 0; i < unk.length; i++) {
            unk[i] = (rand.nextFloat() - 0.5f) / aSize;
        }
        return unk;
    }

    /**
     * Create a random vector, calling {@link #randomVector(int, long)} with a
     * stable random seed, i.e. always generating the same output.
     *
     * @param aSize the size of the vector
     * @return a float[] of the specified size.
     * @see #randomVector(int, long)
     */
    public static float[] randomVector(int aSize)
    {
        return randomVector(aSize, RANDOM_SEED);
    }
}
