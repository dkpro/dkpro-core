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
package de.tudarmstadt.ukp.dkpro.core.mallet.lda.io;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.mallet.lda.LdaTopicModelInferencer;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import static org.apache.uima.fit.util.JCasUtil.select;

/**
 * Write topic proportions to a file in the shape {@code [<docId>\t]<topic_1>\t<topic_2>\t...<topic_n>}
 * <p>
 * This writer depends on the {@link TopicDistribution} annotation which needs to be created by
 * {@link LdaTopicModelInferencer} before.
 * </p>
 */
public class LdaTopicProportionsWriter
        extends JCasFileWriter_ImplBase
{
    private static final Locale LOCALE = Locale.US;
    private static final String COLUMN_SEPARATOR = "\t";

    /**
     * If set to true (default), each output line is preceded by the document id.
     */
    public static final String PARAM_WRITE_DOCID = "writeDocid";
    @ConfigurationParameter(name = PARAM_WRITE_DOCID, mandatory = true, defaultValue = "true")
    private boolean writeDocid;

    /**
     * If {@link #PARAM_SINGULAR_TARGET} is set to false (default), this extension will be appended to the output
     * files. Default: {@code .topics}.
     */
    public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".topics")
    private String filenameExtension;

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        try {
            OutputStream outputStream = getOutputStream(aJCas, filenameExtension);

            if (writeDocid) {
                outputStream.write(DocumentMetaData.get(aJCas).getDocumentId().getBytes());
                outputStream.write(COLUMN_SEPARATOR.getBytes());
            }

            for (TopicDistribution td : select(aJCas, TopicDistribution.class)) {
                int nTopics = td.getTopicProportions().size();

                for (int i = 0; i < nTopics; i++) {
                    outputStream.write(
                            String.format(LOCALE, "%.4f", td.getTopicProportions(i)).getBytes());

                    /* write column separator except for last entry */
                    if (i < nTopics - 1) {
                        outputStream.write(COLUMN_SEPARATOR.getBytes());
                    }
                }
                outputStream.write(System.getProperty("line.separator").getBytes());
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
