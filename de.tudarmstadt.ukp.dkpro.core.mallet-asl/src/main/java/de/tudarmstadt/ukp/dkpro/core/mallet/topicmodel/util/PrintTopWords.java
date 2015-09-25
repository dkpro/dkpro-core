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
package de.tudarmstadt.ukp.dkpro.core.mallet.topicmodel.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.topics.ParallelTopicModel;

/**
 * Print the top n words for each topic of a {@link ParallelTopicModel}.
 *
 */
public class PrintTopWords
{
    private static final String TARGET_FILE_SUFFIX = ".twords";
    private static File modelFile;
    private static int nWords;
    private static final Logger LOG = LoggerFactory.getLogger(PrintTopWords.class);

    public static void main(String[] args)
        throws IOException
    {
        setOptions(args);
        String targetFile = modelFile + TARGET_FILE_SUFFIX;
        LOG.info(String.format("%nReading model from '%s'.%nStoring topic words in '%s'.",
                modelFile, targetFile));
        
        ParallelTopicModel model;
        try {
            model = ParallelTopicModel.read(modelFile);
        }
        catch (Exception e) {
            throw new IOException(e);
        }
        model.printTopWords(new File(targetFile), nWords + 1, false);
    }

    private static void setOptions(String[] args)
    {
        Options options = new Options();
        options.addOption("m", "model", true, "The model file");
        options.addOption("n", "nWords", true, "The number of topic words to print. Default: 10.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cli;
        try {
            cli = parser.parse(options, args);
            modelFile = new File(cli.getOptionValue("model"));
            nWords = Integer.parseInt(cli.getOptionValue("nWords", "10"));
        }
        catch (ParseException | NullPointerException e) {
            new HelpFormatter().printHelp("java -jar PrintTopicWords.jar", options);
            throw new IllegalArgumentException(e);
        }
    }
}
