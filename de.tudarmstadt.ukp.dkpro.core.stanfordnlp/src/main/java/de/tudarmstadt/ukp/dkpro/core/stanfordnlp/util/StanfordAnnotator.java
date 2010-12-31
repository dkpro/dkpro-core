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

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

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
 *
 */
public class StanfordAnnotator
{

	private static final String CONPACKAGE = Constituent.class.getPackage().getName()+".";
	private static final String POSPACKAGE = POS.class.getPackage().getName()+".";
	private static final String DEPPACKAGE = Dependency.class.getPackage().getName()+".";

	/**
	 * The separator that is used by Tsurgeon-Operations to separate additional
	 * tags from node-labels.
	 */
	private static final String TAG_SEPARATOR = "#";

	private TreeWithTokens tokenTree = null;

	public TreeWithTokens getTokenTree()
	{
		return tokenTree;
	}

	public void setTokenTree(TreeWithTokens aTokenTree)
	{
		tokenTree = aTokenTree;
	}

	public JCas getaJCas()
	{
		return aJCas;
	}

	public void setaJCas(JCas aAJCas)
	{
		aJCas = aAJCas;
	}

	private JCas aJCas = null;

	/**
	 * @param tokens
	 *            the documentText of the CAS as <code>Token</code> annotations
	 * @throws CASException
	 */
	public StanfordAnnotator(TreeWithTokens tokenTree)
		throws CASException
	{
		setTokenTree(tokenTree);
		setaJCas(tokenTree.getTokens().get(0).getCAS().getJCas());
	}

	/**
	 * Creates linked constituent annotations, POS annotations and
	 * lemma-annotations.<br/>
	 * Note: The annotations are directly written to the indexes of the CAS.
	 *
	 * @param aJCas
	 *            the JCas for which to create annotations
	 * @param node
	 *            the source tree (with CoreMap nodes)
	 */
	public void createConstituentAnnotationFromTree(boolean createPosTags,
			boolean createLemmas)
	{
		createConstituentAnnotationFromTree(tokenTree.getTree(), null,
				createPosTags, createLemmas);
	}

	/**
	 * Creates linked constituent annotations + POS annotations
	 *
	 * @param aJCas
	 *            the JCas for which to create annotations
	 * @param node
	 *            the source tree
	 * @param parentFS
	 * @param createPosTags
	 *            sets whether to create or not to create POS tags
	 * @param createLemmas
	 *            sets whether to create or not to create Lemmas
	 * @return the child-structure (needed for recursive call only)
	 */
	private Annotation createConstituentAnnotationFromTree(Tree node,
			Annotation parentFS, boolean createPosTags, boolean createLemmas)
	{

		// New FeatureStructure for current node
		Annotation nodeFS = null;

		// calculate span for the current subtree
		IntPair span = tokenTree.getSpan(node);

		// Check if the node has been marked by a TSurgeon operation.
		// If so, add a tag-annotation on the constituent
		String nodeLabelValue = node.value();
		String tag = "";
		if (nodeLabelValue.contains(TAG_SEPARATOR)
				&& !nodeLabelValue.equals(TAG_SEPARATOR)) {
			int separatorIndex = nodeLabelValue.indexOf(TAG_SEPARATOR);
			tag = nodeLabelValue.substring(0, separatorIndex);
			nodeLabelValue = nodeLabelValue.substring(separatorIndex + 1,
					nodeLabelValue.length());
			createTagAnnotation(span.getSource(), span.getTarget(), tag);
		}

		// Check if node is a constituent node on sentence or phrase-level
		if (node.isPhrasal()) {

			// add annotation to annotation tree
			nodeFS = createConstituentAnnotation(span.getSource(), span
					.getTarget(), nodeLabelValue);
			// link to parent
			if (parentFS != null) {
				((Constituent) nodeFS).setParent(parentFS);
			}

			// Do we have any children?
			List<Annotation> childAnnotations = new ArrayList<Annotation>();
			for (Tree child : node.getChildrenAsList()) {
				Annotation childAnnotation = createConstituentAnnotationFromTree(
						child, nodeFS, createPosTags, createLemmas);
				if (childAnnotation != null) {
					childAnnotations.add(childAnnotation);
				}
			}

			// Now that we know how many children we have, link annotation of
			// current node with its children
			FSArray children = new FSArray(aJCas, childAnnotations.size());
			int curChildNum = 0;
			for (FeatureStructure child : childAnnotations) {
				children.set(curChildNum, child);
				curChildNum++;
			}
			((Constituent) nodeFS).setChildren(children);

			// write annotation for current node to index
			aJCas.addFsToIndexes(nodeFS);
		}
		// If the node is a word-level constituent node (== POS):
		// create parent link on token and (if not turned off) create POS tag
		else if (node.isPreTerminal()) {

			// create POS-annotation (annotation over the token)
			POS pos = createPOSAnnotation(span.getSource(), span.getTarget(),
					nodeLabelValue);

			// only add POS to index if we want POS-tagging
			if (createPosTags) {
				aJCas.addFsToIndexes(pos);
			}

			// in any case: get the token that is covered by the POS
			// TODO how about multi word prepositions etc. (e.g. "such as")
			List<Token> coveredToken = JCasUtil.selectCovered(aJCas,
					Token.class, pos);
			// the POS should only cover one token
			assert coveredToken.size() == 1;
			nodeFS = coveredToken.get(0);

			// Lemmatization
			if (createLemmas) {
				WordLemmaTag wlTag = Morphology.lemmatizeStatic(new WordTag(
						nodeFS.getCoveredText(), pos.getPosValue()));
				Lemma lemma = new Lemma(aJCas, span.getSource(), span
						.getTarget());
				lemma.setValue(wlTag.lemma());
				aJCas.addFsToIndexes(lemma);
			}

			// link token to its parent constituent
			if (parentFS != null) {
				((Token) nodeFS).setParent(parentFS);
			}
		}
		return nodeFS;
	}

	/**
	 * Creates a tag-annotation over a constituent
	 *
	 * @param aJCas
	 *            the CAS
	 * @param begin
	 *            start-index of the constituent span
	 * @param end
	 *            end-index of the constituent span
	 * @param tag
	 *            the tag value
	 * @return the annotation
	 */
	public void createTagAnnotation(int begin, int end, String tag)
	{
		Tag newTag = new Tag(aJCas, begin, end);
		newTag.setValue(tag);
		aJCas.addFsToIndexes(newTag);
	}

	/**
	 * Creates a new Constituent annotation. Links to parent- and
	 * child-annotations are not yet created here.
	 *
	 * @param aJCas
	 *            the CAS
	 * @param begin
	 *            start-index of the constituent span
	 * @param end
	 *            end-index of the constituent span
	 * @param constituentType
	 *            the constituent type
	 * @return the annotation
	 */
	public Constituent createConstituentAnnotation(int begin, int end,
			String constituentType)
	{
		// Workaround for Win32 Systems
		if (constituentType.equalsIgnoreCase("PRN")) {
			constituentType = "PRN0";
		}

		// create the necessary objects and methods
		String constituentTypeName = CONPACKAGE + constituentType;

		Type type = aJCas.getTypeSystem().getType(constituentTypeName);

		//if type is unknown, map to X-type
		if(type==null){
			type = aJCas.getTypeSystem().getType(CONPACKAGE+"X");
		}

		Constituent constAnno = (Constituent) aJCas.getCas().createAnnotation(
				type, begin, end);
		constAnno.setStringValue(type.getFeatureByBaseName("ConstituentType"),
				constituentType);
		return constAnno;
	}

	/**
	 * Creates a new Constituent annotation. Links to parent- and
	 * child-annotations are not yet crated here.
	 *
	 * @param aJCas
	 *            the CAS
	 * @param begin
	 *            start-index of the constituent span
	 * @param end
	 *            end-index of the constituent span
	 * @param posType
	 *            the constituent type
	 * @return the annotation
	 */
	public POS createPOSAnnotation(int begin, int end, String posType)
	{
		// get mapping for DKPro-Typesystem
		String mappedPos = StanfordParserPosMapping.getTagClass(aJCas
				.getDocumentLanguage(), posType);
		String constituentTypeName = POSPACKAGE + mappedPos;

		// create instance of the desired type
		Type type = aJCas.getTypeSystem().getType(constituentTypeName);
		POS constAnno = (POS) aJCas.getCas().createAnnotation(type, begin, end);

		// save original (unmapped) postype in feature
		constAnno
				.setStringValue(type.getFeatureByBaseName("PosValue"), posType);
		return constAnno;
	}

	/**
	 * Writes dependency annotations to the JCas
	 *
	 * @param aJCas
	 *            the CAS
	 * @param begin
	 *            start-index of the constituent span
	 * @param end
	 *            end-index of the constituent span
	 * @param aGramRel
	 *            the dependency-type
	 * @param governor
	 *            the governing-word
	 * @param dependent
	 *            the dependent-word
	 */
	public void createDependencyAnnotation(int begin, int end,
			GrammaticalRelation aGramRel, Token governor, Token dependent)
	{
		String dependencyType = aGramRel.getShortName();

		if (dependencyType.equalsIgnoreCase("AUX")) {
			dependencyType = "AUX0";
		}

		// create the necessary objects and methods
		String dependencyTypeName = DEPPACKAGE + dependencyType.toUpperCase();

		Type type = aJCas.getTypeSystem().getType(dependencyTypeName);
		AnnotationFS anno = aJCas.getCas().createAnnotation(type, begin, end);
		anno.setStringValue(type.getFeatureByBaseName("DependencyType"),
				dependencyType);
		anno.setFeatureValue(type.getFeatureByBaseName("Governor"), governor);
		anno.setFeatureValue(type.getFeatureByBaseName("Dependent"), dependent);

		aJCas.addFsToIndexes(anno);

	}

	/**
	 * Creates annotation with Penn Treebank style representations of the syntax
	 * tree
	 *
	 * @param aJCas
	 * @param begin
	 * @param end
	 */
	public void createPennTreeAnnotation(int begin, int end)
	{
		Tree t = tokenTree.getTree();

		// write Penn Treebank-style string to cas
		PennTree pTree = new PennTree(aJCas, begin, end);

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
		CasCopier copier = new CasCopier(srcCAS, aJCas.getCas());

		// now batch-copy the annos
		List<Annotation> copiedAnnos = copier.batchCopyAnnotations(annoList);

		//add copied annos to indexes
		for(Annotation cAnno: copiedAnnos){
			aJCas.addFsToIndexes(cAnno);
		}

	}

}
