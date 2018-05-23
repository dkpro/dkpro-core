/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.lbj;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.util.Level.INFO;

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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.lbj.internal.ConvertToIllinois;
import de.tudarmstadt.ukp.dkpro.core.lbj.internal.ConvertToUima;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.illinois.cs.cogcomp.pos.lbjava.POSTagger;
import edu.illinois.cs.cogcomp.pos.lbjava.POSTaggerKnown;
import edu.illinois.cs.cogcomp.pos.lbjava.POSTaggerUnknown;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Wrapper for the Illinois POS-tagger from the Cognitive Computation Group (CCG).
 */
@Component(OperationType.PART_OF_SPEECH_TAGGER)
@ResourceMetaData(name = "Illinois CCG POS-Tagger")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability("en")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
       outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"})
public class IllinoisPosTagger
    extends JCasAnnotator_ImplBase
{
    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spaming the heap with thousands of strings representing only a few different tags.
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
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = 
            ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false, 
            defaultValue = "classpath:/de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/en-lbj-pos.map")
    private String posMappingLocation;

    private ModelProviderBase<Annotator> modelProvider;

    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        modelProvider = new ModelProviderBase<Annotator>() {
            {
                setContextObject(IllinoisPosTagger.this);
                setDefault(LOCATION, NOT_REQUIRED);
            }

            @Override
            protected POSAnnotator produceResource(URL aUrl) throws IOException
            {
                if (!"en".equals(getAggregatedProperties().getProperty(LANGUAGE))) {
                    throw new IllegalArgumentException("Only language [en] is supported");
                }
                
                POSAnnotator annotator = new POSAnnotator(false);

                SingletonTagset tags = new SingletonTagset(POS.class, "ptb");

                try {
                    POSTagger trainedTagger = (POSTagger) FieldUtils
                            .readField(annotator, "tagger", true);
                    Learner known = (POSTaggerKnown) FieldUtils.readField(trainedTagger, "taggerKnown",
                            true);
                    for (int i = 0; i < known.getLabelLexicon().size(); i++) {
                        tags.add(known.getLabelLexicon().lookupKey(i).getStringValue());
                    }

                    Learner unknown = (POSTaggerUnknown) FieldUtils.readField(trainedTagger,
                            "taggerUnknown", true);
                    for (int i = 0; i < unknown.getLabelLexicon().size(); i++) {
                        tags.add(unknown.getLabelLexicon().lookupKey(i).getStringValue());
                    }
                }
                catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
                
                addTagset(tags);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, getTagset().toString());
                }
                
                return annotator;
            }
        };
        
//        mappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
//                language, taggerProvider);

        mappingProvider = new MappingProvider();
        mappingProvider.setDefault(MappingProvider.LOCATION, posMappingLocation);
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
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
            
            ConvertToUima.convertPOSs(aJCas, casTokens, document, mappingProvider, internTags);
        }
    }
}
