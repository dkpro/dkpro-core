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
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.resolveLocation;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.util.Level.FINE;
import static org.apache.uima.util.Level.WARNING;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.StanfordAnnotator;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeWithTokens;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.ParserData;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * @author Oliver Ferschke
 * @author Niklas Jakob
 *
 */
public class StanfordParser
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_MODEL = "Model";
	@ConfigurationParameter(name = PARAM_MODEL, mandatory = true)
	private String classifierFileName;

	/**
	 * Sets whether to create or not to create dependency annotations. <br/>
	 * Default: {@code true}
	 */
	public static final String PARAM_CREATE_DEPENDENCY_TAGS = "createDependencyTags";
	@ConfigurationParameter(name = PARAM_CREATE_DEPENDENCY_TAGS, mandatory = true, defaultValue = "true")
	private boolean createDependencyTags;

	/**
	 * Sets whether to create or not to create constituent tags. This is
	 * required for POS-tagging and lemmatization.<br/>
	 * Default: {@code true}
	 */
	public static final String PARAM_CREATE_CONSTITUENT_TAGS = "createConstituentTags";
	@ConfigurationParameter(name = PARAM_CREATE_CONSTITUENT_TAGS, mandatory = true, defaultValue = "true")
	private boolean createConstituentTags;

//	public static final String PARAM_CREATE_DEPENDENCY_ANNOTATION_ON_TOKEN = "createDependencyAnnotationOnToken";
//	@ConfigurationParameter(name = PARAM_CREATE_DEPENDENCY_ANNOTATION_ON_TOKEN, mandatory = true, defaultValue = "true")
//	private boolean createDependencyAnnotationOnToken;

	/**
	 * Defines the TreebankLanguagePack that the parser should use.<br/>
	 * The right setting depends on the model you are using. It usually does not have to be changed.<br/>
	 * Default: {@code edu.stanford.nlp.trees.PennTreebankLanguagePack}
	 */
	public static final String PARAM_LANGUAGE_PACK = "TreebankLanguagePack";
	@ConfigurationParameter(name = PARAM_LANGUAGE_PACK, mandatory = true, defaultValue = "edu.stanford.nlp.trees.PennTreebankLanguagePack")
	private String tlpName;

	/**
	 * If this paramter is set to true, each sentence is annotated with a
	 * PennTree-Annotation, containing the whole parse tree in Prenn Treebank
	 * style format.<br/>
	 * Default: {@code false}
	 */
	public static final String PARAM_CREATE_PENN_TREE_STRING = "createPennTreeString";
	@ConfigurationParameter(name = PARAM_CREATE_PENN_TREE_STRING, mandatory = true, defaultValue = "false")
	private boolean createPennTreeString;

	/**
	 * This parameter can be used to override the standard behavior which
	 * uses the <i>Sentence</i> annotation as the basic unit for parsing.<br/>
	 * If the parameter is set with the name of an annotation type <i>x</i>, the parser
	 * will no longer parse <i>Sentence</i>-annotations, but <i>x</i>-Annotations.<br/>
	 * Default: {@code null}
	 */
	public static final String PARAM_ANNOTATIONTYPE_TO_PARSE = "annotationTypeToParse";
	@ConfigurationParameter(name = PARAM_ANNOTATIONTYPE_TO_PARSE, mandatory = false)
	private String annotationTypeToParse;

	/**
	 * Sets whether to create or not to create POS tags. The creation of
	 * constituent tags must be turned on for this to work.<br/>
	 * Default: {@code true}
	 */
	public static final String PARAM_CREATE_POS_TAGS = "createPosTags";
	@ConfigurationParameter(name = PARAM_CREATE_POS_TAGS, mandatory = true, defaultValue = "true")
	private boolean createPosTags;

	/**
	 * Sets whether to create or not to create Lemma tags. The creation of
	 * constituent tags must be turned on for this to work.<br/>
	 * This only works for ENGLISH.<br/>
	 * Default:<br/>
	 * <ul>
	 * <li>true, if document text is English</li>
	 * <li>false, if document text is not English</li>
	 * </ul>
	 * <br/>
	 *
	 * <strong>Info:</strong><br>
	 * The Stanford Morphology-class computes the base form of English words, by
	 * removing just inflections (not derivational morphology). That is, it only
	 * does noun plurals, pronoun case, and verb endings, and not things like
	 * comparative adjectives or derived nominals. It is based on a finite-state
	 * transducer implemented by John Carroll et al., written in flex and
	 * publicly available. See:
	 * http://www.informatics.susx.ac.uk/research/nlp/carroll/morph.html
	 */
	public static final String PARAM_CREATE_LEMMAS = "createLemmas";
	@ConfigurationParameter(name = PARAM_CREATE_LEMMAS, mandatory = false)
	private Boolean paramCreateLemmas;

	private LexicalizedParser lexicalizedParser;
	private GrammaticalStructureFactory gsf;

	// distinction between createLemmas & paramCreateLemmas necessary
	// in order to work with mixed language document collections
	// (correct default behavior for each CAS)
	private Boolean createLemmas;

	private boolean warnedAboutDependencies = false;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		if (!createConstituentTags && !createDependencyTags && !createPennTreeString) {
			getContext().getLogger().log( WARNING, "Invalid parameter configuration... will create" +
					"dependency tags.");
			createDependencyTags = true;
		}

//		if (createDependencyAnnotationOnToken) {
//			createDependencyTags = true;
//		}

		//Check if we want to create Lemmas or POS tags while Consituent tags
		//are disabled. In this case, we have to switch on constituent tagging
		if (!createConstituentTags && ((createLemmas!=null&&createLemmas) || createPosTags)) {
			getContext().getLogger().log(WARNING, "Invalid parameter configuration. Constituent " +
					"tag creation is required for POS tagging and Lemmatization. Will create " +
					"constituent tags.");
			createConstituentTags = true;
		}

		warnedAboutDependencies = false;
	}

	protected LexicalizedParser getParser()
		throws IOException
	{
		if (lexicalizedParser == null) {
			URL url = resolveLocation(classifierFileName, this, getContext());
			lexicalizedParser = new LexicalizedParser(getParserDataFromSerializedFile(url));
			lexicalizedParser.setOptionFlags("-maxLength", String.valueOf(160));

			try {
				TreebankLanguagePack tlp = (TreebankLanguagePack) Class.forName(tlpName).newInstance();
				gsf = tlp.grammaticalStructureFactory();
			}
			catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
			catch (InstantiationException e) {
				throw new IOException(e);
			}
			catch (IllegalAccessException e) {
				throw new IOException(e);
			}
		}
		return lexicalizedParser;
	}

	/**
	 * Processes the given text using the StanfordParser.
	 *
	 * @param aJCas
	 *            the {@link JCas} to process
	 * @throws AnalysisEngineProcessException
	 * @see org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
	 */
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		/*
		 * In order to work with mixed language document collections, default
		 * behavior of lemmatization has to be set anew for each CAS.
		 */
		// If lemmatization is explicitly turned on, but document is not
		// English, give a warning, but still turn it on.
		if (paramCreateLemmas != null && paramCreateLemmas
				&& !aJCas.getDocumentLanguage().equals("en")) {
			getContext()
					.getLogger()
					.log(
							WARNING,
							"Lemmatization is turned on, but does not work with the document language of the current CAS.");

			createLemmas = paramCreateLemmas;
		}
		// If lemmatization was not set, turn it on for English documents
		// and off for non-English documents
		else if (paramCreateLemmas == null) {
			createLemmas = aJCas.getDocumentLanguage().equals("en") ? true
					: false;
		}
		else {
			createLemmas = paramCreateLemmas;
		}

		Type typeToParse;
		if(annotationTypeToParse != null){
			typeToParse = aJCas.getCas().getTypeSystem().getType(annotationTypeToParse);
		}else{
			typeToParse = JCasUtil.getType(aJCas, Sentence.class);
		}
		FSIterator<Annotation> typeToParseIterator = aJCas.getAnnotationIndex(typeToParse).iterator();

		// Iterator each Sentence or whichever construct to parse

		while(typeToParseIterator.hasNext()){
			Annotation currAnnotationToParse = typeToParseIterator.next();
			List<Word> tokenizedSentence = new ArrayList<Word>();
			List<Token> tokens = new ArrayList<Token>();

			/*
			 * Split sentence to tokens for annotating indexes
			 */
			for (Token token : JCasUtil.subiterate(aJCas, Token.class, currAnnotationToParse,
					false, true)) {
				tokenizedSentence.add(getPennTokenText(token));
				tokens.add(token);
			}

			// Get parsetree
			Tree parseTree;
			try {
				getContext().getLogger().log(FINE, tokenizedSentence.toString());
				LexicalizedParser parser = getParser();
				parser.parse(tokenizedSentence);
				parseTree = parser.getBestParse();
			}
			catch (Exception e) {
				throw new AnalysisEngineProcessException(e);
			}

			// Create new StanfordAnnotator object
			StanfordAnnotator sfAnnotator = null;
			try {
				sfAnnotator = new StanfordAnnotator(new TreeWithTokens(parseTree, tokens));
			}
			catch (CASException e) {
				throw new AnalysisEngineProcessException(e);
			}

			if (createPennTreeString) {
				sfAnnotator.createPennTreeAnnotation(currAnnotationToParse.getBegin(),
						currAnnotationToParse.getEnd());
			}

			doCreateDependencyTags(sfAnnotator, currAnnotationToParse, parseTree, tokens);

			if (createConstituentTags) {
				// create constituent annotations from parse tree
				sfAnnotator.createConstituentAnnotationFromTree(createPosTags, createLemmas);
			}
		}
	}

	protected void doCreateDependencyTags(StanfordAnnotator sfAnnotator,
			Annotation currAnnotationToParse, Tree parseTree, List<Token> tokens)
	{
		if (!createDependencyTags) {
			return;
		}

		GrammaticalStructure gs;
		try {
			gs = gsf.newGrammaticalStructure(parseTree);
		}
		catch (Exception e) {
			if (!warnedAboutDependencies) {
				getContext().getLogger().log(WARNING, "Current model does not seem to support " +
						"dependencies.");
				warnedAboutDependencies = true;
			}
			return;
		}

		for (TypedDependency currTypedDep : gs.typedDependencies()) {
			int govIndex = currTypedDep.gov().index();
			int depIndex = currTypedDep.dep().index();
			if (govIndex != 0) {
				// Stanford CoreNLP produces a depencency relation between a verb and ROOT-0 which
				// is not token at all!
				Token govToken = tokens.get(govIndex - 1);
				Token depToken = tokens.get(depIndex - 1);

				sfAnnotator.createDependencyAnnotation(currAnnotationToParse.getBegin(),
						currAnnotationToParse.getEnd(), currTypedDep.reln(), govToken,
						depToken);
			}
		}
	}

	protected Word getPennTokenText(Token aToken)
	{
		String tokenString = aToken.getCoveredText();
		if (tokenString.equals("(") || tokenString.equals("[")) {
			tokenString = "-LRB-";
		}
		else if (tokenString.equals(")") || tokenString.equals("]")) {
			tokenString = "-RRB-";
		}

		return new Word(tokenString, aToken.getBegin(), aToken.getEnd());
	}

	/**
	 * Load the parser from the given location within the classpath.
	 *
	 * @param aUrl
	 *            URL of the parser file.
	 */
	private ParserData getParserDataFromSerializedFile(URL aUrl)
		throws IOException
	{
		getContext().getLogger().log(Level.INFO,
				"Loading parser from serialized file " + aUrl + " ...");
		ObjectInputStream in;
		InputStream is = null;
		try {
			is = aUrl.openStream();

			if (aUrl.toString().endsWith(".gz")) {
				// it's faster to do the buffering _outside_ the gzipping as
				// here
				in = new ObjectInputStream(new BufferedInputStream(
						new GZIPInputStream(is)));
			}
			else {
				in = new ObjectInputStream(new BufferedInputStream(is));
			}
			ParserData pd = (ParserData) in.readObject();
			// Numberer.setNumberers(pd.numbs); // will happen later in
			// makeParsers()
			in.close();
			return pd;
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		finally {
			closeQuietly(is);
		}
	}

}
