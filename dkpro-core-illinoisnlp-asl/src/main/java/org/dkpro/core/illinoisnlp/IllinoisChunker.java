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
package org.dkpro.core.illinoisnlp;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.metadata.SingletonTagset;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.illinoisnlp.internal.ConvertToIllinois;
import org.dkpro.core.illinoisnlp.internal.ConvertToUima;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.Chunker;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Wrapper for the Illinois chunker from the Cognitive Computation Group (CCG).
 */
@Component(OperationType.CHUNKER)
@ResourceMetaData(name = "Illinois CCG Chunker")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk" })
@LanguageCapability("en")
public class IllinoisChunker
    extends JCasAnnotator_ImplBase
{
    /**
     * Log the tag set(s) when a model is loaded.
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;
    
//    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
//    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
//    private String modelLocation;

    /**
     * Use this language instead of the document language.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

//    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
//    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
//    private String variant;
    
    /**
     * Load the chunk tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_CHUNK_MAPPING_LOCATION = 
            ComponentParameters.PARAM_CHUNK_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CHUNK_MAPPING_LOCATION, mandatory = false, defaultValue = "classpath:/org/dkpro/core/api/syntax/tagset/en-conll2000-chunk.map")
    protected String chunkMappingLocation;

    private ModelProviderBase<Annotator> modelProvider;

    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        modelProvider = new ModelProviderBase<Annotator>() {
            {
                setContextObject(IllinoisChunker.this);
                setDefault(LOCATION, NOT_REQUIRED);
            }

            @Override
            protected ChunkerAnnotator produceResource(URL aUrl) throws IOException
            {
                if (!"en".equals(getAggregatedProperties().getProperty(LANGUAGE))) {
                    throw new IllegalArgumentException("Only language [en] is supported");
                }
                
                ChunkerAnnotator annotator = new ChunkerAnnotator();

                SingletonTagset tags = new SingletonTagset(Chunk.class, "conll2000");

                try {
                    Chunker chunker = (Chunker) FieldUtils.readField(annotator, "tagger", true);
                    for (int i = 0; i < chunker.getLabelLexicon().size(); i++) {
                        String tag = chunker.getLabelLexicon().lookupKey(i).getStringValue();
                        // Strip BIO encoding from tagset
                        if (tag.length() > 1) {
                            tag = tag.substring(2);
                            tags.add(tag);
                        }
                    }
                }
                catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
                
                addTagset(tags);

                if (printTagSet) {
                    getLogger().info(getTagset().toString());
                }
                
                return annotator;
            }
        };
        
//        mappingProvider = MappingProviderFactory.createChunkMappingProvider(chunkMappingLocation,
//                language, modelProvider);
        
        mappingProvider = new MappingProvider();
        mappingProvider.setDefault(MappingProvider.LOCATION, chunkMappingLocation);
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, Chunk.class.getName());
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();

        modelProvider.configure(cas);
        mappingProvider.configure(cas);

        ConvertToIllinois converter = new ConvertToIllinois();
        TextAnnotation document = converter.convert(aJCas);

        // Run tagger
        try {
            modelProvider.getResource().getView(document);
        }
        catch (AnnotatorException e) {
            throw new IllegalStateException(e);
        }

        for (Sentence s : select(aJCas, Sentence.class)) {
            // Get tokens from CAS
            List<Token> casTokens = selectCovered(aJCas, Token.class, s);

            ConvertToUima.convertChunks(aJCas, casTokens, document, mappingProvider);
        }
    }
}
