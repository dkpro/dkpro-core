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

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Marker;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.lxf.internal.model.LxfEdge;
import org.dkpro.core.io.lxf.internal.model.LxfGraph;
import org.dkpro.core.io.lxf.internal.model.LxfNode;
import org.dkpro.core.io.lxf.internal.model.LxfRegion;
import org.dkpro.core.io.lxf.internal.model.LxfText;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class DKPro2Lxf
{
    public static void convert(JCas aJCas, LxfGraph aTarget)
    {
        convert(aJCas, null, aTarget);
    }
    
    /**
     * Convert from CAS to LXF.
     * 
     * @param aJCas the source CAS.
     * @param aSource the original LXF. If this is non-null, then delta-mode is enabled.
     * @param aTarget the target LXF.
     */
    public static void convert(JCas aJCas, LxfGraph aSource, LxfGraph aTarget)
    {
        // Add media only when not in delta mode
        if (aSource == null) {
            aTarget.setMedia(new LxfText(aJCas.getDocumentText()));
        }
        
        // Indexes counting up across the whole document
        int sentenceIndex = 0;
        int tokenIndex = 0;
        int dependencyNodeIndex = 0;
        int dependencyEdgeIndex = 0;
        
        Map<Sentence, Collection<Token>> idxSentTok = indexCovered(aJCas, Sentence.class, Token.class);
        Map<Sentence, Collection<Dependency>> idxSentDep = indexCovered(aJCas, Sentence.class, Dependency.class);
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            // Convert or obtain sentence node
            LxfNode sentenceNode;
            if (aSource == null || needsExport(aJCas, sentence)) {
                // Sentence region
                LxfRegion sentenceRegion = new LxfRegion(TOOL_TOKENIZER, sentenceIndex,
                        sentence.getBegin(), sentence.getEnd());
                aTarget.addRegion(sentenceRegion);
                sentenceNode = new LxfNode(LAYER_SENTENCE, TOOL_TOKENIZER, sentenceIndex, 0,
                        sentenceRegion);
                // Setting this to the base text as per discussion
                sentenceNode.setFeature(FEAT_LABEL, sentence.getCoveredText());
                aTarget.addNode(sentenceNode);
            }
            else {
                sentenceNode = aSource.getNode(LAYER_SENTENCE, TOOL_TOKENIZER, sentenceIndex, 0);
            }

            // Tokens, POS, lemma
            Map<Token, LxfNode> idxMorph = new HashMap<>();
            Collection<Token> tokens = idxSentTok.get(sentence);
            for (Token token : tokens) {
                // Convert or obtain token node
                LxfNode tokenNode;
                if (aSource == null || needsExport(aJCas, token)) {
                    LxfRegion tokenRegion = new LxfRegion(TOOL_REPP, tokenIndex, token.getBegin(),
                            token.getEnd());
                    aTarget.addRegion(tokenRegion);
                    tokenNode = new LxfNode(LAYER_TOKEN, TOOL_REPP, tokenIndex, 0, tokenRegion);
                    tokenNode.setFeature(FEAT_LABEL, token.getText());
                    aTarget.addNode(tokenNode);
                    aTarget.addEdge(new LxfEdge(tokenNode.getOrigin(), tokenIndex, 0, tokenNode,
                            sentenceNode));
                }
                else {
                    tokenNode = aSource.getNode(LAYER_TOKEN, TOOL_REPP, tokenIndex, 0);
                }
                
                // Convert POS if exists - if we create a node, pass it on to the lemma conversion
                // as well
                POS pos = token.getPos();
                LxfNode morphNode = null;
                if (pos != null) {
                    if ((aSource == null || needsExport(aJCas, pos))) {
                        morphNode = new LxfNode(LAYER_MORPHOLOGY, TOOL_HUNPOS, tokenIndex, 0);
                        morphNode.setFeature(FEAT_POS, token.getPos().getPosValue());
                        aTarget.addNode(morphNode);
                        aTarget.addEdge(new LxfEdge(morphNode.getOrigin(), tokenIndex, 0, morphNode, tokenNode));
                        
                        // Need to remember this because we may want to connect the dependencies to
                        // this node
                        idxMorph.put(token, morphNode);
                    }
                    else {
                        // FIXME in delta mode we have to try fishing the appropriate morphology
                        // node from the source LXF...
                    }
                }

                // Convert lemma if exists
                Lemma lemma = token.getLemma();
                if (lemma != null && (aSource == null || needsExport(aJCas, lemma))) {
                    // If we have created a sharable morphNode, reuse it here, otherwise create a
                    // new node
                    LxfNode lemmaNode = morphNode;
                    if (lemmaNode == null) {
                        lemmaNode = new LxfNode(LAYER_MORPHOLOGY, TOOL_HUNPOS, tokenIndex, 0);
                        aTarget.addNode(lemmaNode);
                        aTarget.addEdge(new LxfEdge(lemmaNode.getOrigin(), tokenIndex, 0, lemmaNode,
                                tokenNode));
                    }
                    lemmaNode.setFeature(FEAT_LEMMA, token.getPos().getPosValue());
                }

                tokenIndex++;
            }

            // Dependencies
            Collection<Dependency> deps = idxSentDep.get(sentence);
            for (Dependency dep : deps) {
                LxfNode depNode = new LxfNode(LAYER_DEPENDENCY, TOOL_BN, dependencyNodeIndex, 0);
                depNode.setFeature(FEAT_LABEL, dep.getDependencyType());
                // FIXME What is it? depNode.setFeature(FEAT_HEAD, ???);
                aTarget.addNode(depNode);
                
                LxfNode govMorphNode = idxMorph.get(dep.getGovernor());
                LxfNode depMorphNode = idxMorph.get(dep.getDependent());
                               
                aTarget.addEdge(new LxfEdge(depNode.getOrigin(), dependencyEdgeIndex, 0,
                        depMorphNode, depNode));
                dependencyEdgeIndex++;
                
                // FIXME Lap doesn't seem to use self-looping ROOT nodes, so we actually need to
                // handle this here and unloop the ROOT node
                aTarget.addEdge(new LxfEdge(depNode.getOrigin(), dependencyEdgeIndex, 0, depNode,
                        govMorphNode));
                dependencyEdgeIndex++;
                        
                dependencyNodeIndex++;
            }
            
            
            sentenceIndex++;
        }
    }
    
    private static boolean needsExport(JCas aCas, FeatureStructure aFS)
    {
        Marker marker = aCas.getCasImpl().getCurrentMark();
        return marker.isNew(aFS);
    }
}
