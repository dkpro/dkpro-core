/*******************************************************************************
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.nlp4j.internal;

import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.BilouDecoder;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;
import edu.emory.mathcs.nlp.component.template.node.NLPNode;

public class EmoryNlp2Uima
{
    public static void convertPos(CAS aCas, List<Token> aTokens, NLPNode[] aNodes,
            MappingProvider aMappingProvider, boolean internStrings)
    {
        // EmoryNLP tokens start at 1
        int i = 1;
        for (Token t : aTokens) {
            String tag = aNodes[i].getPartOfSpeechTag();

            // Convert the tag produced by the tagger to an UIMA type, create an annotation
            // of this type, and add it to the document.
            Type posTag = aMappingProvider.getTagType(tag);
            POS posAnno = (POS) aCas.createAnnotation(posTag, t.getBegin(), t.getEnd());
            // To save memory, we typically intern() tag strings
            posAnno.setPosValue(internStrings ? tag.intern() : tag);
            posAnno.addToIndexes();
            
            // Connect the POS annotation to the respective token annotation
            t.setPos(posAnno);
            i++;
        }
    }

    public static void convertDependencies(JCas aJCas, List<Token> aTokens, NLPNode[] aNodes,
            MappingProvider aMappingProvider, boolean aInternTags)
    {
        for (int i = 1; i < aNodes.length; i++) {
            NLPNode depNode = aNodes[i];
            NLPNode govNode = depNode.getDependencyHead();
            String label = depNode.getDependencyLabel();

            if (govNode.getID() != 0) {
                Type depRel = aMappingProvider.getTagType(label);
                Dependency dep = (Dependency) aJCas.getCas().createFS(depRel);
                dep.setDependencyType(aInternTags ? label.intern() : label);
                dep.setDependent(aTokens.get(depNode.getID() - 1));
                dep.setGovernor(aTokens.get(govNode.getID() - 1));
                dep.setBegin(dep.getDependent().getBegin());
                dep.setEnd(dep.getDependent().getEnd());
                dep.addToIndexes();
            }
            else {
                Dependency dep = new ROOT(aJCas);
                dep.setDependencyType(label);
                dep.setDependent(aTokens.get(depNode.getID() - 1));
                dep.setGovernor(aTokens.get(depNode.getID() - 1));
                dep.setBegin(dep.getDependent().getBegin());
                dep.setEnd(dep.getDependent().getEnd());
                dep.addToIndexes();
            }
        }
    }

    public static void convertNamedEntities(CAS aCas, List<Token> aTokens, NLPNode[] aNodes,
            MappingProvider aMappingProvider, boolean aInternTags)
    {
        Type neType = aCas.getTypeSystem().getType(NamedEntity.class.getName());
        Feature valueFeat = neType.getFeatureByBaseName("value");

        String[] neTags = new String[aNodes.length-1];
        for (int i = 1; i < aNodes.length; i++) {
            neTags[i-1] = aNodes[i].getNamedEntityTag();
        }
        
        BilouDecoder decoder = new BilouDecoder(aCas, valueFeat, aMappingProvider);
        decoder.setInternTags(aInternTags);
        decoder.decode(aTokens, neTags);
    }
}
