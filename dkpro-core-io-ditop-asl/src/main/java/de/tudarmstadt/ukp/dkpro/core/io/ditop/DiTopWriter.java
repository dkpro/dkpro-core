/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.io.ditop;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.resource.ResourceInitializationException;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.Parameters;

/**
 * This annotator (consumer) writes output files as required by
 * <a href="https://ditop.hs8.de/">DiTop</a>. It requires JCas input annotated by
 * {@link de.tudarmstadt.ukp.dkpro.core.mallet.lda.MalletLdaTopicModelInferencer} using the same
 * model.
 */
@ResourceMetaData(name = "DiTop Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@Parameters(
        exclude = { 
                DiTopWriter.PARAM_TARGET_LOCATION  })
@MimeTypeCapability({MimeTypes.APPLICATION_X_DITOP})
@TypeCapability(
        inputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution" })
public class DiTopWriter
    extends JCasFileWriter_ImplBase
{
    private static final String FIELDSEPARATOR_CONFIGFILE = ";";
    private final static String DOC_TOPICS_FILE = "topics.csv";
    private final static String TOPIC_TERM_FILE = "topicTerm.txt";
    private final static String TOPIC_TERM_MATRIX_FILE = "topicTermMatrix.txt";
    private final static String TOPIC_SUMMARY_FILE = "topicTerm-T15.txt";
    private final static String CONFIG_FILE = "config.all";

    /**
     * The maximum number of topic words to extract. Default: 15
     */
    public static final String PARAM_MAX_TOPIC_WORDS = "maxTopicWords";
    @ConfigurationParameter(name = PARAM_MAX_TOPIC_WORDS, mandatory = true, defaultValue = "15")
    private int maxTopicWords;

    /**
     * A Mallet file storing a serialized {@link ParallelTopicModel}.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    protected File modelLocation;

    /**
     * The corpus name is used to name the corresponding sub-directory and will be set in the
     * configuration file.
     */
    public static final String PARAM_CORPUS_NAME = "corpusName";
    @ConfigurationParameter(name = PARAM_CORPUS_NAME, mandatory = true)
    protected String corpusName;

    /**
     * Directory in which to store output files.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    protected File targetLocation;

    /**
     * If set to true, the new corpus will be appended to an existing config file. If false, the
     * existing file is overwritten. Default: true.
     */
    public static final String PARAM_APPEND_CONFIG = "appendConfig";
    @ConfigurationParameter(name = PARAM_APPEND_CONFIG, mandatory = true, defaultValue = "true")
    protected boolean appendConfig;

    /**
     * If set, only documents with one of the listed collection IDs are written, all others are
     * ignored. If this is empty (null), all documents are written.
     */
    public final static String PARAM_COLLECTION_VALUES = "collectionValues";
    @ConfigurationParameter(name = PARAM_COLLECTION_VALUES, mandatory = false)
    protected String[] collectionValues;

    /**
     * If true (default), only write documents with collection ids matching one of the collection
     * values exactly. If false, write documents with collection ids containing any of the
     * collection value string in collection while ignoring cases.
     */
    public final static String PARAM_COLLECTION_VALUES_EXACT_MATCH = "collectionValuesExactMatch";
    @ConfigurationParameter(name = PARAM_COLLECTION_VALUES_EXACT_MATCH, mandatory = true, defaultValue = "true")
    protected boolean collectionValuesExactMatch;

    private ParallelTopicModel model;
    private File collectionDir;
    protected Set<String> collectionValuesSet;
    private Bag<String> collectionCounter;

    protected BufferedWriter writerDocTopic;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            model = ParallelTopicModel.read(modelLocation);
            collectionDir = new File(targetLocation, corpusName + "_" + model.getNumTopics());
            if (collectionDir.exists()) {
                getLogger().warn(
                        String.format("%s' already exists, overwriting content.", collectionDir));
            }
            collectionDir.mkdirs();
            initializeTopicFile();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

        collectionValuesSet = collectionValues == null ? Collections.<String>emptySet()
                : new HashSet<>(Arrays.asList(collectionValues));
        collectionCounter = new HashBag<>();
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        for (TopicDistribution distribution : select(aJCas, TopicDistribution.class)) {
            String docName = getDocumentId(aJCas);
            String collectionId = getCollectionId(aJCas);

            /* Print and gather collection statistics */
            if (collectionCounter.getCount(collectionId) == 0) {
                getLogger().info("New collection ID observed: " + collectionId);
            }
            collectionCounter.add(collectionId);

            try {
                writeDocTopic(distribution, docName, collectionId);
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    protected void writeDocTopic(TopicDistribution distribution, String docName,
            String collectionId)
        throws IOException
    {
        /* filter by collection id if PARAM_COLLECTION_VALUES is set */
        if (collectionValuesSet.isEmpty() || collectionValuesSet.contains(collectionId)) {
            /* write documents to file */
            writerDocTopic.write(collectionId + ",");
            writerDocTopic.write(docName);
            DoubleArray proportions = distribution.getTopicProportions();
            for (double topicProb : proportions.toArray()) {
                writerDocTopic.write("," + topicProb);
            }
            writerDocTopic.newLine();
        }
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        getLogger().info("Collection statistics: " + collectionCounter.toString());
        getLogger().info(
                collectionValuesSet.isEmpty() ?
                        "Writing all documents." :
                        "Writing documents from these collections only: "
                                + collectionValuesSet.toString());

        try {
            writerDocTopic.close();
            writetermMatrixFiles();
            writeConfigFile();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private void initializeTopicFile()
        throws IOException
    {
        File topicFile = new File(collectionDir, DOC_TOPICS_FILE);
        getLogger().info(String.format("Writing file '%s'.", topicFile.getPath()));
        writerDocTopic = new BufferedWriter(new FileWriter(topicFile));

        /* Write header */
        writerDocTopic.write("Class,Document");
        for (int j = 0; j < model.numTopics; j++) {
            writerDocTopic.write(",T" + j);
        }
        writerDocTopic.newLine();
    }

    /**
     * This method has been copied and slightly adapted from MalletLDA#printTopics in the original
     * DiTop code.
     *
     * @throws IOException
     *             if a low-level I/O error occurs
     */
    private void writetermMatrixFiles()
        throws IOException
    {
        File topicTermFile = new File(collectionDir, TOPIC_TERM_FILE);
        File topicTermMatrixFile = new File(collectionDir, TOPIC_TERM_MATRIX_FILE);
        File topicSummaryFile = new File(collectionDir, TOPIC_SUMMARY_FILE);

        BufferedWriter writerTopicTerm = new BufferedWriter(new FileWriter(topicTermFile));
        BufferedWriter writerTopicTermMatrix = new BufferedWriter(new FileWriter(
                topicTermMatrixFile));
        BufferedWriter writerTopicTermShort = new BufferedWriter(new FileWriter(topicSummaryFile));

        getLogger().info(String.format("Writing file '%s'.", topicTermFile));
        getLogger().info(String.format("Writing file '%s'.", topicTermMatrixFile));
        getLogger().info(String.format("Writing file '%s'.", topicSummaryFile));

        /* Write topic term associations */
        Alphabet alphabet = model.getAlphabet();
        for (int i = 0; i < model.getSortedWords().size(); i++) {
            writerTopicTerm.write("TOPIC " + i + ": ");
            writerTopicTermShort.write("TOPIC " + i + ": ");
            writerTopicTermMatrix.write("TOPIC " + i + ": ");
            /** topic for the label */
            int count = 0;
            TreeSet<IDSorter> set = model.getSortedWords().get(i);
            for (IDSorter s : set) {
                if (count <= maxTopicWords) {
                    writerTopicTermShort.write(alphabet.lookupObject(s.getID()) + ", ");
                }
                count++;
                writerTopicTerm.write(alphabet.lookupObject(s.getID()) + ", ");
                writerTopicTermMatrix.write(alphabet.lookupObject(s.getID()) + " (" + s.getWeight()
                        + "), ");
                /** add to topic label */
            }
            writerTopicTerm.newLine();
            writerTopicTermShort.newLine();
            writerTopicTermMatrix.newLine();
        }

        writerTopicTermMatrix.close();
        writerTopicTerm.close();
        writerTopicTermShort.close();
    }

    private void writeConfigFile()
        throws IOException
    {
        File configFile = new File(targetLocation, CONFIG_FILE);
        Map<String, Set<Integer>> corpora; // holds all corpus names mapped to (multiple) topic
                                           // numbers
        Set<Integer> currentCorpusTopicNumbers; // entry for the current, new topic

        if (appendConfig && configFile.exists()) {
            // read existing entries from config file
            corpora = readConfigFile(configFile);
            currentCorpusTopicNumbers = corpora.containsKey(corpusName) ?
                    corpora.get(corpusName) : new HashSet<>();
        }
        else {
            corpora = new HashMap<>();
            currentCorpusTopicNumbers = new HashSet<>(1, 1);
        }

        currentCorpusTopicNumbers.add(model.getNumTopics());
        corpora.put(corpusName, currentCorpusTopicNumbers);

        getLogger().info(String.format("Writing configuration file '%s'.", configFile.getPath()));
        BufferedWriter configWriter = new BufferedWriter(new FileWriter(configFile));

        for (Entry<String, Set<Integer>> entry : corpora.entrySet()) {
            configWriter.write(entry.getKey());
            for (Integer topicNumber : entry.getValue()) {
                configWriter.write(FIELDSEPARATOR_CONFIGFILE + topicNumber);
            }
            configWriter.newLine();
        }
        configWriter.close();
    }

    /**
     * Read config file in the form <corpus>;<ntopics>[;<ntopics>...]
     * <p>
     * Results in a Map <corpusname>:Set(ntopics1, ...)
     *
     * @param configFile
     *            the config file to read
     * @return a map containing corpus names as keys and a set of topic numbers as values
     * @throws IOException
     *             if an I/O error occurs.
     */
    private static Map<String, Set<Integer>> readConfigFile(File configFile)
        throws IOException
    {
        Map<String, Set<Integer>> entries = new HashMap<>();

        for (String line : FileUtils.readLines(configFile, UTF_8)) {
            String[] fields = line.split(FIELDSEPARATOR_CONFIGFILE);
            if (fields.length < 2) {
                throw new IllegalStateException(String.format(
                        "Could not parse config file '%s': Invalid line:%n%s", configFile, line));
            }
            if (entries.containsKey(fields[0])) {
                throw new IllegalStateException(String.format(
                        "Could not parse config file '%s': duplicate corpus entry '%s'.",
                        configFile, fields[0]));
            }
            Set<Integer> topicCounts = new HashSet<>(fields.length - 1);
            for (int i = 1; i < fields.length; i++) {
                try {
                    topicCounts.add(Integer.parseInt(fields[i]));
                }
                catch (NumberFormatException e) {
                    throw new IllegalStateException(String.format(
                            "Could not parse config file '%s': Invalid topic number '%s'.",
                            configFile, fields[i]));
                }
            }
            entries.put(fields[0], topicCounts);
        }
        return entries;
    }

    /**
     * Extract the collection id from the JCas. Uses {@link DocumentMetaData#getCollectionId()}, but
     * this method can be overwritten to select a different source for the collection id.
     *
     * @param aJCas
     *            the JCas.
     * @return the collection id String or null if it is not available.
     */
    protected String getCollectionId(JCas aJCas)
    {
        String collectionId = DocumentMetaData.get(aJCas).getCollectionId();
        if (collectionId == null) {
            throw new IllegalStateException("Could not extract collection ID for document");
        }

        if (!collectionValuesExactMatch && !collectionValuesSet.contains(collectionId)) {
            collectionId = expandCollectionId(collectionId);
        }

        return collectionId;
    }

    /**
     * This method checks whether any of the specified collection values contains the given String.
     * If it does, returns the matching value; if not, it returns the original value.
     *
     * @param collectionId
     *            the collection ID.
     * @return the first entry from {@code collectionValuesSet} that contains the (lowercased)
     *         {@code collectionId} or the input {@code collectionId}.
     */
    protected String expandCollectionId(String collectionId)
    {
        assert !collectionValuesExactMatch;
        for (String value : collectionValuesSet) {
            if (collectionId.toLowerCase().contains(value.toLowerCase())) {
                getLogger().debug(
                        String.format("Changing collection ID from '%s' to '%s'.",
                                collectionId, value));
                return value;
            }
        }
        return collectionId;
    }

    /**
     * Extract the document id from the JCas. Uses {@link DocumentMetaData#getDocumentId()}, but
     * this method can be overwritten to select a different source for the document id.
     *
     * @param aJCas
     *            the JCas.
     * @return the document id string or null if it is not available.
     */
    protected String getDocumentId(JCas aJCas)
        throws IllegalStateException
    {
        String docName = DocumentMetaData.get(aJCas).getDocumentId();
        if (docName == null) {
            throw new IllegalStateException("Could not extract document ID from metadata.");
        }
        return docName;
    }
}
