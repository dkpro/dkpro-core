/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.TagsetMappingFactory;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.Tag;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.ling.WordLemmaTag;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.IntPair;

/**
 * A StanfordAnnotator-object creates most of the annotations for the
 * StanfordParser component.<br/>
 * The code has been moved away from the Parser component because it is also
 * used by other components (e.g. Transformations)
 *
 * @author Oliver Ferschke
 * @author Richard Eckart de Castilho
 */
public class StanfordAnnotator
{
	private static final String CONPACKAGE = Constituent.class.getPackage().getName()+".";
	private static final String DEPPACKAGE = Dependency.class.getPackage().getName()+".";

	/**
	 * The separator that is used by Tsurgeon-Operations to separate additional
	 * tags from node-labels.
	 */
	private static final String TAG_SEPARATOR = "#";

	private TreeWithTokens tokenTree = null;
	private JCas jCas = null;
	private Map<String, String> mapping = null;

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
		mapping = TagsetMappingFactory.getMapping(TagsetMappingFactory.TAGGER,
				jCas.getDocumentLanguage(), O.class.getName());
	}

	/**
	 * @param tokens
	 *            the documentText of the CAS as <code>Token</code> annotations
	 * @throws CASException
	 */
	public StanfordAnnotator(TreeWithTokens aTokenTree)
		throws CASException
	{
		setTokenTree(aTokenTree);
		setJCas(aTokenTree.getTokens().get(0).getCAS().getJCas());
	}

	/**
	 * Creates linked constituent annotations, POS annotations and lemma-annotations.<br/>
	 * Note: The annotations are directly written to the indexes of the CAS.
	 */
	public void createConstituentAnnotationFromTree(boolean aCreatePos, boolean aCreateLemma)
	{
		createConstituentAnnotationFromTree(tokenTree.getTree(), null, aCreatePos, aCreateLemma);
	}

	/**
	 * Creates linked constituent annotations + POS annotations
	 *
	 * @param aNode
	 *            the source tree
	 * @param aParentFS
	 * @param aCreatePos
	 *            sets whether to create or not to create POS tags
	 * @param aCreateLemmas
	 *            sets whether to create or not to create Lemmas
	 * @return the child-structure (needed for recursive call only)
	 */
	private Annotation createConstituentAnnotationFromTree(Tree aNode, Annotation aParentFS,
			boolean aCreatePos, boolean aCreateLemmas)
	{
		// calculate span for the current subtree
		IntPair span = tokenTree.getSpan(aNode);

		// Check if the node has been marked by a TSurgeon operation.
		// If so, add a tag-annotation on the constituent
		String nodeLabelValue = aNode.value();
		String tag = "";
		if (nodeLabelValue.contains(TAG_SEPARATOR) && !nodeLabelValue.equals(TAG_SEPARATOR)) {
			int separatorIndex = nodeLabelValue.indexOf(TAG_SEPARATOR);
			tag = nodeLabelValue.substring(0, separatorIndex);
			nodeLabelValue = nodeLabelValue.substring(separatorIndex + 1, nodeLabelValue.length());
			createTagAnnotation(span.getSource(), span.getTarget(), tag);
		}

		// Check if node is a constituent node on sentence or phrase-level
		if (aNode.isPhrasal()) {

			// add annotation to annotation tree
			Constituent constituent = createConstituentAnnotation(span.getSource(), span.getTarget(), nodeLabelValue);
			// link to parent
			if (aParentFS != null) {
				constituent.setParent(aParentFS);
			}

			// Do we have any children?
			List<Annotation> childAnnotations = new ArrayList<Annotation>();
			for (Tree child : aNode.getChildrenAsList()) {
				Annotation childAnnotation = createConstituentAnnotationFromTree(child, constituent,
						aCreatePos, aCreateLemmas);
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

			// Lemmatization
			if (aCreateLemmas) {
				WordLemmaTag wlTag = Morphology.lemmatizeStatic(new WordTag(
						token.getCoveredText(), pos.getPosValue()));
				Lemma lemma = new Lemma(jCas, span.getSource(), span.getTarget());
				lemma.setValue(wlTag.lemma());
				token.setLemma(lemma);
				jCas.addFsToIndexes(lemma);
			}

			// link token to its parent constituent
			if (aParentFS != null) {
				((Token) token).setParent(aParentFS);
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
	 * @param jCas
	 *            the CAS
	 * @param aBegin
	 *            start-index of the constituent span
	 * @param aEnd
	 *            end-index of the constituent span
	 * @param aTag
	 *            the tag value
	 * @return the annotation
	 */
	public void createTagAnnotation(int aBegin, int aEnd, String aTag)
	{
		Tag newTag = new Tag(jCas, aBegin, aEnd);
		newTag.setValue(aTag);
		jCas.addFsToIndexes(newTag);
	}

	/**
	 * Creates a new Constituent annotation. Links to parent- and
	 * child-annotations are not yet created here.
	 *
	 * @param jCas
	 *            the CAS
	 * @param aBegin
	 *            start-index of the constituent span
	 * @param aEnd
	 *            end-index of the constituent span
	 * @param aConstituentType
	 *            the constituent type
	 * @return the annotation
	 */
	public Constituent createConstituentAnnotation(int aBegin, int aEnd, String aConstituentType)
	{
		// Workaround for Win32 Systems
		if (aConstituentType.equalsIgnoreCase("PRN")) {
			aConstituentType = "PRN0";
		}

		// create the necessary objects and methods
		String constituentTypeName = CONPACKAGE + aConstituentType;

		Type type = jCas.getTypeSystem().getType(constituentTypeName);

		//if type is unknown, map to X-type
		if (type==null){
			type = jCas.getTypeSystem().getType(CONPACKAGE+"X");
		}

		Constituent constAnno = (Constituent) jCas.getCas().createAnnotation(type, aBegin, aEnd);
		constAnno.setConstituentType(aConstituentType);
		return constAnno;
	}

	/**
	 * Creates a new Constituent annotation. Links to parent- and
	 * child-annotations are not yet crated here.
	 *
	 * @param jCas
	 *            the CAS
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
		Type type = TagsetMappingFactory.getTagType(mapping, aPosType, jCas.getTypeSystem());
		
		// create instance of the desired type
		POS constAnno = (POS) jCas.getCas().createAnnotation(type, aBegin, aEnd);

		// save original (unmapped) postype in feature
		constAnno.setPosValue(aPosType);
		return constAnno;
	}

	/**
	 * Writes dependency annotations to the JCas
	 *
	 * @param jCas
	 *            the CAS
	 * @param aBegin
	 *            start-index of the constituent span
	 * @param aEnd
	 *            end-index of the constituent span
	 * @param aGramRel
	 *            the dependency-type
	 * @param aGovernor
	 *            the governing-word
	 * @param aDependent
	 *            the dependent-word
	 */
	public void createDependencyAnnotation(int aBegin, int aEnd,
			GrammaticalRelation aGramRel, Token aGovernor, Token aDependent)
	{
		String dependencyType = aGramRel.getShortName();

		if (dependencyType.equalsIgnoreCase("AUX")) {
			dependencyType = "AUX0";
		}

		// create the necessary objects and methods
		String dependencyTypeName = DEPPACKAGE + dependencyType.toUpperCase();

		Type type = jCas.getTypeSystem().getType(dependencyTypeName);
		AnnotationFS anno = jCas.getCas().createAnnotation(type, aBegin, aEnd);
		anno.setStringValue(type.getFeatureByBaseName("DependencyType"), dependencyType);
		anno.setFeatureValue(type.getFeatureByBaseName("Governor"), aGovernor);
		anno.setFeatureValue(type.getFeatureByBaseName("Dependent"), aDependent);

		jCas.addFsToIndexes(anno);
	}

	/**
	 * Creates annotation with Penn Treebank style representations of the syntax
	 * tree
	 *
	 * @param aBegin
	 * @param aEnd
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
	 * Recovers annotations from a Stanford Tree-Object, which have been saved
	 * within the CoreLabel of the tree.<br/>
	 *
	 * Note:<br/>
	 * Copying has to be done in batch, because we need to have
	 * ALL annotations that should be recovered together when copying them.
	 * The reason is that some annotations reference each other, which can
	 * cause problem if a referenced annotation has not yet been recovered.
	 *
	 */
	public void recoverAnnotationsFromNodes()
	{
		//create batch-copy list for recovered annotations
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
					srcCAS=srcCAS==null?curAnno.getCAS():srcCAS;

					//TODO using the SPAN as new annotation index might not
					// be correct in all cases - if not an EXACTLY MATCHING
					// node had been found for the saved annotation, this will
					// be wrong. Find a way to incorporate the anno-index here
					curAnno.setBegin(span.getSource());
					curAnno.setEnd(span.getTarget());

					//add anno to batch-copy list
					annoList.add(curAnno);

				} // endfor iterate over annotations

			} // endif check for annotations in node

		} // endwhile iterate over subtrees

		/*
		 * Now that we have gathered all annotations from the tree,
		 * batch-copy them to the new CAS
		 */

		// create CasRecoverer (=adapted version of the CasCopier)
		CasCopier copier = new CasCopier(srcCAS, jCas.getCas());

		// now batch-copy the annos
		List<Annotation> copiedAnnos = copier.batchCopyAnnotations(annoList);

		//add copied annos to indexes
		for (Annotation cAnno: copiedAnnos){
			jCas.addFsToIndexes(cAnno);
		}
	}
}
