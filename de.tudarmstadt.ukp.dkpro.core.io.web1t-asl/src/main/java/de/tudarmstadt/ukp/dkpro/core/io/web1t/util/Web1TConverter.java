/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.io.web1t.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import com.googlecode.jweb1t.JWeb1TIndexer;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathInfo;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringIterable;

public class Web1TConverter
{

    public static final String SENTENCE_START = "<S>";
    public static final String SENTENCE_END = "</S>";

    private static final String LF = "\n";
    private static final String TAB = "\t";

    private final String outputPath;
    private String outputEncoding = "UTF-8";
    private int minNgramLength = 1;
    private int maxNgramLength = 3;
    private int minFrequency = 1;
    private boolean toLowercase = false;
    private boolean writeIndexes = true;
    private float splitThreshold = 1.0f;

    private final Map<Integer, BufferedWriter> ngramWriters;
    private final Map<Integer, FrequencyDistribution<String>> letterFDs;

    public Web1TConverter(String outputPath)
        throws IOException
    {
        super();

        this.outputPath = outputPath;

        ngramWriters = initializeWriters(minNgramLength, maxNgramLength);
        letterFDs = initializeLetterFDs(minNgramLength, maxNgramLength);

        if (splitThreshold >= 100) {
            throw new IllegalArgumentException("Threshold has to be lower 100");
        }
    }

    public void add(JCas jcas, Set<String> inputPaths)
        throws IOException
    {
        ConditionalFrequencyDistribution<Integer, String> cfd = new ConditionalFrequencyDistribution<Integer, String>();

        CAS cas = jcas.getCas();
        Type sentenceType = cas.getTypeSystem().getType(Sentence.class.getName());

        for (AnnotationFS annotation : CasUtil.select(cas, sentenceType)) {

            for (String path : inputPaths) {

                String[] segments = path.split("/", 2);
                String typeName = segments[0];

                Type type = getInputType(cas, typeName);

                List<AnnotationFS> tokens = CasUtil.selectCovered(cas, type, annotation);

                List<String> tokenStrings;
                try {
                    tokenStrings = createStringList(tokens, segments);
                }
                catch (AnalysisEngineProcessException e) {
                    throw new IOException(e);
                }

                for (int ngramLength = minNgramLength; ngramLength <= maxNgramLength; ngramLength++) {
                    cfd.incAll(ngramLength, new NGramStringIterable(tokenStrings, ngramLength,
                            ngramLength));
                }
            }
        }

        add(cfd);
    }

    public void add(ConditionalFrequencyDistribution<Integer, String> cfd)
        throws IOException
    {
        writeFrequencyDistributionsToNGramFiles(cfd);
    }

    public void createIndex()
        throws IOException
    {

        closeWriters(ngramWriters.values());

        Comparator<String> comparator = new Comparator<String>()
        {
            @Override
            public int compare(String r1, String r2)
            {
                return r1.compareTo(r2);
            }
        };

        // read the file with the counts per file and create the final
        // aggregated counts
        for (int level = minNgramLength; level <= maxNgramLength; level++) {
            Integer nextFreeFileNumber = processInputFileForLevel(level, comparator);

            processCreatedMiscFileAgain(level, comparator, nextFreeFileNumber);
        }

        if (writeIndexes) {
            JWeb1TIndexer indexer = new JWeb1TIndexer(outputPath, maxNgramLength);
            indexer.create();
        }
    }

    private int processInputFileForLevel(int level, Comparator<String> comparator)
        throws IOException
    {

        File unsortedInputFile = new File(outputPath, level + ".txt");

        File outputFolder = getOutputFolder(level);
        outputFolder.mkdir();

        FrequencyDistribution<String> letterFD = letterFDs.get(level);

        Web1TFileSplitter splitter = new Web1TFileSplitter(unsortedInputFile, outputFolder,
                outputEncoding, letterFD, splitThreshold, 0);

        splitter.split();
        List<File> splitFiles = splitter.getFiles();

        Web1TFileSorter sorter = new Web1TFileSorter(splitFiles, comparator);
        sorter.sort();
        splitter.cleanUp(); // Remove files from previous step

        LinkedList<File> sortedFiles = sorter.getSortedFiles();

        Web1TFileConsolidator consolidator = new Web1TFileConsolidator(sortedFiles, comparator,
                outputEncoding, minFrequency);

        consolidator.consolidate();
        sorter.cleanUp(); // Remove files from previous step

        LinkedList<File> consolidatedFiles = consolidator.getConsolidatedFiles();

        // rename consolidated files -> final index files
        for (File file : consolidatedFiles) {
            String name = Web1TUtil.cutOffUnderscoredSuffixFromFileName(file);
            file.renameTo(new File(name));
        }

        consolidator.cleanUp();

        unsortedInputFile.delete();

        return splitter.getNextUnusedFileNumber();
    }

    /**
     * Write the frequency distributions to the corresponding n-gram files.
     */
    private void writeFrequencyDistributionsToNGramFiles(
            ConditionalFrequencyDistribution<Integer, String> cfd)
        throws IOException
    {
        for (int level : cfd.getConditions()) {

            if (!ngramWriters.containsKey(level)) {
                throw new IOException("No writer for ngram level " + level + " initialized.");
            }

            writeNGramFile(cfd, level);

        }
    }

    private void writeNGramFile(ConditionalFrequencyDistribution<Integer, String> cfd, int level)
        throws IOException
    {
        FrequencyDistribution<String> letterFD = letterFDs.get(level);
        BufferedWriter writer = ngramWriters.get(level);
        for (String key : cfd.getFrequencyDistribution(level).getKeys()) {

            // add starting letter to frequency distribution
            if (key.length() > 1) {
                String subsKey = key.substring(0, 2);
                String subsKeyLowered = subsKey.toLowerCase();
                letterFD.addSample(subsKeyLowered, 1);
            }
            else {
                String subsKey = key.substring(0, 1);
                String subsKeyLowered = subsKey.toLowerCase();
                letterFD.addSample(subsKeyLowered, 1);
            }

            writer.write(key);
            writer.write(TAB);
            writer.write(Long.toString(cfd.getCount(level, key)));
            writer.write(LF);
        }
        writer.flush();
    }

    private List<String> createStringList(List<AnnotationFS> tokens, String[] segments)
        throws AnalysisEngineProcessException
    {

        List<String> tokenStrings = new ArrayList<String>();
        tokenStrings.add(SENTENCE_START);

        FeaturePathInfo fp = new FeaturePathInfo();
        initializeFeaturePathInfoFrom(fp, segments);

        for (AnnotationFS annotation : tokens) {
            String value = fp.getValue(annotation);
            if (!StringUtils.isBlank(value)) {
                if (toLowercase) {
                    value = value.toLowerCase();
                }
                tokenStrings.add(value);
            }
        }

        tokenStrings.add(SENTENCE_END);

        return tokenStrings;
    }

    private Type getInputType(CAS cas, String typeName)
    {
        Type type = cas.getTypeSystem().getType(typeName);
        if (type == null) {
            throw new IllegalStateException("Type [" + typeName + "] not found in type system");
        }

        return type;
    }

    private void initializeFeaturePathInfoFrom(FeaturePathInfo aFp, String[] featurePathString)
        throws AnalysisEngineProcessException

    {
        try {
            if (featurePathString.length > 1) {
                aFp.initialize(featurePathString[1]);
            }
            else {
                aFp.initialize("");
            }
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * The default file for words which do not account for <code>thresholdSplit</code> percent may
     * have grown large. In order to prevent an real large misc. file we split again.
     */
    private void processCreatedMiscFileAgain(int level, Comparator<String> comparator,
            int nextFileNumber)
        throws IOException
    {
        File folder = getOutputFolder(level);
        File misc = new File(folder, "99999999");

        if (!misc.exists()) {
            return;
        }

        FrequencyDistribution<String> letterFD = createFreqDistForMiscFile(misc);

        float oldThreshold = splitThreshold;
        // Make sure that the misc file is split into little pieces
        splitThreshold /= 10;

        Web1TFileSplitter splitter = new Web1TFileSplitter(misc, folder, "UTF-8", letterFD,
                splitThreshold, nextFileNumber);
        splitter.split();
        List<File> splittedFiles = splitter.getFiles();

        Web1TFileSorter sorter = new Web1TFileSorter(splittedFiles, comparator);
        sorter.sort();
        List<File> sortedFiles = splitter.getFiles();

        splitThreshold = oldThreshold;
        misc.delete();

        Web1TFileConsolidator consolidator = new Web1TFileConsolidator(sortedFiles, comparator,
                outputEncoding, minFrequency);
        consolidator.consolidate();

        LinkedList<File> consolidatedFiles = consolidator.getConsolidatedFiles();

        // rename consolidated files -> final index files
        for (File file : consolidatedFiles) {
            String name = Web1TUtil.cutOffUnderscoredSuffixFromFileName(file);
            file.renameTo(new File(name));
        }

        splitter.cleanUp();
        sorter.cleanUp();
        consolidator.cleanUp();
    }

    /**
     * Creates a new frequency distribution over the starting letters in the misc file as
     * preparation for splitting
     */
    private FrequencyDistribution<String> createFreqDistForMiscFile(File misc)
        throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(misc),
                outputEncoding));

        FrequencyDistribution<String> letterFD = new FrequencyDistribution<String>();

        String readLine = null;
        while ((readLine = reader.readLine()) != null) {
            int indexOfTab = readLine.indexOf(TAB);
            String key = getStartingLetters(readLine, indexOfTab);
            letterFD.addSample(key, 1);
        }
        reader.close();
        return letterFD;
    }

    // private void writeToLog(String desc, String entry) {
    // getContext().getLogger().log(Level.WARNING, desc + entry);
    // }

    private File getOutputFolder(int level)
    {

        return new File(outputPath + "/" + level + "gms");
    }

    private String getStartingLetters(String readLine, int indexOfTab)
    {
        String line = readLine.substring(0, indexOfTab);

        String key = null;
        if (line.length() > 1) {
            key = readLine.substring(0, 2);
        }
        else {
            key = readLine.substring(0, 1);
        }
        key = key.toLowerCase();
        return key;
    }

    private Map<Integer, FrequencyDistribution<String>> initializeLetterFDs(int min, int max)
    {

        Map<Integer, FrequencyDistribution<String>> fdistMap = new HashMap<Integer, FrequencyDistribution<String>>();

        for (int i = min; i <= max; i++) {
            FrequencyDistribution<String> fdist = new FrequencyDistribution<String>();
            fdistMap.put(i, fdist);
        }

        return fdistMap;
    }

    private Map<Integer, BufferedWriter> initializeWriters(int min, int max)
        throws IOException
    {
        Map<Integer, BufferedWriter> writers = new HashMap<Integer, BufferedWriter>();
        for (int level = min; level <= max; level++) {
            File outputFile = new File(outputPath, level + ".txt");

            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileUtils.touch(outputFile);

            writers.put(level, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    outputFile), outputEncoding)));
        }
        return writers;
    }

    private void closeWriters(Collection<BufferedWriter> writers)
        throws IOException
    {
        for (BufferedWriter writer : writers) {
            writer.close();
        }
    }

    public boolean isWriteIndexes()
    {
        return writeIndexes;
    }

    public void setWriteIndexes(boolean writeIndexes)
    {
        this.writeIndexes = writeIndexes;
    }

    public float getSplitThreshold()
    {
        return splitThreshold;
    }

    public void setSplitThreshold(float splitThreshold)
    {
        this.splitThreshold = splitThreshold;
    }

    public String getOutputEncoding()
    {
        return outputEncoding;
    }

    public void setOutputEncoding(String outputEncoding)
    {
        this.outputEncoding = outputEncoding;
    }

    public int getMinNgramLength()
    {
        return minNgramLength;
    }

    public void setMinNgramLength(int minNgramLength)
    {
        this.minNgramLength = minNgramLength;
    }

    public int getMaxNgramLength()
    {
        return maxNgramLength;
    }

    public void setMaxNgramLength(int maxNgramLength)
    {
        this.maxNgramLength = maxNgramLength;
    }

    public int getMinFrequency()
    {
        return minFrequency;
    }

    public void setMinFrequency(int minFrequency)
    {
        if (minFrequency < 1) {
            throw new IllegalArgumentException("Parameter MIN_FREQUENCY is invalid (must be >= 1)");
        }

        this.minFrequency = minFrequency;
    }

    public boolean isToLowercase()
    {
        return toLowercase;
    }

    public void setToLowercase(boolean toLowercase)
    {
        this.toLowercase = toLowercase;
    }

}