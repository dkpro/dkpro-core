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
package org.dkpro.core.api.embeddings.binary;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.api.embeddings.VectorizerUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.dkpro.core.api.embeddings.binary.BinaryWordVectorUtils.convertWordVectorsToBinary;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BinaryWordVectorUtilsTest
{
    // TODO: test for very large date (>2GB should be chunked)
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
    private Map<String, float[]> vectors;

    @Before
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

        assertTrue(vec.contains("t1"));
        assertTrue(vec.contains("t2"));
        assertEquals(3, vec.dimensions());
        assertEquals(2, vec.size());
        assertTrue(vec.isCaseless());

        for (String word : vectors.keySet()) {
            float[] orig = vectors.get(word);
            float[] conv = vec.vectorize(word);

            assertTrue("Vectors differ for " + word, Arrays.equals(orig, conv));
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

            assertTrue("Vectors differ for " + word, Arrays.equals(orig, conv));
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
        assertTrue("Vectors or unknown words should always be the same.",
                Arrays.equals(unk1, unk2));
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
        File binaryTarget = new File(testContext.getTestOutputFolder(), "binaryTarget");
        convertWordVectorsToBinary(vectors, binaryTarget);
        return binaryTarget;
    }

}