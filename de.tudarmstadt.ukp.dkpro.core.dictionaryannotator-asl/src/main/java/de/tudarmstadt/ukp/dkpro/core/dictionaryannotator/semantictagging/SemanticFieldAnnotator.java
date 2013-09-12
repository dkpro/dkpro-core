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
package de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.semantictagging;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticField;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.lexmorph.type.POS" }, outputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NamedEntity" })
/**
 * 
 * This Analysis Engine annotates 
 * English common nouns with semantic field information from WordNet.
 * The annotation is stored in the NamedEntity annotation type.
 *     
 * @author Judith Eckle-Kohler
 * 
 */
public class SemanticFieldAnnotator
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_SEMANTIC_FIELD_RESOURCE = "nounSemanticFieldResource";
    @ExternalResource(key = PARAM_SEMANTIC_FIELD_RESOURCE)
    private SemanticTagResource nounSemanticFieldResource;

    /**
     * Annotation type for which tokens should be merged.
     */
    public static final String PARAM_ANNOTATION_TYPE = "annotationType";
    @ConfigurationParameter(name = PARAM_ANNOTATION_TYPE, mandatory = true)
    private String annotationType;

    /**
     * A constraint on the annotations that should be considered in form of a JXPath statement.
     * Example: set {@link #PARAM_ANNOTATION_TYPE} to a {@code NamedEntity} type and set the
     * {@link #PARAM_CONSTRAINT} to {@code ".[value = 'LOCATION']"} to merge only tokens that are
     * part of a location named entity.
     */
    public static final String PARAM_CONSTRAINT = "constraint";
    @ConfigurationParameter(name = PARAM_CONSTRAINT, mandatory = false)
    private String constraint;

    @Override
    public void initialize(final UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();

        for (AnnotationFS cover : CasUtil.select(cas,
                CasUtil.getAnnotationType(cas, annotationType))) {
            
            // If there is a constraint, check if it matches
            if (constraint != null) {
                JXPathContext ctx = JXPathContext.newContext(cover);
                boolean match = ctx.iterate(constraint).hasNext();
                if (!match) {
                    continue;
                }
            }

            // If the target type is a token, use it directly, otherwise select the covered tokens
            Collection<Token> tokens;
            if (cover instanceof Token) {
                tokens = Collections.singleton((Token) cover);
            }
            else {
                tokens = JCasUtil.selectCovered(aJCas, Token.class, cover);
            }
            
            for (Token token : tokens) {
                try {
                    String semanticField = nounSemanticFieldResource.getSemanticTag(token);
                    SemanticField semanticFieldAnnotation = new SemanticField(aJCas,
                            token.getBegin(), token.getEnd());
                    semanticFieldAnnotation.setValue(semanticField);
                    semanticFieldAnnotation.addToIndexes();
                }
                catch (ResourceAccessException e) {
                    throw new AnalysisEngineProcessException(e);
                }
            }
        }
    }
}
