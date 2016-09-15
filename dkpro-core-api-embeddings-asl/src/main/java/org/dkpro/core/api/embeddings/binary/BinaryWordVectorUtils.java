/*
 * Copyright 2016 Paul Dubs & Richard Eckart de Castilho
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.api.embeddings.binary;

import org.dkpro.core.api.embeddings.VectorizerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import static org.dkpro.core.api.embeddings.binary.BinaryVectorizer.Header;

/**
 * Utility Methods for working with binary dl4j word vector files
 *
 * @author Paul Dubs
 * @author Richard Eckart de Castilho
 * @see <a href="https://gist.github.com/treo/f5a346d53f89566b51bf88a9a42c67c7">Original source</a>
 */
public class BinaryWordVectorUtils
{
    private static final Logger LOG = LoggerFactory.getLogger(BinaryWordVectorUtils.class);

    public static void convertWordVectorsToBinary(Map<String, float[]> vectors, File binaryTarget)
            throws IOException
    {
        convertWordVectorsToBinary(vectors, false, Locale.US, binaryTarget);
    }

    /**
     * Write a map of token embeddings into binary format.
     *
     * @param vectors      a {@code Map<String, float[]>} holding all tokens with embeddings
     * @param aCaseless    if true, tokens are expected to be caseless
     * @param aLocale      the {@link Locale}
     * @param binaryTarget the target file {@link Path}
     * @throws IOException if an I/O error occurs
     */
    public static void convertWordVectorsToBinary(Map<String, float[]> vectors, boolean aCaseless,
            Locale aLocale, File binaryTarget)
            throws IOException
    {
        if (vectors.isEmpty()) {
            LOG.warn("Vectors map is empty, doing nothing.");
            return;
        }

        int vectorLength = vectors.values().iterator().next().length;
        assert vectors.values().stream().allMatch(v -> v.length == vectorLength);

        Header header = prepareHeader(aCaseless, aLocale, vectors.size(), vectorLength);
        DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(binaryTarget)));
        header.write(output);

        LOG.info("Sorting data...");
        String[] words = vectors.keySet().stream()
                .sorted()
                .toArray(String[]::new);

        LOG.info("Writing strings...");
        for (String word : words) {
            output.writeUTF(word);
        }

        LOG.info("Writing UNK vector...");
        {
            float[] vector = VectorizerUtils.randomVector(header.getVectorLength());
            writeVector(output, vector);
        }

        LOG.info("Writing vectors...");
        for (String word : words) {
            float[] vector = vectors.get(word);
            writeVector(output, vector);
        }
        output.close();
    }

    private static void writeVector(DataOutputStream output, float[] vector)
            throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES);
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        floatBuffer.put(vector);
        output.write(buffer.array());
    }

    private static Header prepareHeader(boolean aCaseless,
            Locale aLocale, int wordCount, int vectorLength)
    {
        Header header = new Header();
        header.setVersion(1);
        header.setWordCount(wordCount);
        header.setVectorLength(vectorLength);
        header.setCaseless(aCaseless);
        header.setLocale(aLocale.toString());
        return header;
    }

}