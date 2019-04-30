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

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.core.api.parameter.ComponentParameters;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Split up existing tokens again if they are camel-case text.
 */
@Component(OperationType.SEGMENTER)
@ResourceMetaData(name = "CamelCase Token Segmenter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(inputs = {
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" }, outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class CamelCaseTokenSegmenter
    extends JCasAnnotator_ImplBase
{
    /**
     * Whether to remove the original token.
     */
    public static final String PARAM_DELETE_COVER = ComponentParameters.PARAM_DELETE_COVER;
    @ConfigurationParameter(name = PARAM_DELETE_COVER, mandatory = true, defaultValue = "true")
    private boolean deleteCover;

    /**
     * Optional annotation type to markup the original covered token area when specified. This type
     * must be a subtype of {@link Annotation}.
     */
    public static final String PARAM_MARKUP_TYPE = "markupType";
    @ConfigurationParameter(name = PARAM_MARKUP_TYPE, mandatory = false)
    private String markupType;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        List<Token> toAdd = new ArrayList<Token>();
        List<Token> toRemove = new ArrayList<Token>();

        for (Token t : select(aJCas, Token.class)) {
            if ((t.getEnd() - t.getBegin()) < 2) {
                continue;
            }

            String text = t.getCoveredText();
            int offset = t.getBegin();
            int start = 0;
            boolean seenLower = Character.isLowerCase(text.charAt(0));
            for (int i = 1; i < text.length(); i++) {
                // Upper-case means a new token is starting if we are at a lower-case/upper-case
                // boundary. This allows us to properly treat "GetFileUploadURLRequest"
                boolean nextIsLower = i + 1 < text.length()
                        && Character.isLowerCase(text.charAt(i + 1));
                if (Character.isUpperCase(text.charAt(i)) && (seenLower || nextIsLower)) {
                    toAdd.add(new Token(aJCas, offset + start, offset + i));
                    start = i;
                }
                seenLower = Character.isLowerCase(text.charAt(i));
            }

            // If we would just create the same token again, better do nothing
            if (start == 0) {
                continue;
            }

            // The rest goes into the final token
            toAdd.add(new Token(aJCas, offset + start, offset + text.length()));

            if (deleteCover) {
                toRemove.add(t);
            }

            if (markupType != null) {
                CAS cas = aJCas.getCas();
                AnnotationFS annotation = cas.createAnnotation(CasUtil.getType(cas, markupType),
                        t.getBegin(), t.getEnd());
                cas.addFsToIndexes(annotation);
            }
        }

        for (Token t : toAdd) {
            t.addToIndexes();
        }

        for (Token t : toRemove) {
            t.removeFromIndexes();
        }
    }
}
