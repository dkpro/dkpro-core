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

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.illinoisnlp.internal.ConvertToIllinois;
import org.dkpro.core.illinoisnlp.internal.ConvertToUima;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Wrapper for the Illinois named entity recognizer from the Cognitive Computation Group (CCG).
 */
@Component(OperationType.NAMED_ENTITITY_RECOGNIZER)
@ResourceMetaData(name = "Illinois CCG Named Entity Recognizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
public class IllinoisNamedEntityRecognizer
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

//    /**
//     * Variant of a model the model. Used to address a specific model if here are multiple models
//     * for one language.
//     */
//    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
//    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = true, defaultValue="person")
    protected String variant = "conll";

//    /**
//     * Location from which the model is read.
//     */
//    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
//    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
//    protected String modelLocation;

//    /**
//     * Location of the mapping file for named entity tags to UIMA types.
//     */
//    public static final String PARAM_NAMED_ENTITY_MAPPING_LOCATION = 
//        ComponentParameters.PARAM_NAMED_ENTITY_MAPPING_LOCATION;
//    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_MAPPING_LOCATION, mandatory = false)
    protected String mappingLocation;

    private ModelProviderBase<Annotator> modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<Annotator>() {
            {
                setContextObject(IllinoisNamedEntityRecognizer.this);
                setDefault(LOCATION, NOT_REQUIRED);
            }

            @Override
            protected Annotator produceResource(URL aURL)
                throws IOException
            {
                if (!"en".equals(getAggregatedProperties().getProperty(LANGUAGE))) {
                    throw new IllegalArgumentException("Only language [en] is supported");
                }
                
                NERAnnotator annotator = new NERAnnotator(new ResourceManager(new Properties()),
                        ViewNames.NER_CONLL);

//                SingletonTagset tags = new SingletonTagset(NamedEntity.class, "ptb");
//
//                try {
//                    TrainedPOSTagger trainedTagger = (TrainedPOSTagger) FieldUtils
//                            .readField(annotator, "tagger", true);
//                    Learner known = (POSTaggerKnown) FieldUtils.readField(trainedTagger, "known",
//                            true);
//                    for (int i = 0; i < known.getLabelLexicon().size(); i++) {
//                        tags.add(known.getLabelLexicon().lookupKey(i).getStringValue());
//                    }
//
//                    Learner unknown = (POSTaggerUnknown) FieldUtils.readField(trainedTagger,
//                            "unknown", true);
//                    for (int i = 0; i < unknown.getLabelLexicon().size(); i++) {
//                        tags.add(unknown.getLabelLexicon().lookupKey(i).getStringValue());
//                    }
//                }
//                catch (IllegalAccessException e) {
//                    throw new IllegalStateException(e);
//                }
//                
//                addTagset(tags);
//
//                if (printTagSet) {
//                    getContext().getLogger().log(INFO, getTagset().toString());
//                }
                
                return annotator;
            }
        };

        mappingProvider = new MappingProvider();
        mappingProvider
                .setDefaultVariantsLocation("de/tudarmstadt/ukp/dkpro/core/lbj/lib/ner-default-variants.map");
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/"
                + "core/lbj/lib/ner-${language}-${variant}.map");
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

        ConvertToIllinois converter = new ConvertToIllinois();
        TextAnnotation document = converter.convert(aJCas);

        // Run tagger
        try {
            modelProvider.getResource().getView(document);
        }
        catch (AnnotatorException e) {
            throw new IllegalStateException(e);
        }
        
        ConvertToUima.convertNamedEntity(aJCas, document, mappingProvider);
    }
}
