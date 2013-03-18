/*******************************************************************************
 * Copyright 2010
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

package de.tudarmstadt.ukp.dkpro.core.tokit;

import static org.uimafit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Remove prefixes and suffixes from tokens.
 *
 * @author Richard Eckart de Castilho
 */
public class TokenTrimmer
	extends JCasAnnotator_ImplBase
{
	/**
	 * List of prefixes to remove. 
	 */
	public static final String PARAM_PREFIXES = "prefixes";
	@ConfigurationParameter(name=PARAM_PREFIXES, mandatory=true)
	private String[] prefixes;

	/**
	 * List of suffixes to remove. 
	 */
	public static final String PARAM_SUFFIXES = "suffixes";
	@ConfigurationParameter(name=PARAM_SUFFIXES, mandatory=true)
	private String[] suffixes;

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		Collection<Token> toRemove = new ArrayList<Token>();
		for (Token t : select(aJCas, Token.class)) {
			String text = t.getCoveredText();
			for (String prefix : prefixes) {
				if (text.startsWith(prefix)) {
					t.setBegin(t.getBegin()+prefix.length());
					break;
				}
			}

			text = t.getCoveredText();
			for (String suffix : suffixes) {
				if (text.endsWith(suffix)) {
					t.setEnd(t.getEnd()-suffix.length());
					break;
				}
			}

			if (t.getCoveredText().length() == 0) {
				toRemove.add(t);
			}
		}
		for (Token t : toRemove) {
			t.removeFromIndexes();
		}
	}
}
