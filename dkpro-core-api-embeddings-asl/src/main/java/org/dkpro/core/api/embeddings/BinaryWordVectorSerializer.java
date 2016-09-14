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
package org.dkpro.core.api.embeddings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Utility Methods for working with binary dl4j word vector files
 *
 * @author Paul Dubs
 * @author Richard Eckart de Castilho
 * @author Carsten Schnober
 * @see <a href="https://gist.github.com/treo/f5a346d53f89566b51bf88a9a42c67c7">Original source</a>
 */
public class BinaryWordVectorSerializer
{
    public static final String UNK = "-=*>UNKNOWN TOKEN<*=-";
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryWordVectorSerializer.class);
    public static final int RANDOM_SEED_UNK = 12345;

    public static void convertWordVectorsToBinary(Map<String, float[]> vectors, File binaryTarget)
            throws IOException
    {
        convertWordVectorsToBinary(vectors, false, Locale.US, binaryTarget);
    }

    /**
     * Create a stable random vector for the unknown word.
     */
    protected static float[] makeUnk(int aSize)
    {
        Random rand = new Random(RANDOM_SEED_UNK);
        float[] unk = new float[aSize];
        for (int i = 0; i < unk.length; i++) {
            unk[i] = (rand.nextFloat() - 0.5f) / aSize;
        }
        return unk;
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
            LOGGER.warn("Vectors map is empty, doing nothing.");
            return;
        }

        int vectorLength = vectors.values().iterator().next().length;
        assert vectors.values().stream().allMatch(v -> v.length == vectorLength);

        Header header = prepareHeader(aCaseless, aLocale, vectors.size(), vectorLength);
        DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(binaryTarget)));
        header.write(output);

        LOGGER.info("Sorting data...");
        String[] words = vectors.keySet().stream()
                .sorted()
                .toArray(String[]::new);

        LOGGER.info("Writing strings...");
        for (String word : words) {
            output.writeUTF(word);
        }

        LOGGER.info("Writing UNK vector...");
        {
            float[] vector = makeUnk(header.vectorLength);
            writeVector(output, vector);
        }

        LOGGER.info("Writing vectors...");
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
        header.version = 1;
        header.wordCount = wordCount;
        header.vectorLength = vectorLength;
        header.caseless = aCaseless;
        header.locale = aLocale.toString();
        return header;
    }

    public static class Header
    {
        private static final String MAGIC = "dl4jw2v";

        private int version = 1;

        private int wordCount;
        private int vectorLength;

        private boolean caseless;

        private String locale;

        public static Header read(DataInput aInput)
                throws IOException
        {
            byte[] magicBytes = new byte[MAGIC.length()];
            aInput.readFully(magicBytes);

            if (!MAGIC.equals(new String(magicBytes, StandardCharsets.US_ASCII))) {
                throw new IOException(
                        "The file you provided is either not a DL4J binary word vectors file or corrupted.");
            }

            Header header = new Header();

            header.version = aInput.readByte();
            if (1 != header.version) {
                throw new IOException("Not supported file format version.");
            }

            header.wordCount = aInput.readInt();
            header.vectorLength = aInput.readInt();

            header.caseless = aInput.readBoolean();

            header.locale = aInput.readUTF();

            return header;
        }

        public void write(OutputStream aOutput)
                throws IOException
        {
            DataOutputStream out = new DataOutputStream(aOutput);

            // Magic String to make file recognition easier
            out.write(MAGIC.getBytes(StandardCharsets.US_ASCII));
            out.writeByte(version);

            out.writeInt(wordCount);
            out.writeInt(vectorLength);

            out.writeBoolean(caseless);

            out.writeUTF(locale);

            out.flush();
        }
    }

    public static class BinaryVectorizer
    {
        private final Header header;

        public final String[] words;
        private final FloatBuffer[] parts;
        private final int maxVectorsPerPartition;

        private Locale locale;

        private float[] unk;

        BinaryVectorizer(Header aHeader, RandomAccessFile file, String[] aWords,
                long vectorStartOffset, float[] aUnk)
                throws IOException
        {
            header = aHeader;
            words = aWords;

            unk = aUnk;

            locale = Locale.forLanguageTag(header.locale);

            // Integers can address up to 2 GB (Integer.MAX_VALUE) - to handle large embeddings
            // files, we partition the file into parts of up to 2 GB each.
            maxVectorsPerPartition = Integer.MAX_VALUE / (header.vectorLength * Float.BYTES);
            int maxPartitionSizeBytes = maxVectorsPerPartition * header.vectorLength * Float.BYTES;
            int neededPartitions = aWords.length / maxVectorsPerPartition;
            if (aWords.length % maxPartitionSizeBytes > 0) {
                neededPartitions += 1;
            }

            parts = new FloatBuffer[neededPartitions];
            FileChannel channel = file.getChannel();
            for (int i = 0; i < neededPartitions; i++) {
                long start = vectorStartOffset + ((long) i * maxPartitionSizeBytes);
                long length = maxPartitionSizeBytes;
                if (i == neededPartitions - 1) {
                    length = (aWords.length % maxVectorsPerPartition) * header.vectorLength
                            * Float.BYTES;
                }
                parts[i] = channel.map(FileChannel.MapMode.READ_ONLY, start, length)
                        .asFloatBuffer();
            }
        }

        public int getVectorSize()
        {
            return header.vectorLength;
        }

        public boolean contains(String aWord)
        {
            String word = aWord;
            if (header.caseless) {
                word = word.toLowerCase(locale);
            }

            return Arrays.binarySearch(words, word) >= 0;
        }

        public float[] vectorize(String aWord)
                throws IOException
        {
            String word = aWord;
            if (header.caseless) {
                word = word.toLowerCase(locale);
            }

            int vectorIdx = Arrays.binarySearch(words, word);

            // Word not found
            if (vectorIdx < 0) {
                return unk;
            }

            // Locate the buffer from which to read the vevtor
            int partitionIdx = vectorIdx / maxVectorsPerPartition;
            FloatBuffer part = this.parts[partitionIdx];

            // Locate the position within the buffer from which to read the vector
            int relativeVectorIdx = vectorIdx % maxVectorsPerPartition;
            int offset = relativeVectorIdx * header.vectorLength;
            part.position(offset);

            // Read the vector
            float[] vector = new float[header.vectorLength];
            part.get(vector);
            return vector;
        }

        public static BinaryVectorizer load(File vectorsDir)
                throws IOException
        {
            RandomAccessFile file = new RandomAccessFile(vectorsDir, "rw");

            // Load header
            Header header = Header.read(file);

            // Load words
            String[] words = new String[header.wordCount];
            for (int i = 0; i < header.wordCount; i++) {
                words[i] = file.readUTF();
            }
            LOGGER.info("Loaded " + words.length + " word embeddings.");

            // Load UNK vector
            byte[] buffer = new byte[header.vectorLength * Float.BYTES];
            file.readFully(buffer);
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            float[] unk = new float[header.vectorLength];
            for (int i = 0; i < unk.length; i++) {
                unk[i] = byteBuffer.getFloat(i * Float.BYTES);
            }

            // Rest of the file is mmapped
            long offset = file.getFilePointer();
            return new BinaryVectorizer(header, file, words, offset, unk);
        }
    }
}