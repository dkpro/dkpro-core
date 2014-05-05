/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.treetagger;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.util.Level.INFO;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.annolab.tt4j.TokenAdapter;
import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerModelUtil;
import org.annolab.tt4j.TreeTaggerWrapper;
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
import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.treetagger.internal.DKProExecutableResolver;

/**
 * Part-of-Speech and lemmatizer annotator using TreeTagger.
 */
@TypeCapability(
	    inputs = {
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" },
		outputs = {
		    "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class TreeTaggerPosLemmaTT4J
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
     * Use this TreeTagger executable instead of trying to locate the executable automatically.
     */
    public static final String PARAM_EXECUTABLE_PATH = "executablePath";
    @ConfigurationParameter(name = PARAM_EXECUTABLE_PATH, mandatory = false)
    private File executablePath;
    
	/**
	 * Load the model from this location instead of locating the model automatically.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

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

    /**
     * TT4J setting: Disable some sanity checks, e.g. whether tokens contain line breaks (which is
     * not allowed). Turning this on will increase your performance, but the wrapper may throw
     * exceptions if illegal data is provided.
     */
    public static final String PARAM_PERFORMANCE_MODE = "performanceMode";
    @ConfigurationParameter(name = PARAM_PERFORMANCE_MODE, mandatory = true, defaultValue = "false")
    private boolean performanceMode;
    
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
	
	private CasConfigurableProviderBase<TreeTaggerWrapper<Token>> modelProvider;
	private MappingProvider mappingProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		modelProvider = new ModelProviderBase<TreeTaggerWrapper<Token>>() {
		    private TreeTaggerWrapper<Token> treetagger;
		    
			{
                setContextObject(TreeTaggerPosLemmaTT4J.this);

                setDefault(ARTIFACT_ID, "${groupId}.treetagger-model-tagger-${language}-${variant}");
				setDefault(LOCATION, "classpath:/${package}/lib/tagger-${language}-${variant}.properties");
                setDefaultVariantsLocation("de/tudarmstadt/ukp/dkpro/core/treetagger/lib/tagger-default-variants.map");
				setDefault(VARIANT, "le"); // le = little-endian

				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
				
				treetagger = new TreeTaggerWrapper<Token>();
	            treetagger.setPerformanceMode(performanceMode);
	            DKProExecutableResolver executableProvider = new DKProExecutableResolver(treetagger);
	            executableProvider.setExecutablePath(executablePath);
	            treetagger.setExecutableProvider(executableProvider);
                treetagger.setAdapter(new TokenAdapter<Token>()
                {
                    @Override
                    public String getText(Token aObject)
                    {
                        synchronized (aObject.getCAS()) {
                            return aObject.getCoveredText();
                        }
                    }
                });
			}

			@Override
			protected TreeTaggerWrapper<Token> produceResource(URL aUrl)
			    throws IOException
			{
			    Properties meta = getResourceMetaData();
			    String encoding = meta.getProperty("encoding");
			    String tagset = meta.getProperty("pos.tagset");
			    
			    File modelFile = ResourceUtils.getUrlAsFile(aUrl, true);
			    
                // Reconfigure tagger
                treetagger.setModel(modelFile.getPath() + ":" + encoding);
                
                // Get tagset
                List<String> tags = TreeTaggerModelUtil.getTagset(modelFile, encoding);
                SingletonTagset posTags = new SingletonTagset(POS.class, tagset);
                posTags.addAll(tags);
                addTagset(posTags);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, getTagset().toString());
                }

                return treetagger;
			}
		};

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
	public void process(final JCas aJCas)
		throws AnalysisEngineProcessException
	{
		final CAS cas = aJCas.getCas();

		modelProvider.configure(cas);
		mappingProvider.configure(cas);
		
		TreeTaggerWrapper<Token> treetagger = modelProvider.getResource();

        try {
            List<Token> tokens = new ArrayList<Token>(select(aJCas, Token.class));
            final POS pos[] = new POS[tokens.size()];
            final Lemma lemma[] = new Lemma[tokens.size()];

            // Set the handler creating new UIMA annotations from the analyzed
            // tokens
            final AtomicInteger count = new AtomicInteger(0);
            treetagger.setHandler(new TokenHandler<Token>() {
                @Override
                public void token(Token aToken, String aPos, String aLemma)
                {
                    synchronized (cas) {
                        // Add the Part of Speech
                        if (writePos && aPos != null) {
                            Type posTag = mappingProvider.getTagType(aPos);
                            POS posAnno = (POS) cas.createAnnotation(posTag, aToken.getBegin(),
                                    aToken.getEnd());
                            posAnno.setPosValue(internTags ? aPos.intern() : aPos);
                            aToken.setPos(posAnno);
                            pos[count.get()] = posAnno;
                        }

                        // Add the lemma
                        if (writeLemma && aLemma != null) {
                            Lemma lemmaAnno = new Lemma(aJCas, aToken.getBegin(), aToken.getEnd());
                            lemmaAnno.setValue(internTags ? aLemma.intern() : aLemma);
                            aToken.setLemma(lemmaAnno);
                            lemma[count.get()] = lemmaAnno;
                        }

                        count.getAndIncrement();
                    }
                }
            });

            treetagger.process(tokens);

            // Add the annotations to the indexes
            for (int i = 0; i < count.get(); i++) {
                if (pos[i] != null) {
                    pos[i].addToIndexes();
                }
                if (lemma[i] != null) {
                    lemma[i].addToIndexes();
                }
            }
        }
        catch (TreeTaggerException e) {
            throw new AnalysisEngineProcessException(e);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }		
	}
}
