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

package org.dkpro.core.textnormalizer.annotations;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.featurepath.FeaturePathException;
import org.dkpro.core.api.featurepath.FeaturePathFactory;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Reads a list of words from a text file (one token per line) and retains only tokens or other
 * annotations that match any of these words.
 */
@Component(OperationType.NORMALIZER)
@ResourceMetaData(name = "Annotation-By-Text Filter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class AnnotationByTextFilter
    extends JCasAnnotator_ImplBase
{
    /**
     * Location from which the model is read. This is either a local path or a classpath location.
     * In the latter case, the model artifact (if any) is searched as well.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    private File modelLocation;
    private Set<String> words;

    /**
     * If true, annotation texts are filtered case-independently (i.e. words that
     * occur in the list with different casing are not filtered out).
     */
    public static final String PARAM_IGNORE_CASE = "ignoreCase";
    @ConfigurationParameter(name = PARAM_IGNORE_CASE, mandatory = true, defaultValue = "true")
    private boolean ignoreCase;

    /**
     * The character encoding used by the model.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String modelEncoding;

    /**
     * Annotation type to filter.
     */
    public static final String PARAM_TYPE_NAME = "typeName";
    @ConfigurationParameter(name = PARAM_TYPE_NAME, mandatory = true, defaultValue = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token")
    private String typeName;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            readWords();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    };

    private void readWords()
        throws IOException
    {
        words = new HashSet<>();
        for (String line : FileUtils.readLines(modelLocation, modelEncoding)) {
            words.add(ignoreCase ? line.trim().toLowerCase() : line.trim());
        }
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        Set<AnnotationFS> toRemove = new HashSet<>();
        try {
            for (Entry<AnnotationFS, String> entry : FeaturePathFactory.select(aJCas.getCas(),
                    typeName)) {
                String text = ignoreCase ? entry.getValue().toLowerCase() : entry.getValue();
                if (!words.contains(text)) {
                    toRemove.add(entry.getKey());
                }
            }
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }

        for (AnnotationFS annotation : toRemove) {
            aJCas.removeFsFromIndexes(annotation);
        }
    }

}
