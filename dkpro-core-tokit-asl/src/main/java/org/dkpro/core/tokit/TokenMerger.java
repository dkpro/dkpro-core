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

import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.dkpro.core.api.resources.MappingProvider.BASE_TYPE;
import static org.dkpro.core.api.resources.ResourceObjectProviderBase.LANGUAGE;
import static org.dkpro.core.api.resources.ResourceObjectProviderBase.LOCATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.MappingProvider;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Merges any Tokens that are covered by a given annotation type. E.g. this component can be used
 * to create a single tokens from all tokens that constitute a multi-token named entity.
 */
@ResourceMetaData(name = "Token Merger")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"},
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"})
public class TokenMerger
    extends JCasAnnotator_ImplBase
{
    public static enum LemmaMode
    {
        JOIN, REMOVE, LEAVE
    }

    /**
     * Annotation type for which tokens should be merged.
     */
    public static final String PARAM_ANNOTATION_TYPE = "annotationType";
    @ConfigurationParameter(name = PARAM_ANNOTATION_TYPE, mandatory = true)
    private String annotationType;

//    /**
//     * A constraint on the annotations that should be considered in form of a JXPath statement.
//     * Example: set {@link #PARAM_ANNOTATION_TYPE} to a {@code NamedEntity} type and set the
//     * {@link #PARAM_CONSTRAINT} to {@code ".[value = 'LOCATION']"} to merge only tokens that are
//     * part of a location named entity.
//     */
//    public static final String PARAM_CONSTRAINT = "constraint";
//    @ConfigurationParameter(name = PARAM_CONSTRAINT, mandatory = false)
//    private String constraint;

    /**
     * Configure what should happen to the lemma of the merged tokens. It is possible to JOIN the
     * lemmata to a single lemma (space separated), to REMOVE the lemma or LEAVE the lemma of the
     * first token as-is.
     */
    public static final String PARAM_LEMMA_MODE = "lemmaMode";
    @ConfigurationParameter(name = PARAM_LEMMA_MODE, mandatory = true, defaultValue = "JOIN")
    private LemmaMode lemmaMode;

    /**
     * Set a new POS value for the new merged token. This is the actual tag set value and is subject
     * to tagset mapping. For example when merging tokens for named entities, the new POS value may
     * be set to "NNP" (English/Penn Treebank Tagset).
     */
    public static final String PARAM_POS_VALUE = "posValue";
    @ConfigurationParameter(name = PARAM_POS_VALUE, mandatory = false)
    private String posValue;

    /**
     * Set a new coarse POS value for the new merged token. This is the actual tag set value and is
     * subject to tagset mapping. For example when merging tokens for named entities, the new POS
     * value may be set to "NNP" (English/Penn Treebank Tagset).
     */
    public static final String PARAM_CPOS_VALUE = "cposValue";
    @ConfigurationParameter(name = PARAM_CPOS_VALUE, mandatory = false)
    private String cposValue;

    /**
     * Set a new POS tag for the new merged token. This is the mapped type. If this is specified,
     * tag set mapping will not be performed. This parameter has no effect unless PARAM_POS_VALUE is
     * also set.
     */
    public static final String PARAM_POS_TYPE = "posType";
    @ConfigurationParameter(name = PARAM_POS_TYPE, mandatory = false)
    private String posType;

    /**
     * Use this language instead of the document language to resolve the model and tag set mapping.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Override the tagset mapping.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = 
            ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        mappingProvider = new MappingProvider();
        mappingProvider.setDefault(LOCATION,
                "classpath:/org/dkpro/core/api/lexmorph/tagset/${language}-${pos.tagset}-pos.map");
        mappingProvider.setDefault(BASE_TYPE, POS.class.getName());
        mappingProvider.setDefault("pos.tagset", "default");
        mappingProvider.setOverride(LOCATION, posMappingLocation);
        mappingProvider.setOverride(LANGUAGE, language);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();

        if (posValue != null) {
            mappingProvider.configure(cas);
        }

        List<AnnotationFS> covers = new ArrayList<>(
                CasUtil.select(cas, CasUtil.getAnnotationType(cas, annotationType)));
        Collection<Annotation> toRemove = new ArrayList<Annotation>();
        for (AnnotationFS cover : covers) {
            List<Token> covered = selectCovered(Token.class, cover);
            if (covered.size() < 2) {
                continue;
            }

//            if (constraint != null) {
//                JXPathContext ctx = JXPathContext.newContext(cover);
//                boolean match = ctx.iterate(constraint).hasNext();
//                if (!match) {
//                    continue;
//                }
//            }

            Iterator<Token> i = covered.iterator();

            // Extend first token
            Token token = i.next();
            token.removeFromIndexes();
            token.setEnd(covered.get(covered.size() - 1).getEnd());
            token.addToIndexes();

            // Optionally update the POS value
            if (posValue != null) {
                updatePos(token, toRemove);
            }

            // Record lemma - may be needed for join later
            List<String> lemmata = new ArrayList<String>();
            if (token.getLemma() != null) {
                lemmata.add(token.getLemma().getValue());
            }

            // Mark the rest for deletion - record lemmata if desired for later join
            while (i.hasNext()) {
                Token t = i.next();

                Lemma lemma = t.getLemma();
                if (lemma != null) {
                    lemmata.add(lemma.getValue());
                    toRemove.add(lemma);
                }

                POS pos = t.getPos();
                if (pos != null) {
                    toRemove.add(pos);
                }

                toRemove.add(t);
            }

            // Join lemmata if desired
            if (lemmaMode == LemmaMode.JOIN) {
                Lemma lemma = token.getLemma();
                if (!lemmata.isEmpty()) {
                    if (lemma == null) {
                        lemma = new Lemma(aJCas);
                    }
                    lemma.setValue(StringUtils.join(lemmata, " "));
                }
                // Remove if there was nothing to join... I don't really ever expect to get here
                else if (lemma != null) {
                    token.setLemma(null);
                    toRemove.add(lemma);
                }
            }
            // Remove the lemma - if desired
            else if (lemmaMode == LemmaMode.REMOVE) {
                Lemma lemma = token.getLemma();
                if (lemma != null) {
                    token.setLemma(null);
                    toRemove.add(lemma);
                }
            }

            // Update offsets for lemma
            if (token.getLemma() != null) {
                Lemma lemma = token.getLemma();
                lemma.removeFromIndexes();
                lemma.setBegin(token.getBegin());
                lemma.setEnd(token.getEnd());
                lemma.addToIndexes();
            }
        }

        // Remove tokens no longer needed
        for (Annotation t : toRemove) {
            t.removeFromIndexes();
        }
    }

    private void updatePos(Token aToken, Collection<Annotation> aToRemove)
    {
        // Determine the mapped type
        Type type;
        if (posType != null) {
            type = CasUtil.getType(aToken.getCAS(), posType);
        }
        else {
            type = mappingProvider.getTagType(posValue);
        }

        POS pos = aToken.getPos();
        if (pos != null && !pos.getType().equals(type)) {
            // Remove wrong existing POS annotation
            aToRemove.add(pos);
            pos = null;
        }

        if (pos == null) {
            // Create correct annotation
            pos = (POS) aToken.getCAS().createAnnotation(type, aToken.getBegin(), aToken.getEnd());
            pos.addToIndexes();
        }
        else {
            // Update offsets - no need to add to indexes, was in CAS already
            pos.setBegin(aToken.getBegin());
            pos.setEnd(aToken.getEnd());
        }

        // Update the POS value
        pos.setPosValue(posValue);
        pos.setCoarseValue(cposValue);
        aToken.setPos(pos);
    }
}
