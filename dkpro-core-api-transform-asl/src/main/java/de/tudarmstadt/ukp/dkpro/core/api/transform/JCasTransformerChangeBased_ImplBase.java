/*******************************************************************************
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
 ******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.api.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

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

        super.afterProcess(aInput, aOutput);
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
