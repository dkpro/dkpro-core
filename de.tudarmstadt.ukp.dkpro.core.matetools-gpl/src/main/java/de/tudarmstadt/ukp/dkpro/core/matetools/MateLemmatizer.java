/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.matetools;

import is2.data.SentenceData09;
import is2.io.CONLLReader09;
import is2.lemmatizer.Lemmatizer;
import is2.lemmatizer.Options;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * <p>
 * DKPro Annotator for the MateToolsLemmatizer
 * </p>
 * 
 * Required annotations:<br/>
 * <ul>
 * <li>Token</li>
 * <li>Sentence</li>
 * </ul>
 * 
 * Generated annotations:<br/>
 * <ul>
 * <li>Lemma</li>
 * </ul>
 * 
 * 
 * @author AnNa
 * @author zesch
 */
public class MateLemmatizer
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
    public static final String PARAM_VARIANT = "variant";
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;

    private CasConfigurableProviderBase<Lemmatizer> modelProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<Lemmatizer>()
        {
            {
                setContextObject(MateLemmatizer.this);

                setDefault(ARTIFACT_ID,
                        "${groupId}.matetools-model-lemmatizer-${language}-${variant}");
                setDefault(LOCATION,
                        "classpath:/${package}/lib/lemmatizer-${language}-${variant}.properties");
                setDefaultVariantsLocation("${package}/lib/lemmatizer-default-variants.map");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected Lemmatizer produceResource(URL aUrl)
                throws IOException
            {
                File modelFile = ResourceUtils.getUrlAsFile(aUrl, true);

                String[] args = { "-model", modelFile.getPath() };
                Options option = new Options(args);
                return new Lemmatizer(option); // create a lemmatizer
            }
        };
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        CAS cas = jcas.getCas();

        modelProvider.configure(cas);

        for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
            List<Token> tokens = JCasUtil.selectCovered(Token.class, sentence);

            List<String> forms = new LinkedList<String>();
            forms.add(CONLLReader09.ROOT);
            forms.addAll(JCasUtil.toText(tokens));

            SentenceData09 sd = new SentenceData09();
            sd.init(forms.toArray(new String[0]));
            String[] lemmas = modelProvider.getResource().apply(sd).plemmas;

            for (int i = 0; i < lemmas.length; i++) {
                Token token = tokens.get(i);
                Lemma lemma = new Lemma(jcas, token.getBegin(), token.getEnd());
                lemma.setValue(lemmas[i]);
                lemma.addToIndexes();
                token.setLemma(lemma);
            }
        }
    }
}