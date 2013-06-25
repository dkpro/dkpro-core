/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.matetools;

import static org.uimafit.util.JCasUtil.selectCovered;
import is2.data.SentenceData09;
import is2.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * <p>
 * DKPro Annotator for the MateToolsParser
 * </p>
 * 
 * Please cite the following paper, if you use the parser Bernd Bohnet. 2010. Top Accuracy and Fast
 * Dependency Parsing is not a Contradiction. The 23rd International Conference on Computational
 * Linguistics (COLING 2010), Beijing, China.
 * 
 * Required annotations:<br/>
 * <ul>
 * <li>Sentence</li>
 * <li>Token</li>
 * <li>Lemma</li>
 * <li>POS</li>
 * </ul>
 * 
 * Generated annotations:<br/>
 * <ul>
 * <li>Dependency</li>
 * </ul>
 * 
 * 
 * @author AnNa, zesch
 */
public class MateParser
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

	private CasConfigurableProviderBase<Parser> modelProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		modelProvider = new CasConfigurableProviderBase<Parser>()
		{
			{
			    setContextObject(MateParser.this);
			    
				setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
				setDefault(ARTIFACT_ID,
						"de.tudarmstadt.ukp.dkpro.core-nonfree-model-parser-${language}-${variant}");

				setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/matetools/lib/"
						+ "parser-${language}-${variant}.model");
				setDefault(VARIANT, "crosstrain");

				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected Parser produceResource(URL aUrl)
				throws IOException
			{
				File modelFile = ResourceUtils.getUrlAsFile(aUrl, true);

				String[] args = { "-model", modelFile.getPath() };
				is2.parser.Options option = new is2.parser.Options(args);
				return new is2.parser.Parser(option); // create a parser
			}
		};
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{
		CAS cas = jcas.getCas();

		modelProvider.configure(cas);

		for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
			List<Token> tokens = JCasUtil.selectCovered(Token.class, sentence);

			LinkedList<String> forms = new LinkedList<String>();
			forms.add("<root>");
			for (Token token : selectCovered(Token.class, sentence)) {
				forms.add(token.getCoveredText());
			}

			LinkedList<String> lemmas = new LinkedList<String>();
			lemmas.add("<root>");
			for (Lemma lemma : JCasUtil.selectCovered(Lemma.class, sentence)) {
				lemmas.add(lemma.getValue());
			}

			LinkedList<String> posTags = new LinkedList<String>();
			posTags.add("<root-POS>");
			for (POS posTag : JCasUtil.selectCovered(POS.class, sentence)) {
				posTags.add(posTag.getPosValue());
			}

			SentenceData09 sd = new SentenceData09();
			sd.init(forms.toArray(new String[0]));
			sd.setLemmas(lemmas.toArray(new String[0]));
			sd.setPPos(posTags.toArray(new String[0]));

			SentenceData09 parsed = modelProvider.getResource().parse(sd);
			for (int i = 0; i < parsed.labels.length; i++) {
				Token sourceToken = tokens.get(i);
				// Token targetToken = null; //for "ROOT"
				if (parsed.heads[i] != 0) {
					Token targetToken = tokens.get(parsed.heads[i] - 1);
	                Dependency dep = new Dependency(jcas);
					dep.setGovernor(targetToken);
					dep.setDependent(sourceToken);
					dep.setDependencyType(parsed.labels[i]);
					dep.setBegin(dep.getGovernor().getBegin());
                    dep.setEnd(dep.getGovernor().getEnd());
					dep.addToIndexes();
				}
			}
		}
	}
}