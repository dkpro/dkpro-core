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
package de.tudarmstadt.ukp.dkpro.core.maltparser;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.util.Level.INFO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.parser.SingleMalt;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * <p>
 * DKPro Annotator for the MaltParser
 * </p>
 *
 * Required annotations:<br/>
 * <ul>
 * <li>Token</li>
 * <li>Sentence</li>
 * <li>POS</li>
 * </ul>
 *
 * Generated annotations:<br/>
 * <ul>
 * <li>Dependency (annotated over sentence-span)</li>
 * </ul>
 *
 *
 * @author Oliver Ferschke
 * @author Richard Eckart de Castilho
 */

@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"},
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"})

public class MaltParser
	extends JCasAnnotator_ImplBase
{
    private static final String UNUSED = "_";
    
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
	 * Log the tag set(s) when a model is loaded.
	 *
	 * Default: {@code false}
	 */
	public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
	@ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
	protected boolean printTagSet;

	/**
     * Process anyway, even if the model relies on features that are not supported by this
     * component.
     * 
     * Default: {@code false}
     */
    public static final String PARAM_IGNORE_MISSING_FEATURES = "ignoreMissingFeatures";
    @ConfigurationParameter(name = PARAM_IGNORE_MISSING_FEATURES, mandatory = true, defaultValue = "false")
    protected boolean ignoreMissingFeatures;

	// Not sure if we'll ever have to use different symbol tables
    // public static final String SYMBOL_TABLE = "symbolTableName";
    // @ConfigurationParameter(name = SYMBOL_TABLE, mandatory = true, defaultValue = "DEPREL")
	private final String symbolTableName = "DEPREL";

	private Logger logger;
	private SymbolTable symbolTable;
	private File workingDir;

	private CasConfigurableProviderBase<MaltParserService> modelProvider;
	private Set<String> features;


	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		logger = getContext().getLogger();

		try {
			workingDir = File.createTempFile("maltparser", ".tmp");
			workingDir.delete();
			workingDir.mkdirs();
			workingDir.deleteOnExit();
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}

		modelProvider = new ModelProviderBase<MaltParserService>() {
			private MaltParserService parser;

			{
			    setContextObject(MaltParser.this);

				setDefault(ARTIFACT_ID,
						"${groupId}.maltparser-model-parser-${language}-${variant}");
				setDefault(VARIANT, "linear");

				setDefault(LOCATION, "classpath:/${package}/lib/parser-${language}-${variant}.mco");

				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

			@Override
			protected MaltParserService produceResource(URL aUrl) throws IOException
			{
				if (parser != null) {
					// Terminates the parser model
					try {
						parser.terminateParserModel();
						parser = null;
					}
					catch (MaltChainedException e) {
						logger.log(Level.SEVERE,
								"MaltParser exception while terminating parser model: " + e.getMessage());
					}
				}

				try {
				    // Warn if the model uses features that we currently do not support
				    features = getFeatures(aUrl);
				    Set<String> unsupportedFeatures = new HashSet<String>(features);
				    getLogger().info("Model uses these supported features: " + features);
				    unsupportedFeatures.remove("FORM"); // we know covered text
				    unsupportedFeatures.remove("LEMMA"); // we know lemma if lemmatizer ran before
				    unsupportedFeatures.remove("POSTAG"); // we know POS tag if POS tagger ran before
				    // CPOSTAG - only supported if we know a mapping from POSTAG to CPOSTAG (FIXME)
				    // FEATS - not properly supported in DKPro Core yet! (FIXME)
                    if (!unsupportedFeatures.isEmpty()) {
                        String message = "Model these uses unsupported features: " + unsupportedFeatures;
                        if (ignoreMissingFeatures) {
                            getLogger().warn(message); 
                        }
                        else {
                            throw new IOException(message);
                        }
                    }
				    
					// However, Maltparser is not happy at all if the model file does not have the right
					// name, so we are forced to create a temporary directory and place the file there.
					File modelFile = new File(workingDir, getRealName(aUrl));
					if (!modelFile.exists()) {
						InputStream is = null;
						OutputStream os = null;
						try {
							is = aUrl.openStream();
							os = new FileOutputStream(modelFile);
							IOUtils.copy(is, os);
							modelFile.deleteOnExit();
						}
						finally {
							IOUtils.closeQuietly(is);
							IOUtils.closeQuietly(os);
						}
					}

					// Maltparser has a very odd way of finding out which command line options it supports.
					// By manually initializing the OptionManager before Maltparser tries it, we can work
					// around Maltparsers' own broken code.
					if (OptionManager.instance().getOptionContainerIndices().size() == 0) {
						OptionManager.instance().loadOptionDescriptionFile(
								MaltParserService.class.getResource("/appdata/options.xml"));
						OptionManager.instance().generateMaps();
					}

					// Ok, now we can finally initialize the parser
					parser = new MaltParserService();
					parser.initializeParserModel("-w " + workingDir + " -c " + modelFile.getName()
							+ " -m parse");
					// parser.initializeParserModel("-u " + modelUrl.toString() + " -m parse");


	                Properties metadata = getResourceMetaData();

                    PropertyAccessor paDirect = PropertyAccessorFactory.forDirectFieldAccess(parser);
                    SingleMalt singleMalt = (SingleMalt) paDirect.getPropertyValue("singleMalt");

	                SingletonTagset posTags = new SingletonTagset(
	                        POS.class, metadata.getProperty("pos.tagset"));
                    SymbolTable posTagTable = singleMalt.getSymbolTables().getSymbolTable("POSTAG");
                    for (int i : posTagTable.getCodes()) {
                        posTags.add(posTagTable.getSymbolCodeToString(i));
                    }
	                addTagset(posTags);

	                SingletonTagset depTags = new SingletonTagset(
	                        Dependency.class, metadata.getProperty("dependency.tagset"));
                    SymbolTable depRelTable = singleMalt.getSymbolTables().getSymbolTable("DEPREL");
                    for (int i : depRelTable.getCodes()) {
                        depTags.add(depRelTable.getSymbolCodeToString(i));
                    }
	                addTagset(depTags);

	                if (printTagSet) {
	                    getContext().getLogger().log(INFO, getTagset().toString());
	                }

					return parser;
				}
				catch (MaltChainedException e) {
					logger.log(Level.SEVERE,
							"MaltParser exception while initializing parser model: " + e.getMessage());
					throw new IOException(e);
				}
			}
		};
	}

	/**
	 * @see AnalysisComponent#collectionProcessComplete()
	 */
	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException
	{
		if (workingDir != null && workingDir.isDirectory()) {
			FileUtils.deleteQuietly(workingDir);
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		modelProvider.configure(aJCas.getCas());

		// Iterate over all sentences
		for (Sentence curSentence : select(aJCas, Sentence.class)) {

			// Generate list of tokens for current sentence
			List<Token> tokens = selectCovered(Token.class, curSentence);

			// Generate input format required by parser
			String[] parserInput = new String[tokens.size()];
			for (int i = 0; i < parserInput.length; i++) {
				Token t = tokens.get(i);
				
				int id = i + 1;
				String form = t.getCoveredText();
				String lemma = UNUSED;
				String cpostag = UNUSED;
				String postag = UNUSED;
				String feats = UNUSED;
				
				if (features.contains("LEMMA")) {
				    if (t.getLemma() != null) {
				        lemma = t.getLemma().getValue();
				    }
				    else if (!ignoreMissingFeatures) {
	                    throw new IllegalStateException(
	                            "Model uses feature LEMMA but there is no lemma information in CAS");
				    }
				}
				
                // Actually, this cannot work, because we only know about the DKPro Core coarse
                // grained categories, which are most likely different from the coarse-grained
                // categories required by the model. We would need to include a mapping with the
                // model to recover the required coarse grained categories from the fine-grained
                // categories in POSTAG.
                if (features.contains("CPOSTAG")) {
//                    if (t.getPos() != null) {
//                        cpostag = t.getPos().getPosValue();
//                    }
//                    else 
                    if (!ignoreMissingFeatures) {
                        throw new IllegalStateException(
                                "Model uses feature CPOSTAG but there is no part-of-speech information in CAS");
                    }
                }

                if (features.contains("POSTAG")) {
                    if (t.getPos() != null) {
                        postag = t.getPos().getPosValue();
                    }
                    else if (!ignoreMissingFeatures) {
                        throw new IllegalStateException(
                                "Model uses feature POSTAG but there is no part-of-speech information in CAS");
                    }
                }

                if (features.contains("FEATS")) {
                    // FIXME When we have better support for features, hook them in here!
//                    if (t.getPos() != null) {
//                        lemma = t.getPos().getPosValue();
//                    }
//                    else 
                    if (!ignoreMissingFeatures) {
                        throw new IllegalStateException(
                                "Model uses feature FEATS but there is no morphology information in CAS");
                    }
                }

				// This only works for the English model. Other models have different input
				// formats. See http://www.maltparser.org/mco/mco.html
                parserInput[i] = String.format("%d\t%s\t%s\t%s\t%s\t%s", id, form, lemma, cpostag,
                        postag, feats);
			}

			// Parse sentence
			DependencyStructure graph = null;
			try {
				// Parses the sentence
				graph = modelProvider.getResource().parse(parserInput);
				symbolTable = graph.getSymbolTables().getSymbolTable(symbolTableName);
			}
			catch (MaltChainedException e) {
				logger.log(Level.WARNING,
						"MaltParser exception while parsing sentence: " + e.getMessage(), e);
				// don't pass on exception - go on with next sentence
				continue;
			}

			/*
			 * Generate annotations: NOTE: Index of token in tokenList corresponds to node in
			 * DependencyGraph with NodeIndex+1
			 */
			try {
				// iterate over all tokens in current sentence
				for (int i = 0; i < tokens.size(); i++) {
					// Start with Node 1 - we omit ROOT-dependencies,
					// because we don't have a ROOT-token.
					TokenNode curNode = graph.getTokenNode(i + 1);

					// iterate over all dependencies for current token
					for (Edge edge : curNode.getHeadEdges()) {
						int sourceIdx = edge.getSource().getIndex();
						int targetIdx = edge.getTarget().getIndex();

						// get corresponding token for node in DependencyGraph
						Token sourceToken = sourceIdx > 0 ? tokens.get(sourceIdx - 1) : null;
						Token targetToken = targetIdx > 0 ? tokens.get(targetIdx - 1) : null;

						// create dep-annotation for current edge
						if (sourceToken != null && targetToken != null) {
							Dependency dep = new Dependency(aJCas);
                            dep.setDependencyType(edge.getLabelSymbol(symbolTable));
							dep.setGovernor(sourceToken); // TODO check if source=Governor
							dep.setDependent(targetToken); // TODO check if target=Dependent
		                    dep.setBegin(dep.getDependent().getBegin());
		                    dep.setEnd(dep.getDependent().getEnd());
							dep.addToIndexes();
						}
					}
				}
			}
			catch (MaltChainedException e) {
				logger.log(Level.WARNING, "MaltParser exception creating dependency annotations: "
						+ e.getMessage(), e);
				// don't pass on exception - go on with next sentence
				continue;
			}
		}
	}

	private String getRealName(URL aUrl) throws IOException
	{
		JarEntry je = null;
		JarInputStream jis = null;

		try {
			jis = new JarInputStream(aUrl.openConnection().getInputStream());
			while ((je = jis.getNextJarEntry()) != null) {
				String entryName = je.getName();
				if (entryName.endsWith(".info")) {
					int indexUnderScore = entryName.lastIndexOf('_');
					int indexSeparator = entryName.lastIndexOf(File.separator);
					if (indexSeparator == -1) {
						indexSeparator = entryName.lastIndexOf('/');
					}
					if (indexSeparator == -1) {
						indexSeparator = entryName.lastIndexOf('\\');
					}
					int indexDot = entryName.lastIndexOf('.');
					if (indexUnderScore == -1 || indexDot == -1) {
						throw new IllegalStateException(
								"Could not find the configuration name and type from the URL '"
										+ aUrl.toString() + "'. ");
					}

					return entryName.substring(indexSeparator+1, indexUnderScore) + ".mco";
				}
			}

			throw new IllegalStateException(
					"Could not find the configuration name and type from the URL '"
							+ aUrl.toString() + "'. ");
		}
		finally {
			IOUtils.closeQuietly(jis);
		}
	}
	
	private Set<String> getFeatures(URL aUrl) throws IOException
	{
        JarEntry je = null;
        JarInputStream jis = null;

        try {
            jis = new JarInputStream(aUrl.openConnection().getInputStream());
            while ((je = jis.getNextJarEntry()) != null) {
                String entryName = je.getName();

                if (entryName.endsWith(".info")) {
                    Set<String> features = new HashSet<String>();
                    
                    for (String line : IOUtils.readLines(jis, "UTF-8")) {
                        if (line.contains("InputColumn(")) {
                            int offset = line.indexOf("InputColumn(");
                            while (offset >= 0) {
                                int comma = line.indexOf(',', offset+1);
                                features.add(line.substring(offset+12,comma).trim());
                                offset = line.indexOf("InputColumn(", comma);
                            }
                        }
                    }
                    
                    return features;
                }
            }

            throw new IllegalStateException(
                    "Could not find the configuration name and type from the URL '"
                            + aUrl.toString() + "'. ");
        }
        finally {
            IOUtils.closeQuietly(jis);
        }
	}
}
