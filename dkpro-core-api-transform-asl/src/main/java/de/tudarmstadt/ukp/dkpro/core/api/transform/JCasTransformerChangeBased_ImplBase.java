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

package de.tudarmstadt.ukp.dkpro.core.api.transform;

import static org.apache.uima.fit.util.CasUtil.getType;
import static org.apache.uima.fit.util.CasUtil.selectFS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AnnotationBaseFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasCopier;

import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.ImmutableInterval;
import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.Interval;

/**
 * Base-class for normalizers that do insert/delete/replace operations. Please mind that these
 * operations must not overlap!.
 */
public abstract class JCasTransformerChangeBased_ImplBase
    extends JCasTransformer_ImplBase
{
    private JCas input;
    private List<Change> changes;

    @Override
    public void beforeProcess(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        super.beforeProcess(aInput, aOutput);
        changes = new ArrayList<Change>();

        // Remember the input CAS so that we can access its text in replace()
        input = aInput;
    }

    @Override
    public void afterProcess(JCas aInput, JCas aOutput)
    {
        AlignedString alignedString = new AlignedString(aInput.getDocumentText());

        Collections.sort(changes, Interval.SEG_START_CMP);

        // A sanity check here would be good to see if there are any overlapping changes,
        // because these would cause corrupt output
        Change previousChange = null;
        for (Change change : changes) {
            if (previousChange != null && change.overlaps(previousChange)) {
                throw new IllegalStateException("Change " + change + " must not overlap with "
                        + previousChange);
            }
            previousChange = change;
        }

        // Apply changes in reverse order so that offsets of unprocessed changes remain valid
        ListIterator<Change> li = changes.listIterator(changes.size());
        while (li.hasPrevious()) {
            Change change = li.previous();
            switch (change.getAction()) {
            case INSERT:
                alignedString.insert(change.getStart(), change.getText());
                break;
            case DELETE:
                alignedString.delete(change.getStart(), change.getEnd());
                break;
            case REPLACE:
                alignedString.replace(change.getStart(), change.getEnd(), change.getText());
                break;
            default:
                throw new IllegalStateException("Unknown change action [" + change.getAction()
                        + "]");
            }
        }

        aOutput.setDocumentText(alignedString.get());

        // Below we repeat the super.afterProcess() code but inject adjustments of the offsets.
        // We do NOT call super.afterProcess()
        
        // Copy the annotation types mentioned in PARAM_TYPES_TO_COPY
        // We have do do this in the afterProcess() phase, because otherwise the SofA in the
        // target CAS does not exist yet.
        CAS inputCas = aInput.getCas();
        CAS outputCas = aOutput.getCas();

        CasCopier copier = new CasCopier(inputCas, aOutput.getCas());
        
        Feature mDestSofaFeature = aOutput.getTypeSystem()
                .getFeatureByFullName(CAS.FEATURE_FULL_NAME_SOFA);

        for (String typeName : getTypesToCopy()) {
            Type annotationType = getType(outputCas, CAS.TYPE_NAME_ANNOTATION);
            Feature beginFeature = annotationType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_BEGIN);
            Feature endFeature = annotationType.getFeatureByBaseName(CAS.FEATURE_BASE_NAME_END);
            
            for (FeatureStructure fs : selectFS(inputCas, getType(inputCas, typeName))) {
                if (!copier.alreadyCopied(fs)) {
                    FeatureStructure fsCopy = copier.copyFs(fs);
                    // Make sure that the sofa annotation in the copy is set
                    if (fs instanceof AnnotationBaseFS) {
                        FeatureStructure sofa = fsCopy.getFeatureValue(mDestSofaFeature);
                        if (sofa == null) {
                            fsCopy.setFeatureValue(mDestSofaFeature, aOutput.getSofa());
                        }
                    }
                    
                    // Update the begin/end offsets
                    if (fs instanceof AnnotationFS) {
                        AnnotationFS annoFs = (AnnotationFS) fs;
                        Interval i = alignedString.inverseResolve(new ImmutableInterval(annoFs
                                .getBegin(), annoFs.getEnd()));
                        fsCopy.setIntValue(beginFeature, i.getStart());
                        fsCopy.setIntValue(endFeature, i.getEnd());
                    }
                    
                    aOutput.addFsToIndexes(fsCopy);
                }
            }
        }
    }

    public void insert(int aBegin, String aText)
    {
        changes.add(new Change(ChangeAction.INSERT, aBegin, -1, aText));
    }

    public void delete(int aBegin, int aEnd)
    {
        changes.add(new Change(ChangeAction.DELETE, aBegin, aEnd, null));
    }

    public void replace(int aBegin, int aEnd, String aText)
    {
        // Create a change action only if the new text differs from the old text.
        // This avoids clutter in the changes list and improves performance when applying the
        // changes later.
        if (!aText.equals(input.getDocumentText().substring(aBegin, aEnd))) {
            changes.add(new Change(ChangeAction.REPLACE, aBegin, aEnd, aText));
        }
    }

    private static enum ChangeAction
    {
        INSERT,
        DELETE,
        REPLACE
    }

    private static class Change extends ImmutableInterval
    {
        private ChangeAction action;
        private String text;

        public Change(ChangeAction aAction, int aBegin, int aEnd, String aText)
        {
            super(aBegin, aEnd);
            action = aAction;
            text = aText;
        }

        public ChangeAction getAction()
        {
            return action;
        }

        public void setAction(ChangeAction aAction)
        {
            action = aAction;
        }

        public String getText()
        {
            return text;
        }

        public void setText(String aText)
        {
            text = aText;
        }

        @Override
        public String toString()
        {
            return "[action=" + action + ", text=" + text + ", begin=" + getStart()
                    + ", end=" + getEnd() + "]";
        }
    }
}
