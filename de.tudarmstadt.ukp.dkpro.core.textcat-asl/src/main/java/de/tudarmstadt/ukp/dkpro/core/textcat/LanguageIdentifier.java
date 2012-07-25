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
package de.tudarmstadt.ukp.dkpro.core.textcat;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.knallgrau.utils.textcat.TextCategorizer;

/**
 * Identifies the language of a document.
 */
public class LanguageIdentifier
	extends JCasAnnotator_ImplBase
{
	private static final Map<String, String> langName2ISO = new HashMap<String, String>();
	static {
		langName2ISO.put("german", "de");
		langName2ISO.put("english", "en");
		langName2ISO.put("french", "fr");
		langName2ISO.put("spanish", "es");
		langName2ISO.put("italian", "it");
		langName2ISO.put("swedish", "sv");
		langName2ISO.put("polish", "pl");
		langName2ISO.put("dutch", "nl");
		langName2ISO.put("norwegian", "no");
		langName2ISO.put("finnish", "fi");
		langName2ISO.put("albanian", "sq");
		langName2ISO.put("slovakian", "sk");
		langName2ISO.put("slovenian", "sl");
		langName2ISO.put("danish", "da");
		langName2ISO.put("hungarian", "hu");
	}

	private final TextCategorizer categorizer = new TextCategorizer();

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		String docText = aJCas.getDocumentText();
		if (docText != null) {
			String result = categorizer.categorize(docText);
			aJCas.setDocumentLanguage(langName2ISO.get(result));
		}
	}
}
