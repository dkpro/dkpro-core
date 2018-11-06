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
package de.tudarmstadt.ukp.dkpro.core.mallet.lda.io;

import static org.apache.uima.fit.util.JCasUtil.selectSingle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.mallet.lda.MalletLdaTopicModelInferencer;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution;

/**
 * Write the topic proportions according to an LDA topic model to an output file. The proportions
 * need to be inferred in a previous step using {@link MalletLdaTopicModelInferencer}.
 */
@ResourceMetaData(name = "Mallet LDA Sorted Topic Proportions Writer")
public class MalletLdaTopicsProportionsSortedWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Number of topics to generate.
     */
    public static final String PARAM_N_TOPICS = "nTopics";
    @ConfigurationParameter(name = PARAM_N_TOPICS, mandatory = true, defaultValue = "3")
    private int nTopics;

    private BufferedWriter writer;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        File targetLocation = new File(getTargetLocation());
        targetLocation.getParentFile().mkdirs();
        try {
            writer = new BufferedWriter(new FileWriter(targetLocation));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        getLogger().info("Writing output to " + targetLocation);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
     */
    /**
     * Extract the {@link TopicDistribution} annotation (must be available) and write to an output
     * file.
     */
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        /* extract topic proportions */
        double[] proportions = selectSingle(aJCas, TopicDistribution.class)
                .getTopicProportions()
                .toArray();

        /* extract indexes of top n topics */
        List<Integer> topIndexes = IntStream
                .range(0, proportions.length)
                .boxed()
                .sorted((i1, i2) -> -Double.compare(proportions[i1], proportions[i2]))
                .limit(nTopics)
                .collect(Collectors.toList());

        try {
            writer.write(DocumentMetaData.get(aJCas).getDocumentId());
            for (int i : topIndexes) {
                writer.write(String.format(Locale.US, "\t%d:%.4f", i, proportions[i]));
            }
            writer.newLine();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            writer.close();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        getLogger().info("Output written to " + getTargetLocation());
    }
}
