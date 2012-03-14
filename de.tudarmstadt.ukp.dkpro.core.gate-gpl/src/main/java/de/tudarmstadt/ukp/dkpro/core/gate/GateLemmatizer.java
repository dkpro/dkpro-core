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
package de.tudarmstadt.ukp.dkpro.core.gate;

import gate.creole.ResourceInstantiationException;
import gate.creole.morph.Interpret;

import java.io.IOException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


/**
 * Wrapper for the GATE rule based lemmatizer.
 * 
 * Based on code by Asher Stern from the BIUTEE textual entailment tool.
 * 
 * @author zesch
 * @since 1.4.0
 *
 */
public class GateLemmatizer extends JCasAnnotator_ImplBase {

    public static final String PARAM_RULE_FILE = "RuleFile";
    @ConfigurationParameter(name = PARAM_RULE_FILE, mandatory = false)
    private String ruleFile;
	
	// constants
	public static final String GATE_LEMMATIZER_VERB_CATEGORY_STRING = "VB";
	public static final String GATE_LEMMATIZER_NOUN_CATEGORY_STRING = "NN";
	public static final String GATE_LEMMATIZER_ALL_CATEGORIES_STRING = "*";

	private Interpret gateLemmatizerInterpretObject = null;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException
	{
		super.initialize(context);
		
		URL ruleFileURL = null;
		try {
			if (ruleFile != null) {
				ruleFileURL = ResourceUtils.resolveLocation(ruleFile, this, context);
			}
			else {
				ruleFileURL = ResourceUtils.resolveLocation("classpath:/rules/en.rul", this, context);
			}
						
			gateLemmatizerInterpretObject = new Interpret();
			gateLemmatizerInterpretObject.init(ruleFileURL);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		} catch (ResourceInstantiationException e) {
			throw new ResourceInitializationException(e);
		}		
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{
		String category = null;
		for (Token token : JCasUtil.select(jcas, Token.class)) {
			POS pos = token.getPos();
			
			if (pos != null) {
				if (pos.getClass().equals(V.class)) {
					category = GATE_LEMMATIZER_VERB_CATEGORY_STRING;
				}
				else if (pos.getClass().equals(N.class)) {
					category = GATE_LEMMATIZER_NOUN_CATEGORY_STRING;
				}
				else if (pos.getClass().equals(PR.class)) {
					category = GATE_LEMMATIZER_NOUN_CATEGORY_STRING;
				}
				else
					category = GATE_LEMMATIZER_ALL_CATEGORIES_STRING;
			}
			else
			{
				category = GATE_LEMMATIZER_ALL_CATEGORIES_STRING;
			}
			
			String tokenString = token.getCoveredText();
			String lemmaString = gateLemmatizerInterpretObject.runMorpher(tokenString, category);
			
			Lemma lemma = new Lemma(jcas, token.getBegin(), token.getEnd());
			lemma.setValue(lemmaString);
			lemma.addToIndexes();
			
			// remove (a potentially existing) old lemma before adding a new one
			if (token.getLemma() != null) {
				Lemma oldLemma = token.getLemma();
				oldLemma.removeFromIndexes();
			}
			
			token.setLemma(lemma);
		}
	}
}