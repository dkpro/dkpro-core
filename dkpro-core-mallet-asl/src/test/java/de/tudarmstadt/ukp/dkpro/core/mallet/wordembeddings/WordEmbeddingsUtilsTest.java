package de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WordEmbeddingsUtilsTest
{
    @Test
    public void testReadEmbeddingFileTxt()
            throws IOException, URISyntaxException
    {
        File modelFile = new File("src/test/resources/dummy.vec");
        int expectedSize = 699;
        int expectedDimensions = 50;
        boolean hasHeader = false;

        Map<String, double[]> embeddings = WordEmbeddingsUtils
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

        Map<String, double[]> embeddings = WordEmbeddingsUtils
                .readEmbeddingFileTxt(modelFile, hasHeader);

        assertEquals(expectedSize, embeddings.size());
        embeddings.values().forEach(vector -> assertEquals(expectedDimensions, vector.length));
    }
}