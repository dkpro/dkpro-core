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
package de.tudarmstadt.ukp.dkpro.core.castransformation;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import de.tudarmstadt.ukp.dkpro.core.castransformation.internal.AlignmentFactory;
import de.tudarmstadt.ukp.dkpro.core.castransformation.internal.AlignmentStorage;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Applies changes annotated using a {@link SofaChangeAnnotation}.
 *
 * @since 1.1.0
 * @see Backmapper
 */
@ResourceMetaData(name = "CAS Transformation - Apply")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
            "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation"},
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
            "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation"})

public class ApplyChangesAnnotator
    extends JCasAnnotator_ImplBase
{
    public static final String VIEW_SOURCE = "source";
    public static final String VIEW_TARGET = "target";

    public static final String OP_INSERT = "insert";
    public static final String OP_REPLACE = "replace";
    public static final String OP_DELETE = "delete";

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try {
            JCas sourceView = aJCas.getView(VIEW_SOURCE);
            JCas targetView = aJCas.createView(VIEW_TARGET);
            DocumentMetaData.copy(sourceView, targetView);
            applyChanges(sourceView, targetView);
        }
        catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    protected void applyChanges(JCas aSourceView, JCas aTargetView)
    {
        AlignedString as = AlignmentFactory.createAlignmentsFor(aSourceView);

        // Set the text of the new Sofa
        aTargetView.setDocumentText(as.get());

        // Set document language
        aTargetView.setDocumentLanguage(aSourceView.getDocumentLanguage());

        // Optionally we may want to remember the AlignedString for the backmapper.
        AlignmentStorage.getInstance().put(aSourceView.getCasImpl().getBaseCAS(),
                aSourceView.getViewName(), aTargetView.getViewName(), as);
    }
}
