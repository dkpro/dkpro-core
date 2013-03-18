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

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.util.Level.FINE;
import static org.apache.uima.util.Level.INFO;
import static org.apache.uima.util.Level.WARNING;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.StanfordAnnotator;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeWithTokens;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
import edu.stanford.nlp.process.PTBEscapingProcessor;
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
	/**
	 * Write the tag set(s) to the log when a model is loaded.
	 */
	public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
	@ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue="false")
	protected boolean printTagSet;

	/**
	 * Use this language instead of the document language to resolve the model and tag set mapping.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	/**
	 * Variant of a model the model. Used to address a specific model if here are multiple models
	 * for one language.
	 */
	public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
	@ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
	protected String variant;

	/**
	 * Location from which the model is read.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

	public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
	@ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
	protected String posMappingLocation;

	/**
	 * Sets whether to create or not to create dependency annotations. <br/>
	 * Default: {@code true}
	 */
	public static final String PARAM_WRITE_DEPENDENCY = ComponentParameters.PARAM_WRITE_DEPENDENCY;
	@ConfigurationParameter(name = PARAM_WRITE_DEPENDENCY, mandatory = true, defaultValue = "true")
	private boolean writeDependency;
	
	/**
	 * Sets whether to create or not to create collapsed dependencies. <br/>
	 * Default: {@code false}
	 */
	public static final String PARAM_CREATE_COLLAPSED_DEPENDENCIES= "createCollapsedDependencies";
	@ConfigurationParameter(name = PARAM_CREATE_COLLAPSED_DEPENDENCIES, mandatory = false, defaultValue="false")
	protected boolean createCollapsedDependencies;
	
	/**
	 * Sets whether to create or not to create constituent tags. This is
	 * required for POS-tagging and lemmatization.<br/>
	 * Default: {@code true}
	 */
	public static final String PARAM_WRITE_CONSTITUENT = ComponentParameters.PARAM_WRITE_CONSTITUENT;
	@ConfigurationParameter(name = PARAM_WRITE_CONSTITUENT, mandatory = true, defaultValue = "true")
	private boolean writeConstituent;

	/**
	 * If this parameter is set to true, each sentence is annotated with a
	 * PennTree-Annotation, containing the whole parse tree in Penn Treebank
	 * style format.<br/>
	 * Default: {@code false}
	 */
	public static final String PARAM_WRITE_PENN_TREE = ComponentParameters.PARAM_WRITE_PENN_TREE;
	@ConfigurationParameter(name = PARAM_WRITE_PENN_TREE, mandatory = true, defaultValue = "false")
	private boolean writePennTree;

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
	public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
	@ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "true")
	private boolean writePos;
	
	/**
	 * Sets whether to use or not to use already existing POS tags from another annotator for the
	 * parsing process. These should be mapped to the PTB tagset with {@link PosMapper} before.<br/>
	 * Default: {@code false}
	 */
	public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
	@ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "false")
	private boolean readPos;
	
	/**
	 * Maximum number of tokens in a sentence. Longer sentences are not parsed. This is to
	 * avoid out of memory exceptions.<br/>
	 * Default: {@code 130}
	 */
	public static final String PARAM_MAX_TOKENS = "maxTokens";
	@ConfigurationParameter(name = PARAM_MAX_TOKENS, mandatory = true, defaultValue = "130")
	private int maxTokens;
	
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

	/**
	 * Enable all traditional PTB3 token transforms (like -LRB-, -RRB-). 
	 * 
	 * @see PTBEscapingProcessor
	 */
	public static final String PARAM_PTB3_ESCAPING = "ptb3Escaping";
	@ConfigurationParameter(name = PARAM_PTB3_ESCAPING, mandatory = true, defaultValue="true")
	private boolean ptb3Escaping;
	
	public static final String PARAM_QUOTE_BEGIN = "quoteBegin";
	@ConfigurationParameter(name = PARAM_QUOTE_BEGIN, mandatory = false)
	private List<String> quoteBegin;
	
	public static final String PARAM_QUOTE_END = "quoteEnd";
	@ConfigurationParameter(name = PARAM_QUOTE_END, mandatory = false)
	private List<String> quoteEnd;

	private GrammaticalStructureFactory gsf;

	// distinction between createLemmas & paramCreateLemmas necessary
	// in order to work with mixed language document collections
	// (correct default behavior for each CAS)
	private Boolean createLemmas;

	private CasConfigurableProviderBase<LexicalizedParser> modelProvider;
	private MappingProvider posMappingProvider;
	
	private PTBEscapingProcessor<HasWord, String, Word> escaper = 
			new PTBEscapingProcessor<HasWord, String, Word>();

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		if (!writeConstituent && !writeDependency && !writePennTree) {
			getContext().getLogger().log( WARNING, "Invalid parameter configuration... will create" +
					"dependency tags.");
			writeDependency = true;
		}

		//Check if we want to create Lemmas or POS tags while Consituent tags
		//are disabled. In this case, we have to switch on constituent tagging
		if (!writeConstituent && ((createLemmas!=null&&createLemmas) || writePos)) {
			getContext().getLogger().log(WARNING, "Invalid parameter configuration. Constituent " +
					"tag creation is required for POS tagging and Lemmatization. Will create " +
					"constituent tags.");
			writeConstituent = true;
		}

		modelProvider = new CasConfigurableProviderBase<LexicalizedParser>() {
			{
				setDefault(VERSION, "20120709.0");
				setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
				setDefault(ARTIFACT_ID,
						"de.tudarmstadt.ukp.dkpro.core.stanfordnlp-model-parser-${language}-${variant}");
				
				setDefaultVariantsLocation(
						"de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/parser-default-variants.map");
				setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/" +
						"parser-${language}-${variant}.properties");
				
				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}
			
			@Override
			protected LexicalizedParser produceResource(URL aUrl) throws IOException
			{
				getContext().getLogger().log(Level.INFO,
						"Loading parser from serialized file " + aUrl + " ...");
				ObjectInputStream in = null;
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
					LexicalizedParser pd = (LexicalizedParser) in.readObject();
					TreebankLanguagePack lp = pd.getTLPParams().treebankLanguagePack();
					try {
						gsf = lp.grammaticalStructureFactory();
					}
					catch (UnsupportedOperationException e) {
						getContext().getLogger().log(WARNING, "Current model does not seem to support " +
								"dependencies.");
						gsf = null;
					}

					if (printTagSet) {
						// https://mailman.stanford.edu/pipermail/parser-user/2012-November/002117.html
						// The tagIndex does give all and only the set of POS tags used in the
						// current grammar. However, these are the split tags actually used by the
						// grammar.  If you really want the user-visible non-split tags of the
						// original treebank, then you'd need to map them all through the
						// op.treebankLanguagePack().basicCategory(). -- C. Manning
						Set<String> posTags = new HashSet<String>();
						for (String tag : pd.tagIndex) {
							posTags.add(lp.basicCategory(tag));
						}			
						// https://mailman.stanford.edu/pipermail/parser-user/2012-November/002117.html
						// For constituent categories, there isn't an index of just them. The
						// stateIndex has both constituent categories and POS tags in it, so you'd
						// need to set difference out the tags from the tagIndex, and then it's as
						// above. -- C. Manning
						Set<String> constTags = new HashSet<String>();
						for (String tag : pd.stateIndex) {
							String t = lp.basicCategory(tag);
							// https://mailman.stanford.edu/pipermail/parser-user/2012-December/002156.html
							// The parser algorithm used is a binary parser, so what we do is 
							// binarize trees by turning A -> B, C, D into A -> B, @A, @A -> C, D.
							// (That's roughly how it goes, although the exact details are somewhat
							// different.)  When parsing, we parse to a binarized tree and then
							// unbinarize it before returning.  That's the origin of the @ classes.
							// -- J. Bauer
							if (!t.startsWith("@")) {
								constTags.add(t);
							}
						}
						constTags.removeAll(posTags);
						
						printTags("tagger", posTags);
						printTags("parser", constTags);
					}

					pd.setOptionFlags("-maxLength", String.valueOf(maxTokens));
					return pd;
				}
				catch (ClassNotFoundException e) {
					throw new IOException(e);
				}
				finally {
					closeQuietly(in);
					closeQuietly(is);
				}
			}
		};
		
		posMappingProvider = new MappingProvider();
		posMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
				"core/api/lexmorph/tagset/${language}-${tagger.tagset}-tagger.map");
		posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
		posMappingProvider.setDefault("tagger.tagset", "default");
		posMappingProvider.setOverride(MappingProvider.LOCATION, posMappingLocation);
		posMappingProvider.setOverride(MappingProvider.LANGUAGE, language);
		posMappingProvider.addImport("tagger.tagset", modelProvider);
	}

	private void printTags(String aType, Collection<String> aTags)
	{
		List<String> tags = new ArrayList<String>(aTags);
		Collections.sort(tags);
		
		StringBuilder sb = new StringBuilder();
		sb.append("Model of " + aType + " contains [").append(tags.size()).append("] tags: ");
		
		for (String tag : tags) {
			sb.append(tag);
			sb.append(" ");
		}
		getContext().getLogger().log(INFO, sb.toString());
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
		modelProvider.configure(aJCas.getCas());
		posMappingProvider.configure(aJCas.getCas());
		
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
			List<HasWord> tokenizedSentence = new ArrayList<HasWord>();
			List<Token> tokens = new ArrayList<Token>();

			// Split sentence to tokens for annotating indexes
			for (Token token : JCasUtil.selectCovered(Token.class, currAnnotationToParse)) {
				tokenizedSentence.add(tokenToWord(token));
				tokens.add(token);
			}

			Tree parseTree;
			try {
				getContext().getLogger().log(FINE, tokenizedSentence.toString());
				LexicalizedParser parser = modelProvider.getResource();
				if (tokenizedSentence.size() <= maxTokens) {
					if (ptb3Escaping) {
						// Apply escaper to the whole sentence, not to each token individually. The
						// escaper takes context into account, e.g. when transforming regular double
						// quotes into PTB opening and closing quotes (`` and '').
						tokenizedSentence = escaper.apply(tokenizedSentence);
						for (HasWord w : tokenizedSentence) {
							if (quoteBegin != null && quoteBegin.contains(w.word())) {
								w.setWord("``");
							}
							else if (quoteEnd != null && quoteEnd.contains(w.word())) {
								w.setWord("\'\'");
							}
						}
					}
					
					// Get parse
					LexicalizedParserQuery query = parser.parserQuery();
					query.parse(tokenizedSentence);
					parseTree = query.getBestParse();
				} 
				else{
					continue;
				}
				
			}
			catch (Exception e) {
				throw new AnalysisEngineProcessException(e);
			}
			
			// Create new StanfordAnnotator object
			StanfordAnnotator sfAnnotator = null;
			try {
				sfAnnotator = new StanfordAnnotator(new TreeWithTokens(parseTree, tokens));
				sfAnnotator.setPosMappingProvider(posMappingProvider);
			}
			catch (CASException e) {
				throw new AnalysisEngineProcessException(e);
			}

			// Create Penn bracketed structure annotations
			if (writePennTree) {
				sfAnnotator.createPennTreeAnnotation(currAnnotationToParse.getBegin(),
						currAnnotationToParse.getEnd());
			}
			
			// Create dependency annotations
			if (writeDependency && gsf != null) {
				doCreateDependencyTags(sfAnnotator, currAnnotationToParse, parseTree, tokens);
			}
			
			// Create constituent annotations
			if (writeConstituent) {
				sfAnnotator.createConstituentAnnotationFromTree(writePos, createLemmas);
			}
		}
	}

	protected void doCreateDependencyTags(StanfordAnnotator sfAnnotator,
			Annotation currAnnotationToParse, Tree parseTree, List<Token> tokens)
	{
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
		Collection<TypedDependency> dependencies = null;
		if (createCollapsedDependencies) {
			dependencies = gs.typedDependenciesCollapsed();
		}
		else {
			dependencies = gs.typedDependencies();
		}
		
		for (TypedDependency currTypedDep : dependencies) {
			int govIndex = currTypedDep.gov().index();
			int depIndex = currTypedDep.dep().index();
			if (govIndex != 0) {
				// Stanford CoreNLP produces a dependency relation between a verb and ROOT-0 which
				// is not token at all!
				Token govToken = tokens.get(govIndex - 1);
				Token depToken = tokens.get(depIndex - 1);

				sfAnnotator.createDependencyAnnotation(currAnnotationToParse.getBegin(),
						currAnnotationToParse.getEnd(), currTypedDep.reln(), govToken,
						depToken);
			}
		}
	}

	protected Word tokenToWord(Token aToken)
	{
		if (readPos && aToken.getPos() != null) {
			String posValue = aToken.getPos().getPosValue();
			TaggedWord tw = new TaggedWord(aToken.getCoveredText(), posValue);
			tw.setBeginPosition(aToken.getBegin());
			tw.setEndPosition(aToken.getEnd());
			return tw;
		}
		else {
			return new Word(aToken.getCoveredText(), aToken.getBegin(), aToken.getEnd());
		}
	}
}
