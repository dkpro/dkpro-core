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
package org.dkpro.core.ixa;

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.apache.uima.util.Level.INFO;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.ixa.internal.IxaLemmatizerTagsetDescriptionProvider;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import eus.ixa.ixa.pipe.lemma.LemmatizerME;
import eus.ixa.ixa.pipe.lemma.LemmatizerModel;

/**
 * Lemmatizer using the OpenNLP-based Ixa implementation.
 */
@Component(OperationType.LEMMATIZER)
@ResourceMetaData(name = "IXA Lemmatizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" }, 
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class IxaLemmatizer
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
     * Log the tag set(s) when a model is loaded.
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

    private CasConfigurableProviderBase<LemmatizerME> modelProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<LemmatizerME>(this, "lemmatizer")
        {
            {
                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(LOCATION,
                        "classpath:/de/tudarmstadt/ukp/dkpro/core/ixa/lib/lemmatizer-${language}-${variant}.properties");
            }
            
            @Override
            protected LemmatizerME produceResource(InputStream aStream)
                throws Exception
            {
                LemmatizerModel model = new LemmatizerModel(aStream);

                // Extract tagset information from the model
                IxaLemmatizerTagsetDescriptionProvider tsdp = 
                        new IxaLemmatizerTagsetDescriptionProvider(
                                getResourceMetaData().getProperty("pos.tagset"), POS.class,
                                model.getLemmatizerSequenceModel(), "t0");
                addTagset(tsdp, false);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, tsdp.toString());
                }
                
                return new LemmatizerME(model);
            }

        };
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        modelProvider.configure(aJCas.getCas());
        LemmatizerME analyzer = modelProvider.getResource();

        // Iterate over all sentences
        Map<Sentence, Collection<Token>> index = indexCovered(aJCas, Sentence.class, Token.class);
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            Collection<Token> tokens = index.get(sentence);
            String[] tokenTexts = toText(tokens).toArray(new String[tokens.size()]);
            String[] tokenPos = tokens.stream()
                    .map(t -> { return t.getPos().getPosValue(); })
                    .toArray(s -> { return new String[tokens.size()]; });
            
            String[] encodedLemmas = analyzer.lemmatize(tokenTexts, tokenPos);
            String[] lemmas = analyzer.decodeLemmas(tokenTexts, encodedLemmas);
            
            int i = 0;
            for (Token t : tokens) {
                String lemmaString = lemmas[i];
                if (lemmaString == null) {
                    lemmaString = t.getText();
                }
                Lemma l = new Lemma(aJCas, t.getBegin(), t.getEnd());
                l.setValue(lemmaString);
                l.addToIndexes();

                t.setLemma(l);
                i++;
            }
        }
    }
}
