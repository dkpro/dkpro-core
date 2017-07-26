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
package de.tudarmstadt.ukp.dkpro.core.nlp4j;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import edu.emory.mathcs.nlp.component.tokenizer.EnglishTokenizer;
import edu.emory.mathcs.nlp.component.tokenizer.Tokenizer;
import edu.emory.mathcs.nlp.component.tokenizer.token.Token;

/**
 * Segmenter using Emory NLP4J.
 */
@ResourceMetaData(name="NLP4J Segmenter")
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class Nlp4JSegmenter
    extends SegmenterBase
{
    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    private CasConfigurableProviderBase<Tokenizer> modelProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<Tokenizer>()
        {
            {
                setContextObject(Nlp4JSegmenter.this);
                setDefault(LOCATION, NOT_REQUIRED + "-${language}");
                setOverride(LANGUAGE, language);
            }

            @Override
            protected Tokenizer produceResource(URL aUrl)
                throws IOException
            {
                String language = getAggregatedProperties().getProperty(LANGUAGE);
                
                if (!language.equals("en")) {
                    throw new IllegalArgumentException(new Throwable(
                            "Emory NLP4J supports only English"));
                }
                
                return new EnglishTokenizer();
            }
        };
    }

    @Override
    protected void process(JCas aJCas, String aText, int aZoneBegin)
        throws AnalysisEngineProcessException
    {
        modelProvider.configure(aJCas.getCas());
        Tokenizer segmenter = modelProvider.getResource();

        List<List<Token>> sentences = segmenter.segmentize(aText);

        for (List<Token> sentence : sentences) {
            // Tokens actually start only at index 1 - the 0 index is some odd "@#r$%"
            for (Token token : sentence) {
                createToken(aJCas, aZoneBegin + token.getStartOffset(),
                        aZoneBegin + token.getEndOffset());
            }
            
            int sentBegin = aZoneBegin + sentence.get(0).getStartOffset();
            int sentEnd = aZoneBegin + sentence.get(sentence.size() - 1).getEndOffset();
            
            createSentence(aJCas, sentBegin, sentEnd);
        }
    }
}
