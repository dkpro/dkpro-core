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
package de.tudarmstadt.ukp.dkpro.core.treetagger;

import static org.uimafit.util.CasUtil.iterate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.annolab.tt4j.ModelResolver;
import org.annolab.tt4j.TokenAdapter;
import org.annolab.tt4j.TokenHandler;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public
class TreeTaggerPosLemmaTT4J
extends TreeTaggerTT4JBase<AnnotationFS>
{
	public static final String PARAM_TYPE_ADAPTER = "TypeAdapter";
	@ConfigurationParameter(name=PARAM_TYPE_ADAPTER, mandatory=false)
	private String typeAdapterClass;

	@Override
	public
	void process(
			final CAS aCas)
	throws AnalysisEngineProcessException
	{
		getContext().getLogger().log(Level.FINE, "Running TreeTagger annotator");
		try {
			final String language;

	        // If language override is not active && the document language is
			// "x-unspecified" which means that is has not been set at all
			// then we should throw an exception, as we do not know what language to
			// use. Using a default language in this case, could lead to very
			// confusing results for the user that are hard to track down.
	        if (languageCode != null) {
	        	language = languageCode;
	        }
	        else if (
	        		aCas.getDocumentLanguage() != null &&
	        		!aCas.getDocumentLanguage().equals("x-unspecified")
	        ) {
	        	language = aCas.getDocumentLanguage();
	        }
	        else {
	            throw new AnalysisEngineProcessException(new Throwable(
	            		"Neither the LanguageCode parameter nor the document " +
	            		"language is set. Do not know what language to use. " +
	            		"Exiting."));
	        }

			final TypeSystem ts = aCas.getTypeSystem();
			final Type tokenType = ts.getType(Token.class.getName());
			final Type lemmaTag = ts.getType(Lemma.class.getName());
			final Type tokenTag = ts.getType(Token.class.getName());
			final Feature lemmaValue = lemmaTag.getFeatureByBaseName("value");
			final Feature featLemma = tokenTag.getFeatureByBaseName("lemma");
			final Feature featPos = tokenTag.getFeatureByBaseName("pos");
			List<AnnotationFS> tokens = new ArrayList<AnnotationFS>();
			for (AnnotationFS fs : iterate(aCas, tokenType)) {
				tokens.add(fs);
			}
			final AnnotationFS pos[] = new AnnotationFS[tokens.size()];
			final AnnotationFS lemma[] = new AnnotationFS[tokens.size()];

			// Set the handler creating new UIMA annotations from the analyzed
			// tokens
			final AtomicInteger count = new AtomicInteger(0);
        	treetagger.setModel(language);
			treetagger.setHandler(new TokenHandler<AnnotationFS>() {
				@Override
				public
				void token(
						AnnotationFS aToken,
						String aPos,
						String aLemma)
				{
					synchronized (aCas) {
						// Add the Part of Speech
						Type posTag = getTagType((DKProModel) treetagger.getModel(), aPos, ts);
						AnnotationFS posAnno = aCas.createAnnotation(
								posTag, aToken.getBegin(), aToken.getEnd());
						posAnno.setStringValue(posTag.getFeatureByBaseName("PosValue"), aPos);
						pos[count.get()] = posAnno;

						// Add the lemma
						AnnotationFS lemmaAnno = aCas.createAnnotation(
								lemmaTag, aToken.getBegin(), aToken.getEnd());
						if (aLemma != null) {
							lemmaAnno.setStringValue(lemmaValue, aLemma);
						}
						else {
							lemmaAnno.setStringValue(lemmaValue, aToken.getCoveredText());
						}
						lemma[count.get()] = lemmaAnno;

						aToken.setFeatureValue(featPos, posAnno);
						aToken.setFeatureValue(featLemma, lemmaAnno);

						count.getAndIncrement();
					}
				}
			});

			treetagger.process(tokens);

			getContext().getLogger().log(Level.FINE, treetagger.getStatus());
			getContext().getLogger().log(Level.FINE, "Parsed " + count.get() + " pos segments");

			// Add the annotations to the indexes
			for (int i = 0; i < count.get(); i++) {
				if (pos[i] != null) {
					aCas.addFsToIndexes(pos[i]);
				}
				if (lemma[i] != null) {
					aCas.addFsToIndexes(lemma[i]);
				}
			}
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	protected
	ModelResolver getModelResolver()
	{
		return new PosModelResolver();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected TokenAdapter<AnnotationFS> getAdapter()
		throws ResourceInitializationException
	{
		try {
	    	if (typeAdapterClass == null) {
	    		typeAdapterClass = DKProTokenAdapter.class.getName();
	    	}

			return (TokenAdapter<AnnotationFS>) Class.forName(typeAdapterClass).newInstance();
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	public static class PosModelResolver
		extends DKProModelResolver
	{
		@Override
		protected String getType()
		{
			return "tagger";
		}
	}

	public static class DKProTokenAdapter
		implements TokenAdapter<AnnotationFS>
	{
		@Override
		public
		String getText(
				AnnotationFS aObject)
		{
			synchronized (aObject.getCAS()) {
				return aObject.getCoveredText();
			}
		}
	}
}
