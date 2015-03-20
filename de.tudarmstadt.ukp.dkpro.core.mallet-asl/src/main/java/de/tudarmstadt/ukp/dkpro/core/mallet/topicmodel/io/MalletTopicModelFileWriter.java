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
package de.tudarmstadt.ukp.dkpro.core.mallet.topicmodel.io;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.mallet.topicmodel.MalletTopicModelInferencer;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution;

/**
 * Write topic proportions to a file in the shape {@code<docId>\t<topic1>,<topic2>,...}
 * <p>
 * This depends on the {@link TopicDistribution} annotation which should have been created by
 * {@link MalletTopicModelInferencer} before.
 * </p>
 *
 * @author Carsten Schnober
 */
public class MalletTopicModelFileWriter
    extends JCasFileWriter_ImplBase
{
    private static final Locale LOCALE = Locale.US;
    private static final String COLUMN_SEPARATOR = "\t";
    private static final String TOPIC_SEPARATOR = ",";

    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;
    private BufferedWriter targetWriter;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            targetWriter = new BufferedWriter(new FileWriter(targetLocation));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try {
            targetWriter.write(DocumentMetaData.get(aJCas).getDocumentId() + COLUMN_SEPARATOR);
            for (TopicDistribution td : select(aJCas, TopicDistribution.class)) {
                int nTopics = td.getTopicProportions().size();
                for (int i = 0; i < nTopics; i++) {
                    double proportion = td.getTopicProportions(i);
                    targetWriter.write(String.format(LOCALE, "%.4f", proportion));

                    /* write comma unless for last entry */
                    if (i < nTopics - 1) {
                        targetWriter.write(TOPIC_SEPARATOR);
                    }
                }
            }
            targetWriter.newLine();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        try {
            targetWriter.close();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
