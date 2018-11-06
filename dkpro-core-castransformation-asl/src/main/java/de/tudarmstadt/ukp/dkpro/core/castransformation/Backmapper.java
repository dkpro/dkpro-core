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

import static org.apache.uima.fit.util.CasUtil.selectAllFS;

import java.util.LinkedList;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AnnotationBaseFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.internal.util.IntListIterator;
import org.apache.uima.internal.util.PositiveIntSet;
import org.apache.uima.internal.util.PositiveIntSet_impl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.Logger;

import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.ImmutableInterval;
import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.Interval;
import de.tudarmstadt.ukp.dkpro.core.castransformation.internal.AlignmentFactory;
import de.tudarmstadt.ukp.dkpro.core.castransformation.internal.AlignmentStorage;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * After processing a file with the {@code ApplyChangesAnnotator} this annotator
 * can be used to map the annotations created in the cleaned view back to the
 * original view.
 * <p>
 * This annotator is able to resume the mapping after a CAS restore from any point after the cleaned
 * view has been created, as long as no changes were made to SofaChangeAnnotations in the original
 * view.
 * @see ApplyChangesAnnotator
 */
@ResourceMetaData(name = "CAS Transformation - Map back")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class Backmapper
    extends JCasAnnotator_ImplBase
{
    /**
     * Chain of views for backmapping. This should be the reverse of the chain of views that the
     * {@code ApplyChangesAnnotator} has used.
     *
     * For example, if view A has been mapped to B using {@code ApplyChangesAnnotator}, then this
     * parameter should be set using an array containing [B, A].
     */
    public static final String PARAM_CHAIN = "Chain";

    @ConfigurationParameter(name = PARAM_CHAIN, mandatory = false, defaultValue = {
            ApplyChangesAnnotator.VIEW_SOURCE, ApplyChangesAnnotator.VIEW_TARGET})
    protected LinkedList<String> sofaChain = new LinkedList<>();

    @Override
    public void process(final JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try {
            // Now we can copy the complete CAS while mapping back the offsets.
            // We first use the CAS copier and then update the offsets.
            getLogger().info("Copying annotations from [" + sofaChain.getFirst() +
                    "] to [" + sofaChain.getLast() + "]");
            
            // Copy the annotations
            CAS sourceView = aJCas.getCas().getView(sofaChain.getFirst());
            CAS targetView = aJCas.getCas().getView(sofaChain.getLast());
            Feature mDestSofaFeature = targetView.getTypeSystem()
                    .getFeatureByFullName(CAS.FEATURE_FULL_NAME_SOFA);
            CasCopier cc = new CasCopier(sourceView, targetView);
            int docAnno = sourceView.getLowLevelCAS()
                    .ll_getFSRef(sourceView.getDocumentAnnotation());
            final PositiveIntSet copiedFs = new PositiveIntSet_impl();
            for (FeatureStructure fs : selectAllFS(sourceView)) {
                int ref = sourceView.getLowLevelCAS().ll_getFSRef(fs);
                if (ref == docAnno) {
                    // Skip document annotation
                    continue;
                }
                
                // This returns either a new copy -- or -- if an FS has been copied as a
                // transitively referenced feature of another FS, it will return an existing copy
                FeatureStructure fsCopy = cc.copyFs(fs);
                
                // Make sure that the sofa annotation in the copy is set
                if (fs instanceof AnnotationBaseFS) {
                    FeatureStructure sofa = fsCopy.getFeatureValue(mDestSofaFeature);
                    if (sofa == null) {
                        fsCopy.setFeatureValue(mDestSofaFeature, targetView.getSofa());
                    }
                }
                
                // We will still update the offsets, so we do not index the copy just yet
                copiedFs.add(targetView.getLowLevelCAS().ll_getFSRef(fsCopy));
            }
            
            // Get the final target view
            JCas targetViewJCas = aJCas.getView(sofaChain.getLast());

            LinkedList<String> workChain = new LinkedList<>(sofaChain);
            String target = workChain.poll();
            String source = null;

            do {
                source = target;
                target = workChain.poll();

                // Ok, so now we update the offsets.
                String realSource = aJCas.getCas().getView(source).getViewName();
                String realTarget = aJCas.getCas().getView(target).getViewName();
                
                AlignedString as = getAlignedString(aJCas, realSource, realTarget);

                updateOffsets(sourceView, targetViewJCas, as, copiedFs);
            }
            while (!workChain.isEmpty());
            
            // Now we index the copied FSes again
            IntListIterator it = copiedFs.iterator();
            while (it.hasNext()) {
                FeatureStructure fs = targetView.getLowLevelCAS().ll_getFSForRef(it.next());
                targetView.addFsToIndexes(fs);
            }
        }
        catch (UIMAException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
    
    private AlignedString getAlignedString(JCas aSomeCase, String from, String to)
            throws AnalysisEngineProcessException, CASException {
        CAS baseCas = aSomeCase.getCasImpl().getBaseCAS();

        // Try to get the AlignedString for the current JCas.
        AlignmentStorage asstore = AlignmentStorage.getInstance();
        AlignedString as = asstore.get(baseCas, to, from);

        if (as == null) {
            // Attempt to reconstruct the alignment from the SofaChangeAnnotations.
            // This only works when they have not been altered in the mean time.
            Logger logger = getLogger();
            if (logger.isInfoEnabled()) {
                logger.info("No mapping found from [" + from + "] to [" + to + "] on ["
                        + baseCas.hashCode() + "]. "
                        + "Restoring mapping from SofaChangeAnnotation found in [" + to + "]."
                );
            }
            JCas view = aSomeCase.getCas().getView(to).getJCas();
            as = AlignmentFactory.createAlignmentsFor(view);
        }

        // If there is none we have to fail. Practically this should never happen
        // when the alignment state is reconstructed in the previous step.
        if (as == null) {
            throw new AnalysisEngineProcessException(new IllegalStateException(
                    "No mapping found from [" + from + "] to [" + to + "] on ["
                            + baseCas.hashCode() + "]"));
        }

        return as;
    }

    private void updateOffsets(CAS sourceView, JCas targetView, AlignedString as, 
            PositiveIntSet aCopiedFs)
        throws CASException, AnalysisEngineProcessException
    {
        // We only update annotations that were copied, nothing that was already there.
        IntListIterator it = aCopiedFs.iterator();
        
        while (it.hasNext()) {
            FeatureStructure fs = targetView.getLowLevelCas().ll_getFSForRef(it.next());
            if (fs instanceof Annotation) {

                // Now we update the offsets
                Annotation a = (Annotation) fs;
//            System.out.printf("Orig   %s %3d %3d : %s%n", a.getType().getShortName(),
//                    a.getBegin(), a.getEnd(),
//                    sourceView.getDocumentText().substring(a.getBegin(), a.getEnd()));
//            System.out.printf("Before %s %3d %3d : %s%n", a.getType().getShortName(),
//                    a.getBegin(), a.getEnd(), a.getCoveredText());
                Interval resolved = as.resolve(new ImmutableInterval(a.getBegin(), a.getEnd()));
                a.setBegin(resolved.getStart());
                a.setEnd(resolved.getEnd());
//            System.out.printf("After  %s %3d %3d : %s%n", a.getType().getShortName(),
//                    a.getBegin(), a.getEnd(), a.getCoveredText());
            }
        }
    }
}
