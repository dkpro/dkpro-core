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

import static de.tudarmstadt.ukp.dkpro.core.testing.validation.Message.Level.ERROR;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;

public class DependencyRootTypeCheck
    implements Check
{
    @Override
    public boolean check(JCas aCas, List<Message> aMessages)
    {
        for (Sentence sentence : select(aCas, Sentence.class)) {
            Collection<Dependency> dependencies = selectCovered(Dependency.class, sentence);
            if (dependencies.isEmpty()) {
                continue;
            }
            
            List<Dependency> roots = dependencies.stream()
                    .filter(dep -> 
                        dep.getGovernor() != null && 
                        dep.getDependent() != null &&
                        dep.getGovernor() == dep.getDependent())
                    .collect(Collectors.toList());
            
            roots.stream()
                    .filter(dep -> !ROOT.class.equals(dep.getClass()))
                    .forEach(dep -> aMessages.add(new Message(this, ERROR,
                        "Root dependency is not of type [%s]: %s", ROOT.class.getName(), dep)));
        }
        
        return aMessages.stream().anyMatch(m -> m.level == ERROR);
    }
}
