/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.textnormalizer.util;

import static org.dkpro.core.castransformation.ApplyChangesAnnotator.OP_DELETE;
import static org.dkpro.core.castransformation.ApplyChangesAnnotator.OP_INSERT;
import static org.dkpro.core.castransformation.ApplyChangesAnnotator.OP_REPLACE;

import java.util.Collections;
import java.util.List;

import org.dkpro.core.api.transform.alignment.AlignedString;

import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;

public class NormalizationUtils
{

    public static void applyChanges(AlignedString as, List<SofaChangeAnnotation> changes)
    {

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
