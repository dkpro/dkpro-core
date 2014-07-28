package de.tudarmstadt.ukp.dkpro.core.arktools;

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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.resource.ResourceInitializationException;

import cmu.arktweetnlp.impl.Model;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;
import cmu.arktweetnlp.impl.features.FeatureExtractor;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Wrapper for Twitter Tokenizer and POS Tagger.
 * 
 * As described in: Olutobi Owoputi, Brendan O’Connor, Chris Dyer, Kevin Gimpel,
 * Nathan Schneider and Noah A. Smith. Improved Part-of-Speech Tagging for
 * Online Conversational Text with Word Clusters In Proceedings of NAACL 2013.
 *
 * @author zesch
 *
 */
public class ArktweetPosTagger extends CasAnnotator_ImplBase {

	/**
	 * Use this language instead of the document language to resolve the model
	 * and tag set mapping.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	/**
	 * Variant of a model the model. Used to address a specific model if here
	 * are multiple models for one language.
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

	/**
	 * Location of the mapping file for part-of-speech tags to UIMA types.
	 */
	public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
	@ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
	protected String posMappingLocation;

	private Type tokenType;
	private Feature featPos;

	private CasConfigurableProviderBase<TweetTagger> modelProvider;
	private MappingProvider mappingProvider;

	/**
	 * Loads a model from a file. The tagger should be ready to tag after
	 * calling this.
	 * 
	 * @param modelFilename
	 * @throws IOException
	 */

	public class TweetTagger {
		Model model;
		FeatureExtractor featureExtractor;

		public void loadModel(String modelFilename) throws IOException {
			model = Model.loadModelFromText(modelFilename);
			featureExtractor = new FeatureExtractor(model, false);
		}
	}

	/**
	 * One token and its tag.
	 **/
	public static class TaggedToken {
		public AnnotationFS token;
		public String tag;
	}

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		modelProvider = new ModelProviderBase<TweetTagger>() {
			{
				setContextObject(ArktweetPosTagger.this);

				setDefault(ARTIFACT_ID,
						"${groupId}.arktools-model-tagger-${language}-${variant}");
				setDefault(LOCATION,
						"classpath:/${package}/lib/tagger-${language}-${variant}.properties");
				setDefault(VARIANT, "default");

				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected TweetTagger produceResource(URL aUrl) throws IOException {
				try {
					TweetTagger model = new TweetTagger();
					model.loadModel(ResourceUtils.getUrlAsFile(aUrl, false)
							.getAbsolutePath());

					return model;
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		};

		mappingProvider = new MappingProvider();
		mappingProvider.setDefault(MappingProvider.LOCATION,
				"classpath:/de/tudarmstadt/ukp/dkpro/"
						+ "core/api/lexmorph/tagset/en-arktweet.map");
		mappingProvider.setDefault(MappingProvider.BASE_TYPE,
				POS.class.getName());
		mappingProvider.setDefault("pos.tagset", "arktweet");
	}

	@Override
	public void typeSystemInit(TypeSystem aTypeSystem)
			throws AnalysisEngineProcessException {
		super.typeSystemInit(aTypeSystem);

		tokenType = aTypeSystem.getType(Token.class.getName());
		featPos = tokenType.getFeatureByBaseName("pos");
	}

	@Override
	public void process(CAS cas) throws AnalysisEngineProcessException {

		mappingProvider.configure(cas);
		modelProvider.configure(cas);

		List<AnnotationFS> tokens = CasUtil.selectCovered(cas, tokenType, 0,
				cas.getDocumentText().length());
		List<TaggedToken> taggedTokens = tagTweetTokens(tokens,
				modelProvider.getResource());

		for (TaggedToken taggedToken : taggedTokens) {

			Type posType = mappingProvider.getTagType(taggedToken.tag);

			AnnotationFS posAnno = cas.createAnnotation(posType,
					taggedToken.token.getBegin(), taggedToken.token.getEnd());
			posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"),
					taggedToken.tag);
			cas.addFsToIndexes(posAnno);

			taggedToken.token.setFeatureValue(featPos, posAnno);
		}
	}

	private List<TaggedToken> tagTweetTokens(
			List<AnnotationFS> annotatedTokens, TweetTagger tweetTagModel) {

		List<String> tokens = new LinkedList<String>();
		for (AnnotationFS a : annotatedTokens) {
			String tokenText = a.getCoveredText();
			tokenText = ArktweetTokenizer.normalizeText(tokenText);
			tokens.add(tokenText);
		}

		Sentence sentence = new Sentence();
		sentence.tokens = tokens;
		ModelSentence ms = new ModelSentence(sentence.T());
		tweetTagModel.featureExtractor.computeFeatures(sentence, ms);
		tweetTagModel.model.greedyDecode(ms, false);

		ArrayList<TaggedToken> taggedTokens = new ArrayList<TaggedToken>();

		for (int t = 0; t < sentence.T(); t++) {
			TaggedToken tt = new TaggedToken();
			tt.token = annotatedTokens.get(t);
			tt.tag = tweetTagModel.model.labelVocab.name(ms.labels[t]);
			taggedTokens.add(tt);
		}
		return taggedTokens;
	}
}