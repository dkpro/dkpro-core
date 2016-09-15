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

import org.dkpro.core.api.embeddings.Vectorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

/**
 * A {@link Vectorizer} for a binary file. Initialize with {@link #load(File)}.
 *
 * @see BinaryWordVectorUtils
 */
public class BinaryVectorizer
        implements Vectorizer
{
    private static final Logger LOG = LoggerFactory.getLogger(BinaryVectorizer.class);
    private final String[] words;
    private final Header header;
    private final FloatBuffer[] parts;
    private final int maxVectorsPerPartition;
    private Locale locale;
    private float[] unknownVector;

    private BinaryVectorizer(Header aHeader, RandomAccessFile file, String[] aWords,
            long vectorStartOffset, float[] aUnk)
            throws IOException
    {
        header = aHeader;
        words = aWords;

        unknownVector = aUnk;

        locale = Locale.forLanguageTag(header.getLocale());

        // Integers can address up to 2 GB (Integer.MAX_VALUE) - to handle large embeddings
        // files, we partition the file into parts of up to 2 GB each.
        maxVectorsPerPartition = Integer.MAX_VALUE / (header.getVectorLength() * Float.BYTES);
        int maxPartitionSizeBytes =
                maxVectorsPerPartition * header.getVectorLength() * Float.BYTES;
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
                length = (aWords.length % maxVectorsPerPartition) * header.getVectorLength()
                        * Float.BYTES;
            }
            parts[i] = channel.map(FileChannel.MapMode.READ_ONLY, start, length)
                    .asFloatBuffer();
        }
    }

    /**
     * Load a binary embeddings file and return a new {@link BinaryVectorizer} object.
     *
     * @param f a {@link File}
     * @return a new {@link BinaryVectorizer}
     * @throws IOException if an I/O error occurs
     */
    public static BinaryVectorizer load(File f)
            throws IOException
    {
        RandomAccessFile file = new RandomAccessFile(f, "rw");

        // Load header
        Header header = Header.read(file);

        // Load words
        String[] words = new String[header.getWordCount()];
        for (int i = 0; i < header.getWordCount(); i++) {
            words[i] = file.readUTF();
        }
        LOG.info("Loaded " + words.length + " word embeddings.");

        // Load UNK vector
        byte[] buffer = new byte[header.getVectorLength() * Float.BYTES];
        file.readFully(buffer);
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        float[] unk = new float[header.getVectorLength()];
        for (int i = 0; i < unk.length; i++) {
            unk[i] = byteBuffer.getFloat(i * Float.BYTES);
        }

        // Rest of the file is mmapped
        long offset = file.getFilePointer();
        return new BinaryVectorizer(header, file, words, offset, unk);
    }

    @Override public float[] vectorize(String aWord)
            throws IOException
    {
        String word = aWord;
        if (header.isCaseless()) {
            word = word.toLowerCase(locale);
        }

        int vectorIdx = Arrays.binarySearch(words, word);

        // Word not found
        if (vectorIdx < 0) {
            return unknownVector;
        }

        // Locate the buffer from which to read the vevtor
        int partitionIdx = vectorIdx / maxVectorsPerPartition;
        FloatBuffer part = this.parts[partitionIdx];

        // Locate the position within the buffer from which to read the vector
        int relativeVectorIdx = vectorIdx % maxVectorsPerPartition;
        int offset = relativeVectorIdx * header.getVectorLength();
        part.position(offset);

        // Read the vector
        float[] vector = new float[header.getVectorLength()];
        part.get(vector);
        return vector;
    }

    @Override public boolean contains(String aWord)
    {
        String word = aWord;
        if (header.isCaseless()) {
            word = word.toLowerCase(locale);
        }

        return Arrays.binarySearch(words, word) >= 0;
    }

    @Override public float[] unknownVector()
    {
        return unknownVector;
    }

    @Override public int dimensions()
    {
        return header.getVectorLength();
    }

    @Override public int size()
    {
        return header.getWordCount();
    }

    @Override public boolean isCaseless()
    {
        return header.isCaseless();
    }

    static class Header
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

        public int getVersion()
        {
            return version;
        }

        public void setVersion(int version)
        {
            this.version = version;
        }

        public int getWordCount()
        {
            return wordCount;
        }

        public void setWordCount(int wordCount)
        {
            this.wordCount = wordCount;
        }

        public boolean isCaseless()
        {
            return caseless;
        }

        public void setCaseless(boolean caseless)
        {
            this.caseless = caseless;
        }

        public String getLocale()
        {
            return locale;
        }

        public void setLocale(String locale)
        {
            this.locale = locale;
        }

        public int getVectorLength()
        {
            return vectorLength;
        }

        public void setVectorLength(int vectorLength)
        {
            this.vectorLength = vectorLength;
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
}
