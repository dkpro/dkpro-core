/*******************************************************************************
 * Copyright 2012
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.hunpos;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Part-of-Speech annotator using OpenNLP. Requires {@link Sentence}s to be annotated before.
 *
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
	    inputs = {
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
		outputs = {
		    "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class HunPosTagger
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
	 * Load the model from this location instead of locating the model automatically.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

    /**
     * The character encoding used by the model.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = false, defaultValue="UTF-8")
    protected String modelEncoding;

	/**
	 * Load the part-of-speech tag to UIMA type mapping from this location instead of locating
	 * the mapping automatically.
	 */
	public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
	@ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
	protected String posMappingLocation;

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
	@ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue="false")
	protected boolean printTagSet;

    private CasConfigurableProviderBase<File> modelProvider;
    private RuntimeProvider runtimeProvider;
	private MappingProvider mappingProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

        modelProvider = new CasConfigurableProviderBase<File>()
        {
            {
                setContextObject(HunPosTagger.this);

                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/hunpos/lib/"
                                + "tagger-${language}-${variant}.model");
                setDefault(VARIANT, "default");
                setDefaultVariantsLocation("de/tudarmstadt/ukp/dkpro/core/hunpos/lib/tagger-default-variants.map");

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
        runtimeProvider = new RuntimeProvider("classpath:/de/tudarmstadt/ukp/dkpro/core/hunpos/bin/");

		mappingProvider = new MappingProvider();
		mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
				"core/api/lexmorph/tagset/${language}-${pos.tagset}-pos.map");
		mappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
		mappingProvider.setDefault("pos.tagset", "default");
		mappingProvider.setOverride(MappingProvider.LOCATION, posMappingLocation);
		mappingProvider.setOverride(MappingProvider.LANGUAGE, language);
		mappingProvider.addImport("pos.tagset", modelProvider);
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		CAS cas = aJCas.getCas();

		modelProvider.configure(cas);
		mappingProvider.configure(cas);

        File model = modelProvider.getResource();
        File executable;
        
        try {
            executable = runtimeProvider.getFile("hunpos-tag");
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
		
        ProcessBuilder pb = new ProcessBuilder(executable.getAbsolutePath(),
                model.getAbsolutePath());
        pb.redirectError(Redirect.INHERIT);
        
        StringBuffer lastOut = new StringBuffer();
        String lastIn = null;
        boolean success = false;
        Process proc = null;
        try {
            proc = pb.start();
            
            PrintWriter out = new PrintWriter(new OutputStreamWriter(proc.getOutputStream(),
                    modelEncoding));
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(),
                    modelEncoding));
            
            for (Sentence sentence : select(aJCas, Sentence.class)) {
                List<Token> tokens = selectCovered(Token.class, sentence);
                
                // Skip empty sentences
                if (tokens.isEmpty()) {
                    continue;
                }
                
                // Send full sentence
                for (Token token : tokens) {
                    lastOut.append(token.getCoveredText()).append(' ');
                    out.printf("%s%n", token.getCoveredText());
                }
                out.printf("%n");
                out.flush();

                // Read sentence tags
                String[] tags = new String[tokens.size()];
                for (int i = 0; i < tokens.size(); i++) {
                    lastIn = in.readLine();
                    tags[i] = lastIn.split("\t", 2)[1].trim();
                }
                in.readLine(); // Read extra new line after sentence
                
                int i = 0;
                for (Token t : tokens) {
                    Type posTag = mappingProvider.getTagType(tags[i]);
                    POS posAnno = (POS) cas.createAnnotation(posTag, t.getBegin(), t.getEnd());
                    posAnno.setPosValue(internTags ? tags[i].intern() : tags[i]);
                    posAnno.addToIndexes();
                    t.setPos(posAnno);
                    i++;
                }
                
                lastOut.setLength(0);
            }
            
            success = true;
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            if (!success) {
                getLogger().error("Sent before error: ["+lastOut+"]");
                getLogger().error("Last response before error: ["+lastIn+"]");
            }
            if (proc != null) {
                proc.destroy();
            }
        }
	}
	
    @Override
    public void destroy()
    {
        runtimeProvider.uninstall();
        super.destroy();
    }
}
