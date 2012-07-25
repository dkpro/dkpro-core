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
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Split up existing tokens again if they are camel-case text.
 *
 * @author Richard Eckart de Castilho
 */
public class CamelCaseTokenSegmenter
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_DELETE_COVER = "DeleteCover";
	@ConfigurationParameter(name=PARAM_DELETE_COVER, mandatory=true, defaultValue="true")
	private boolean deleteCover;

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		List<Token> toAdd = new ArrayList<Token>();
		List<Token> toRemove = new ArrayList<Token>();

		for (Token t : select(aJCas, Token.class)) {
			if ((t.getEnd() - t.getBegin()) < 2) {
				continue;
			}

			String text = t.getCoveredText();
			int offset = t.getBegin();
			int start = 0;
			boolean seenLower = Character.isLowerCase(text.charAt(0));
			for (int i = 1; i < text.length(); i++) {
				// Upper-case means a new token is starting if we are at a lower-case/upper-case
				// boundary. This allows us to properly treat "GetFileUploadURLRequest"
				boolean nextIsLower = i + 1 < text.length()
						&& Character.isLowerCase(text.charAt(i + 1));
				if (Character.isUpperCase(text.charAt(i)) && (seenLower || nextIsLower)) {
					toAdd.add(new Token(aJCas, offset + start, offset + i));
					start = i;
				}
				seenLower = Character.isLowerCase(text.charAt(i));
			}

			// If we would just create the same token again, better do nothing
			if (start == 0) {
				continue;
			}

			// The rest goes into the final token
			toAdd.add(new Token(aJCas, offset + start, offset + text.length()));

			if (deleteCover) {
				toRemove.add(t);
			}
		}

		for (Token t : toAdd) {
			t.addToIndexes();
		}

		for (Token t : toRemove) {
			t.removeFromIndexes();
		}
	}
}
