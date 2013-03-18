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

import static org.uimafit.util.CasUtil.select;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.annolab.tt4j.ModelResolver;
import org.annolab.tt4j.TokenAdapter;
import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * @author Richard Eckart de Castilho
 */
public class TreeTaggerPosLemmaTT4J
	extends TreeTaggerTT4JBase<AnnotationFS>
{
	/**
	 * Location of the mapping file for part-of-speech tags to UIMA types.
	 */
	public static final String PARAM_TAGGER_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
	@ConfigurationParameter(name = PARAM_TAGGER_MAPPING_LOCATION, mandatory = false)
	protected String posMappingLocation;
	
	public static final String PARAM_TYPE_ADAPTER = "TypeAdapter";
	@ConfigurationParameter(name=PARAM_TYPE_ADAPTER, mandatory=false)
	private String typeAdapterClass;

	/**
	 * Write part-of-speech information.
	 * 
	 * Default: {@code true}
	 */
	public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
	@ConfigurationParameter(name=PARAM_WRITE_POS, mandatory=true, defaultValue="true")
	private boolean writePos;

	/**
	 * Write lemma information.
	 * 
	 * Default: {@code true}
	 */
	public static final String PARAM_WRITE_LEMMA = ComponentParameters.PARAM_WRITE_LEMMA;
	@ConfigurationParameter(name=PARAM_WRITE_LEMMA, mandatory=true, defaultValue="true")
	private boolean writeLemma;

	private Type tokenType;
	private Type lemmaType;
	private Feature lemmaValue;
	private Feature featLemma;
	private Feature featPos;
	
	private MappingProvider taggerMappingProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		
		taggerMappingProvider = new MappingProvider();
		taggerMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
				"core/api/lexmorph/tagset/${language}-${tagger.tagset}-tagger.map");
		taggerMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
		taggerMappingProvider.setDefault("tagger.tagset", "default");
		taggerMappingProvider.setOverride(MappingProvider.LOCATION, posMappingLocation);
		taggerMappingProvider.setOverride(MappingProvider.LANGUAGE, languageCode);
		taggerMappingProvider.addImport("tagger.tagset", treetagger);
	}
	
	@Override
	public void typeSystemInit(TypeSystem aTypeSystem)
		throws AnalysisEngineProcessException
	{
		super.typeSystemInit(aTypeSystem);
		
		tokenType = aTypeSystem.getType(Token.class.getName());
		lemmaType = aTypeSystem.getType(Lemma.class.getName());
		lemmaValue = lemmaType.getFeatureByBaseName("value");
		featLemma = tokenType.getFeatureByBaseName("lemma");
		featPos = tokenType.getFeatureByBaseName("pos");
	}

	@Override
	public void process(final CAS aCas)
		throws AnalysisEngineProcessException
	{
		getLogger().debug("Running TreeTagger POS tagger and lemmatizer");
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

			List<AnnotationFS> tokens = new ArrayList<AnnotationFS>();
			for (AnnotationFS fs : select(aCas, tokenType)) {
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
				public void token(AnnotationFS aToken, String aPos, String aLemma)
				{
					synchronized (aCas) {
						TypeSystem ts = aCas.getTypeSystem();
						// Add the Part of Speech
						if (writePos && aPos != null) {
							Type posType = taggerMappingProvider.getTagType(aPos);
							AnnotationFS posAnno = aCas.createAnnotation(
									posType, aToken.getBegin(), aToken.getEnd());
							posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"),
									isInternStrings() ? aPos.intern() : aPos);
							pos[count.get()] = posAnno;
							aToken.setFeatureValue(featPos, posAnno);
						}

						// Add the lemma
						if (writeLemma && aLemma != null) {
							AnnotationFS lemmaAnno = aCas.createAnnotation(
									lemmaType, aToken.getBegin(), aToken.getEnd());
							lemmaAnno.setStringValue(lemmaValue,
									isInternStrings() ? aLemma.intern() : aLemma);
							lemma[count.get()] = lemmaAnno;
							aToken.setFeatureValue(featLemma, lemmaAnno);
						}

						count.getAndIncrement();
					}
				}
			});

			// Must be done after configuring the TreeTagger since we import from it
			taggerMappingProvider.configure(aCas);

			treetagger.process(tokens);

			Logger log = getContext().getLogger();
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, treetagger.getStatus());
				log.log(Level.FINE, "Parsed " + count.get() + " pos segments");
			}

			// Add the annotations to the indexes
			for (int i = 0; i < count.get(); i++) {
				if (pos[i] != null) {
					aCas.addFsToIndexes(pos[i]);
				}
				if (lemma[i] != null) {
					aCas.addFsToIndexes(lemma[i]);
				}
			}
		}
		catch (TreeTaggerException e) {
			throw new AnalysisEngineProcessException(e);
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	protected ModelResolver getModelResolver()
	{
		return new PosModelResolver(modelPath, modelEncoding);
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

	/**
	 * @author Richard Eckart de Castilho
	 */
	private class PosModelResolver
		extends DKProModelResolver
	{
		public PosModelResolver()
		{
			super(null, null);
		}

		public PosModelResolver(File aModelPath, String aModelEncoding)
		{
			super(aModelPath, aModelEncoding);
		}

		@Override
		protected String getType()
		{
			return "tagger";
		}
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	public static class DKProTokenAdapter
		implements TokenAdapter<AnnotationFS>
	{
		@Override
		public String getText(AnnotationFS aObject)
		{
			synchronized (aObject.getCAS()) {
				return aObject.getCoveredText();
			}
		}
	}
}
