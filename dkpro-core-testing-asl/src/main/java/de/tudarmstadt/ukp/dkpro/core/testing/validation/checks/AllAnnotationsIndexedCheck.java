/*
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.testing.validation.checks;

import static de.tudarmstadt.ukp.dkpro.core.testing.validation.CasAnalysisUtils.getNonIndexedFSesWithOwner;
import static de.tudarmstadt.ukp.dkpro.core.testing.validation.Message.Level.ERROR;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;

public class AllAnnotationsIndexedCheck
    implements Check
{
    @Override
    public boolean check(JCas aCas, List<Message> aMessages)
    {
        Map<FeatureStructure, FeatureStructure> nonIndexed = getNonIndexedFSesWithOwner(aCas
                .getCas());

        if (!nonIndexed.isEmpty()) {
            aMessages.add(new Message(this, ERROR, "Unindexed annotations: %d", nonIndexed.size()));

            for (Entry<FeatureStructure, FeatureStructure> e : nonIndexed.entrySet()) {
                aMessages.add(new Message(this, ERROR,
                        "Non-index annotation [%s] reachable through [%s]", e.getKey(), e
                                .getValue()));
            }
        }

        return nonIndexed.isEmpty();
    }
}
