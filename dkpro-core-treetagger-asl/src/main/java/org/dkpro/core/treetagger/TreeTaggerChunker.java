/*
 * Copyright 2017
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
 */
package org.dkpro.core.treetagger;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.util.Level.INFO;
import static org.dkpro.core.api.resources.MappingProviderFactory.createChunkMappingProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.annolab.tt4j.DefaultModel;
import org.annolab.tt4j.TokenAdapter;
import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerModelUtil;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.metadata.SingletonTagset;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.api.resources.ResourceUtils;
import org.dkpro.core.treetagger.internal.DKProExecutableResolver;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Chunk annotator using TreeTagger.
 */
@Component(OperationType.CHUNKER)
@ResourceMetaData(name = "TreeTagger Chunker")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk" })
public class TreeTaggerChunker
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
     * Use this TreeTagger executable instead of trying to locate the executable automatically.
     */
    public static final String PARAM_EXECUTABLE_PATH = "executablePath";
    @ConfigurationParameter(name = PARAM_EXECUTABLE_PATH, mandatory = false)
    private File executablePath;
    
    /**
     * URI of the model artifact. This can be used to override the default model resolving 
     * mechanism and directly address a particular model.
     * 
     * <p>The URI format is {@code mvn:${groupId}:${artifactId}:${version}}. Remember to set
     * the variant parameter to match the artifact. If the artifact contains the model in
     * a non-default location, you  also have to specify the model location parameter, e.g.
     * {@code classpath:/model/path/in/artifact/model.bin}.</p>
     */
    public static final String PARAM_MODEL_ARTIFACT_URI = 
            ComponentParameters.PARAM_MODEL_ARTIFACT_URI;
    @ConfigurationParameter(name = PARAM_MODEL_ARTIFACT_URI, mandatory = false)
    protected String modelArtifactUri;
    
    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;

    /**
     * The character encoding used by the model.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = false)
    protected String modelEncoding;

    /**
     * Enable/disable type mapping.
     */
    public static final String PARAM_MAPPING_ENABLED = ComponentParameters.PARAM_MAPPING_ENABLED;
    @ConfigurationParameter(name = PARAM_MAPPING_ENABLED, mandatory = true, defaultValue = 
            ComponentParameters.DEFAULT_MAPPING_ENABLED)
    protected boolean mappingEnabled;

    /**
     * Location of the mapping file for chunk tags to UIMA types.
     */
    public static final String PARAM_CHUNK_MAPPING_LOCATION = 
            ComponentParameters.PARAM_CHUNK_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CHUNK_MAPPING_LOCATION, mandatory = false)
    protected String chunkMappingLocation;

    /**
     * Log the tag set(s) when a model is loaded.
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

    /**
     * TT4J setting: Disable some sanity checks, e.g. whether tokens contain line breaks (which is
     * not allowed). Turning this on will increase your performance, but the wrapper may throw
     * exceptions if illegal data is provided.
     */
    public static final String PARAM_PERFORMANCE_MODE = "performanceMode";
    @ConfigurationParameter(name = PARAM_PERFORMANCE_MODE, mandatory = true, defaultValue = "false")
    private boolean performanceMode;
    
    /**
     * A sequence to flush the internal TreeTagger buffer and to force it to output the rest of the
     * completed analysis. This is typically just a sequence of like 5-10 full stops (".") separated
     * by new line characters. However, some models may require a different flush sequence, e.g. a
     * short sentence in the respective language. For chunker models, mind that the sentence must
     * also be POS tagged, e.g. {@code Nous-PRO:PER\n...}.
     */
    public static final String PARAM_FLUSH_SEQUENCE = "flushSequence";
    @ConfigurationParameter(name = PARAM_FLUSH_SEQUENCE, mandatory = false)
    private String flushSequence;
    
    private CasConfigurableProviderBase<TreeTaggerWrapper<Token>> modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<TreeTaggerWrapper<Token>>() {
            private TreeTaggerWrapper<Token> treetagger;
            
            {
                setContextObject(TreeTaggerChunker.this);

                setDefault(ARTIFACT_ID, "${groupId}.treetagger-model-chunker-${language}-${variant}");
                setDefault(LOCATION,
                        "classpath:/de/tudarmstadt/ukp/dkpro/core/treetagger/lib/chunker-${language}-${variant}.properties");
                setDefault(VARIANT, "le"); // le = little-endian

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
                
                treetagger = new TreeTaggerWrapper<Token>();
                treetagger.setPerformanceMode(performanceMode);
                treetagger.setEpsilon(0.00000001);
                treetagger.setHyphenHeuristics(true);
                DKProExecutableResolver executableProvider = new DKProExecutableResolver(
                        treetagger);
                executableProvider.setExecutablePath(executablePath);
                treetagger.setExecutableProvider(executableProvider);
            }

            @Override
            protected TreeTaggerWrapper<Token> produceResource(URL aUrl)
                throws IOException
            {
                Properties meta = getResourceMetaData();
                String encoding = modelEncoding != null ? modelEncoding : meta
                        .getProperty("encoding");
                String tagset = meta.getProperty("chunk.tagset");
                String flush = meta.getProperty("flushSequence",
                        DefaultModel.DEFAULT_FLUSH_SEQUENCE);
                if (flushSequence != null) {
                    flush = flushSequence;
                }
                
                File modelFile = ResourceUtils.getUrlAsFile(aUrl, true);
                
                DefaultModel model = new DefaultModel(modelFile.getPath() + ":" + encoding,
                        modelFile, encoding, flush);
                
                // Reconfigure tagger
                treetagger.setModel(model);
                treetagger.setAdapter(new MappingTokenAdapter(meta));
                
                // Get tagset
                List<String> tags = TreeTaggerModelUtil.getTagset(modelFile, encoding);
                SingletonTagset chunkTags = new SingletonTagset(Chunk.class, tagset);
                for (String tag : tags) {
                    String[] fields1 = tag.split("/");
                    String[] fields2 = fields1[1].split("-");
                    String chunkTag = fields2.length == 2 ? fields2[1] : fields2[0];
                    chunkTags.add(chunkTag);
                }
                addTagset(chunkTags);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, getTagset().toString());
                }

                return treetagger;
            }
        };

        mappingProvider = createChunkMappingProvider(this, chunkMappingLocation, language,
                modelProvider);
    }

    @Override
    public void process(final JCas aJCas)
        throws AnalysisEngineProcessException
    {
        final CAS cas = aJCas.getCas();

        modelProvider.configure(cas);
        mappingProvider.configure(cas);

        // Set the handler creating new UIMA annotations from the analyzed tokens
        final TokenHandler<Token> handler = new TokenHandler<Token>()
        {
            private String openChunk;
            private int start;
            private int end;

            @Override
            public void token(Token aToken, String aChunk, String aDummy)
            {
                synchronized (cas) {
                    if (aChunk == null) {
                        // End of processing signal
                        chunkComplete();
                        return;
                    }

                    String[] fields1 = aChunk.split("/");
                    String[] fields2 = fields1[1].split("-");
                    //String tag = fields1[0];
                    String flag = fields2.length == 2 ? fields2[0] : "NONE";
                    String chunk = fields2.length == 2 ? fields2[1] : fields2[0];

                    // Start of a new chunk
                    if (!chunk.equals(openChunk) || "B".equals(flag)) {
                        if (openChunk != null) {
                            // End of previous chunk
                            chunkComplete();
                        }

                        openChunk = chunk;
                        start = aToken.getBegin();
                    }

                    // Record how much of the chunk we have seen so far
                    end = aToken.getEnd();
                }
            }

            private void chunkComplete()
            {
                if (openChunk != null) {
                    Type chunkType = mappingProvider.getTagType(openChunk);
                    Chunk chunk = (Chunk) cas.createAnnotation(chunkType, start, end);
                    chunk.setChunkValue(openChunk.intern());
                    cas.addFsToIndexes(chunk);
                    openChunk = null;
                }
            }
        };

        try {
            TreeTaggerWrapper<Token> treetagger = modelProvider.getResource();
            treetagger.setHandler(handler);
            
            // Issue #636 - process each sentence individually to ensure that sentence boundaries
            // are respected
            for (Sentence sentence : select(aJCas, Sentence.class)) {
                List<Token> posTags = new ArrayList<Token>(selectCovered(Token.class, sentence));
                treetagger.process(posTags);
                
                // Commit the final chunk
                handler.token(null, null, null);
            }
        }
        catch (TreeTaggerException e) {
            throw new AnalysisEngineProcessException(e);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }        
    }
    
    private static class MappingTokenAdapter implements TokenAdapter<Token>
    {
        private Map<String, String> mapping;

        public MappingTokenAdapter(Properties aMetadata)
        {
            mapping = new HashMap<String, String>();
            
            for (Entry<Object, Object> e : aMetadata.entrySet()) {
                String key = String.valueOf(e.getKey());
                if (key.startsWith("pos.tag.map.")) {
                    String old = key.substring("pos.tag.map.".length());
                    String rep = String.valueOf(e.getValue());
                    mapping.put(old, rep);
                }
            }
        }
        
        @Override
        public String getText(Token aToken)
        {
            synchronized (aToken.getCAS()) {
                String pos = mapping.get(aToken.getPosValue());
                if (pos == null) {
                    pos = aToken.getPosValue();
                }
                
                return aToken.getText() + "-" + pos;
            }
        }
    }
}
