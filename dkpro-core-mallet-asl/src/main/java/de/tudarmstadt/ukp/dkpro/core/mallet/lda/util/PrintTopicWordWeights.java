/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.mallet.lda.util;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extract the n most important words for each topic in the given {@link ParallelTopicModel} files
 * and print them with normalized proportion to a new file.
 * <p>
 * Command line arguments are a list of {@link ParallelTopicModel} file locations each followed by
 * the number of words to print for it, e.g.: {@code java -jar PrintTopicWordWeights model1 100 
 * model2 1000}.
 *
 */
public class PrintTopicWordWeights
{
    private static final Locale LOCALE = Locale.US;
    private static final String FIELD_SEPARATOR = ",";
    private static final String OUTPUTFILE_SUFFIX = ".topics";
    private final static Log LOGGER = LogFactory.getLog(PrintTopicWordWeights.class);
    private ParallelTopicModel model;
    private Alphabet alphabet;
    private final int nWords;

    /**
     * Initialize.
     *
     * @param modelFile
     *            the file containing the model
     * @param nWords
     *            the number of words that should be written for each topic
     * @throws IOException
     *             if the model cannot be read
     */
    public PrintTopicWordWeights(File modelFile, int nWords)
        throws IOException
    {
        try {
            model = ParallelTopicModel.read(modelFile);
        }
        catch (Exception e) {
            throw new IOException(e);
        }
        alphabet = model.getAlphabet();
        this.nWords = nWords;
    }

    /**
     * Write the output for single file.
     *
     * @param targetFile
     *            the file into which the output is written
     *
     * @throws IOException
     *             if an I/O error occurs while writing to the output file
     */
    public void writeWords(File targetFile)
        throws IOException
    {
        targetFile.getParentFile().mkdirs();
        LOGGER.info("Writing output to " + targetFile);
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(targetFile));

        /* iterate over topics */
        for (TreeSet<IDSorter> topic : model.getSortedWords()) {
            int wordCount = 0;
            /* iterate over word IDs in topic (sorted by weight) */
            for (IDSorter id : topic) {
                double weight = id.getWeight() / alphabet.size(); // normalize
                String word = ((String) alphabet.lookupObject(id.getID())).replaceAll("\r\n", " ");

                if (word.contains(FIELD_SEPARATOR)) {
                    LOGGER.debug("Ignoring '" + word + "'.");
                }
                else {
                    outputStream.write(
                            String.format(LOCALE, "%s%s%f", word, FIELD_SEPARATOR, weight));

                    wordCount++;
                    if (wordCount >= nWords) {
                        break; // go to next topic
                    }
                    outputStream.write(FIELD_SEPARATOR); // FIXME: why is there a ',' at the end of
                                                         // each line?
                }
            }
            outputStream.newLine();
        }
        outputStream.close();

    }

    public static void main(String[] args)
        throws IOException
    {
        if (args.length < 2 || args.length % 2 != 0) {
            printHelp();
            System.exit(1);
        }

        /* iterate over input files */
        for (int i = 0; i < args.length; i += 2) {
            File modelFile = new File(args[i]);
            int nWords = Integer.parseInt(args[i + 1]);

            PrintTopicWordWeights writer = new PrintTopicWordWeights(modelFile, nWords);

            File targetFile = new File(modelFile.getAbsolutePath() + OUTPUTFILE_SUFFIX + nWords);
            writer.writeWords(targetFile);
        }
    }

    private static void printHelp()
    {
        System.out.println(
                "command line arguments: <model_file_1> <nWords_1> [<file_2> <nWords_2> ...]");
    }
}
