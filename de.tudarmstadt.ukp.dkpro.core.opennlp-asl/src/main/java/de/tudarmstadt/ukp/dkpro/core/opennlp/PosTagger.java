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

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.resolveLocation;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;
import static org.uimafit.util.JCasUtil.toText;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.TagsetMappingFactory;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Part-of-Speech annotator using OpenNLP. Requires {@link Sentence}s to be annotated before.
 * Currently only Maxent models are supported.
 * 
 * @author Richard Eckart de Castilho
 */
public class PosTagger
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

	public static final String PARAM_TAG_MAPPING_LOCATION = "TagMappingLocation";
	@ConfigurationParameter(name = PARAM_TAG_MAPPING_LOCATION, mandatory = false)
	protected String tagMappingLocation;

	public static final String PARAM_INTERN_STRINGS = "InternStrings";
	@ConfigurationParameter(name = PARAM_INTERN_STRINGS, mandatory = false, defaultValue = "true")
	private boolean internStrings;

	private POSTagger posTagger;
	private Map<String, String> tagMapping;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		try {
			if (tagMappingLocation != null) {
				tagMapping = TagsetMappingFactory.getMapping(tagMappingLocation);
			}
			else if (language != null) {
				tagMapping = TagsetMappingFactory.getMapping("tagger", language, O.class.getName());
			}
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}

		InputStream is = null;
		try {
			URL modelUrl;
			if (modelLocation != null) {
				modelUrl = resolveModel(modelLocation, this, getContext());
			}
			else if (language != null) {
				modelUrl = resolveModel(language + "-maxent", this, getContext());
			}
			else {
				throw new IllegalArgumentException(
						"Neither PARAM_LANGUAGE nor PARAM_MODEL specified");
			}
			is = modelUrl.openStream();
			POSModel model = new POSModel(is);
			posTagger = new POSTaggerME(model);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		finally {
			closeQuietly(is);
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		TypeSystem ts = aJCas.getCas().getTypeSystem();
		CAS cas = aJCas.getCas();

		for (Sentence sentence : select(aJCas, Sentence.class)) {
			List<Token> tokens = selectCovered(aJCas, Token.class, sentence);
			String[] tokenTexts = toText(tokens).toArray(new String[tokens.size()]);

			String[] tags = posTagger.tag(tokenTexts);

			int i = 0;
			for (Token t : tokens) {
				POS posAnno;
				if (tagMapping != null) {
					Type posTag = TagsetMappingFactory.getTagType(tagMapping, tags[i], ts);
					posAnno = (POS) cas.createAnnotation(posTag, t.getBegin(), t.getEnd());
					posAnno.setStringValue(posTag.getFeatureByBaseName("PosValue"),
							internStrings ? tags[i].intern() : tags[i]);
				}
				else {
					posAnno = new POS(aJCas, t.getBegin(), t.getEnd());
					posAnno.setPosValue(internStrings ? tags[i].intern() : tags[i]);
				}
				posAnno.addToIndexes();
				t.setPos((POS) posAnno);
				i++;
			}
		}
	}

	protected static URL resolveModel(String aModel, Object aCaller, UimaContext aContext)
		throws IOException
	{
		URL modelUrl = null;

		try {
			// Try shorthand
			if (!aModel.contains(":")) {
				modelUrl = resolveLocation(
						"classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/tagger-" + aModel
								+ ".bin", aCaller, aContext);
			}
		}
		catch (FileNotFoundException e) {
			// Ignore
		}
		if (modelUrl == null) {
			modelUrl = resolveLocation(aModel, aCaller, aContext);
		}
		return modelUrl;
	}
}
