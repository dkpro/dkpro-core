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
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;

/**
 * Detect grammatical errors in text using LanguageTool a rule based grammar checker.
 *
 * @author Zhi Shen
 */
@TypeCapability(
	    outputs = {
		    "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly" })
public class LanguageToolChecker
	extends JCasAnnotator_ImplBase
{
	/**
	 * Use this language instead of the document language to resolve the model.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	private String language;

	private ModelProviderBase<JLanguageTool> modelProvider;
	
	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		
		modelProvider = new ModelProviderBase<JLanguageTool>() {
		    {
                setContextObject(LanguageToolChecker.this);
                setDefault(LOCATION, NOT_REQUIRED);
                
                setOverride(LANGUAGE, language);
		    }
		    
		    @Override
		    protected JLanguageTool produceResource(URL aUrl)
		        throws IOException
		    {
                Properties props = getAggregatedProperties();
		        Language lang = Language.getLanguageForShortName(props.getProperty(LANGUAGE));
		        if (lang == null) {
		            throw new IOException("The language code '"
		                    + props.getProperty(LANGUAGE) + "' is not supported by LanguageTool.");
		        }
		        JLanguageTool langTool = new JLanguageTool(lang);
	            langTool.activateDefaultPatternRules();
	            return langTool;
		    }
		};
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
	    modelProvider.configure(aJCas.getCas());
	    
		// get document text
		String docText = aJCas.getDocumentText();

		try {
			List<RuleMatch> matches = modelProvider.getResource().check(docText);
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
