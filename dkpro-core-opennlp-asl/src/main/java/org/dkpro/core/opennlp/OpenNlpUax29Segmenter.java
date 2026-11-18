/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.opennlp;

import static org.dkpro.core.api.resources.ResourceObjectProviderBase.LANGUAGE;

import java.io.InputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.parameter.ResourceParameter;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.CasConfigurableStreamProviderBase;
import org.dkpro.core.api.segmentation.SegmenterBase;

import eu.openminted.share.annotations.api.DocumentationResource;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.uax29.WordSegmenter;

/**
 * Tokenizer using the rule-based OpenNLP implementation of the Unicode Text Segmentation algorithm
 * (<a href="https://www.unicode.org/reports/tr29/">UAX #29</a>) word boundaries.
 * <p>
 * Tokenization does not require a model and works for any language. Every word boundary segment
 * that is not only whitespace becomes a token, including punctuation.
 * </p>
 * <p>
 * UAX #29 sentence boundaries are not implemented by OpenNLP. Sentences are therefore detected
 * using an OpenNLP sentence model (same models as used by {@link OpenNlpSegmenter}). If no sentence
 * model is available for the document language, disable {@link #PARAM_WRITE_SENTENCE} and use a
 * different component to detect sentences.
 * </p>
 */
@ResourceMetaData(name = "OpenNLP UAX #29 Segmenter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class OpenNlpUax29Segmenter
    extends SegmenterBase
{
    /**
     * Override the default variant used to locate the sentence model.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * Load the sentence model from this location instead of locating the model automatically.
     */
    public static final String PARAM_SEGMENTATION_MODEL_LOCATION = //
            ComponentParameters.PARAM_SEGMENTATION_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_SEGMENTATION_MODEL_LOCATION, mandatory = false)
    @ResourceParameter(MimeTypes.APPLICATION_X_OPENNLP_SENT)
    protected String segmentationModelLocation;

    private CasConfigurableProviderBase<SentenceDetectorME> sentenceModelProvider;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        sentenceModelProvider = new CasConfigurableStreamProviderBase<SentenceDetectorME>()
        {
            {
                setContextObject(OpenNlpUax29Segmenter.this);

                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(ARTIFACT_ID,
                        "de.tudarmstadt.ukp.dkpro.core.opennlp-model-sentence-${language}-${variant}");

                setDefault(LOCATION,
                        "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/sentence-${language}-${variant}.properties");
                setDefault(VARIANT, "maxent");

                setOverride(LOCATION, segmentationModelLocation);
                setOverride(VARIANT, variant);
            }

            @Override
            protected SentenceDetectorME produceResource(InputStream aStream) throws Exception
            {
                return new SentenceDetectorME(new SentenceModel(aStream));
            }
        };
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        if (isWriteSentence()) {
            // Use the language parameter of the base class if set, otherwise the document language
            sentenceModelProvider.setOverride(LANGUAGE, getLanguage(aJCas));
            sentenceModelProvider.configure(aJCas.getCas());
        }

        super.process(aJCas);
    }

    @Override
    protected void process(JCas aJCas, String aText, int aZoneBegin)
        throws AnalysisEngineProcessException
    {
        if (isWriteSentence()) {
            for (var span : sentenceModelProvider.getResource().sentPosDetect(aText)) {
                createSentence(aJCas, span.getStart() + aZoneBegin, span.getEnd() + aZoneBegin);
            }
        }

        if (isWriteToken()) {
            // UAX #29 word boundaries do not depend on sentence boundaries, so we can segment
            // the whole zone at once. SegmenterBase.createToken() trims the segments and does
            // not create tokens for whitespace-only segments.
            WordSegmenter.forEachSegment(aText,
                    (aBegin, aEnd) -> createToken(aJCas, aBegin + aZoneBegin, aEnd + aZoneBegin));
        }
    }
}
