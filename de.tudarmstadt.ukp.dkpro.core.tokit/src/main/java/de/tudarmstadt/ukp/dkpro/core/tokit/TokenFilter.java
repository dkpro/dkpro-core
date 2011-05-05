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

import static org.uimafit.util.JCasUtil.iterate;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Filters tokens that do not conform to certain conditions.
 *
 * @author Torsten Zesch, Richard Eckart de Castilho
 */
public class TokenFilter
	extends JCasAnnotator_ImplBase
{
    
	/** 
	 * Certain components e.g. TreeTagger cannot handle very long tokens.
	 * Thus, we filter any tokens longer than MAX_TOKEN_LENGTH.
	 * 
	 * Any value less than 1 will not remove any tokens, but leave the tokens untouched.
	 */
	public static final String PARAM_MAX_TOKEN_LENGTH = "MaxTokenLengthFilterLongTokens";
	@ConfigurationParameter(name=PARAM_MAX_TOKEN_LENGTH, mandatory=true, defaultValue="0")
	private int maxTokenLength;

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
	    if (maxTokenLength < 1) {
	        return;
	    }
	    
		Collection<Token> toRemove = new ArrayList<Token>();
		for (Token t : iterate(aJCas, Token.class)) {
			if (t.getCoveredText().length() > maxTokenLength) {
			    toRemove.add(t);
			}
		}
		for (Token t : toRemove) {
			t.removeFromIndexes();
		}
	}
}