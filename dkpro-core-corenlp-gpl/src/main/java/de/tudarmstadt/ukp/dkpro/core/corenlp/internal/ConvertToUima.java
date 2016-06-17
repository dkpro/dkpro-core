/**
 * Copyright 2007-2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.corenlp.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.AbstractTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;

public class ConvertToUima
{
    public static void convertPOSs(JCas aJCas, Annotation document,
            MappingProvider mappingProvider, boolean internStrings)
    {
        for (CoreMap s : document.get(SentencesAnnotation.class)) {
            for (CoreLabel t : s.get(TokensAnnotation.class)) {
                Token token = t.get(TokenKey.class);
                String tag = t.get(PartOfSpeechAnnotation.class);
                Type tagType = mappingProvider.getTagType(tag);
                POS anno = (POS) aJCas.getCas().createAnnotation(tagType, token.getBegin(),
                        token.getEnd());
                anno.setPosValue(internStrings ? tag.intern() : tag);
                anno.addToIndexes();
                token.setPos(anno);
            }
        }
    }
    
    public static void convertNamedEntities(JCas aJCas, Annotation document,
            MappingProvider mappingProvider, boolean internStrings)
    {
        for (CoreMap s : document.get(SentencesAnnotation.class)) {
            for (CoreLabel t : s.get(TokensAnnotation.class)) {
                Token token = t.get(TokenKey.class);
                String tag = t.get(NamedEntityTagAnnotation.class);
                
                // "O" is the hard-coded tag in CoreNLP to indicate no NER on this token
                if ("O".equals(tag)) {
                    continue;
                }
                
                Type tagType = mappingProvider.getTagType(tag);
                NamedEntity anno = (NamedEntity) aJCas.getCas().createAnnotation(tagType,
                        token.getBegin(), token.getEnd());
                anno.setValue(internStrings ? tag.intern() : tag);
                anno.addToIndexes();
            }
        }
    }
    
    public static void convertLemmas(JCas aJCas, Annotation document)
    {
        for (CoreMap s : document.get(SentencesAnnotation.class)) {
            for (CoreLabel t : s.get(TokensAnnotation.class)) {
                Token token = t.get(TokenKey.class);
                String tag = t.get(LemmaAnnotation.class);
                Lemma anno = new Lemma(aJCas, token.getBegin(), token.getEnd());
                anno.setValue(tag);
                anno.addToIndexes();
                token.setLemma(anno);
            }
        }
    }
    
    public static void convertDependencies(JCas aJCas, Annotation document,
            MappingProvider mappingProvider, boolean internStrings)
    {
        for (CoreMap s : document.get(SentencesAnnotation.class)) {
            SemanticGraph graph = s.get(CollapsedDependenciesAnnotation.class);
            
            // If there are no dependencies for this sentence, skip it. Might well mean we
            // skip all sentences because normally either there are dependencies for all or for
            // none.
            if (graph == null) {
                continue;
            }
            
            for (IndexedWord root : graph.getRoots()) {
                Dependency dep = new ROOT(aJCas);
                dep.setDependencyType("root");
                dep.setDependent(root.get(TokenKey.class));
                dep.setGovernor(root.get(TokenKey.class));
                dep.setBegin(dep.getDependent().getBegin());
                dep.setEnd(dep.getDependent().getEnd());
                dep.addToIndexes();
            }
            
            for (SemanticGraphEdge edge : graph.edgeListSorted()) {
                Token dependent = edge.getDependent().get(TokenKey.class);
                Token governor = edge.getGovernor().get(TokenKey.class);
                
                // For the type mapping, we use getShortName() instead, because the <specific>
                // actually doesn't change the relation type
                String labelUsedForMapping = edge.getRelation().getShortName();
                
                // The nndepparser may produce labels in which the shortName contains a colon.
                // These represent language-specific labels of the UD, cf: 
                // http://universaldependencies.github.io/docs/ext-dep-index.html
                labelUsedForMapping = StringUtils.substringBefore(labelUsedForMapping, ":");
                
                // Need to use toString() here to get "<shortname>_<specific>"
                String actualLabel = edge.getRelation().toString();
                
                Type depRel = mappingProvider.getTagType(labelUsedForMapping);
                Dependency dep = (Dependency) aJCas.getCas().createFS(depRel);
                dep.setDependencyType(internStrings ? actualLabel.intern() : actualLabel);
                dep.setDependent(dependent);
                dep.setGovernor(governor);
                dep.setBegin(dep.getDependent().getBegin());
                dep.setEnd(dep.getDependent().getEnd());
                dep.setFlavor(edge.isExtra() ? DependencyFlavor.ENHANCED : DependencyFlavor.BASIC);
                dep.addToIndexes();
            }
        }
    }

    public static void convertConstituents(JCas aJCas, Annotation aDocument,
            MappingProvider aMappingProvider, boolean aInternStrings,
            TreebankLanguagePack aTreebankLanguagePack)
    {
        for (CoreMap s : aDocument.get(SentencesAnnotation.class)) {
            Tree tree = s.get(TreeCoreAnnotations.TreeAnnotation.class);
            tree.setSpans();
            List<CoreLabel> tokens = s.get(TokensAnnotation.class);
            convertConstituentTreeNode(aJCas, aTreebankLanguagePack, tree, null, aInternStrings,
                    aMappingProvider, tokens);
        }
        
    }

    private static org.apache.uima.jcas.tcas.Annotation convertConstituentTreeNode(JCas aJCas,
            TreebankLanguagePack aTreebankLanguagePack, Tree aNode,
            org.apache.uima.jcas.tcas.Annotation aParentFS, boolean internStrings,
            MappingProvider constituentMappingProvider, List<CoreLabel> tokens)
    {
        // Get node label
        String nodeLabelValue = aNode.value();
        
        // Extract syntactic function from node label
        String syntacticFunction = null;
        AbstractTreebankLanguagePack tlp = (AbstractTreebankLanguagePack) aTreebankLanguagePack;
        int gfIdx = nodeLabelValue.indexOf(tlp.getGfCharacter());
        if (gfIdx > 0) {
            syntacticFunction = nodeLabelValue.substring(gfIdx + 1);
            nodeLabelValue = nodeLabelValue.substring(0, gfIdx);
        }

        // Check if node is a constituent node on sentence or phrase-level
        if (aNode.isPhrasal()) {
            Type constType = constituentMappingProvider.getTagType(nodeLabelValue);

            IntPair span = aNode.getSpan();
            int begin = tokens.get(span.getSource()).get(CharacterOffsetBeginAnnotation.class);
            int end = tokens.get(span.getTarget()).get(CharacterOffsetEndAnnotation.class);
            
            Constituent constituent = (Constituent) aJCas.getCas().createAnnotation(constType,
                    begin, end);
            constituent.setConstituentType(internStrings ? nodeLabelValue.intern() : 
                nodeLabelValue);
            constituent.setSyntacticFunction(internStrings && syntacticFunction != null ? 
                    syntacticFunction.intern() : syntacticFunction);
            constituent.setParent(aParentFS);

            // Do we have any children?
            List<org.apache.uima.jcas.tcas.Annotation> childAnnotations = new ArrayList<>();
            for (Tree child : aNode.getChildrenAsList()) {
                org.apache.uima.jcas.tcas.Annotation childAnnotation = convertConstituentTreeNode(
                        aJCas, aTreebankLanguagePack, child, constituent, internStrings,
                        constituentMappingProvider, tokens);
                if (childAnnotation != null) {
                    childAnnotations.add(childAnnotation);
                }
            }
            
            // Now that we know how many children we have, link annotation of
            // current node with its children
            constituent.setChildren(FSCollectionFactory.createFSArray(aJCas, childAnnotations));

            constituent.addToIndexes();

            return constituent;
        }
        // Create parent link on token
        else if (aNode.isPreTerminal()) {
            // link token to its parent constituent
            List<Tree> children = aNode.getChildrenAsList();
            assert children.size() == 1;
            Tree terminal = children.get(0);
            CoreLabel label = (CoreLabel) terminal.label();
            Token token = label.get(TokenKey.class);
            token.setParent(aParentFS);
            return token;
        }
        else {
            throw new IllegalArgumentException("Node must be either phrasal nor pre-terminal");
        }
    }
    
    public static void convertPennTree(JCas aJCas, Annotation aDocument)
    {
        for (CoreMap s : aDocument.get(SentencesAnnotation.class)) {
            Tree tree = s.get(TreeCoreAnnotations.TreeAnnotation.class);
            int begin = s.get(CharacterOffsetBeginAnnotation.class);
            int end = s.get(CharacterOffsetEndAnnotation.class);

            // create tree with simple labels and get penn string from it
            tree = tree.deepCopy(tree.treeFactory(), StringLabel.factory());

            // write Penn Treebank-style string to cas
            PennTree pTree = new PennTree(aJCas, begin, end);
            pTree.setPennTree(tree.pennString());
            pTree.addToIndexes();
        }
        
    }

    public static void convertCorefChains(JCas aJCas, Annotation aDocument)
    {
        List<CoreMap> sentences = aDocument.get(SentencesAnnotation.class);
        Map<Integer, CorefChain> chains = aDocument
                .get(CorefCoreAnnotations.CorefChainAnnotation.class);
        for (CorefChain chain : chains.values()) {
            CoreferenceLink last = null;
            for (CorefMention mention : chain.getMentionsInTextualOrder()) {
                CoreLabel beginLabel = sentences.get(mention.sentNum - 1)
                        .get(TokensAnnotation.class).get(mention.startIndex - 1);
                CoreLabel endLabel = sentences.get(mention.sentNum - 1).get(TokensAnnotation.class)
                        .get(mention.endIndex - 2);
                CoreferenceLink link = new CoreferenceLink(aJCas, beginLabel.get(TokenKey.class)
                        .getBegin(), endLabel.get(TokenKey.class).getEnd());
    
                if (mention.mentionType != null) {
                    link.setReferenceType(mention.mentionType.toString());
                }
    
                if (last == null) {
                    // This is the first mention. Here we'll initialize the chain
                    CoreferenceChain corefChain = new CoreferenceChain(aJCas);
                    corefChain.setFirst(link);
                    corefChain.addToIndexes();
                }
                else {
                    // For the other mentions, we'll add them to the chain.
                    last.setNext(link);
                }
                last = link;
    
                link.addToIndexes();
            }
        }
    }
}
