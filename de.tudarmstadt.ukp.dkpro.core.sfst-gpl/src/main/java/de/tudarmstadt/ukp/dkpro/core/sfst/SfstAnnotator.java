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
package de.tudarmstadt.ukp.dkpro.core.sfst;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.sfst.parser.AnalysisParser;
import de.tudarmstadt.ukp.dkpro.core.sfst.parser.ParsedAnalysis;
import de.tudarmstadt.ukp.dkpro.core.sfst.parser.SimpleParser;
import de.tudarmstadt.ukp.dkpro.core.sfst.parser.TagType;
import de.tudarmstadt.ukp.dkpro.core.sfst.parser.TurkishAnalysisParser;

/**
 * UIMA wrapper for morphological analyzer based on SFST.
 * 
 * (Binaries and models are distributed under GPL licence.
 *  Run the Ant build script to download and install artifacts locally.)
 *  
 * Writes Lemma, POS, and Morpheme annotations.
 * 
 * 
 * @author zesch
 * @author eckart
 * @author flekova
 * 
 */

public class SfstAnnotator
    extends JCasAnnotator_ImplBase
{
    /**
     * Write part-of-speech information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
    @ConfigurationParameter(name=PARAM_WRITE_POS, mandatory=true, defaultValue="true")
    private boolean writePos;

    /**
     * Write lemma information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_WRITE_LEMMA = ComponentParameters.PARAM_WRITE_LEMMA;
    @ConfigurationParameter(name=PARAM_WRITE_LEMMA, mandatory=true, defaultValue="true")
    private boolean writeLemma;

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
	 * Load the model from this location instead of locating the model automatically.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

    private CasConfigurableProviderBase<File> modelProvider;
    private CasConfigurableProviderBase<AnalysisParser> parserProvider;
    private RuntimeProvider runtimeProvider;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        // Returns instance of AnalysisParser based on the language. Currently available for Turkish
        // only.
        parserProvider = new CasConfigurableProviderBase<AnalysisParser>()
        {
            {
                setContextObject(SfstAnnotator.this);
                
                setDefault(LOCATION, NOT_REQUIRED);
                setOverride(LANGUAGE, language);
            }

            @Override
            protected AnalysisParser produceResource(URL aUrl)
                throws IOException
            {
                AnalysisParser ap;
                Properties props = getAggregatedProperties();
                String language = props.getProperty(PARAM_LANGUAGE);

                if (language.equals("tr")) {
                    ap = new TurkishAnalysisParser();
                }
                else {
                    ap = new SimpleParser();
                }
                 
                return ap;
            }
        };

        // Returns FST automaton for specified language, which is then passed to fst-infl from SFST.
        // Currently available for Turkish and German.
        modelProvider = new CasConfigurableProviderBase<File>()
        {
            {
                setContextObject(SfstAnnotator.this);
                
                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/sfst/lib/"
                                + "morph-${language}-${variant}.a");
                setDefault(VARIANT, "default");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected File produceResource(URL aUrl)
                throws IOException
            {
                return ResourceUtils.getUrlAsFile(aUrl, true);
            }
        };

        // provider for the sfst binary
        runtimeProvider = new RuntimeProvider("classpath:/de/tudarmstadt/ukp/dkpro/core/sfst/bin/");
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {

        // set language of the analysis
        modelProvider.configure(jcas.getCas());
        parserProvider.configure(jcas.getCas());

        File model = modelProvider.getResource();
        File executable;

        try {
            executable = runtimeProvider.getFile("fst-infl");
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

        try {
            for (Token token : JCasUtil.select(jcas, Token.class)) {
                List<String> results = runTRMorph(executable, model, token.getCoveredText());

                AnalysisParser parser = parserProvider.getResource();
                
                if (results.size() > 0) {
                    
                    // TODO currently, we just use the first analysis 
                    //      as we have no way to decide which analyis is the correct one
                    ParsedAnalysis parse = parser.parse(results.get(0));
                    
                    // TODO: concert parse into more fine-grained morph tags
                    Morpheme morpheme = new Morpheme(jcas, token.getBegin(), token.getEnd());
                    morpheme.setMorphTag(parse.getRaw());
                    morpheme.addToIndexes();
                    
                    if (writeLemma && parse.getLemma() != null) {
                        Lemma lemma = new Lemma(jcas, token.getBegin(), token.getEnd());
                        lemma.setValue(parse.getLemma());
                        lemma.addToIndexes();
                    }
                    
                    // TODO add more fine-grained POS tags                    
                    if (writePos && parse.getTag(TagType.POS) != null) {
                        POS pos = new POS(jcas, token.getBegin(), token.getEnd());
                        pos.setPosValue(parse.getTag(TagType.POS).name()); 
                        pos.addToIndexes();
                    }
                }
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void destroy()
    {
        runtimeProvider.uninstall();
        super.destroy();
    }

    public List<String> runTRMorph(File aExecutable, File aModel, String token)
        throws IOException
    {

        File tempFile = null;
        Process p = null;
        InputStream in = null;

        try {
            tempFile = File.createTempFile("sfst-morph-token", ".txt");
            FileUtils.writeStringToFile(tempFile, token);
            ProcessBuilder pb = new ProcessBuilder(aExecutable.getAbsolutePath(),
                    aModel.getAbsolutePath(), tempFile.getAbsolutePath());
            pb.redirectErrorStream();

            p = pb.start();
            in = p.getInputStream();
            List<String> result = IOUtils.readLines(in, "UTF-8");
            ListIterator<String> i = result.listIterator();

            while (i.hasNext()) {
                String line = i.next();
                // skip lines starting with ">" as this indicates input instead
                // of analysis result
                if (line.startsWith(">")) {
                    i.remove();
                }
                // if trmorph cannot analyse the token, return the token itself
                if (line.startsWith("no result for")) {
                    i.set(token);
                }
            }

            return result;
        }
        finally {
            IOUtils.closeQuietly(in);
            if (p != null) {
                p.destroy();
            }
            if (tempFile != null) {
                FileUtils.forceDelete(tempFile);
            }
        }
    }
}
