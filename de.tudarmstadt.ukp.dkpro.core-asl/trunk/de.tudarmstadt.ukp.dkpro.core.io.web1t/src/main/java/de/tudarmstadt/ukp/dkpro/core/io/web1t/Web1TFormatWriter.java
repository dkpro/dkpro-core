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
package de.tudarmstadt.ukp.dkpro.core.io.web1t;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.CasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathInfo;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringIterable;

public class Web1TFormatWriter
    extends JCasAnnotator_ImplBase
{

    public static final String SENTENCE_START = "<S>";
    public static final String SENTENCE_END = "</S>";

    private static final String LF = "\n";
    private static final String TAB = "\t";

    public static final String PARAM_INPUT_TYPES = "inputTypes";
    @ConfigurationParameter(name = PARAM_INPUT_TYPES, mandatory = true)
    private Set<String> inputPaths;

    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File outputPath;

    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String outputEncoding;

    public static final String PARAM_MIN_NGRAM_LENGTH = "MinNgramLength";
    @ConfigurationParameter(name = PARAM_MIN_NGRAM_LENGTH, mandatory = true, defaultValue = "1")
    private int minNgramLength;

    public static final String PARAM_MAX_NGRAM_LENGTH = "MaxNgramLength";
    @ConfigurationParameter(name = PARAM_MAX_NGRAM_LENGTH, mandatory = true, defaultValue = "3")
    private int maxNgramLength;

    private Map<Integer, BufferedWriter> ngramWriters;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        ngramWriters = initializeWriters(minNgramLength, maxNgramLength);
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
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

                List<String> tokenStrings = createStringList(tokens, segments);

                for (int ngramLength = minNgramLength; ngramLength <= maxNgramLength; ngramLength++) {
                    cfd.addSamples(
                            ngramLength,
                            new NGramStringIterable(tokenStrings, ngramLength, ngramLength)
                    );
                }
            }
        }

        writeFrequencyDistributionsToNGramFiles(cfd);

    }

    /**
     * Write the frequency distributions to the corresponding n-gram files.
     * @param cfd
     * @throws AnalysisEngineProcessException
     */
    private void writeFrequencyDistributionsToNGramFiles(ConditionalFrequencyDistribution<Integer, String> cfd)
        throws AnalysisEngineProcessException
    {
        for (int level : cfd.getConditions()) {

            if (!ngramWriters.containsKey(level)) {
                throw new AnalysisEngineProcessException(new IOException("No writer for ngram level " + level
                        + " initialized."));
            }

            writeNGramFile(cfd, level);

        }
    }

    private void writeNGramFile(ConditionalFrequencyDistribution<Integer, String> cfd, int level)
        throws AnalysisEngineProcessException
    {
        try {
            BufferedWriter writer = ngramWriters.get(level);
            for (String key : cfd.getFrequencyDistribution(level).getKeys()) {
                writer.write(key);
                writer.write(TAB);
                writer.write(Long.toString(cfd.getCount(level, key)));
                writer.write(LF);
            }
            writer.flush();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
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

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        closeWriters(ngramWriters.values());

        // read the file with the counts per file and create the final
        // aggregated counts
        for (int level = minNgramLength; level <= maxNgramLength; level++) {
            FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
            try {
                File inputFile = new File(outputPath, level + ".txt");

                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    String[] parts = inputLine.split(TAB);
                    if (parts.length != 2) {
                        getLogger().warn("Wrong file format in line: " + inputLine);
                        continue;
                    }
                    String ngram = parts[0];
                    String count = parts[1];
                    fd.addSample(ngram, new Integer(count));
                }
                reader.close();

                File outputPath = new File(inputFile.getParentFile(), level + "gms/");
                FileUtils.forceMkdir(outputPath);
                File outputFile = new File(outputPath, level + ".txt");

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
                        outputEncoding));

                List<String> keyList = new ArrayList<String>(fd.getKeys());
                Collections.sort(keyList);
                for (String key : keyList) {
                    writer.write(key);
                    writer.write(TAB);
                    writer.write(Long.valueOf(fd.getCount(key)).toString());
                    writer.write(LF);
                }
                writer.flush();
                writer.close();

                // cleanup
                if (!inputFile.delete()) {
                    throw new IOException("Could not clean up.");
                }
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    private Map<Integer, BufferedWriter> initializeWriters(int min, int max)
        throws ResourceInitializationException
    {
        Map<Integer, BufferedWriter> writers = new HashMap<Integer, BufferedWriter>();
        for (int level = min; level <= max; level++) {
            try {
                File outputFile = new File(outputPath, level + ".txt");

                if (outputFile.exists()) {
                    if (!outputFile.delete()) {
                        throw new IOException("Could not delete already existing output file.");
                    }
                }
                FileUtils.touch(outputFile);

                writers.put(level, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
                        outputEncoding)));
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        }
        return writers;
    }

    private void closeWriters(Collection<BufferedWriter> writers)
        throws AnalysisEngineProcessException
    {
        try {
            for (BufferedWriter writer : writers) {
                writer.close();
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}