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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Locale;
import java.util.Map;

import org.dkpro.core.api.embeddings.VectorizerUtils;
import org.dkpro.core.api.embeddings.binary.BinaryVectorizer.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Methods for working with binary dl4j word vector files.
 * <p>
 * The core of this code has been written in the context of
 * <a href="https://deeplearning4j.org/">dl4j</a>, but provides a generic solution to efficiently
 * storing and reading word embeddings with a memory-mapped file.
 *
 * @author Paul Dubs
 * @author Richard Eckart de Castilho
 * @see <a href="https://gist.github.com/treo/f5a346d53f89566b51bf88a9a42c67c7">Original source</a>
 */
public class BinaryWordVectorUtils
{
    private static final Logger LOG = LoggerFactory.getLogger(BinaryWordVectorUtils.class);
    private static final Locale DEFAULT_LOCALE = Locale.US;

    /**
     * Write a map of token embeddings into binary format. Uses the default locale {@link Locale#US}
     * and assume case-sensitivity iff there is any token containing an uppercase letter.
     *
     * @param vectors      a {@code Map<String, float[]>} holding all tokens with embeddings
     * @param binaryTarget the target file {@link File}
     * @throws IOException if an I/O error occurs
     * @see #convertWordVectorsToBinary(Map, boolean, Locale, File)
     */
    public static void convertWordVectorsToBinary(Map<String, float[]> vectors, File binaryTarget)
            throws IOException
    {
        boolean caseless = vectors.keySet().stream()
                .allMatch(token -> token.equals(token.toLowerCase()));
        convertWordVectorsToBinary(vectors, caseless, DEFAULT_LOCALE, binaryTarget);
    }

    /**
     * Write a map of token embeddings into binary format.
     *
     * @param vectors      a {@code Map<String, float[]>} holding all tokens with embeddings
     * @param aCaseless    if true, tokens are expected to be caseless
     * @param aLocale      the {@link Locale}
     * @param binaryTarget the target file {@link File}
     * @throws IOException if an I/O error occurs
     */
    public static void convertWordVectorsToBinary(Map<String, float[]> vectors, boolean aCaseless,
            Locale aLocale, File binaryTarget)
            throws IOException
    {
        if (vectors.isEmpty()) {
            throw new IllegalArgumentException("Word embeddings map must not be empty.");
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
