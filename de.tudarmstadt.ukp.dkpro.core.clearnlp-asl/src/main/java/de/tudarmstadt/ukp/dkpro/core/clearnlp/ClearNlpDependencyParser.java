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

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.util.Level.INFO;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;

import com.googlecode.clearnlp.classification.model.StringModel;
import com.googlecode.clearnlp.component.dep.CDEPPassParser;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.nlp.NLPLib;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * Clear parser annotator. 
 * 
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
		inputs={
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
				"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"
		},
		outputs={
				"de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"
		}
)
public class ClearNlpDependencyParser
	extends JCasAnnotator_ImplBase
{
	/**
	 * Write the tag set(s) to the log when a model is loaded.
	 */
	public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
	@ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
	protected boolean printTagSet;

	/**
	 * Use this language instead of the document language to resolve the model.
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

	private File workingDir;

	private CasConfigurableProviderBase<CDEPPassParser> parserProvider;
		
	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		parserProvider = new CasConfigurableProviderBase<CDEPPassParser>()
		{
			{
				setDefault(VERSION, "20121229.0");
				setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
				setDefault(ARTIFACT_ID,
						"de.tudarmstadt.ukp.dkpro.core.clearnlp-model-parser-${language}-${variant}");
				
				setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/clearnlp/lib/" +
						"parser-${language}-${variant}.bin");
				setDefault(VARIANT, "ontonotes");
				
				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected CDEPPassParser produceResource(URL aUrl)
				throws IOException
			{
				InputStream is = null;
				try {
					is = aUrl.openStream();
					CDEPPassParser parser = (CDEPPassParser) EngineGetter.getComponent(is,
							getAggregatedProperties().getProperty(LANGUAGE), NLPLib.MODE_DEP);

					if (printTagSet) {
						StringBuilder sb = new StringBuilder();
						try {
							Set<String> tagSet = new HashSet<String>();
							
							for (StringModel model : parser.getModels()) {
								for (String label : model.getLabels()) {
									String[] fields = label.split("_");
									if (fields.length == 3) {
										tagSet.add(fields[2]);
									}
//									else {
//										getContext().getLogger().log(WARNING,
//												"Unknown label format: [" + label + "]");
//									}
								}
							}
							
							List<String> tagList = new ArrayList<String>(tagSet);
							Collections.sort(tagList);
							
							sb.append("Model contains [").append(tagList.size()).append("] tags: ");
							
							for (String tag : tagList) {
								sb.append(tag);
								sb.append(" ");
							}
						}
						catch (Exception e) {
							sb.append("Unable to find tagset information.");
						}
						
						getContext().getLogger().log(INFO, sb.toString());
					}
					
					return parser;
				}
				catch (Exception e) {
					throw new IOException(e);
				}
				finally {
					closeQuietly(is);
				}
			}
		};
	}

	/**
	 * @see org.apache.uima.AnalysisComponent.AnalysisComponent#collectionProcessComplete()
	 */
	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException
	{
		if (workingDir != null && workingDir.isDirectory()) {
			FileUtils.deleteQuietly(workingDir);
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		parserProvider.configure(aJCas.getCas());

		// Iterate over all sentences
		for (Sentence sentence : select(aJCas, Sentence.class)) {
			List<Token> tokens = selectCovered(aJCas, Token.class, sentence);
			
			DEPTree tree = new DEPTree();

			// Generate input format required by parser
			for (int i = 0; i < tokens.size(); i++) {
				Token t = tokens.get(i);
				DEPNode node = new DEPNode(i+1, tokens.get(i).getCoveredText());
				node.pos = t.getPos().getPosValue();
				if (t.getLemma() != null) {
					node.lemma = t.getLemma().getValue();
				}
				tree.add(node);
			}

			// Parse sentence
			CDEPPassParser parser = parserProvider.getResource();
			parser.process(tree);
		
			for (int i = 1; i < tree.size(); i++) {
				DEPNode node = tree.get(i);

				if (node.hasHead()) {
					if (node.getHead().id == 0) {
						// Skip root relation
						continue;
					}
					
					Dependency dep = new Dependency(aJCas, sentence.getBegin(), sentence.getEnd());
					dep.setGovernor(tokens.get(node.getHead().id-1)); 
					dep.setDependent(tokens.get(node.id-1));
					dep.setDependencyType(node.getLabel());
					dep.addToIndexes();
				}
			}
		}
	}
}
