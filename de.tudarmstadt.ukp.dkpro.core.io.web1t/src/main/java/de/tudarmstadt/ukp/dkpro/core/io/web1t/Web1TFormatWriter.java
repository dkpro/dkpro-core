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

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.teaching.ngram.NGramIterable;

public class Web1TFormatWriter
    extends JCasAnnotator_ImplBase
{
    
    public static final String SENTENCE_START = "<S>";
    public static final String SENTENCE_END = "</S>";
    
    private static final String LF = "\n";
    private static final String TAB = "\t";

    public static final String PARAM_OUTPUT_PATH = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory=true)
    private File outputPath;
    
    public static final String PARAM_OUTPUT_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_OUTPUT_ENCODING, mandatory=true, defaultValue="UTF-8")
    private String outputEncoding;

    public static final String PARAM_MIN_NGRAM_LENGTH = "MinNgramLength";
    @ConfigurationParameter(name = PARAM_MIN_NGRAM_LENGTH, mandatory=true, defaultValue="1")
    private int minNgramLength;

    public static final String PARAM_MAX_NGRAM_LENGTH = "MaxNgramLength";
    @ConfigurationParameter(name = PARAM_MAX_NGRAM_LENGTH, mandatory=true, defaultValue="3")
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

        for (Sentence s : JCasUtil.select(jcas, Sentence.class)) {
            List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, s);
            
            // add sentence begin marker
            List<String> tokenStrings = new ArrayList<String>();
            tokenStrings.add(SENTENCE_START);
            
            // add all token strings
            tokenStrings.addAll(JCasUtil.toText(tokens));
            
            // add sentence end marker
            tokenStrings.add(SENTENCE_END);
            
            for (int ngramLength=minNgramLength; ngramLength<=maxNgramLength; ngramLength++) {
                cfd.addSamples(
                        ngramLength,
                        new NGramIterable(tokenStrings, ngramLength, ngramLength)
                );
            }
        }
        
        // write the frequency distributions to the corresponding n-gram files
        for (int level : cfd.getConditions()) {
            if (!ngramWriters.containsKey(level)) {
                throw new AnalysisEngineProcessException(new IOException("No writer for ngram level " + level + " initialized."));
            }

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
    }
    
    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();
        
        closeWriters(ngramWriters.values());
        
        // read the file with the counts per file and create the final aggregated counts
        for (int level=minNgramLength; level<=maxNgramLength; level++) {
            FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
            try {
                File inputFile = new File(outputPath, level + ".txt");

                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    String[] parts = inputLine.split(TAB);
                    String ngram = parts[0];
                    String count = parts[1];
                    fd.addSample(ngram, new Integer(count));
                }
                reader.close();
                
                File outputPath = new File(inputFile.getParentFile(), level + "gms/");
                outputPath.mkdir();
                File outputFile = new File(outputPath, level + ".txt");
                
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(outputFile), outputEncoding));
                
                List<String> keyList = new ArrayList<String>(fd.getKeys());
                Collections.sort(keyList);
                for (String key : keyList) {
                    writer.write(key);
                    writer.write(TAB);
                    writer.write(new Long(fd.getCount(key)).toString());
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
    
    private Map<Integer, BufferedWriter> initializeWriters(int min, int max) throws ResourceInitializationException {
        Map<Integer, BufferedWriter> writers = new HashMap<Integer, BufferedWriter>();
        for (int level=min; level<=max; level++) {
            try {
                File outputFile = new File(outputPath, level + ".txt");

                if (outputFile.exists()) {
                    outputFile.delete();
                }
                FileUtils.touch(outputFile);

                writers.put(level, new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(outputFile), outputEncoding)));
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
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}