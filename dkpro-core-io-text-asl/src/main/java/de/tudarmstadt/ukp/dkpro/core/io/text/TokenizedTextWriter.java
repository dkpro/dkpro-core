/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.io.text;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator.PhraseSequenceGenerator;
import de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator.StringSequenceGenerator;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * This class writes a set of pre-processed documents into a large text file containing one sentence
 * per line and tokens split by whitespaces. Optionally, annotations other than tokens (e.g. lemmas)
 * are written as specified by {@link #PARAM_FEATURE_PATH}.
 */
@ResourceMetaData(name = "Tokenized Text Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.TEXT_PLAIN})
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class TokenizedTextWriter
        extends JCasFileWriter_ImplBase
{
    private static final String TOKEN_SEPARATOR = " ";
    private static final String NUMBER_REPLACEMENT = "NUM";
    private static final String STOPWORD_REPLACEMENT = "STOP";
    private static final String DEFAULT_COVERING_TYPE = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence";

    /**
     * Encoding for the target file. Default is UTF-8.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String targetEncoding;

    /**
     * The feature path, e.g.
     * {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token/lemma/value} for lemmas.
     * Default: {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token} (i.e. token
     * texts).
     */
    public static final String PARAM_FEATURE_PATH = "featurePath";
    /**
     * All tokens that match this regex are replaced by {@code NUM}. Examples:
     * <ul>
     * <li>^[0-9]+$
     * <li>^[0-9,\.]+$
     * <li>^[0-9]+(\.[0-9]*)?$
     * </ul>
     * <p>
     * Make sure that these regular expressions are fit to the segmentation, e.g. if your work on
     * tokens, your tokenizer might split prefixes such as + and - from the rest of the number.
     */
    @ConfigurationParameter(name = PARAM_FEATURE_PATH, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String featurePath;

    public static final String PARAM_NUMBER_REGEX = "numberRegex";
    @ConfigurationParameter(name = PARAM_NUMBER_REGEX, mandatory = true, defaultValue = "")
    private String numberRegex;

    /**
     * All the tokens listed in this file (one token per line) are replaced by {@code STOP}. Empty
     * lines and lines starting with {@code #} are ignored. Casing is ignored.
     */
    public static final String PARAM_STOPWORDS_FILE = "stopwordsFile";
    @ConfigurationParameter(name = PARAM_STOPWORDS_FILE, mandatory = true, defaultValue = "")
    private String stopwordsFile;

    /**
     * Set the output file extension. Default: {@code .txt}.
     */
    public static final String PARAM_EXTENSION = "extension";
    @ConfigurationParameter(name = PARAM_EXTENSION, mandatory = true, defaultValue = ".txt")
    private String extension = ".txt";

    /**
     * In the output file, each unit of the covering type is written into a separate line. The
     * default (set in {@link #DEFAULT_COVERING_TYPE}), is sentences so that each sentence is
     * written to a line.
     * <p>
     * If no linebreaks within a document is desired, set this value to {@code null}.
     */
    public static final String PARAM_COVERING_TYPE = "coveringType";
    @ConfigurationParameter(name = PARAM_COVERING_TYPE, mandatory = true, 
            defaultValue = DEFAULT_COVERING_TYPE)
    private String coveringType;

    private StringSequenceGenerator sequenceGenerator;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            sequenceGenerator = new PhraseSequenceGenerator.Builder()
                    .featurePath(featurePath)
                    .filterRegex(numberRegex)
                    .filterRegexReplacement(NUMBER_REPLACEMENT)
                    .stopwordsFile(stopwordsFile)
                    .stopwordsReplacement(STOPWORD_REPLACEMENT)
                    .coveringType(coveringType)
                    .buildStringSequenceGenerator();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
     */
    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        try {
            OutputStream outputStream = getOutputStream(aJCas, extension);

            /* iterate over sentences */
            for (String[] line : sequenceGenerator.tokenSequences(aJCas)) {
                if (line.length > 0) {
                    /* write first token */
                    outputStream.write(line[0].getBytes(targetEncoding));

                    /* write remaining tokens with token separator */
                    for (int i = 1; i < line.length; i++) {
                        outputStream.write((TOKEN_SEPARATOR + line[i]).getBytes(targetEncoding));
                    }
                }
                outputStream.write(System.lineSeparator().getBytes(targetEncoding));
            }
        }
        catch (FeaturePathException | IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        if (getTargetLocation() == null) {
            getLogger().info("Output written to file <stdout>");
        }
        else {
            getLogger().info("Output written to file " + getTargetLocation());
        }

        super.collectionProcessComplete();
    }
}
