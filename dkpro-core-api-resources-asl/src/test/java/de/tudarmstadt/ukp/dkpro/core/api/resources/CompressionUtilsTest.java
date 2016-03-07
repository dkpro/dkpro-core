package de.tudarmstadt.ukp.dkpro.core.api.resources;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class CompressionUtilsTest
{
    private static void testCompression(CompressionMethod compressionMethod)
            throws IOException
    {
        String text = StringUtils.repeat("This is a test. ", 100000);

        File file = new File("compressed" + compressionMethod.getExtension());

        OutputStream os = CompressionUtils.getOutputStream(file);
        os.write(text.getBytes());
        os.close();
        InputStream is = CompressionUtils.getInputStream(file.getPath(), new FileInputStream(file));
        assertEquals(text, IOUtils.toString(is));
        is.close();
        file.delete();
    }

    @Test
    public void testUncompressed()
            throws Exception
    {
        CompressionMethod compressionMethod = CompressionMethod.NONE;
        String text = StringUtils.repeat("This is a test. ", 100000);
        testCompression(compressionMethod);
    }

    @Test
    public void testXZ()
            throws Exception
    {
        CompressionMethod compressionMethod = CompressionMethod.XZ;
        testCompression(compressionMethod);
    }

    @Test
    public void testBZip2()
            throws Exception
    {
        CompressionMethod compressionMethod = CompressionMethod.BZIP2;
        testCompression(compressionMethod);
    }

    @Test
    public void testGZip()
            throws Exception
    {
        CompressionMethod compressionMethod = CompressionMethod.GZIP;
        testCompression(compressionMethod);
    }

    @Test
    public void testPrintWriter()
            throws IOException
    {
        CompressionMethod compressionMethod = CompressionMethod.XZ;
        String text = StringUtils.repeat("This is a test. ", 100000);
        File file = new File("compressed" + compressionMethod.getExtension());
        PrintWriter printWriter = new PrintWriter(CompressionUtils.getOutputStream(file));

        printWriter.write(text);
        printWriter.close();

        InputStream is = CompressionUtils.getInputStream(file.getPath(), new FileInputStream(file));
        assertEquals(text, IOUtils.toString(is));
        is.close();
        file.delete();
    }
}