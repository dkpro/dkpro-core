package de.tudarmstadt.ukp.dkpro.core.castransformation.internal;

import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator;
import de.tudarmstadt.ukp.dkpro.core.castransformation.Backmapper;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Supports building of alignment state from the {@link ApplyChangesAnnotator} to the {@link Backmapper} using
 * {@link de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation} found in the source view. The
 * alignment state is building during instance construction and is immutable after.
 */

public class AlignmentBuild {

    private final AlignedString alignmentState;

    public AlignmentBuild(JCas aSourceView) {
        FSIndex<Annotation> idx = aSourceView.getAnnotationIndex(SofaChangeAnnotation.type);

        // Apply all the changes
        alignmentState = new AlignedString(aSourceView.getDocumentText());

        // Collect all those edits that are going to be executed.
        //
        // | A | C1 C2 R
        // BBBBBB + - -
        // BBBBBBBBBB + + +
        // BBBBBBBBBBBBBBBBB + + +
        // BBBBBBB - + -
        // BBBBBBBBBBBBB - + -
        // BBBBBBBB - + -
        //
        if (idx.size() > 0) {
            List<SofaChangeAnnotation> edits = new ArrayList<SofaChangeAnnotation>();
            {
                // Get an iterator over all the change annotations. Per UIMA default
                // this iterator is sorted first by begin and then by end offsets.
                // We will make use of this fact here to skip change annotations that
                // are covered by others. The earliest longest change wins - this means
                // the one with the smallest begin offset and the largest end offset.
                FSIterator<Annotation> it = idx.iterator();

                SofaChangeAnnotation top = (SofaChangeAnnotation) it.get();
                edits.add(top);
                it.moveToNext();
                while (it.isValid()) {
                    SofaChangeAnnotation b = (SofaChangeAnnotation) it.get();
                    if (((top.getBegin() <= b.getBegin()) && // C1
                            (top.getEnd() > b.getBegin()) // C2
                            )
                            || ((top.getBegin() == b.getBegin()) && (top.getEnd() == b.getEnd()))) {
                        // Found annotation covering current annotation. Skipping
                        // current annotation.
                    }
                    else {
                        top = b;
                        edits.add(top);
                    }
                    it.moveToNext();
                }
            }

            // If we remove or add stuff all offsets right of the change location
            // will change and thus the offsets in the change annotation are no
            // longer valid. If we move from right to left it works better because
            // the left offsets remain stable.
            Collections.reverse(edits);
            for (SofaChangeAnnotation a : edits) {
                if (ApplyChangesAnnotator.OP_INSERT.equals(a.getOperation())) {
                    alignmentState.insert(a.getBegin(), a.getValue());
                }
                if (ApplyChangesAnnotator.OP_DELETE.equals(a.getOperation())) {
                    alignmentState.delete(a.getBegin(), a.getEnd());
                }
                if (ApplyChangesAnnotator.OP_REPLACE.equals(a.getOperation())) {
                    alignmentState.replace(a.getBegin(), a.getEnd(), a.getValue());
                }
            }
        }
    }

    public AlignedString getAlignmentState() {
        return alignmentState;
    }

    public static AlignedString from(JCas aSourceView) {
        return new AlignmentBuild(aSourceView).getAlignmentState();
    }
}
