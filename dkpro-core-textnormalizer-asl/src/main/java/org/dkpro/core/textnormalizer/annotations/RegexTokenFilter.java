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

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Remove every token that does or does not match a given regular expression.
 */
@Component(OperationType.NORMALIZER)
@ResourceMetaData(name = "Regex Token Filter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class RegexTokenFilter
    extends JCasAnnotator_ImplBase
{
    /**
     * Every token that does or does not match this regular expression will be removed.
     */
    public static final String PARAM_REGEX = "regex";
    @ConfigurationParameter(name = PARAM_REGEX, mandatory = true)
    private String regex;

    /**
     * If this parameter is set to true (default), retain only tokens that match the regex given in
     * {@link #PARAM_REGEX}. If set to false, all tokens that match the given regex are removed.
     */
    public static final String PARAM_MUST_MATCH = "mustMatch";
    @ConfigurationParameter(name = PARAM_MUST_MATCH, mandatory = true, defaultValue = "true")
    private boolean mustMatch;

    private Pattern filterRegex;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        filterRegex = Pattern.compile(regex);
    };

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        List<Token> toRemove = new LinkedList<>();
        for (Token token : select(aJCas, Token.class)) {
            if (mustMatch && !filterRegex.matcher(token.getCoveredText()).matches()
                    || !mustMatch && filterRegex.matcher(token.getCoveredText()).matches()) {
                toRemove.add(token);
            }
        }
        for (Token token : toRemove) {
            token.removeFromIndexes();
        }
    }
}
