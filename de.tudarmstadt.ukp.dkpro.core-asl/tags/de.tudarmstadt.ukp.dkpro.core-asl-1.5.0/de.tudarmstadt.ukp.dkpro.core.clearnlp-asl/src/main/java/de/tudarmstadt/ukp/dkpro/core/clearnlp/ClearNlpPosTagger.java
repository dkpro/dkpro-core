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
package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.apache.uima.util.Level.INFO;

import java.io.InputStream;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.googlecode.clearnlp.classification.model.StringModel;
import com.googlecode.clearnlp.component.pos.CPOSTagger;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.nlp.NLPDecode;
import com.googlecode.clearnlp.nlp.NLPLib;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Part-of-Speech annotator using Clear NLP. Requires {@link Sentence}s to be annotated before.
 *
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
	    inputs = {
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
		outputs = {
		    "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class ClearNlpPosTagger
	extends JCasAnnotator_ImplBase
{
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
	 */
	public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
	@ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
	private boolean internTags;

	/**
	 * Log the tag set(s) when a model is loaded.
	 */
	public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
	@ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue="false")
	protected boolean printTagSet;

	private CasConfigurableProviderBase<CPOSTagger> modelProvider;
	private MappingProvider posMappingProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		modelProvider = new ModelProviderBase<CPOSTagger>() {
			{
			    setContextObject(ClearNlpPosTagger.this);

                setDefault(ARTIFACT_ID, "${groupId}.clearnlp-model-tagger-${language}-${variant}");
				setDefault(LOCATION, "classpath:/${package}/lib/tagger-${language}-${variant}.bin");
				setDefault(VARIANT, "ontonotes");

				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected CPOSTagger produceResource(InputStream aStream)
			    throws Exception
			{
                CPOSTagger tagger = (CPOSTagger) EngineGetter.getComponent(aStream,
                        getAggregatedProperties().getProperty(LANGUAGE), NLPLib.MODE_POS);

                SingletonTagset tags = new SingletonTagset(POS.class, getResourceMetaData()
                        .getProperty(("pos.tagset")));
                for (StringModel model : tagger.getModels()) {
                    tags.addAll(asList(model.getLabels()));
                }
                addTagset(tags, true);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, getTagset().toString());
                }

                return tagger;
			}
		};

		posMappingProvider = new MappingProvider();
		posMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
				"core/api/lexmorph/tagset/${language}-${pos.tagset}-pos.map");
		posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
		posMappingProvider.setDefault("pos.tagset", "default");
		posMappingProvider.setOverride(MappingProvider.LOCATION, posMappingLocation);
		posMappingProvider.setOverride(MappingProvider.LANGUAGE, language);
		posMappingProvider.addImport("pos.tagset", modelProvider);
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
			List<String> tokenTexts = asList(toText(tokens).toArray(new String[tokens.size()]));

			NLPDecode nlp = new NLPDecode();
			DEPTree tree = nlp.toDEPTree(tokenTexts);

			CPOSTagger tagger = modelProvider.getResource();
			tagger.process(tree);

			String[] posTags = tree.getPOSTags();

			int i = 0;
			for (Token t : tokens) {
				String tag = internTags ? posTags[i+1].intern() : posTags[i+1];
				Type posTag = posMappingProvider.getTagType(tag);
				POS posAnno = (POS) cas.createAnnotation(posTag, t.getBegin(), t.getEnd());
				posAnno.setPosValue(tag);
				posAnno.addToIndexes();
				t.setPos(posAnno);
				i++;
			}
		}
	}
}
