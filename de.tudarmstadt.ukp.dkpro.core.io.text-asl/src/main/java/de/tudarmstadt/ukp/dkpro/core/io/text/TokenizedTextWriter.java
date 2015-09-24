/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.text;

import static org.apache.uima.fit.util.CasUtil.selectCovered;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory.FeaturePathIterator;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathInfo;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

/**
 * This class writes a set of pre-processed documents into a large text file containing one sentence
 * per line and tokens split by whitespaces. Optionally, annotations other than tokens (e.g. lemmas)
 * are written as specified by {@link #PARAM_FEATURE_PATH}.
 */
public class TokenizedTextWriter
    extends JCasFileWriter_ImplBase
{
    private static final String TOKEN_SEPARATOR = " ";

    /**
     * The feature path, e.g.
     * {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma/value} for lemmas. Default:
     * {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token} (i.e. token texts).
     * <p>
     * In order to specify a different annotation use the annotation class' type name (e.g.
     * {@code Token.class.getTypeName()}) and optionally append a field, e.g. {@code /value} to
     * specify the feature path. If you do not specify a field, the covered text is used.
     * 
     */
    public static final String PARAM_FEATURE_PATH = "featurePath";
    @ConfigurationParameter(name = PARAM_FEATURE_PATH, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token") /// coveredText()")
    private String featurePath;

    private BufferedWriter targetWriter;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        getLogger().info("Writing to file " + getTargetLocation());
        try {
            targetWriter = new BufferedWriter(new FileWriter(getTargetLocation()));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    };

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
        String[] segments = featurePath.split("/", 2);
        String typeName = segments[0];

        Type type = aJCas.getTypeSystem().getType(typeName);
        if (type == null) {
            throw new IllegalStateException("Type [" + typeName + "] not found in type system");
        }

        try {
            FeaturePathInfo fpInfo = initFeaturePathInfo(segments);
            writeTokens(aJCas, type, fpInfo);
        }
        catch (FeaturePathException | IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * Iterates over each {@link Sentence} and writes all the contained elements (e.g. tokens) of
     * the specified annotations type to the output file. Every sentence is written to a single
     * line; the tokens are separated by whitespaces.
     * 
     * @param aJCas
     *            a {@link JCas}
     * @param type
     *            the annotation {@link Type}
     * @param fpInfo
     *            a {@link FeaturePathInfo} to be used to the {@link FeaturePathIterator}
     * @throws IOException
     *             if an IO error occurs while writing
     */
    private void writeTokens(JCas aJCas, Type type, FeaturePathInfo fpInfo)
        throws IOException
    {
        /* iterate over sentences */
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            getLogger().trace("Sentence: '" + sentence.getCoveredText() + "'.");
            FeaturePathIterator<AnnotationFS> valueIter = new FeaturePathIterator<>(
                    selectCovered(aJCas.getCas(), type, sentence).iterator(), fpInfo);

            boolean isFirst = true; // this is the first token of a sentence
            while (valueIter.hasNext()) {
                String text = valueIter.next().getValue();
                targetWriter.write(isFirst ? text : TOKEN_SEPARATOR + text);
                isFirst = false;
            }
            getLogger().trace("End of sentence.");
            targetWriter.newLine();
        }
    }

    /**
     * Generate a feature path info.
     * 
     * @param segments
     *            an array of strings previously split so that the first element represents the
     *            feature type and the second element (if applicable) contains the feature path.
     * @return a {@link FeaturePathInfo}
     * @throws FeaturePathException
     *             if an error occurs during initialization of the feature path
     */
    private FeaturePathInfo initFeaturePathInfo(String[] segments)
        throws FeaturePathException
    {
        FeaturePathInfo fpInfo = new FeaturePathInfo();
        fpInfo.initialize(segments.length > 1 ? segments[1] : "");
        return fpInfo;
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        IOUtils.closeQuietly(targetWriter);
        getLogger().info("Output written to file " + getTargetLocation());
        super.collectionProcessComplete();
    }
}
