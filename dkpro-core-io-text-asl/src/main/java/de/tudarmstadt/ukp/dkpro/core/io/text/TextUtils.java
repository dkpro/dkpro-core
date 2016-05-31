package de.tudarmstadt.ukp.dkpro.core.io.text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;

public class TextUtils
{
    /**
     * Read a file containing stopwords (one per line).
     * <p>
     * Empty lines and lines starting with ("#") are filtered out.
     *
     * @param file      input file
     * @param lowercase if true, lowercase everything
     * @return a collection of unique stopwords
     * @throws IOException
     */
    public static Set<String> readStopwordsFile(File file, boolean lowercase)
            throws IOException
    {
        return Files.readAllLines(file.toPath()).stream()
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .filter(l -> !l.startsWith("#"))
                .map(l -> lowercase ? l.toLowerCase() : l)
                .collect(Collectors.toSet());
    }
}
