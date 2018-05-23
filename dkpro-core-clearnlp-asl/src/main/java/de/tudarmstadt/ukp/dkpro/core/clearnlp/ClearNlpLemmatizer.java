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
package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.InputStream;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.component.morph.DefaultMPAnalyzer;
import com.clearnlp.component.morph.EnglishMPAnalyzer;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Lemmatizer using Clear NLP.
 */
@Component(OperationType.LEMMATIZER)
@ResourceMetaData(name = "ClearNLP Lemmatizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" },
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" }
)
public class ClearNlpLemmatizer
    extends JCasAnnotator_ImplBase
{

    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false, defaultValue = "en")
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

    private CasConfigurableProviderBase<AbstractComponent> modelProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<AbstractComponent>(this, "clearnlp", "lemma")
        {
            @Override
            protected AbstractComponent produceResource(InputStream aStream)
                throws Exception
            {
                String lang = getAggregatedProperties().getProperty(LANGUAGE);
                AbstractComponent lemmatizer;
                if (lang.equals("en")) {
                    lemmatizer = new EnglishMPAnalyzer(aStream);
                }
                else {
                    lemmatizer = new DefaultMPAnalyzer();
                }
                return lemmatizer;
            }
        };
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {

        modelProvider.configure(aJCas.getCas());
        AbstractComponent analyzer = modelProvider.getResource();

        // Iterate over all sentences
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<Token> tokens = selectCovered(aJCas, Token.class, sentence);

            DEPTree tree = new DEPTree();

            // Generate input format required by analyzer
            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);
                DEPNode node = new DEPNode(i + 1, tokens.get(i).getText());
                node.pos = t.getPos().getPosValue();
                tree.add(node);
            }

            analyzer.process(tree);

            int i = 0;
            for (Token t : tokens) {
                DEPNode node = tree.get(i + 1);
                String lemmaString = node.lemma;
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
