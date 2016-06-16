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

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.Locale;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;

/**
 * Change tokens to follow a specific casing: all upper case, all lower case, or 'normal case':
 * lowercase everything but the first character of a token and the characters immediately following
 * a hyphen.
 *
 *
 */
public class TokenCaseTransformer
    extends JCasTransformerChangeBased_ImplBase
{
    private static final String HYPHEN = "-";

    public static enum Case
    {
        UPPERCASE, LOWERCASE, NORMALCASE
    }

    /**
     * The case to convert tokens to:
     * <ul>
     * <li>UPPERCASE: uppercase everything.</li>
     * <li>LOWERCASE: lowercase everything.</li>
     * <li>NORMALCASE: retain first letter in word and after hyphens, lowercase everything else.</li>
     * </ul>
     */
    public static final String PARAM_CASE = "tokenCase";
    @ConfigurationParameter(name = PARAM_CASE, mandatory = true)
    private Case tokenCase;

    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        Locale locale = Locale.forLanguageTag(aInput.getDocumentLanguage());

        for (Token token : select(aInput, Token.class)) {
            String origTokenText = token.getCoveredText();
            String filteredToken = origTokenText;

            if (!filteredToken.isEmpty()) {
                switch (tokenCase) {
                case UPPERCASE:
                    filteredToken = origTokenText.toUpperCase(locale);
                    break;
                case LOWERCASE:
                    filteredToken = origTokenText.toLowerCase(locale);
                    break;
                case NORMALCASE:
                    StringBuilder normalized = new StringBuilder(origTokenText.toLowerCase());
                    normalized.setCharAt(0, origTokenText.charAt(0));

                    /* after hyphen, retain original case */
                    int hyphenPosition = normalized.indexOf(HYPHEN);
                    while (hyphenPosition != -1) {
                        if (normalized.length() > hyphenPosition + 1) {
                            normalized.setCharAt(hyphenPosition + 1,
                                    origTokenText.charAt(hyphenPosition + 1));
                        }
                        hyphenPosition = normalized.indexOf(HYPHEN, hyphenPosition + 1);
                    }
                    filteredToken = normalized.toString();
                    break;
                default:
                    throw new IllegalStateException("Unknown case parameter [" + tokenCase + "]");
                }
            }
            if (!filteredToken.equals(origTokenText)) {
                replace(token.getBegin(), token.getEnd(), filteredToken);
            }
        }
    }
}
