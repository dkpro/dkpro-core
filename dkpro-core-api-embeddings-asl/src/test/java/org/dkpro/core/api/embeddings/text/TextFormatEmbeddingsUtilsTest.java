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

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.api.embeddings.binary.BinaryVectorizer;
import org.dkpro.core.api.embeddings.text.TextFormatEmbeddingsUtils;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TextFormatEmbeddingsUtilsTest
{
    @Rule
    public DkproTestContext testContext = new DkproTestContext();

    @Test
    public void testReadEmbeddingFileTxt()
            throws IOException, URISyntaxException
    {
        File modelFile = new File("src/test/resources/dummy.vec");
        int expectedSize = 699;
        int expectedDimensions = 50;
        boolean hasHeader = false;

        Map<String, float[]> embeddings = TextFormatEmbeddingsUtils
                .readEmbeddingFileTxt(modelFile, hasHeader);

        assertEquals(expectedSize, embeddings.size());
        embeddings.values().forEach(vector -> assertEquals(expectedDimensions, vector.length));
    }

    @Test
    public void testReadEmbeddingFileTxtWithHeader()
            throws IOException, URISyntaxException
    {
        File modelFile = new File("src/test/resources/dummy_with_header.vec");
        int expectedSize = 699;
        int expectedDimensions = 50;
        boolean hasHeader = true;

        Map<String, float[]> embeddings = TextFormatEmbeddingsUtils
                .readEmbeddingFileTxt(modelFile, hasHeader);

        assertEquals(expectedSize, embeddings.size());
        embeddings.values().forEach(vector -> assertEquals(expectedDimensions, vector.length));
    }

    @Test
    public void testReadEmbeddingFileTxtCompressed()
            throws IOException, URISyntaxException
    {
        File modelFile = new File("src/test/resources/embeddings.gz");
        int expectedSize = 699;
        int expectedDimensions = 50;
        boolean hasHeader = false;

        Map<String, float[]> embeddings = TextFormatEmbeddingsUtils
                .readEmbeddingFileTxt(modelFile, hasHeader);

        assertEquals(expectedSize, embeddings.size());
        embeddings.values().forEach(vector -> assertEquals(expectedDimensions, vector.length));
    }

    @Test
    public void testConvertMalletEmbeddingsToBinary()
            throws IOException
    {
        File modelFile = new File("src/test/resources/dummy.vec");
        File targetFile = new File(testContext.getTestOutputFolder(), "binary");

        Map<String, float[]> embeddings = TextFormatEmbeddingsUtils
                .readEmbeddingFileTxt(modelFile, false);
        TextFormatEmbeddingsUtils.convertMalletEmbeddingsToBinary(modelFile, targetFile);
        BinaryVectorizer vec = BinaryVectorizer.load(targetFile);

        for (String token : embeddings.keySet()) {
            assertTrue("Arrays to not match for token " + token,
                    Arrays.equals(embeddings.get(token), vec.vectorize(token)));
        }
    }

    // TODO add tests for caseless
}