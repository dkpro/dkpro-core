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
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.FEAT_LABEL;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.FEAT_LEMMA;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.FEAT_POS;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.LAYER_DEPENDENCY;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.LAYER_MORPHOLOGY;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.LAYER_SENTENCE;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.LAYER_TOKEN;

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

    public static void convert(JCas aJCas, LxfGraph aSource, LxfGraph aTarget)
    {
        convert(aJCas, aSource, aTarget, createIdMap("dkpro", aSource));
    }

    /**
     * Creates an id map that contains of the correspondence between tools and annotation layer. The
     * key in the map is the layer and the value is the annotation tool that created ther layer. If
     * the layer was present in the source than the tool from the source will be used for the layer.
     * Otherwise the toolName will be used.
     * 
     * @param toolName
     *            - Tool name for new layers
     * @param aSource
     *            - original lxf for DKPro
     * @return
     */
    public static Map<String, String> createIdMap(String toolName, LxfGraph aSource)
    {
        Map<String, String> ids = new HashMap<>();
        if (aSource != null) {
            for (LxfNode n : aSource.getNodes()) {
                for (String tool : n.getAnnotations().keySet()) {
                    ids.put(n.getAnnotations().get(tool).get("class"), tool);
                }
            }
        }
        if (!ids.containsKey(LAYER_DEPENDENCY))
            ids.put(LAYER_DEPENDENCY, toolName);
        if (!ids.containsKey(LAYER_MORPHOLOGY))
            ids.put(LAYER_MORPHOLOGY, toolName);
        if (!ids.containsKey(LAYER_SENTENCE))
            ids.put(LAYER_SENTENCE, toolName);
        if (!ids.containsKey(LAYER_TOKEN))
            ids.put(LAYER_TOKEN, toolName);
        return ids;
    }

    /**
     * Convert from CAS to LXF.
     * 
     * @param aJCas
     *            the source CAS.
     * @param aSource
     *            the original LXF. If this is non-null, then delta-mode is enabled.
     * @param aTarget
     *            the target LXF.
     * @param ids
     *            The ids of the tool responsible for generation of the annotation Layer. The key is
     *            the annotation layer. The value is the tool that generates the annotation.
     */
    public static void convert(JCas aJCas, LxfGraph aSource, LxfGraph aTarget,
            Map<String, String> ids)
    {
        if (aSource == null) {
            aTarget.setMedia(new LxfText(aJCas.getDocumentText()));
        }

        ToolGeneratorIndex toolEdgeIndex = new ToolGeneratorIndex(ids.values());
        ToolGeneratorIndex toolNodeIndex = new ToolGeneratorIndex(ids.values());
        ToolGeneratorIndex toolRegionIndex = new ToolGeneratorIndex(ids.values());
        NodeIterator iter = new NodeIterator(aSource);

        Map<Sentence, Collection<Token>> idxSentTok = indexCovered(aJCas, Sentence.class,
                Token.class);

        Map<Sentence, Collection<Dependency>> idxSentDep = indexCovered(aJCas, Sentence.class,
                Dependency.class);

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            LxfNode sentenceNode;

            String toolid = ids.get(LAYER_SENTENCE);

            if (aSource == null || needsExport(aJCas, sentence)) {
                // Sentence region
                LxfRegion sentenceRegion = new LxfRegion(toolid, toolRegionIndex.nextIndex(toolid),
                        sentence.getBegin(), sentence.getEnd());
                aTarget.addRegion(sentenceRegion);
                sentenceNode = new LxfNode(LAYER_SENTENCE, toolid, toolNodeIndex.nextIndex(toolid),
                        0, sentenceRegion);
                // Setting this to the base text as per discussion
                sentenceNode.setFeature(FEAT_LABEL, sentence.getCoveredText());
                aTarget.addNode(sentenceNode);

            }
            else {
                sentenceNode = iter.next(toolid, LAYER_SENTENCE);
            }

            // Tokens, POS, lemma
            Map<Token, LxfNode> idxMorph = new HashMap<>();
            Collection<Token> tokens = idxSentTok.get(sentence);
            for (Token token : tokens) {
                // Convert or obtain token node
                LxfNode tokenNode;
                toolid = ids.get(LAYER_TOKEN);

                if (aSource == null || needsExport(aJCas, token)) {
                    LxfRegion tokenRegion = new LxfRegion(toolid, toolRegionIndex.nextIndex(toolid),
                            token.getBegin(), token.getEnd());
                    aTarget.addRegion(tokenRegion);
                    tokenNode = new LxfNode(LAYER_TOKEN, toolid, toolNodeIndex.nextIndex(toolid), 0,
                            tokenRegion);

                    String form = token.getText();
                    tokenNode.setFeature(FEAT_LABEL, form);
                    aTarget.addNode(tokenNode);
                    int edgeIndex = toolEdgeIndex.nextIndex(toolid);
                    aTarget.addEdge(new LxfEdge(tokenNode.getOrigin(), edgeIndex, 0, tokenNode,
                            sentenceNode));
                }
                else {
                    tokenNode = iter.next(toolid, LAYER_TOKEN);
                }

                toolid = ids.get(LAYER_MORPHOLOGY);

                // Convert POS if exists - if we create a node, pass it on to the lemma conversion
                // as well
                POS pos = token.getPos();
                LxfNode morphNode = null;
                if (pos != null) {
                    if ((aSource == null || needsExport(aJCas, pos))) {
                        morphNode = new LxfNode(LAYER_MORPHOLOGY, toolid,
                                toolNodeIndex.nextIndex(toolid), 0);
                        morphNode.setFeature(FEAT_POS, token.getPos().getPosValue());
                        aTarget.addNode(morphNode);
                        aTarget.addEdge(new LxfEdge(morphNode.getOrigin(),
                                toolEdgeIndex.nextIndex(toolid), 0, morphNode, tokenNode));

                        // Need to remember this because we may want to connect the dependencies to
                        // this node
                        idxMorph.put(token, morphNode);
                    }
                    else {
                        morphNode = iter.next(toolid, LAYER_MORPHOLOGY);
                        idxMorph.put(token, morphNode);
                    }
                }
                // Convert lemma if exists
                Lemma lemma = token.getLemma();
                if (lemma != null && (aSource == null || needsExport(aJCas, lemma))) {
                    // If we have created a sharable morphNode, reuse it here, otherwise create a
                    // new node
                    LxfNode lemmaNode = morphNode;
                    if (lemmaNode == null) {
                        lemmaNode = new LxfNode(LAYER_MORPHOLOGY, toolid,
                                toolNodeIndex.nextIndex(toolid), 0);
                        aTarget.addNode(lemmaNode);
                        aTarget.addEdge(new LxfEdge(lemmaNode.getOrigin(),
                                toolEdgeIndex.nextIndex(toolid), 0, lemmaNode, tokenNode));
                        idxMorph.put(token, lemmaNode);
                    }
                    lemmaNode.setFeature(FEAT_LEMMA, token.getPos().getPosValue());
                }

            }

            toolid = ids.get(LAYER_DEPENDENCY);

            // Dependencies
            Collection<Dependency> deps = idxSentDep.get(sentence);

            for (Dependency dep : deps) {

                if (aSource != null && !needsExport(aJCas, dep))
                    continue;

                LxfNode depNode = new LxfNode(LAYER_DEPENDENCY, toolid,
                        toolNodeIndex.nextIndex(toolid), 0);
                depNode.setFeature(FEAT_LABEL, dep.getDependencyType());
                aTarget.addNode(depNode);

                LxfNode govMorphNode = idxMorph.get(dep.getGovernor());
                LxfNode depMorphNode = idxMorph.get(dep.getDependent());

                aTarget.addEdge(new LxfEdge(depNode.getOrigin(), toolEdgeIndex.nextIndex(toolid), 0,
                        depNode, depMorphNode));

                if (!govMorphNode.getId().equals(depMorphNode.getId())) {
                    aTarget.addEdge(new LxfEdge(depNode.getOrigin(),
                            toolEdgeIndex.nextIndex(toolid), 0, govMorphNode, depNode));

                }
            }
        }
    }

    private static boolean needsExport(JCas aCas, FeatureStructure aFS)
    {
        Marker marker = aCas.getCasImpl().getCurrentMark();
        return marker.isNew(aFS);
    }
}
