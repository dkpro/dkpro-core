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

package org.dkpro.core.tokit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.core.api.featurepath.FeaturePathException;
import org.dkpro.core.api.featurepath.FeaturePathFactory;

import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Removes annotations that do not conform to minimum or maximum length constraints.
 *
 * (This was previously called TokenFilter).
 */
@Component(OperationType.NORMALIZER)
@ResourceMetaData(name = "Annotation-By-Length Filter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class AnnotationByLengthFilter
    extends JCasAnnotator_ImplBase
{
    /**
     * A set of annotation types that should be filtered.
     */
    public static final String PARAM_FILTER_ANNOTATION_TYPES = "FilterTypes";
    @ConfigurationParameter(name = PARAM_FILTER_ANNOTATION_TYPES, mandatory = true, 
            defaultValue = {})
    private Set<String> filterTypes;

    /**
     * Any annotation in filterTypes shorter than this value will be removed.
     */
    public static final String PARAM_MIN_LENGTH = "MinLengthFilter";
    @ConfigurationParameter(name = PARAM_MIN_LENGTH, mandatory = true, defaultValue = "0")
    private int minTokenLength;

    /**
     * Any annotation in filterAnnotations shorter than this value will be removed.
     */
    public static final String PARAM_MAX_LENGTH = "MaxLengthFilter";
    @ConfigurationParameter(name = PARAM_MAX_LENGTH, mandatory = true, defaultValue = "1000")
    private int maxTokenLength;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {

        for (String filterType : filterTypes) {
            try {
                Collection<Annotation> toRemove = new ArrayList<Annotation>();
                for (Entry<AnnotationFS, String> entry : FeaturePathFactory.select(aJCas.getCas(),
                        filterType)) {
                    int length = entry.getKey().getCoveredText().length();
                    if (length < minTokenLength || length > maxTokenLength) {
                        toRemove.add((Annotation) entry.getKey());
                    }
                }
                for (Annotation anno : toRemove) {
                    anno.removeFromIndexes();
                }
            }
            catch (FeaturePathException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }
}
