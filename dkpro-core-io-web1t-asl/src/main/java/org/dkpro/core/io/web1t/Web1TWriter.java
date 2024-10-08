/*
 * Copyright 2017
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
package org.dkpro.core.io.web1t;

import java.io.IOException;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.web1t.util.Web1TConverter;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.Parameters;

/**
 * Web1T n-gram index format writer.
 */
@ResourceMetaData(name = "Web1T N-Gram Index Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@Parameters(exclude = { Web1TWriter.PARAM_TARGET_LOCATION })
@MimeTypeCapability({ MimeTypes.TEXT_X_NGRAM })
@TypeCapability(inputs = { Sentence._TypeName })
public class Web1TWriter
    extends JCasAnnotator_ImplBase
{
    /**
     * Types to generate n-grams from.
     *
     * Example: {@code Token.class.getName() + "/pos/PosValue"} for part-of-speech n-grams
     */
    public static final String PARAM_INPUT_TYPES = "inputTypes";
    @ConfigurationParameter(name = PARAM_INPUT_TYPES, mandatory = true)
    private Set<String> inputPaths;

    /**
     * Location to which the output is written.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private String outputPath;

    /**
     * Character encoding of the output data.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = false, defaultValue = "UTF-8")
    private String outputEncoding;

    /**
     * Minimum n-gram length.
     */
    public static final String PARAM_MIN_NGRAM_LENGTH = "minNgramLength";
    @ConfigurationParameter(name = PARAM_MIN_NGRAM_LENGTH, mandatory = false, defaultValue = "1")
    private int minNgramLength;

    /**
     * Maximum n-gram length.
     */
    public static final String PARAM_MAX_NGRAM_LENGTH = "maxNgramLength";
    @ConfigurationParameter(name = PARAM_MAX_NGRAM_LENGTH, mandatory = false, defaultValue = "3")
    private int maxNgramLength;

    /**
     * Create a lower case index.
     */
    public static final String PARAM_LOWERCASE = "lowercase";
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = false, defaultValue = "false")
    private boolean lowercase;

    /**
     * Create the indexes that jWeb1T needs to operate. (default: true)
     */
    public static final String PARAM_CREATE_INDEXES = "createIndexes";
    @ConfigurationParameter(name = PARAM_CREATE_INDEXES, mandatory = false, defaultValue = "true")
    private boolean createIndexes;

    /**
     * Specifies the minimum frequency a NGram must have to be written to the final index. The
     * specified value is interpreted as inclusive value, the default is 1. Thus, all NGrams with a
     * frequency of at least 1 or higher will be written.
     */
    public static final String PARAM_MIN_FREQUENCY = "minFreq";
    @ConfigurationParameter(name = PARAM_MIN_FREQUENCY, mandatory = false, defaultValue = "1")
    private int minFreq;

    /**
     * The input file(s) is/are split into smaller files for quick access. An own file is created if
     * the first two starting letters (or the starting letter if the word has a length of 1
     * character) account for at least x% of all starting letters in the input file(s). The default
     * value for splitting a file is 1.0%. Every word that has starting characters which does not
     * suffice the threshold is written with other words that also did not meet the threshold into
     * an own file for miscellaneous words. A high threshold will lead to only a few, but large
     * files and a most likely very large misc. file. A low threshold results in many small files.
     * Use a zero or a negative value to write everything to one file.
     */
    public static final String PARAM_SPLIT_TRESHOLD = "splitFileTreshold";
    @ConfigurationParameter(name = PARAM_SPLIT_TRESHOLD, mandatory = false, defaultValue = "1.0")
    private float splitThreshold;

    /**
     * The type being used for segments
     */
    public static final String PARAM_CONTEXT_TYPE = "contextType";
    @ConfigurationParameter(name = PARAM_CONTEXT_TYPE, mandatory = true, defaultValue = Sentence._TypeName)
    protected String contextType;

    private Web1TConverter converter;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            converter = new Web1TConverter(outputPath, minNgramLength, maxNgramLength);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        converter.setWriteIndexes(createIndexes);
        converter.setSplitThreshold(splitThreshold);
        converter.setMinFrequency(minFreq);
        converter.setToLowercase(lowercase);
        converter.setOutputEncoding(outputEncoding);
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException
    {

        try {
            converter.add(jcas, inputPaths, jcas.getCas().getTypeSystem().getType(contextType));
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * The input files for each ngram level is read, splitted according to the frequency of the
     * words starting letter in the files and the split files are individually sorted and
     * consolidated.
     */
    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            converter.createIndex();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
