package de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.util;

import static de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator.OP_DELETE;
import static de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator.OP_INSERT;
import static de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator.OP_REPLACE;

import java.util.Collections;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.AlignedString;

public class NormalizationUtils {

public static void applyChanges(AlignedString as, List<SofaChangeAnnotation> changes) {
        
        // If we remove or add stuff all offsets right of the change location
        // will change and thus the offsets in the change annotation are no
        // longer valid. If we move from right to left it works better because
        // the left offsets remain stable.
        Collections.reverse(changes);
        for (SofaChangeAnnotation c : changes) {
            if (OP_INSERT.equals(c.getOperation())) {
                // getContext().getLogger().log(INFO,
                // "Performing insert: "+a.getBegin()+"-"+a.getEnd());
                as.insert(c.getBegin(), c.getValue());
            }
            if (OP_DELETE.equals(c.getOperation())) {
                // getContext().getLogger().log(INFO,
                // "Performing delete: "+a.getBegin()+"-"+a.getEnd());
                as.delete(c.getBegin(), c.getEnd());
            }
            if (OP_REPLACE.equals(c.getOperation())) {
                // getContext().getLogger().log(INFO,
                // "Performing replace: "+a.getBegin()+"-"+a.getEnd());
                as.replace(c.getBegin(), c.getEnd(), c.getValue());
            }
        }
    }
}
