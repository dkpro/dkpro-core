/*
 * Copyright 2014
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

package org.dkpro.core.textnormalizer.casfilter;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.FlowControllerFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.flow.FlowController;
import org.apache.uima.flow.impl.FixedFlowController;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * This class calls the {@code pass()} methods to determine whether a JCas should be filtered out or
 * passed on in a pipeline. Therefore, the {@code pass()} method has to be implemented, returning
 * true if a document should be passed on in the pipeline and false if it should be filtered out.
 * <p>
 * The filter (sub-)classes should be applied within a an {@link AggregateBuilder} applying a
 * {@link FlowController} as in the
 * {@link CasFilter_ImplBase#createAggregateBuilderDescription(AnalysisEngineDescription...)}
 * method.
 * <p>
 * Note that methods such as
 * {@link SimplePipeline#runPipeline(org.apache.uima.cas.CAS, 
 * org.apache.uima.analysis_engine.AnalysisEngine...)} and {@link 
 * SimplePipeline#iteratePipeline(org.apache.uima.collection.CollectionReaderDescription, 
 * AnalysisEngineDescription...)}
 * do not allow direct access to the JCas' produced by a JCasMultiplier.
 */
public abstract class CasFilter_ImplBase
    extends JCasMultiplier_ImplBase
{
    private JCas current = null;

    @Override
    public boolean hasNext()
        throws AnalysisEngineProcessException
    {
        return current != null;
    }

    @Override
    public AbstractCas next()
        throws AnalysisEngineProcessException
    {
        JCas result = current;
        current = null;
        return result;
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        current = pass(aJCas) ? aJCas : null;
    }

    /**
     * This method determines whether a document / JCas is removed or retained. If this method
     * returns true, the document is retained, if it returns false, it is removed.
     *
     * @param aJCas
     *            the currently processed JCas
     * @return true if the document is to be retained, false if it is to be removed
     */
    protected abstract boolean pass(JCas aJCas);

    /**
     * Creates a new AnalysisEngineDescription from an Aggregrator that contains all input
     * AnalysisEngineDescriptions in given order. This is intended for the use of a filter like
     * {@link CasFilter_ImplBase}; all subsequent analysis engines will only see the documents that
     * have passed the filter.
     *
     * @param aEngines
     *            {@link AnalysisEngineDescription}s that should be aggregated.
     * @return a single {@link AnalysisEngineDescription} aggregating all the input engines.
     * @throws ResourceInitializationException
     *             if any input analysis engine cannot be initialized
     */
    public static AnalysisEngineDescription createAggregateBuilderDescription(
            AnalysisEngineDescription... aEngines)
        throws ResourceInitializationException
    {
        AggregateBuilder aggregateBuilder = new AggregateBuilder();
        aggregateBuilder.setFlowControllerDescription(FlowControllerFactory
                .createFlowControllerDescription(FixedFlowController.class,
                        FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, "drop"));

        for (AnalysisEngineDescription aEngine : aEngines) {
            aggregateBuilder.add(aEngine);
        }
        return aggregateBuilder.createAggregateDescription();
    }

    /**
     * @see CasFilter_ImplBase#createAggregateBuilderDescription(AnalysisEngineDescription...)
     * @param aEngines
     *            a list of {@link AnalysisEngineDescription}s
     * @return a single {@link AnalysisEngineDescription} aggregating all the input engines.
     * @throws ResourceInitializationException
     *             if any input analysis engine cannot be initialized
     */
    public static AnalysisEngineDescription createAggregateBuilderDescription(
            List<AnalysisEngineDescription> aEngines)
        throws ResourceInitializationException
    {
        return createAggregateBuilderDescription(aEngines
                .toArray(new AnalysisEngineDescription[aEngines.size()]));
    }
}
