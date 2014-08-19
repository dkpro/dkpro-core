/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model.DfModel;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model.DfStore;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util.TermIterator;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util.TfidfUtils;

/**
 * This consumer builds a {@link DfModel}. It collects the df (document frequency) counts for the
 * processed collection. The counts are serialized as a {@link DfModel}-object.
 * 
 * @author zesch, n_erbs, parzonka
 */
public class TfidfConsumer
    extends JCasAnnotator_ImplBase
{
    @Deprecated
    public static final String PARAM_OUTPUT_PATH = ComponentParameters.PARAM_TARGET_LOCATION;
    /**
     * Specifies the path and filename where the model file is written.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private String outputPath;

    /**
     * If set to true, the whole text is handled in lower case.
     */
    public static final String PARAM_LOWERCASE = "lowercase";
    @ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = true, defaultValue = "false")
    private boolean lowercase;

    /**
     * This annotator is type agnostic, so it is mandatory to specify the type of the working
     * annotation and how to obtain the string representation with the feature path.
     */
    public static final String PARAM_FEATURE_PATH = "featurePath";
    @ConfigurationParameter(name = PARAM_FEATURE_PATH, mandatory = true)
    private String featurePath;

    private DfStore dfStore;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        dfStore = new DfStore(featurePath, lowercase);
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        dfStore.registerNewDocument();

        for (String term : TermIterator.create(jcas, featurePath, lowercase)) {
            dfStore.countTerm(term);
        }

        dfStore.closeCurrentDocument();
    }

    /**
     * When this method is called by the framework, the dfModel is serialized.
     */
    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        try {
            TfidfUtils.writeDfModel(dfStore, outputPath);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}