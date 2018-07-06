/*
 * Copyright 2017
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

import static de.tudarmstadt.ukp.dkpro.core.testing.validation.Message.Level.ERROR;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.Collection;
import java.util.List;

import org.apache.uima.fit.util.FSUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;

public class ParentSetCheck implements Check
{
    // tag::check-example[]
    @Override
    public boolean check(JCas aJCas, List<Message> aMessages)
    {
        for (Constituent parent : select(aJCas, Constituent.class)) {
            Collection<Annotation> children = select(parent.getChildren(), Annotation.class);
            for (Annotation child : children) {
                Annotation declParent = FSUtil.getFeature(child, "parent", Annotation.class);
                
                if (declParent == null) {
                    aMessages.add(new Message(this, ERROR, String.format(
                            "Child without parent set: %s", child)));
                    
                }
                else if (declParent != parent) {
                    aMessages.add(new Message(this, ERROR, String.format(
                            "Child points to wrong parent: %s", child)));
                    
                }
            }
        }
        
        return aMessages.stream().anyMatch(m -> m.level == ERROR);
    }
    // end::check-example[]
}
