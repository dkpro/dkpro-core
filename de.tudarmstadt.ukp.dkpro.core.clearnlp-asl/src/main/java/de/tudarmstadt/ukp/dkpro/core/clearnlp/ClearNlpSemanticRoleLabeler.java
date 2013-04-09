/*******************************************************************************
 * Copyright 2013
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
import static org.apache.uima.util.Level.INFO;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.FSCollectionFactory;

import com.googlecode.clearnlp.classification.model.StringModel;
import com.googlecode.clearnlp.component.AbstractComponent;
import com.googlecode.clearnlp.component.AbstractStatisticalComponent;
import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPLib;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.nlp.NLPLib;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableStreamProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.srl.type.SemanticArgument;
import de.tudarmstadt.ukp.dkpro.core.api.srl.type.SemanticPredicate;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class ClearNlpSemanticRoleLabeler
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
	 * Location from which the predicate identifier model is read.
	 */
	public static final String PARAM_PRED_MODEL_LOCATION = "predModelLocation";
	@ConfigurationParameter(name = PARAM_PRED_MODEL_LOCATION, mandatory = false)
	protected String predModelLocation;

	/**
	 * Location from which the roleset classification model is read.
	 */
	public static final String PARAM_ROLE_MODEL_LOCATION = "roleModelLocation";
	@ConfigurationParameter(name = PARAM_ROLE_MODEL_LOCATION, mandatory = false)
	protected String roleModelLocation;

	/**
	 * Location from which the semantic role labeling model is read.
	 */
	public static final String PARAM_SRL_MODEL_LOCATION = "srlModelLocation";
	@ConfigurationParameter(name = PARAM_SRL_MODEL_LOCATION, mandatory = false)
	protected String srlModelLocation;

	private CasConfigurableProviderBase<AbstractComponent> predicateFinder;

	private CasConfigurableProviderBase<AbstractComponent> roleSetClassifier;

	private CasConfigurableProviderBase<AbstractComponent> roleLabeller;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		predicateFinder = new CasConfigurableStreamProviderBase<AbstractComponent>()
		{
			{
				setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/clearnlp/lib/"
						+ "pred-${language}-${variant}.bin");
				setDefault(VARIANT, "ontonotes");

				setOverride(LOCATION, predModelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected AbstractComponent produceResource(InputStream aStream)
				throws Exception
			{
				AbstractComponent component = EngineGetter.getComponent(aStream,
						getAggregatedProperties().getProperty(LANGUAGE), NLPLib.MODE_PRED);
				printTags(NLPLib.MODE_PRED, component);
				return component;
			}
		};

		roleSetClassifier = new CasConfigurableStreamProviderBase<AbstractComponent>()
		{
			{
				setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/clearnlp/lib/"
						+ "role-${language}-${variant}.bin");
				setDefault(VARIANT, "ontonotes");

				setOverride(LOCATION, roleModelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected AbstractComponent produceResource(InputStream aStream)
				throws Exception
			{
				AbstractComponent component = EngineGetter.getComponent(aStream,
						getAggregatedProperties().getProperty(LANGUAGE), NLPLib.MODE_ROLE);
				printTags(NLPLib.MODE_ROLE, component);
				return component;
			}
		};

		roleLabeller = new CasConfigurableStreamProviderBase<AbstractComponent>()
		{
			{
				setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/clearnlp/lib/"
						+ "srl-${language}-${variant}.bin");
				setDefault(VARIANT, "ontonotes");

				setOverride(LOCATION, srlModelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected AbstractComponent produceResource(InputStream aStream)
				throws Exception
			{
				AbstractComponent component = EngineGetter.getComponent(aStream,
						getAggregatedProperties().getProperty(LANGUAGE), NLPLib.MODE_SRL);
				printTags(NLPLib.MODE_SRL, component);
				return component;
			}
		};
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		predicateFinder.configure(aJCas.getCas());
		roleSetClassifier.configure(aJCas.getCas());
		roleLabeller.configure(aJCas.getCas());

		// Iterate over all sentences
		for (Sentence sentence : select(aJCas, Sentence.class)) {
			List<Token> tokens = selectCovered(aJCas, Token.class, sentence);

			DEPTree tree = new DEPTree();

			// Generate:
			// - DEPNode
			// - pos tags
			// - lemma
			for (int i = 0; i < tokens.size(); i++) {
				Token t = tokens.get(i);
				DEPNode node = new DEPNode(i + 1, tokens.get(i).getCoveredText());
				node.pos = t.getPos().getPosValue();
				node.lemma = t.getLemma().getValue();
				tree.add(node);
			}

			// Generate:
			// Dependency relations
			for (Dependency dep : selectCovered(Dependency.class, sentence)) {
				int headIndex = tokens.indexOf(dep.getGovernor());
				int tokenIndex = tokens.indexOf(dep.getDependent());

				DEPNode token = tree.get(tokenIndex + 1);
				DEPNode head = tree.get(headIndex + 1);

				token.setHead(head, dep.getDependencyType());
			}

			// Do the SRL
			predicateFinder.getResource().process(tree);
			roleSetClassifier.getResource().process(tree);
			roleLabeller.getResource().process(tree);

			// Convert the results into UIMA annotations
			Map<Token, SemanticPredicate> predicates = new HashMap<Token, SemanticPredicate>();
			Map<SemanticPredicate, List<SemanticArgument>> predArgs = new HashMap<SemanticPredicate, List<SemanticArgument>>();

			for (int i = 0; i < tokens.size(); i++) {
				DEPNode parserNode = tree.get(i + 1);
				Token argumentToken = tokens.get(i);

				for (DEPArc argPredArc : parserNode.getSHeads()) {
					Token predToken = tokens.get(argPredArc.getNode().id - 1);

					// Instantiate the semantic predicate annotation if it hasn't been done yet
					SemanticPredicate pred = predicates.get(predToken);
					if (pred == null) {
						// Create the semantic predicate annotation itself
						pred = new SemanticPredicate(aJCas, predToken.getBegin(),
								predToken.getEnd());
						pred.setCategory(argPredArc.getNode().getFeat(DEPLib.FEAT_PB));
						pred.addToIndexes();
						predicates.put(predToken, pred);

						// Prepare a list to store its arguments
						predArgs.put(pred, new ArrayList<SemanticArgument>());
					}

					// Instantiate the semantic argument annotation
					SemanticArgument arg = new SemanticArgument(aJCas, argumentToken.getBegin(),
							argumentToken.getEnd());
					arg.setRole(argPredArc.getLabel());
					arg.addToIndexes();

					// Remember to which predicate this argument belongs
					predArgs.get(pred).add(arg);
				}
			}

			for (Entry<SemanticPredicate, List<SemanticArgument>> e : predArgs.entrySet()) {
				e.getKey().setArguments(
						(FSArray) FSCollectionFactory.createFSArray(aJCas, e.getValue()));
			}
		}
	}

	private void printTags(String aType, AbstractComponent aComponent)
	{
		if (printTagSet && (aComponent instanceof AbstractStatisticalComponent)) {
			AbstractStatisticalComponent component = (AbstractStatisticalComponent) aComponent;

			Set<String> tagSet = new HashSet<String>();

			for (StringModel model : component.getModels()) {
				tagSet.addAll(asList(model.getLabels()));
			}

			List<String> tagList = new ArrayList<String>(tagSet);
			Collections.sort(tagList);

			StringBuilder sb = new StringBuilder();
			sb.append("Model of " + aType + " contains [").append(tagList.size())
					.append("] tags: ");

			for (String tag : tagList) {
				sb.append(tag);
				sb.append(" ");
			}
			getContext().getLogger().log(INFO, sb.toString());
		}
	}
}
