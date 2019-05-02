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
package org.dkpro.core.testing.validation.checks;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.dkpro.core.testing.validation.Message.Level.ERROR;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.validation.Message;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;

public class BasicDependenciesFormATreeCheck
    implements Check
{
    @Override
    public boolean check(JCas aCas, List<Message> aMessages)
    {
        for (Sentence sentence : select(aCas, Sentence.class)) {
            // Get only the basic dependencies (assuming that those where the flavor is set to 
            // null are also basic!
            List<Dependency> basicDependencies = selectCoveredBasic(sentence);
            if (basicDependencies.isEmpty()) {
                continue;
            }

            // Check that there is exactly a single ROOT dependency
            List<Dependency> roots = basicDependencies.stream()
                    .filter(dep -> ROOT.class.equals(dep.getClass()))
                    .collect(Collectors.toList());
            if (roots.size() != 1) {
                aMessages.add(new Message(this, ERROR,
                        "Sentence [%s] has multiple dependency roots: %s", sentence, roots));
                continue;
            }

            // Check that each token has at most one basic dependency attached to it
            Map<Token, Dependency> idxDep = new HashMap<>();
            Collection<Token> tokens = selectCovered(Token.class, sentence);
            for (Token token : tokens) {
                List<Dependency> attachedDependencies = selectCoveredBasic(token);
                if (attachedDependencies.size() > 1) {
                    attachedDependencies.stream()
                            .forEach(dep -> aMessages.add(new Message(this, ERROR,
                                "Multiple dependencies attached to [%s]: %s", token, dep)));
                }
                else if (!attachedDependencies.isEmpty()) {
                    idxDep.put(token, attachedDependencies.get(0));
                }
            }
            
            // Check that for each dependency, we can reach the ROOT node
            nextDep: for (Dependency dep : basicDependencies) {
                Dependency cur = dep;
                while (true) {
                    // Check if the dependency is attached
                    if (cur.getGovernor() == null || cur.getDependent() == null) {
                        aMessages.add(new Message(this, ERROR,
                                "Root of dependency [%s] is not tree root but [%s]", dep, cur));
                        continue nextDep;
                    }
                    
                    // Check if root was reached
                    if (cur.getGovernor() == cur.getDependent()) {
                        continue nextDep;
                    }
                    
                    
                    // Check if governor token has no dependency attached
                    Dependency next = idxDep.get(cur.getGovernor());
                    if (next == null) {
                        aMessages.add(new Message(this, ERROR,
                                "Governor [%s] of dependency [%s] has no further dependency attached",
                                cur.getGovernor(), cur));
                        continue nextDep;
                    }
                    
                    cur = next;
                }
            }
        }
        
        return aMessages.stream().anyMatch(m -> m.level == ERROR);
    }
    
    private List<Dependency> selectCoveredBasic(AnnotationFS aAnnotation)
    {
        return selectCovered(Dependency.class, aAnnotation).stream().filter(dep -> 
                DependencyFlavor.BASIC.equals(dep.getFlavor()) || dep.getFlavor() == null)
                .collect(Collectors.toList());
    }
}
