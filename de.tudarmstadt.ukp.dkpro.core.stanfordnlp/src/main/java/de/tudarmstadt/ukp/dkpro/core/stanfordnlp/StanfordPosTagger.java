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

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.resolveLocation;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.CustomTaggerConfig;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * @author Richard Eckart de Castilho
 */
public class StanfordPosTagger
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_MODEL_PATH = "ModelPath";
	@ConfigurationParameter(name=PARAM_MODEL_PATH, mandatory=true)
	private String modelPath;

	private MaxentTagger tagger;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		try {
			String url = resolveLocation(modelPath, this, getContext()).toString();
			tagger = new MaxentTagger(url, new CustomTaggerConfig("-model", url), false);
		}
		catch (ClassNotFoundException e) {
			throw new ResourceInitializationException(e);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		for (Sentence s : select(aJCas, Sentence.class)) {
			List<Token> tokens = selectCovered(Token.class, s);
			List<TaggedWord> words = new ArrayList<TaggedWord>(tokens.size());
			for (Token t : tokens) {
				words.add(new TaggedWord(t.getCoveredText()));
			}
			words = tagger.tagSentence(words);
			for (int i = 0; i < tokens.size(); i++) {
				Token t = tokens.get(i);
				TaggedWord tt = words.get(i);
				POS pos = new POS(aJCas, t.getBegin(), t.getEnd());
				pos.setPosValue(tt.tag());
				t.setPos(pos);
				pos.addToIndexes();
			}
		}
	}
}
