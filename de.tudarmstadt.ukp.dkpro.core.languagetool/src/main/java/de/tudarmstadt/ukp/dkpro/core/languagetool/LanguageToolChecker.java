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
package de.tudarmstadt.ukp.dkpro.core.languagetool;

import java.io.IOException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly;

/**
 * Detect grammatical errors in text using LanguageTool a rule based grammar checker.
 *
 * @author Zhi Shen
 */
public class LanguageToolChecker
	extends JCasAnnotator_ImplBase
{

	public static final String PARAM_LANGUAGE_CODE = "LanguageCode";
	@ConfigurationParameter(name = PARAM_LANGUAGE_CODE, mandatory = true, defaultValue = "en")
	private String languageCode;
	private Language language;

	private boolean languageOverride = false;
	private JLanguageTool langTool;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		languageOverride = true;
		language = Language.getLanguageForShortName(languageCode);
		if (language == null) {
			throw new ResourceInitializationException(new Throwable("The language code '"
					+ languageCode + "' is not supported by LanguageTool."));
		}
		try {
			langTool = new JLanguageTool(language);
			langTool.activateDefaultPatternRules();
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		if (!languageOverride && aJCas.getDocumentLanguage().equals("x-unspecified")) {
			throw new AnalysisEngineProcessException(new Throwable(
					"Neither the LanguageCode parameter " + "nor the document language is set. "
							+ "Do not know what language to use. Exiting."));
		}

		if (!languageOverride && !languageCode.equals(aJCas.getDocumentLanguage())) {
			language = Language.getLanguageForShortName(aJCas.getDocumentLanguage());
			if (language == null) {
				throw new AnalysisEngineProcessException(new Throwable("The language code '"
						+ languageCode + "' is not supported by LanguageTool."));
			}
			languageCode = aJCas.getDocumentLanguage();
			try {
				langTool = new JLanguageTool(language);
				langTool.activateDefaultPatternRules();
			}
			catch (IOException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}

		// get document text
		String docText = aJCas.getDocumentText();

		try {
			List<RuleMatch> matches = langTool.check(docText);
			for (RuleMatch match : matches) {
				// create annotation
				GrammarAnomaly annotation = new GrammarAnomaly(aJCas);
				annotation.setBegin(match.getFromPos());
				annotation.setEnd(match.getToPos());
				annotation.setDescription(match.getMessage());
				annotation.addToIndexes();
				getContext().getLogger().log(Level.FINEST, "Found: " + annotation);
			}
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}
