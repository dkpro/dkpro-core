/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.util.Level.INFO;

import java.io.InputStream;
import java.util.List;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.Tagset;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.opennlp.internal.OpenNlpChunkerTagsetDescriptionProvider;

/**
 * Chunk annotator using OpenNLP.
 */
@TypeCapability(
	    inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
		outputs = {
		    "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk" })
public class OpenNlpChunker
	extends JCasAnnotator_ImplBase
{
	/**
	 * Use this language instead of the document language to resolve the model.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	/**
	 * Override the default variant used to locate the model.
	 */
	public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
	@ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
	protected String variant;

	/**
	 * Load the model from this location instead of locating the model automatically.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

	/**
	 * Load the part-of-speech tag to UIMA type mapping from this location instead of locating
	 * the mapping automatically.
	 */
	public static final String PARAM_CHUNK_MAPPING_LOCATION = ComponentParameters.PARAM_CHUNK_MAPPING_LOCATION;
	@ConfigurationParameter(name = PARAM_CHUNK_MAPPING_LOCATION, mandatory = false)
	protected String chunkMappingLocation;

	/**
	 * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
	 * spamming the heap with thousands of strings representing only a few different tags.
	 *
	 * Default: {@code true}
	 */
	public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
	@ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
	private boolean internTags;

	/**
	 * Log the tag set(s) when a model is loaded.
	 *
	 * Default: {@code false}
	 */
	public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
	@ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue="false")
	protected boolean printTagSet;

	private CasConfigurableProviderBase<Chunker> modelProvider;
	private MappingProvider mappingProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		modelProvider = new ModelProviderBase<Chunker>() {
			{
                setContextObject(OpenNlpChunker.this);

                setDefault(ARTIFACT_ID, "${groupId}.opennlp-model-chunker-${language}-${variant}");
				setDefault(LOCATION, "classpath:/${package}/lib/chunker-${language}-${variant}.bin");
                setDefaultVariantsLocation("de/tudarmstadt/ukp/dkpro/core/opennlp/lib/chunker-default-variants.map");
				setDefault(VARIANT, "default");

				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected Chunker produceResource(InputStream aStream)
			    throws Exception
			{
			    ChunkerModel model = new ChunkerModel(aStream);

                Tagset tsdp = new OpenNlpChunkerTagsetDescriptionProvider(getResourceMetaData()
                        .getProperty("chunk.tagset"), Chunk.class, model.getChunkerModel());
                addTagset(tsdp);

				if (printTagSet) {
					getContext().getLogger().log(INFO, tsdp.toString());
				}

				return new ChunkerME(model);
			}
		};

		mappingProvider = new MappingProvider();
		mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
				"core/api/syntax/tagset/${language}-${chunk.tagset}-chunk.map");
		mappingProvider.setDefault(MappingProvider.BASE_TYPE, Chunk.class.getName());
		mappingProvider.setDefault("chunk.tagset", "default");
		mappingProvider.setOverride(MappingProvider.LOCATION, chunkMappingLocation);
		mappingProvider.setOverride(MappingProvider.LANGUAGE, language);
		mappingProvider.addImport("chunk.tagset", modelProvider);
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		CAS cas = aJCas.getCas();

		modelProvider.configure(cas);
		mappingProvider.configure(cas);
		
		Type chunkType = cas.getTypeSystem().getType(Chunk.class.getName());
		Feature chunkValue = chunkType.getFeatureByBaseName("chunkValue");

		BioDecoder decoder = new BioDecoder(cas, chunkValue);
		
		for (Sentence sentence : select(aJCas, Sentence.class)) {
			List<Token> tokens = selectCovered(aJCas, Token.class, sentence);
			String[] tokenTexts = new String[tokens.size()];
			String[] tokenTags = new String[tokens.size()];
			int i = 0;
			for (Token t : tokens) {
			    tokenTexts[i] = t.getCoveredText();
                if (t.getPos() == null || t.getPos().getPosValue() == null) {
                    throw new IllegalStateException("Every token must have a POS tag.");
                }
			    tokenTexts[i] = t.getPos().getPosValue();
                //System.out.printf("%s %s %n", t.getCoveredText(), t.getPos().getPosValue());
			    i++;
			}

			String[] chunkTags = modelProvider.getResource().chunk(tokenTexts, tokenTags);
			decoder.decode(tokens, chunkTags);
		}
	}
	
	private class BioDecoder
	{
	    private CAS cas;
	    private Feature chunkValue;
	    
        private String openChunk;
        private int start;
        private int end;

        public BioDecoder(CAS aCas, Feature aChunkValue)
        {
            super();
            cas = aCas;
            chunkValue = aChunkValue;
        }

        public void decode(List<Token> aTokens, String[] aChunkTags)
        {
            int i = 0;
            for (Token token : aTokens) {
                // System.out.printf("%s %s %n", token.getCoveredText(), aChunkTags[i]);
                String fields[] = aChunkTags[i].split("-");
                String flag = fields.length == 2 ? fields[0] : "NONE";
                String chunk = fields.length == 2 ? fields[1] : fields[0];
    
                // Start of a new hunk
                if (!chunk.equals(openChunk) || "B".equals(flag)) {
                    if (openChunk != null) {
                        // End of previous chunk
                        chunkComplete();
                    }
    
                    openChunk = chunk;
                    start = token.getBegin();
                }
    
                // Record how much of the chunk we have seen so far
                end = token.getEnd();
                
                i++;
            }
            
            // End of processing signal
            chunkComplete();
        }
        
        private void chunkComplete()
        {
            if (openChunk != null) {
                Type chunkType = mappingProvider.getTagType(openChunk);
                AnnotationFS chunk = cas.createAnnotation(chunkType, start, end);
                chunk.setStringValue(chunkValue, internTags ? openChunk.intern() :
                    openChunk);
                cas.addFsToIndexes(chunk);
                openChunk = null;
            }
        }
	}
}
