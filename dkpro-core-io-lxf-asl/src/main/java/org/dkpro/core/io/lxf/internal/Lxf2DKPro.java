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
 */package org.dkpro.core.io.lxf.internal;

import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.FEAT_LABEL;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.FEAT_LEMMA;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.FEAT_POS;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.LAYER_DEPENDENCY;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.LAYER_MORPHOLOGY;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.LAYER_SENTENCE;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.LAYER_TOKEN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.lxf.internal.model.LxfEdge;
import org.dkpro.core.io.lxf.internal.model.LxfGraph;
import org.dkpro.core.io.lxf.internal.model.LxfNode;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;

public class Lxf2DKPro
{
    public static void convert(LxfGraph aLxf, JCas aJCas)
    {

        aJCas.setDocumentText(aLxf.getMedia().getData());

        List<LxfNode> sentenceNodes = aLxf.getNodes().stream()
                .filter(n -> LAYER_SENTENCE.equals(n.getLayer())).collect(Collectors.toList());
        for (LxfNode sn : sentenceNodes) {
            int[] region = aLxf.getRegion(sn.getLinks().get(0).get(0)).getAnchors();
            Sentence sentence = new Sentence(aJCas, region[0], region[1]);
            // label feature on sentence seems redundant because tokens also have it
            // token.setForm(s.getFeature(FEAT_LABEL));
            sentence.addToIndexes();
        }

        // Convert tokens
        Map<String, Token> idxToken = new HashMap<>();
        List<LxfNode> tokenNodes = aLxf.getNodes().stream()
                .filter(n -> LAYER_TOKEN.equals(n.getLayer())).collect(Collectors.toList());
        for (LxfNode tn : tokenNodes) {
            int[] region = aLxf.getRegion(tn.getLinks().get(0).get(0)).getAnchors();
            Token token = new Token(aJCas, region[0], region[1]);
            token.setText(tn.getFeature(FEAT_LABEL));
            token.addToIndexes();
            idxToken.put(tn.getId(), token);
        }

        // Convert morphology (pos, lemma)
        List<LxfNode> posNodes = aLxf.getNodes().stream()
                .filter(n -> LAYER_MORPHOLOGY.equals(n.getLayer())).collect(Collectors.toList());
        for (LxfNode pn : posNodes) {
            // We assume that if there is a POS it must be attached to exactly one token node
            String tokenId = aLxf.getEdges(pn, LAYER_TOKEN).get(0).getTo()[1];
            Token token = idxToken.get(tokenId);

            // Convert POS if pos feature is set
            if (pn.getFeature(FEAT_POS) != null) {
                POS pos = new POS(aJCas, token.getBegin(), token.getEnd());
                pos.setPosValue(pn.getFeature(FEAT_POS));
                pos.addToIndexes();
                token.setPos(pos);
            }

            // Convert Lemma if lemma feature is set
            if (pn.getFeature(FEAT_LEMMA) != null) {
                Lemma lemma = new Lemma(aJCas, token.getBegin(), token.getEnd());
                lemma.setValue(pn.getFeature(FEAT_LEMMA));
                lemma.addToIndexes();
                token.setLemma(lemma);
            }
        }

        // Convert dependencies
        List<LxfNode> dependencyNodes = aLxf.getNodes().stream()
                .filter(n -> LAYER_DEPENDENCY.equals(n.getLayer())).collect(Collectors.toList());

        for (LxfNode dn : dependencyNodes) {
            // We assume that if there is a dependency it must be attached to exactly one governor
            // and one dependent
            List<LxfEdge> govEdges = aLxf.getEdges(LAYER_MORPHOLOGY, dn);
            List<LxfEdge> depEdges = aLxf.getEdges(dn, LAYER_MORPHOLOGY);

            LxfNode govMorphNode = govEdges.isEmpty() ? null
                    : aLxf.getNode(govEdges.get(0).getFrom());
            LxfNode depMorphNode = depEdges.isEmpty() ? null
                    : aLxf.getNode(depEdges.get(0).getTo());

            // We assume that the gov and dep nodes are attached each to exactly one token
            Token govToken;
            Token depToken;
            try {
                govToken = govMorphNode != null
                        ? idxToken.get(aLxf.getEdges(govMorphNode, LAYER_TOKEN).get(0).getTo()[1])
                        : null;
                depToken = depMorphNode != null
                        ? idxToken.get(aLxf.getEdges(depMorphNode, LAYER_TOKEN).get(0).getTo()[1])
                        : null;
            }
            catch (IndexOutOfBoundsException e) {
                // Ok, so looks like somebody forgot to link the POS to the tokens... let's see
                // if we can recover from that somehow, e.g. by going over the indexes.
                govToken = govMorphNode != null
                        ? idxToken.get(String.format("repp-n%d@1", govMorphNode.getIndex() + 1))
                        : null;
                depToken = depMorphNode != null
                        ? idxToken.get(String.format("repp-n%d@1", depMorphNode.getIndex() + 1))
                        : null;
            }

            // Create dependency relation according to DKPro Core conventions
            if (depToken != null && govToken != null) {
                Dependency dep = new Dependency(aJCas);
                dep.setDependencyType(dn.getFeature(FEAT_LABEL));
                dep.setFlavor(DependencyFlavor.BASIC);
                dep.setGovernor(govToken);
                dep.setDependent(depToken);
                dep.setBegin(dep.getDependent().getBegin());
                dep.setEnd(dep.getDependent().getEnd());
                dep.addToIndexes();
            }
            else if (depToken != null && govToken == null) {
                Dependency dep = new ROOT(aJCas);
                dep.setDependencyType("ROOT");
                dep.setFlavor(DependencyFlavor.BASIC);
                dep.setGovernor(depToken);
                dep.setDependent(depToken);
                dep.setBegin(dep.getGovernor().getBegin());
                dep.setEnd(dep.getDependent().getEnd());
                dep.addToIndexes();
            }
            else {
                throw new IllegalStateException("Illegal dependency relation.");
            }
        }
    }
}
