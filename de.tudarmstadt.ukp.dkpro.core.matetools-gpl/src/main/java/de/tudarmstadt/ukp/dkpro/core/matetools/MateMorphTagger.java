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

import is2.mtag.Main;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


/**
 * <p>
 * DKPro Annotator for the MateToolsMorphTagger
 * </p>
 * 
 * Required annotations:<br/>
 * <ul>
 * <li>Sentence</li>
 * <li>Token</li>
 * <li>Lemma</li>
 * </ul>
 * 
 * Generated annotations:<br/>
 * <ul>
 * <li>MorphTag</li>
 * </ul>
 * 
 * 
 * @author AnNa, zesch
 */
public class MateMorphTagger extends JCasAnnotator_ImplBase {
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    public static final String PARAM_VARIANT = "variant";
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;

    public static final String PARAM_MAPPING_LOCATION = "mappingLocation";
    @ConfigurationParameter(name = PARAM_MAPPING_LOCATION, mandatory = false)
    protected String mappingLocation;

    private CasConfigurableProviderBase<Main> modelProvider;
            
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new CasConfigurableProviderBase<Main>() {
            {
                setDefault(VERSION, "20120626.0");
                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(ARTIFACT_ID,
                        "de.tudarmstadt.ukp.dkpro.core-nonfree-model-morphtagger-${language}-${variant}");
                
                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/matetools/lib/" +
                          "morphtagger-${language}-${variant}.model");
                
                setDefault(VARIANT, "default");
                                
                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }
            
            @Override
            protected Main produceResource(URL aUrl) throws IOException
            {
                java.io.File modelFile = ResourceUtils.getUrlAsFile(aUrl, true);
                
                String[] args = {"-model", modelFile.getPath()};
                is2.mtag.Options option = new is2.mtag.Options(args);
                return new is2.mtag.Main(option);  // create a MorphTagger
            }
        };
     
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {
        CAS cas = jcas.getCas();

        modelProvider.configure(cas);
                       
        try {
    	    for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
    	        List<Token> tokens = JCasUtil.selectCovered(Token.class, sentence);
    	        List<Lemma> lemmas = JCasUtil.selectCovered(Lemma.class, sentence);
    	        
    	        String[] morphTags = modelProvider.getResource().out(
                        JCasUtil.toText(tokens).toArray(new String[0]), 
                        JCasUtil.toText(lemmas).toArray(new String[0])).pfeats;
    	        
    	        for (int i = 0; i < morphTags.length; i++) {
    	            Token token = tokens.get(i);
    	            Morpheme morpheme = new Morpheme(jcas, token.getBegin(), token.getEnd());
    	            morpheme.setMorphTag(morphTags[i]);
    	            morpheme.addToIndexes();
    	        }
    	    }
       } catch (Exception e) {
           throw new AnalysisEngineProcessException(e);
       }
    }
}