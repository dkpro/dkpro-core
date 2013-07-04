/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.util.Level.INFO;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.util.Span;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.opennlp.internal.OpenNlpParserTagsetDescriptionProvider;
import de.tudarmstadt.ukp.dkpro.core.opennlp.internal.OpenNlpTagsetDescriptionProvider;

/**
 * Parser annotator using OpenNLP. Requires {@link Sentence}s to be annotated before.
 *
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
	    inputs = {
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
		outputs = {
		    "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent",
		    "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree"})
public class OpenNlpParser
	extends JCasAnnotator_ImplBase
{
	private static final String CONPACKAGE = Constituent.class.getPackage().getName()+".";

	/**
	 * Use this language instead of the document language to resolve the model.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	/**
	 * Override the default variant used to locate the model.
	 */
	public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
	@ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
	protected String variant;

	/**
	 * Load the model from this location instead of locating the model automatically.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

	/**
	 * Load the part-of-speech tag to UIMA type mapping from this location instead of locating
	 * the mapping automatically.
	 */
	public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
	@ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
	protected String posMappingLocation;

	/**
	 * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
	 * spaming the heap with thousands of strings representing only a few different tags.
	 *
	 * Default: {@code true}
	 */
	public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
	@ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
	private boolean internTags;

	/**
	 * Log the tag set(s) when a model is loaded.
	 *
	 * Default: {@code false}
	 */
	public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
	@ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue="false")
	protected boolean printTagSet;

	/**
	 * Sets whether to create or not to create POS tags. The creation of
	 * constituent tags must be turned on for this to work.<br/>
	 *
	 * Default: {@code true}
	 */
	public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
	@ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "true")
	private boolean createPosTags;

	/**
	 * If this parameter is set to true, each sentence is annotated with a PennTree-Annotation,
	 * containing the whole parse tree in Penn Treebank style format.
	 *
	 * Default: {@code false}
	 */
	public static final String PARAM_CREATE_PENN_TREE_STRING = ComponentParameters.PARAM_WRITE_PENN_TREE;
	@ConfigurationParameter(name = PARAM_CREATE_PENN_TREE_STRING, mandatory = true, defaultValue = "false")
	private boolean createPennTreeString;

	private CasConfigurableProviderBase<Parser> modelProvider;
	private MappingProvider posMappingProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		modelProvider = new OpenNlpParserModelProvider();

		posMappingProvider = new MappingProvider();
		posMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
				"core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
		posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
		posMappingProvider.setDefault("tagger.tagset", "default");
		posMappingProvider.setOverride(MappingProvider.LOCATION, posMappingLocation);
		posMappingProvider.setOverride(MappingProvider.LANGUAGE, language);
		posMappingProvider.addImport("tagger.tagset", modelProvider);

	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		CAS cas = aJCas.getCas();

		modelProvider.configure(cas);
		posMappingProvider.configure(cas);

		for (Sentence sentence : select(aJCas, Sentence.class)) {
			List<Token> tokens = selectCovered(aJCas, Token.class, sentence);

		    Parse parseInput = new Parse(cas.getDocumentText(),
		    		new Span(sentence.getBegin(), sentence.getEnd()),
		    		AbstractBottomUpParser.INC_NODE, 0, 0);
		    int i=0;
			for (Token t : tokens) {
				parseInput.insert(new Parse(cas.getDocumentText(), new Span(t.getBegin(), t.getEnd()),
						AbstractBottomUpParser.TOK_NODE, 0, i));
				i++;
		    }

			Parse parseOutput = modelProvider.getResource().parse(parseInput);

			createConstituentAnnotationFromTree(aJCas, parseOutput, null, tokens);

			if (createPennTreeString) {
				StringBuffer sb = new StringBuffer();
				parseOutput.setType("ROOT"); // in DKPro the root is ROOT, not TOP
				parseOutput.show(sb);

				PennTree pTree = new PennTree(aJCas, sentence.getBegin(), sentence.getEnd());
				pTree.setPennTree(sb.toString());
				pTree.addToIndexes();
			}
		}
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
	private Annotation createConstituentAnnotationFromTree(JCas aJCas, Parse aNode, Annotation aParentFS, List<Token> aTokens)
	{
		// If the node is a word-level constituent node (== POS):
		// create parent link on token and (if not turned off) create POS tag
		if (aNode.isPosTag()) {
			Token token = getToken(aTokens, aNode.getSpan().getStart(), aNode.getSpan().getEnd());

			// link token to its parent constituent
			if (aParentFS != null) {
				token.setParent(aParentFS);
			}

			// only add POS to index if we want POS-tagging
			if (createPosTags) {
				Type posTag = posMappingProvider.getTagType(aNode.getType());
				POS posAnno = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(), token.getEnd());
				posAnno.setPosValue(internTags ? aNode.getType().intern() : aNode.getType());
				posAnno.addToIndexes();
				token.setPos(posAnno);
			}

			return token;
		}
		// Check if node is a constituent node on sentence or phrase-level
		else {
			String typeName = aNode.getType();
			if (AbstractBottomUpParser.TOP_NODE.equals(typeName)) {
				typeName = "ROOT"; // in DKPro the root is ROOT, not TOP
			}

			// create the necessary objects and methods
			String constituentTypeName = CONPACKAGE + typeName;

			Type type = aJCas.getTypeSystem().getType(constituentTypeName);

			//if type is unknown, map to X-type
			if (type==null){
				type = aJCas.getTypeSystem().getType(CONPACKAGE+"X");
			}

			Constituent constAnno = (Constituent) aJCas.getCas().createAnnotation(type,
					aNode.getSpan().getStart(), aNode.getSpan().getEnd());
			constAnno.setConstituentType(typeName);

			// link to parent
			if (aParentFS != null) {
				constAnno.setParent(aParentFS);
			}

			// Do we have any children?
			List<Annotation> childAnnotations = new ArrayList<Annotation>();
			for (Parse child : aNode.getChildren()) {
				Annotation childAnnotation = createConstituentAnnotationFromTree(aJCas, child, constAnno, aTokens);
				if (childAnnotation != null) {
					childAnnotations.add(childAnnotation);
				}
			}

			// Now that we know how many children we have, link annotation of
			// current node with its children
			FSArray childArray = FSCollectionFactory.createFSArray(aJCas, childAnnotations);
			constAnno.setChildren(childArray);

			// write annotation for current node to index
			aJCas.addFsToIndexes(constAnno);

			return constAnno;
		}
	}

	/**
	 * Given a list of tokens (e.g. those from a sentence) return the one at the specified position.
	 */
	private Token getToken(List<Token> aTokens, int aBegin, int aEnd)
	{
		for (Token t : aTokens) {
			if (aBegin == t.getBegin() && aEnd == t.getEnd()) {
				return t;
			}
		}
		throw new IllegalStateException("Token not found");
	}

    private class OpenNlpParserModelProvider
        extends ModelProviderBase<Parser>
    {
        {
            setContextObject(OpenNlpParser.this);

            setDefault(ARTIFACT_ID, "${groupId}.opennlp-model-parser-${language}-${variant}");
            setDefault(LOCATION, "classpath:/${package}/lib/parser-${language}-${variant}.bin");
            setDefault(VARIANT, "chunking");

            setOverride(LOCATION, modelLocation);
            setOverride(LANGUAGE, language);
            setOverride(VARIANT, variant);
        }

        @Override
        protected Parser produceResource(InputStream aStream)
            throws Exception
        {
            ParserModel model = new ParserModel(aStream);
            Properties metadata = getResourceMetaData();

            addTagset(new OpenNlpTagsetDescriptionProvider(
                    metadata.getProperty("tagger.tagset"), POS.class, model.getParserTaggerModel()
                            .getPosModel()));
            addTagset(new OpenNlpParserTagsetDescriptionProvider(
                    metadata.getProperty("constituent.type.tagset"), Constituent.class, model
                            .getParserChunkerModel().getChunkerModel()));

            if (printTagSet) {
                getContext().getLogger().log(INFO, getTagset().toString());
            }

            return ParserFactory.create(model);
        }
    }
}
