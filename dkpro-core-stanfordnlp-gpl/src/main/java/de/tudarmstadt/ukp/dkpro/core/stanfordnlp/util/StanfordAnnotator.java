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
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.Tag;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.AbstractTreebankLanguagePack;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.IntPair;

/**
 * A StanfordAnnotator-object creates most of the annotations for the StanfordParser component.
 * <p>
 * The code has been moved away from the Parser component because it is also used by other
 * components (e.g. Transformations)
 *
 */
public class StanfordAnnotator
{
    private static final String DEPPACKAGE = Dependency.class.getPackage().getName() + ".";

    /**
     * The separator that is used by Tsurgeon-Operations to separate additional tags from
     * node-labels.
     */
    private static final String TAG_SEPARATOR = "#";

    private TreeWithTokens tokenTree = null;
    private JCas jCas = null;
    private MappingProvider posMappingProvider;
    private MappingProvider constituentMappingProvider;

    public TreeWithTokens getTokenTree()
    {
        return tokenTree;
    }

    public void setTokenTree(TreeWithTokens aTokenTree)
    {
        tokenTree = aTokenTree;
    }

    public JCas getJCas()
    {
        return jCas;
    }

    public void setJCas(JCas aJCas)
    {
        jCas = aJCas;
    }

    public void setPosMappingProvider(MappingProvider aPosMappingProvider)
    {
        posMappingProvider = aPosMappingProvider;
    }

    public void setConstituentMappingProvider(MappingProvider aConstituentMappingProvider)
    {
        constituentMappingProvider = aConstituentMappingProvider;
    }
    
    public StanfordAnnotator(TreeWithTokens aTokenTree)
        throws CASException
    {
        setTokenTree(aTokenTree);
        setJCas(aTokenTree.getTokens().get(0).getCAS().getJCas());
    }

    /**
     * Creates linked constituent annotations, POS annotations and lemma-annotations.
     * <p>
     * Note: The annotations are directly written to the indexes of the CAS.
     * 
     * @param aTreebankLanguagePack
     *            the language pack.
     * @param aCreatePos
     *            whether to create POS annotations.
     */
    public void createConstituentAnnotationFromTree(TreebankLanguagePack aTreebankLanguagePack,
            boolean aCreatePos)
    {
        createConstituentAnnotationFromTree(aTreebankLanguagePack, tokenTree.getTree(), null,
                aCreatePos);
    }

    /**
     * Creates linked constituent annotations + POS annotations
     * 
     * @param aTreebankLanguagePack
     *            the language pack.
     * @param aNode
     *            the source tree
     * @param aParentFS
     *            the parent annotation
     * @param aCreatePos
     *            sets whether to create or not to create POS tags
     * @return the child-structure (needed for recursive call only)
     */
    private Annotation createConstituentAnnotationFromTree(
            TreebankLanguagePack aTreebankLanguagePack, Tree aNode, Annotation aParentFS,
            boolean aCreatePos)
    {
        String nodeLabelValue = aNode.value();
        String syntacticFunction = null;
        AbstractTreebankLanguagePack tlp = (AbstractTreebankLanguagePack) aTreebankLanguagePack;
        int gfIdx = nodeLabelValue.indexOf(tlp.getGfCharacter());
        if (gfIdx > 0) {
            syntacticFunction = nodeLabelValue.substring(gfIdx + 1);
            nodeLabelValue = nodeLabelValue.substring(0, gfIdx);
        }

        // calculate span for the current subtree
        IntPair span = tokenTree.getSpan(aNode);

        // Check if the node has been marked by a TSurgeon operation.
        // If so, add a tag-annotation on the constituent
        if (nodeLabelValue.contains(TAG_SEPARATOR) && !nodeLabelValue.equals(TAG_SEPARATOR)) {
            int separatorIndex = nodeLabelValue.indexOf(TAG_SEPARATOR);
            String tag = nodeLabelValue.substring(0, separatorIndex);
            nodeLabelValue = nodeLabelValue.substring(separatorIndex + 1, nodeLabelValue.length());
            createTagAnnotation(span.getSource(), span.getTarget(), tag);
        }

        // Check if node is a constituent node on sentence or phrase-level
        if (aNode.isPhrasal()) {

            // add annotation to annotation tree
            Constituent constituent = createConstituentAnnotation(span.getSource(),
                    span.getTarget(), nodeLabelValue, syntacticFunction);
            // link to parent
            if (aParentFS != null) {
                constituent.setParent(aParentFS);
            }

            // Do we have any children?
            List<Annotation> childAnnotations = new ArrayList<Annotation>();
            for (Tree child : aNode.getChildrenAsList()) {
                Annotation childAnnotation = createConstituentAnnotationFromTree(
                        aTreebankLanguagePack, child, constituent, aCreatePos);
                if (childAnnotation != null) {
                    childAnnotations.add(childAnnotation);
                }
            }

            // Now that we know how many children we have, link annotation of
            // current node with its children
            FSArray children = new FSArray(jCas, childAnnotations.size());
            int curChildNum = 0;
            for (FeatureStructure child : childAnnotations) {
                children.set(curChildNum, child);
                curChildNum++;
            }
            constituent.setChildren(children);

            // write annotation for current node to index
            jCas.addFsToIndexes(constituent);

            return constituent;
        }
        // If the node is a word-level constituent node (== POS):
        // create parent link on token and (if not turned off) create POS tag
        else if (aNode.isPreTerminal()) {
            // create POS-annotation (annotation over the token)
            POS pos = createPOSAnnotation(span.getSource(), span.getTarget(), nodeLabelValue);

            // in any case: get the token that is covered by the POS
            // TODO how about multi word prepositions etc. (e.g. "such as")
            List<Token> coveredTokens = JCasUtil.selectCovered(jCas, Token.class, pos);
            // the POS should only cover one token
            assert coveredTokens.size() == 1;
            Token token = coveredTokens.get(0);

            // only add POS to index if we want POS-tagging
            if (aCreatePos) {
                jCas.addFsToIndexes(pos);
                token.setPos(pos);
            }

            // link token to its parent constituent
            if (aParentFS != null) {
                token.setParent(aParentFS);
            }

            return token;
        }
        else {
            throw new IllegalArgumentException("Node must be either phrasal nor pre-terminal");
        }
    }

    /**
     * Creates a tag-annotation over a constituent
     *
     * @param aBegin
     *            start-index of the constituent span
     * @param aEnd
     *            end-index of the constituent span
     * @param aTag
     *            the tag value
     */
    public void createTagAnnotation(int aBegin, int aEnd, String aTag)
    {
        Tag newTag = new Tag(jCas, aBegin, aEnd);
        newTag.setValue(aTag);
        jCas.addFsToIndexes(newTag);
    }

    /**
     * Creates a new Constituent annotation. Links to parent- and child-annotations are not yet
     * created here.
     *
     * @param aBegin
     *            start-index of the constituent span
     * @param aEnd
     *            end-index of the constituent span
     * @param aConstituentType
     *            the constituent type
     * @param aSyntacticFunction
     *            the syntactic function
     * @return the annotation
     */
    public Constituent createConstituentAnnotation(int aBegin, int aEnd, String aConstituentType,
            String aSyntacticFunction)
    {
        // create the necessary objects and methods
        Type constType = constituentMappingProvider.getTagType(aConstituentType);

        Constituent constAnno = (Constituent) jCas.getCas().createAnnotation(constType, aBegin, aEnd);
        constAnno.setConstituentType(aConstituentType);
		constAnno.setSyntacticFunction(aSyntacticFunction);
		return constAnno;
	}

    /**
     * Creates a new Constituent annotation. Links to parent- and child-annotations are not yet
     * created here.
     *
     * @param aBegin
     *            start-index of the constituent span
     * @param aEnd
     *            end-index of the constituent span
     * @param aPosType
     *            the constituent type
     * @return the annotation
     */
    public POS createPOSAnnotation(int aBegin, int aEnd, String aPosType)
    {
        // get mapping for DKPro-Typesystem
        Type type = posMappingProvider.getTagType(aPosType);

        // create instance of the desired type
        POS anno = (POS) jCas.getCas().createAnnotation(type, aBegin, aEnd);

        // save original (unmapped) postype in feature
        anno.setPosValue(aPosType);
        anno.setCoarseValue(anno.getClass().equals(POS.class) ? null
                : anno.getType().getShortName().intern());

        return anno;
    }

    public Dependency createDependencyAnnotation(GrammaticalRelation aDependencyType,
            Token aGovernor, Token aDependent)
    {
        return createDependencyAnnotation(jCas, aDependencyType, aGovernor, aDependent);
    }
    
    /**
     * Writes dependency annotations to the JCas
     * 
     * @param jCas
     *            a CAS.
     * @param aDependencyType
     *            the dependency type
     * @param aGovernor
     *            the governing-word
     * @param aDependent
     *            the dependent-word
     * @return the newly created dependency annotation.
     */
    public static Dependency createDependencyAnnotation(JCas jCas, GrammaticalRelation aDependencyType,
            Token aGovernor, Token aDependent)
    {
        // create the necessary objects and methods
        String dependencyTypeName = DEPPACKAGE + aDependencyType.getShortName().toUpperCase();

        Type type = jCas.getTypeSystem().getType(dependencyTypeName);
        if (type == null) {
            // Fall back to generic type. If we used a mapping provider, we'd do that too.
            type = JCasUtil.getType(jCas, Dependency.class);
            // throw new IllegalStateException("Type [" + dependencyTypeName + "] mapped to tag ["
            // + dependencyType + "] is not defined in type system");
        }

        Dependency dep = (Dependency) jCas.getCas().createFS(type);
        dep.setDependencyType(aDependencyType.toString());
        dep.setGovernor(aGovernor);
        dep.setDependent(aDependent);
        dep.setBegin(dep.getDependent().getBegin());
        dep.setEnd(dep.getDependent().getEnd());
        dep.addToIndexes();
        
        return dep;
    }

    /**
     * Creates annotation with Penn Treebank style representations of the syntax tree
     * 
     * @param aBegin
     *            start offset.
     * @param aEnd
     *            end offset.
     */
    public void createPennTreeAnnotation(int aBegin, int aEnd)
    {
        Tree t = tokenTree.getTree();

        // write Penn Treebank-style string to cas
        PennTree pTree = new PennTree(jCas, aBegin, aEnd);

        // create tree with simple labels and get penn string from it
        t = t.deepCopy(t.treeFactory(), StringLabel.factory());

        pTree.setPennTree(t.pennString());
        pTree.addToIndexes();
    }

    /**
     * Recovers annotations from a Stanford Tree-Object, which have been saved within the CoreLabel
     * of the tree.
     *<p>
     * Note:
     * Copying has to be done in batch, because we need to have ALL annotations that should be
     * recovered together when copying them. The reason is that some annotations reference each
     * other, which can cause problem if a referenced annotation has not yet been recovered.
     */
    public void recoverAnnotationsFromNodes()
    {
        // create batch-copy list for recovered annotations
        List<Annotation> annoList = new ArrayList<Annotation>();

        Iterator<Tree> treeIterator = tokenTree.getTree().iterator();
        CAS srcCAS = null;

        while (treeIterator.hasNext()) {

            Tree curTree = treeIterator.next();

            // get the collection from the label of the best-fitting node in
            // which we store UIMA annotations
            Collection<Annotation> annotations = ((CoreLabel) curTree.label())
                    .get(UIMAAnnotations.class);

            // do we have any annotations stored in the node?
            if (annotations != null && annotations.size() > 0) {

                // translate values which are now relative to the
                // node-span back to absolute value (depending on the
                // new offset of the node-span within the new CAS)

                IntPair span = tokenTree.getSpan(curTree);
                // iterate over all annotations
                for (Annotation curAnno : annotations) {
                    srcCAS = srcCAS == null ? curAnno.getCAS() : srcCAS;

                    // TODO using the SPAN as new annotation index might not
                    // be correct in all cases - if not an EXACTLY MATCHING
                    // node had been found for the saved annotation, this will
                    // be wrong. Find a way to incorporate the anno-index here
                    curAnno.setBegin(span.getSource());
                    curAnno.setEnd(span.getTarget());

                    // add anno to batch-copy list
                    annoList.add(curAnno);

                } // endfor iterate over annotations

            } // endif check for annotations in node

        } // endwhile iterate over subtrees

        /*
         * Now that we have gathered all annotations from the tree, batch-copy them to the new CAS
         */

        // create CasRecoverer (=adapted version of the CasCopier)
        CasCopier copier = new CasCopier(srcCAS, jCas.getCas());

        // now batch-copy the annos
        List<Annotation> copiedAnnos = copier.batchCopyAnnotations(annoList);

        // add copied annos to indexes
        for (Annotation cAnno : copiedAnnos) {
            jCas.addFsToIndexes(cAnno);
        }
    }
}
