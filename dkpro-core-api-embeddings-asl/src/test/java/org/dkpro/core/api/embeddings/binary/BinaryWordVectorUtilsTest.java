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
package org.dkpro.core.api.embeddings.binary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dkpro.core.api.embeddings.binary.BinaryWordVectorUtils.convertWordVectorsToBinary;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.dkpro.core.api.embeddings.VectorizerUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BinaryWordVectorUtilsTest
{
    private @TempDir File tempDir;
    
    // TODO: test for very large data (>2GB should be chunked)
    private Map<String, float[]> vectors;

    @BeforeEach
    public void setUp()
    {
        vectors = new HashMap<>();
        vectors.put("t1", new float[] { 0.1f, -0.2f, 0.9f });
        vectors.put("t2", new float[] { 0.4f, 0.32f, -0.9f });
    }

    @Test
    public void testConvertWordVectorsToBinary()
            throws Exception
    {
        File binaryTarget = writeBinaryFile(vectors);

        BinaryVectorizer vec = BinaryVectorizer.load(binaryTarget);

        assertThat(vec.contains("t1")).isTrue();
        assertThat(vec.contains("t2")).isTrue();
        assertThat(vec.dimensions()).isEqualTo(3);
        assertThat(vec.size()).isEqualTo(2);
        assertThat(vec.isCaseless()).isTrue();

        for (String word : vectors.keySet()) {
            float[] orig = vectors.get(word);
            float[] conv = vec.vectorize(word);

            assertThat(conv).containsExactly(orig);
        }
    }

    @Test
    public void testConvertWordVectorsToBinaryCaseSensitive()
            throws Exception
    {
        vectors.put("T1", new float[] { 0.1f, 0.2f, 0.3f });
        File binaryTarget = writeBinaryFile(vectors);

        BinaryVectorizer vec = BinaryVectorizer.load(binaryTarget);

        assertTrue(vec.contains("t1"));
        assertTrue(vec.contains("t2"));
        assertTrue(vec.contains("T1"));
        assertFalse(vec.contains("T2"));
        assertEquals(3, vec.dimensions());
        assertEquals(3, vec.size());
        assertFalse(vec.isCaseless());

        for (String word : vectors.keySet()) {
            float[] orig = vectors.get(word);
            float[] conv = vec.vectorize(word);

            assertTrue(Arrays.equals(orig, conv), "Vectors differ for " + word);
        }
    }

    @Test
    public void testRandomVector()
            throws IOException
    {
        File binaryTarget = writeBinaryFile(vectors);

        BinaryVectorizer vec = BinaryVectorizer.load(binaryTarget);
        float[] randVector = VectorizerUtils.randomVector(3);

        float[] unk1 = vec.vectorize("unk1");
        float[] unk2 = vec.vectorize("unk2");
        assertTrue(Arrays.equals(randVector, unk1));
        assertTrue(Arrays.equals(randVector, unk2));
        assertTrue(
                Arrays.equals(unk1, unk2), "Vectors or unknown words should always be the same.");
    }

    /**
     * Write a binary vectors file to a testContext-dependent location.
     *
     * @return the binary vectors {@link File}
     * @throws IOException if an I/O error occurs
     */
    private File writeBinaryFile(Map<String, float[]> vectors)
            throws IOException
    {
        File binaryTarget = new File(tempDir, "binaryTarget");
        convertWordVectorsToBinary(vectors, binaryTarget);
        return binaryTarget;
    }
}
