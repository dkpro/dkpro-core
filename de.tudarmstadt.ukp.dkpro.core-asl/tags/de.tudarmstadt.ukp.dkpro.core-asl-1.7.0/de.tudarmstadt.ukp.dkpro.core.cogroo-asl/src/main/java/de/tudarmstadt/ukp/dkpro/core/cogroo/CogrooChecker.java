/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.cogroo;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cogroo.analyzer.ComponentFactory;
import org.cogroo.checker.CheckDocument;
import org.cogroo.checker.GrammarChecker;
import org.cogroo.entities.Mistake;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;

/**
 * Detect grammatical errors in text using CoGrOO.
 */
@TypeCapability(
	    outputs = {
		    "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly" })
public class CogrooChecker
	extends JCasAnnotator_ImplBase
{
    public static enum DetailLevel {
        SHORT, LONG, FULL
    }
    
	/**
	 * Use this language instead of the document language to resolve the model.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	private String language;

	/**
	 * Set detail level.
	 */
	public static final String PARAM_DETAIL_LEVEL = "detailLevel";
    @ConfigurationParameter(name = PARAM_DETAIL_LEVEL, mandatory = true, defaultValue="SHORT")
	private DetailLevel detailLevel;
	
	private ModelProviderBase<GrammarChecker> modelProvider;
	
	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		
		modelProvider = new ModelProviderBase<GrammarChecker>() {
		    {
                setContextObject(CogrooChecker.this);
                setDefault(LOCATION, NOT_REQUIRED);
                
                setOverride(LANGUAGE, language);
		    }
		    
		    @Override
		    protected GrammarChecker produceResource(URL aUrl)
		        throws IOException
		    {
                Properties props = getAggregatedProperties();
                if (!"pt".equals(props.getProperty(LANGUAGE))) {
                    throw new IOException("The language code '"
                            + props.getProperty(LANGUAGE) + "' is not supported by LanguageTool.");
                }
                
                ComponentFactory factory = ComponentFactory.create(new Locale("pt", "BR"));
                return new GrammarChecker(factory.createPipe());
		    }
		};
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
	    modelProvider.configure(aJCas.getCas());
	    
		// get document text
	    CheckDocument document = new CheckDocument(aJCas.getDocumentText());

		modelProvider.getResource().analyze(document);
		for (Mistake match : document.getMistakes()) {
			// create annotation
			GrammarAnomaly annotation = new GrammarAnomaly(aJCas);
			annotation.setBegin(match.getStart());
			annotation.setEnd(match.getEnd());
			switch (detailLevel) {
			case SHORT:
	            annotation.setDescription(match.getShortMessage());
	            break;
			case LONG:
	            annotation.setDescription(match.getLongMessage());
                break;
			case FULL:
	            annotation.setDescription(match.getFullMessage());
                break;
			}
			annotation.addToIndexes();
			if (getLogger().isTraceEnabled()) {
			    getLogger().trace("Found: " + annotation);
			}
		}
	}
}
