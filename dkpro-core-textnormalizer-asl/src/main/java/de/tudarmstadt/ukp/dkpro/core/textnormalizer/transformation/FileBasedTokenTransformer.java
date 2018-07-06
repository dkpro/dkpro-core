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

import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Replaces all tokens that are listed in the file in {@link #PARAM_MODEL_LOCATION} by the string
 * specified in {@link #PARAM_REPLACEMENT}.
 */
@Component(OperationType.NORMALIZER)
@ResourceMetaData(name = "File-based Token Transformer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class FileBasedTokenTransformer
    extends JCasTransformerChangeBased_ImplBase
{
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    private String modelLocation;
    private Collection<String> tokensToReplace;

    public static final String PARAM_REPLACEMENT = "replacement";
    @ConfigurationParameter(name = PARAM_REPLACEMENT, mandatory = true)
    private String replacement;

    public static final String PARAM_IGNORE_CASE = "ignoreCase";
    @ConfigurationParameter(name = PARAM_IGNORE_CASE, mandatory = true, defaultValue = "false")
    private boolean ignoreCase;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            tokensToReplace = readTokens(new File(modelLocation));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        select(aInput, Token.class).stream()
                .filter(token -> ignoreCase
                        ? tokensToReplace.contains(token.getCoveredText().toLowerCase())
                        : tokensToReplace.contains(token.getCoveredText()))
                .forEach(token -> replace(token.getBegin(), token.getEnd(), replacement));
    }

    private Collection<String> readTokens(File file)
        throws IOException
    {
        return Files.lines(file.toPath())
                .map(String::trim)
                .map(line -> ignoreCase ? line.toLowerCase() : line)
                .collect(Collectors.toSet());
    }
}
