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

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.dkpro.core.api.embeddings.BinaryWordVectorSerializer.*;
import static org.junit.Assert.assertTrue;

/**
 * Created by schnober on 13.09.16.
 */
public class BinaryWordVectorSerializerTest
{
    // TODO: test for very large date (>2GB should be chunked)

    @Rule
    public DkproTestContext context = new DkproTestContext();
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

        for (String word : vectors.keySet()) {
            float[] orig = vectors.get(word);
            float[] conv = vec.vectorize(word);

            assertTrue("Vectors differ for " + word, Arrays.equals(orig, conv));
        }
    }

    @Test
    public void testUnk()
            throws IOException
    {
        File binaryTarget = writeBinaryFile(vectors);

        BinaryVectorizer vec = BinaryVectorizer.load(binaryTarget);

        float[] unk1 = vec.vectorize("unk1");
        float[] unk2 = vec.vectorize("unk2");
        assertTrue(Arrays.equals(unk1, unk2));
    }

    @Test
    public void testUnkStable()
            throws IOException
    {
        float[] unk1 = makeUnk(3);
        float[] unk2 = makeUnk(3);
        assertTrue(Arrays.equals(unk1, unk2));
    }

    /**
     * Write a binary vectors file to a context-dependent location.
     *
     * @return the binary vectors {@link File}
     * @throws IOException if an I/O error occurs
     */
    private File writeBinaryFile(Map<String, float[]> vectors)
            throws IOException
    {
        File binaryTarget = new File(context.getTestOutputFolder(), "binaryTarget");
        convertWordVectorsToBinary(vectors, binaryTarget);
        return binaryTarget;
    }

}