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

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Simple dictionary-based hyphenation remover. 
 */
@Component(OperationType.NORMALIZER)
@ResourceMetaData(name = "Hyphenation Remover")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class HyphenationRemover
    extends JCasTransformerChangeBased_ImplBase
{
    /**
     * Expect at least one whitespace character behind the dash to avoid
     * conflating words which may be written with a dash or without, such as
     * "non-empty" and "nonempty".
     */
    private static final Pattern HYPHEN_PATTERN = Pattern.compile(
            "\\b(\\p{L}+)-[\\p{Space}]+(\\p{L}+)\\b");
    
    /**
     * Location from which the model is read. This is either a local path or a classpath location.
     * In the latter case, the model artifact (if any) is searched as well.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    private String modelLocation;

    /**
     * The character encoding used by the model.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String modelEncoding;

    private Set<String> dict;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        try {
            URL url = ResourceUtils.resolveLocation(modelLocation);
            try (InputStream is = url.openStream()) {
                dict = new HashSet<>(IOUtils.readLines(is ,modelEncoding));
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        StringBuilder c_new = new StringBuilder();
        final Matcher m = HYPHEN_PATTERN.matcher(aInput.getDocumentText());
        while (m.find()) {
            // The capturing groups count should be exactly 2.
            assert m.groupCount() == 2 : "Expected 2 groups but got " + m.groupCount();

            c_new.setLength(0);
            c_new.append(m.group(1));
            c_new.append(m.group(2));

            if (dict.contains(c_new.toString())) {
                replace(m.start(1), m.end(2), c_new.toString());
//                getLogger().info(
//                        "Conflated: [" + aInput.getDocumentText().substring(m.start(1), m.end(2))
//                                + "] to [" + c_new + "]");
            }
        }
    }
}
