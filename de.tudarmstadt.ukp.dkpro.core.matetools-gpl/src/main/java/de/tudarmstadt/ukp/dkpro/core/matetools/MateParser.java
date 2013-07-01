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

import static org.apache.uima.util.Level.INFO;
import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.parser.MFO;
import is2.parser.Options;
import is2.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
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
@TypeCapability(
        inputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" }, 
        outputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
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

    /**
     * Log the tag set(s) when a model is loaded.
     *
     * Default: {@code false}
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;
    
	private CasConfigurableProviderBase<Parser> modelProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		modelProvider = new ModelProviderBase<Parser>()
		{
			{
				setContextObject(MateParser.this);

				setDefault(ARTIFACT_ID,
						"${groupId}.matetools-model-parser-${language}-${variant}");
				setDefault(LOCATION, "classpath:/${package}/lib/parser-${language}-${variant}.properties");
                setDefaultVariantsLocation("${package}/lib/parser-default-variants.map");

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
				Options option = new Options(args);
				Parser parser = new Parser(option); // create a parser
				
				Properties metadata = getResourceMetaData();
				
				HashMap<String, HashMap<String, Integer>> featureSet = MFO.getFeatureSet();
                SingletonTagset posTags = new SingletonTagset(
                        POS.class, metadata.getProperty("pos.tagset"));
                HashMap<String, Integer> posTagFeatures = featureSet.get("POS");
                posTags.addAll(posTagFeatures.keySet());
                addTagset(posTags);

                SingletonTagset depTags = new SingletonTagset(
                        Dependency.class, metadata.getProperty("dependency.tagset"));
                HashMap<String, Integer> depTagFeatures = featureSet.get("REL");
                depTags.addAll(depTagFeatures.keySet());
                addTagset(depTags);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, getTagset().toString());
                }

				return parser;
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

			List<String> forms = new LinkedList<String>();
			forms.add(CONLLReader09.ROOT);
			forms.addAll(JCasUtil.toText(tokens));

//			List<String> lemmas = new LinkedList<String>();
			List<String> posTags = new LinkedList<String>();
//			lemmas.add(CONLLReader09.ROOT_LEMMA);
			posTags.add(CONLLReader09.ROOT_POS);
			for (Token token : tokens) {
//				lemmas.add(token.getLemma().getValue());
				posTags.add(token.getPos().getPosValue());
			}

			SentenceData09 sd = new SentenceData09();
			sd.init(forms.toArray(new String[0]));
//			sd.setLemmas(lemmas.toArray(new String[0]));
			sd.setPPos(posTags.toArray(new String[0]));
			SentenceData09 parsed = modelProvider.getResource().apply(sd);

			for (int i = 0; i < parsed.labels.length; i++) {
				if (parsed.pheads[i] != 0) {
					Token sourceToken = tokens.get(parsed.pheads[i] - 1);
					Token targetToken = tokens.get(i);

					Dependency dep = new Dependency(jcas);
					dep.setBegin(sourceToken.getBegin());
					dep.setEnd(sourceToken.getEnd());
					dep.setGovernor(sourceToken);
					dep.setDependent(targetToken);
					dep.setDependencyType(parsed.plabels[i]);
					dep.addToIndexes();
				}
			}
		}
	}
}