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

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;

/**
 * A {@link JCasTransformerChangeBased_ImplBase} implementation that replaces tokens based on a
 * regular expressions.
 * <p>
 * The parameters {@link #PARAM_REGEX} defines the regular expression to be searcher,
 * {@link #PARAM_REPLACEMENT} defines the string with which matching patterns are replaces.
 * 
 *
 */
public class RegexBasedTokenTransformer
    extends JCasTransformerChangeBased_ImplBase
{
    /**
     * Define the regular expression to be replaced
     */
    public static final String PARAM_REGEX = "regex";
    @ConfigurationParameter(name = PARAM_REGEX, mandatory = true)
    private String regex;
    private Pattern regexPattern;

    /**
     * Define the string to replace matching tokens with
     */
    public static final String PARAM_REPLACEMENT = "replacement";
    @ConfigurationParameter(name = PARAM_REPLACEMENT, mandatory = true)
    private String replacement;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        regexPattern = Pattern.compile(regex);
    };

    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        select(aInput, Token.class).stream()
                .filter(token -> regexPattern.matcher(token.getCoveredText()).matches())
                .forEach(token -> replace(token.getBegin(), token.getEnd(), replacement));
    }

}
