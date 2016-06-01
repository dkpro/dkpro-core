/*******************************************************************************
 * Copyright 2015
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.text;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory.FeaturePathIterator;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.io.TextUtils;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.apache.uima.fit.util.JCasUtil.select;

/**
 * This class writes a set of pre-processed documents into a large text file containing one sentence
 * per line and tokens split by whitespaces. Optionally, annotations other than tokens (e.g. lemmas)
 * are written as specified by {@link #PARAM_FEATURE_PATH}.
 */
public class TokenizedTextWriter
        extends JCasFileWriter_ImplBase
{
    private static final String TOKEN_SEPARATOR = " ";
    private static final String NUMBER_REPLACEMENT = "NUM";
    private static final String STOPWORD_REPLACEMENT = "STOP";
    public static final boolean LOWERCASE_STOPWORDS = true;

    /**
     * Encoding for the target file. Default is UTF-8.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String targetEncoding;

    /**
     * The feature path, e.g.
     * {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token/lemma/value} for lemmas. Default:
     * {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token} (i.e. token texts).
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
    @ConfigurationParameter(name = PARAM_NUMBER_REGEX, mandatory = false)
    private String numberRegex;

    /**
     * All the tokens listed in this file (one token per line) are replaced by {@code STOP}. Empty
     * lines and lines starting with {@code #} are ignored. Casing is ignored.
     */
    public static final String PARAM_STOPWORDS_FILE = "stopwordsFile";
    @ConfigurationParameter(name = PARAM_STOPWORDS_FILE, mandatory = false)
    private File stopwordsFile;
    private Set<String> stopwords;

    /**
     * Set the output file extension. Default: {@code .txt}.
     */
    public static final String PARAM_EXTENSION = "extension";
    @ConfigurationParameter(name = PARAM_EXTENSION, mandatory = true, defaultValue = ".txt")
    private String extension = ".txt";

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            stopwords = stopwordsFile == null
                    ? Collections.emptySet()
                    : TextUtils.readStopwordsFile(stopwordsFile, LOWERCASE_STOPWORDS);
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
            for (Sentence sentence : select(aJCas, Sentence.class)) {
                getLogger().trace("Sentence: '" + sentence.getCoveredText() + "'.");
                FeaturePathIterator<AnnotationFS> valueIterator = FeaturePathUtils
                        .featurePathIterator(aJCas, featurePath, Optional.of(sentence));

                if (valueIterator.hasNext()) {
                    // write first token
                    writeToken(outputStream, valueIterator.next().getValue());
                }
                while (valueIterator.hasNext()) {
                    // write other tokens
                    outputStream.write(TOKEN_SEPARATOR.getBytes(targetEncoding));
                    writeToken(outputStream, valueIterator.next().getValue());
                }
                getLogger().trace("End of sentence.");
                outputStream.write(System.lineSeparator().getBytes(targetEncoding));
            }
        }
        catch (FeaturePathException | IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * Write a token while replacing stopwords and numbers if specified,
     *
     * @param outputStream the {@link OutputStream} to write to
     * @param text         the token to write
     * @throws IOException if a low-level I/O error occurs
     */
    private void writeToken(OutputStream outputStream, String text)
            throws IOException
    {
        text = stopwords.contains(text.toLowerCase()) ? STOPWORD_REPLACEMENT : text;
        text = numberRegex != null && text.matches(numberRegex)
                ? NUMBER_REPLACEMENT
                : text;
        outputStream.write(text.getBytes(targetEncoding));
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
