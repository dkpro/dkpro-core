/*
 * Copyright 2007-2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.lingpipe;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.apache.uima.util.Level.INFO;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
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

import com.aliasi.chunk.AbstractCharLmRescoringChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.HmmChunker;
import com.aliasi.chunk.TokenShapeChunker;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.symbol.SymbolTable;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ResourceParameter;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * LingPipe named entity recognizer.
 */
@Component(OperationType.NAMED_ENTITITY_RECOGNIZER)
@ResourceMetaData(name = "LingPipe Named Entity Recognizer")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
public class LingPipeNamedEntityRecognizer
    extends JCasAnnotator_ImplBase
{
    /**
     * Log the tag set(s) when a model is loaded.
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Variant of a model the model. Used to address a specific model if here are multiple models
     * for one language.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * URI of the model artifact. This can be used to override the default model resolving 
     * mechanism and directly address a particular model.
     */
    public static final String PARAM_MODEL_ARTIFACT_URI = 
            ComponentParameters.PARAM_MODEL_ARTIFACT_URI;
    @ConfigurationParameter(name = PARAM_MODEL_ARTIFACT_URI, mandatory = false)
    protected String modelArtifactUri;
    
    /**
     * Location from which the model is read.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    @ResourceParameter(MimeTypes.APPLICATION_X_LINGPIPE_NER)
    protected String modelLocation;

    /**
     * Location of the mapping file for named entity tags to UIMA types.
     */
    public static final String PARAM_NAMED_ENTITY_MAPPING_LOCATION = 
            ComponentParameters.PARAM_NAMED_ENTITY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_MAPPING_LOCATION, mandatory = false)
    protected String mappingLocation;

    private ModelProviderBase<Chunker> modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<Chunker>(this, "lingpipe", "ner")
        {
            @Override
            protected Chunker produceResource(InputStream aStream)
                throws Exception
            {
                ObjectInputStream ois = new ObjectInputStream(aStream);
                Chunker chunker = (Chunker) ois.readObject();

                System.out.println(chunker.getClass());

                SingletonTagset tags = new SingletonTagset(NamedEntity.class, null);
                if (chunker instanceof HmmChunker) {
                    HiddenMarkovModel hmm = ((HmmChunker) chunker).getDecoder().getHmm();
                    
                    List<String> prefixes = asList("B_", "M_", "E_", "W_", "BB_O_", "EE_O_",
                            "WW_O_");
                    
                    for (int n = 0; n < hmm.stateSymbolTable().numSymbols(); n++) {
                        String tag = hmm.stateSymbolTable().idToSymbol(n);

                        if (prefixes.contains(StringUtils.substring(tag, 0, 5))) {
                            tag = tag.substring(5);
                        }
                        else if (prefixes.contains(StringUtils.substring(tag, 0, 2))) {
                            tag = tag.substring(2);
                        }
                        if ("BOS".equals(tag) || "MM_O".equals(tag)) {
                            // BOS is reserved by the system
                            continue;
                        }
                        
                        tags.add(tag);
                    }
                }
                else if (chunker instanceof TokenShapeChunker) {
                    Object decoder = FieldUtils.readField(chunker, "mDecoder", true);
                    Object estimator = FieldUtils.readField(decoder, "mEstimator", true);
                    SymbolTable tagTable = (SymbolTable) FieldUtils.readField(estimator,
                            "mTagSymbolTable", true);
                    for (int n = 0; n < tagTable.numSymbols(); n++) {
                        String tag = tagTable.idToSymbol(n);
                        // Handle BIO encoding
                        if (tag.startsWith("B-") || tag.startsWith("I-")) {
                            tag = tag.substring(2);
                        }
                        if ("O".equals(tag)) {
                            continue;
                        }
                        tags.add(tag);
                    }
                }
                else if (chunker instanceof AbstractCharLmRescoringChunker) {
                    @SuppressWarnings("unchecked")
                    Map<String, Character> typeToChar = (Map<String, Character>) FieldUtils
                            .readField(chunker, "mTypeToChar", true);
                    for (String tag : typeToChar.keySet()) {
                        tags.add(tag);
                    }
                }
                addTagset(tags);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, tags.toString());
                }

                return chunker;
            }
        };

        mappingProvider = new MappingProvider();
        mappingProvider.setDefaultVariantsLocation(
                "de/tudarmstadt/ukp/dkpro/core/lingpipe/lib/ner-default-variants.map");
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/"
                + "core/lingpipe/lib/ner-${language}-${variant}.map");
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, NamedEntity.class.getName());
        mappingProvider.setOverride(MappingProvider.LOCATION, mappingLocation);
        mappingProvider.setOverride(MappingProvider.LANGUAGE, language);
        mappingProvider.setOverride(MappingProvider.VARIANT, variant);
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();
        modelProvider.configure(cas);
        mappingProvider.configure(cas);

        // get the document text
        List<Token> tokenList = new ArrayList<Token>(select(aJCas, Token.class));
        String[] tokens = toText(tokenList).toArray(new String[tokenList.size()]);

        Chunking chunking = modelProvider.getResource().chunk(cas.getDocumentText());
        
        // get the named entities and their character offsets
        for (Chunk namedEntity : chunking.chunkSet()) {
            Type type = mappingProvider.getTagType(namedEntity.type());
            NamedEntity neAnno = (NamedEntity) cas.createAnnotation(type, namedEntity.start(),
                    namedEntity.end());
            neAnno.setValue(namedEntity.type());
            neAnno.addToIndexes();
        }
    }
}
