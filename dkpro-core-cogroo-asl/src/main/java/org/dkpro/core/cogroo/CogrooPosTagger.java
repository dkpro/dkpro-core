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
package org.dkpro.core.cogroo;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.dkpro.core.api.parameter.ComponentParameters.DEFAULT_MAPPING_ENABLED;
import static org.dkpro.core.api.resources.MappingProviderFactory.createPosMappingProvider;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cogroo.analyzer.Analyzer;
import org.cogroo.analyzer.ComponentFactory;
import org.cogroo.text.Document;
import org.cogroo.text.impl.DocumentImpl;
import org.cogroo.text.impl.SentenceImpl;
import org.cogroo.text.impl.TokenImpl;
import org.dkpro.core.api.lexmorph.pos.POSUtils;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.ModelProviderBase;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * POS-tagger using CoGrOO.
 */
@Component(OperationType.PART_OF_SPEECH_TAGGER)
@ResourceMetaData(name = "CoGrOO POS-Tagger")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability("pt")
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class CogrooPosTagger
    extends JCasAnnotator_ImplBase
{
    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Enable/disable type mapping.
     */
    public static final String PARAM_MAPPING_ENABLED = ComponentParameters.PARAM_MAPPING_ENABLED;
    @ConfigurationParameter(name = PARAM_MAPPING_ENABLED, defaultValue = DEFAULT_MAPPING_ENABLED)
    protected boolean mappingEnabled;

    /**
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating the
     * mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    private CasConfigurableProviderBase<Analyzer> modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<Analyzer>()
        {
            {
                setContextObject(CogrooPosTagger.this);

                setDefault(LOCATION, NOT_REQUIRED);
                setOverride(LANGUAGE, language);
            }

            @Override
            protected Analyzer produceResource(URL aUrl) throws IOException
            {
                Properties props = getAggregatedProperties();

                String language = props.getProperty(LANGUAGE);

                if (!"pt".equals(language)) {
                    throw new IOException("The language code '" + language
                            + "' is not supported by LanguageTool.");
                }

                ComponentFactory factory = ComponentFactory.create(new Locale("pt", "BR"));
                return factory.createPOSTagger();
            }
        };

        mappingProvider = createPosMappingProvider(this, posMappingLocation, "bosque", language);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();
        modelProvider.configure(cas);
        mappingProvider.configure(cas);

        // This is actually quite some overhead, because internally Cogroo is just using a
        // Morphlogik dictionary which simply takes a token and pos tag and returnes a list of
        // lemmata. It would be much more efficient to use the dictionary directly.

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            // We set up one CoGrOO document for each sentence. That makes it easier to maintain
            // a list of tokens of the sentence, which we later need to attached the lemmata to the
            // tokens.

            // Construct the document
            Document doc = new DocumentImpl();
            doc.setText(aJCas.getDocumentText());

            // Extract the sentence and its tokens
            org.cogroo.text.Sentence cSent = new SentenceImpl(sentence.getBegin(),
                    sentence.getEnd(), doc);
            List<org.cogroo.text.Token> cTokens = new ArrayList<org.cogroo.text.Token>();
            List<Token> dTokens = selectCovered(Token.class, sentence);
            for (Token dTok : dTokens) {
                TokenImpl cTok = new TokenImpl(dTok.getBegin() - sentence.getBegin(),
                        dTok.getEnd() - sentence.getBegin(), dTok.getText());
                cTokens.add(cTok);
            }
            cSent.setTokens(cTokens);
            doc.setSentences(asList(cSent));

            // Process
            modelProvider.getResource().analyze(doc);

            assert cSent.getTokens().size() == dTokens.size();

            // Convert from CoGrOO to UIMA model
            Iterator<Token> dTokIt = dTokens.iterator();
            for (org.cogroo.text.Token cTok : cSent.getTokens()) {
                // CoGrOO allows storing multiple lemmas per token. DKPro Core only allows one lemma
                // per token. We just take the first one here. If we would run the grammar
                // checking based on the DKPro Core lemmata, we might miss certain errors for this
                // reason.
                Token dTok = dTokIt.next();

                Type posTag = mappingProvider.getTagType(cTok.getPOSTag());
                POS posAnno = (POS) cas.createAnnotation(posTag, cSent.getStart() + cTok.getStart(),
                        cSent.getStart() + cTok.getEnd());
                String tag = cTok.getPOSTag();
                posAnno.setPosValue(tag != null ? tag.intern() : null);
                POSUtils.assignCoarseValue(posAnno);
                posAnno.addToIndexes();
                dTok.setPos(posAnno);
            }
        }
    }
}
