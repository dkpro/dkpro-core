package de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings;

import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by schnober on 04.03.16.
 */
public class WordEmbeddingsUtilsTest
{
    @Test
    public void testReadEmbeddingFileTxt()
            throws Exception
    {
        File modelFile = new File(
                Thread.currentThread().getContextClassLoader().getResource("dummy.vec").toURI());
        int expectedSize = 699;
        int expectedDimensions = 50;

        Map<String, double[]> embeddings = WordEmbeddingsUtils
                .readEmbeddingFileTxt(modelFile, false);

        assertEquals(expectedSize, embeddings.size());
        embeddings.values().forEach(vector -> assertEquals(expectedDimensions, vector.length));
    }
}