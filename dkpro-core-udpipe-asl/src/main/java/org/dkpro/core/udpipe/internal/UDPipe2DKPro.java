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
package org.dkpro.core.udpipe.internal;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;

import cz.cuni.mff.ufal.udpipe.Sentence;
import cz.cuni.mff.ufal.udpipe.Word;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.pos.POSUtils;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;

public class UDPipe2DKPro
{
    public static void convertPosLemmaMorph(Sentence sentence, Collection<Token> tokens, JCas aJCas,
            MappingProvider mappingProvider)
    {
        CAS cas = aJCas.getCas();
        
        int i = 1; // the first tag is <root>
        for (Token t : tokens) {
            Word w = sentence.getWords().get(i);
            String xtag = w.getXpostag();
            String utag = w.getUpostag();
            
            // For Norwegian xtag is not provided. It is a blank string.
            // So the value of Utag is used as an replacement. 
            if (xtag.length() == 0 && utag.length() > 0) {
                xtag = utag;
            }
            
            // Convert the tag produced by the tagger to an UIMA type, create an annotation
            // of this type, and add it to the document.
            Type posTag = mappingProvider.getTagType(xtag);
            POS posAnno = (POS) cas.createAnnotation(posTag, t.getBegin(), t.getEnd());
            // To save memory, we typically intern() tag strings
            posAnno.setPosValue(xtag.intern());
            if (utag == null) {
                POSUtils.assignCoarseValue(posAnno);
            }
            else {
                posAnno.setCoarseValue(utag.intern());
            }
            posAnno.addToIndexes();
            
            // Connect the POS annotation to the respective token annotation
            t.setPos(posAnno);
            
            if (StringUtils.isNotBlank(w.getLemma())) {
                Lemma lemma = new Lemma(aJCas, t.getBegin(), t.getEnd());
                lemma.setValue(w.getLemma());
                lemma.addToIndexes();
                t.setLemma(lemma);
            }

            if (StringUtils.isNotBlank(w.getForm())) {
                MorphologicalFeatures morph = new MorphologicalFeatures(aJCas, t.getBegin(),
                        t.getEnd());
                morph.setValue(w.getFeats());
                morph.addToIndexes();
                t.setMorph(morph);
            }

            i++;
        }
    }
    
    public static void convertParse(Sentence sentence, List<Token> tokens, JCas aJCas,
            MappingProvider mappingProvider)
    {
        for (int i = 1; i < sentence.getWords().size(); i++) {
            Word w = sentence.getWords().get(i);
            
            if (StringUtils.isNotBlank(w.getDeprel())) {
                int depId = w.getId();
                int govId = w.getHead();

                // Model the root as a loop onto itself
                makeDependency(mappingProvider, aJCas, govId, depId, w.getDeprel(),
                        DependencyFlavor.BASIC, tokens);
            }
            
            if (StringUtils.isNotBlank(w.getDeps())) {
                // list items separated by vertical bar
                String[] items = w.getDeps().split("\\|");
                for (String item : items) {
                    String[] sItem = item.split(":");
                    
                    int depId = w.getId();
                    int govId = Integer.valueOf(sItem[0]);

                    makeDependency(mappingProvider, aJCas, govId, depId, sItem[1],
                            DependencyFlavor.ENHANCED, tokens);
                }
            }
        }
    }
    
    private static Dependency makeDependency(MappingProvider mappingProvider, JCas aJCas, int govId,
            int depId, String label, String flavor, List<Token> tokens)
    {
        // write dependency information as annotation to JCas
        Type depRel = mappingProvider.getTagType(label);
        
        Dependency rel;

        if (govId == 0) {
            rel = new ROOT(aJCas);
            rel.setGovernor(tokens.get(depId - 1));
            rel.setDependent(tokens.get(depId - 1));
        }
        else {
            rel = (Dependency) aJCas.getCas().createFS(depRel);
            rel.setGovernor(tokens.get(govId - 1));
            rel.setDependent(tokens.get(depId - 1));
        }

        rel.setDependencyType(label);
        rel.setFlavor(flavor);
        rel.setBegin(rel.getDependent().getBegin());
        rel.setEnd(rel.getDependent().getEnd());
        rel.addToIndexes();

        return rel;
    }
}
