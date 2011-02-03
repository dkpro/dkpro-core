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
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

/**
 * @author Richard Eckart de Castilho
 */
public
class TreeTaggerChunkerTT4J
extends TreeTaggerTT4JBase<AnnotationFS>
{
	/**
	 * This component requires a type system in which all Part-of-Speech tag can be reached in-order
	 * by iterating over a particular type. The name of the type can be set here.
	 */
	public static final String PARAM_POS_BASE_TYPE = "PartOfSpeechBaseType";
	@ConfigurationParameter(name=PARAM_POS_BASE_TYPE, mandatory=false)
	private String posBaseType;

	public static final String PARAM_TYPE_ADAPTER = "TypeAdapter";
	@ConfigurationParameter(name=PARAM_TYPE_ADAPTER, mandatory=false)
	private String typeAdapterClass;

    @Override
    public
    void initialize(
    		UimaContext context)
    throws ResourceInitializationException
    {
    	super.initialize(context);

    	if (posBaseType == null) {
    		posBaseType = POS.class.getName();
    	}

    	treetagger.setEpsilon(0.00000001);
    	treetagger.setHyphenHeuristics(true);
    }

    @Override
    public void destroy()
    {
    	typeAdapterClass = null;
    	posBaseType = null;
    	super.destroy();
    }

	@Override
	public
	void process(
			final CAS aCas)
	throws AnalysisEngineProcessException
	{
		getContext().getLogger().log(Level.INFO, "Running TreeTagger annotator");
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

			// Set the handler creating new UIMA annotations from the analyzed
			// tokens
			final AtomicInteger count = new AtomicInteger(0);
        	treetagger.setModel(language);
        	final TokenHandler<AnnotationFS> handler = new TokenHandler<AnnotationFS>() {
				private String openChunk;
				private int start;
				private int end;

				@Override
				public
				void token(
						AnnotationFS aPOS,
						String aChunk,
						String aDummy)
				{
					synchronized (aCas) {
						if (aChunk == null) {
							// End of processing signal
							chunkComplete();
							return;
						}

						String fields1[] = aChunk.split("/");
						String fields2[] = fields1[1].split("-");
						//String tag = fields1[0];
						String flag = fields2.length == 2 ? fields2[0] : "NONE";
						String chunk = fields2.length == 2 ? fields2[1] : fields2[0];

						// Start of a new hunk
						if (!chunk.equals(openChunk) || "B".equals(flag)) {
							if (openChunk != null) {
								// End of previous chunk
								chunkComplete();
							}

							openChunk = chunk;
							start = aPOS.getBegin();
						}

						// Record how much of the chunk we have seen so far
						end = aPOS.getEnd();
					}
				}

				private
				void chunkComplete()
				{
					if (openChunk != null) {
						Type chunkType = getTagType((DKProModel) treetagger.getModel(), openChunk,
								aCas.getTypeSystem());
						AnnotationFS chunk = aCas.createAnnotation(chunkType, start, end);
						Feature feat = chunkType.getFeatureByBaseName("chunkValue");
						if (feat != null) {
							chunk.setStringValue(feat, openChunk);
						}
						aCas.addFsToIndexes(chunk);

						count.getAndIncrement();
						openChunk = null;
					}
				}
			};
			treetagger.setHandler(handler);

			Type tokenType = aCas.getTypeSystem().getType(posBaseType);
			List<AnnotationFS> tokens = new ArrayList<AnnotationFS>();
			for (AnnotationFS fs : iterate(aCas, tokenType)) {
				tokens.add(fs);
			}

			treetagger.process(tokens);
			// Commit the final chunk
			handler.token(null, null, null);

			getContext().getLogger().log(Level.INFO, "Parsed " + count.get() + " chunks");
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected TokenAdapter<AnnotationFS> getAdapter()
		throws ResourceInitializationException
	{
		try {
	    	if (typeAdapterClass == null) {
	    		typeAdapterClass = DKProPOSTokenAdapter.class.getName();
	    	}

			return (TokenAdapter<AnnotationFS>) Class.forName(typeAdapterClass).newInstance();
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	protected
	ModelResolver getModelResolver()
	{
		return new ChunkerModelResolver();
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	public class ChunkerModelResolver
		extends DKProModelResolver
	{
		@Override
		protected String getType()
		{
			return "chunker";
		}
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	public static class DKProPOSTokenAdapter
		implements TokenAdapter<AnnotationFS>
	{
		@Override
		public
		String getText(
				AnnotationFS aObject)
		{
			synchronized (aObject.getCAS()) {
				Type t = aObject.getCAS().getTypeSystem().getType(POS.class.getName());
				String pos = aObject.getFeatureValueAsString(t.getFeatureByBaseName("PosValue"));
				return aObject.getCoveredText()+"-"+pos;
			}
		}
	}
}
