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

import static org.apache.uima.util.Level.INFO;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
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
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
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
public class MaltParser
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_PRINT_TAGSET = "printTagSet";
	@ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue="false")
	protected boolean printTagSet;

	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	public static final String PARAM_VARIANT = "variant";
	@ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
	protected String variant;

	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

	// Not sure if we'll ever have to use different symbol tables
	public static final String SYMBOL_TABLE = "symbolTableName";
	@ConfigurationParameter(name = SYMBOL_TABLE, mandatory = true, defaultValue = "DEPREL")
	private String symbolTableName;

	private Logger logger;
	private SymbolTable symbolTable;
	private File workingDir;
	
	private CasConfigurableProviderBase<MaltParserService> modelProvider;

	
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

		modelProvider = new CasConfigurableProviderBase<MaltParserService>() {
			private MaltParserService parser;
			
			{
				setDefault(VERSION, "1.7");
				setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
				setDefault(ARTIFACT_ID,
						"de.tudarmstadt.ukp.dkpro.core.maltparser-model-parser-${language}-${variant}");
				setDefault(VARIANT, "linear");
				
				setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/maltparser/lib/" +
						"parser-${language}-${variant}.mco");
				
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

					if (printTagSet) {
						PropertyAccessor paDirect = PropertyAccessorFactory.forDirectFieldAccess(parser);
						SingleMalt singleMalt = (SingleMalt) paDirect.getPropertyValue("singleMalt");
						
						List<String> posTags = new ArrayList<String>();
						SymbolTable posTagTable = singleMalt.getSymbolTables().getSymbolTable("POSTAG");
						for (int i : posTagTable.getCodes()) {
							posTags.add(posTagTable.getSymbolCodeToString(i));
						}
						Collections.sort(posTags);

						getContext().getLogger().log(INFO, "Model expects [" + posTags.size() + 
								"] postags: "+StringUtils.join(posTags, " "));

						List<String> depRels = new ArrayList<String>();
						SymbolTable depRelTable = singleMalt.getSymbolTables().getSymbolTable("DEPREL");
						for (int i : depRelTable.getCodes()) {
							depRels.add(depRelTable.getSymbolCodeToString(i));
						}
						Collections.sort(depRels);

						getContext().getLogger().log(INFO, "Model contains [" + depRels.size() + 
								"] tags: "+StringUtils.join(depRels, " "));
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
	 * @see org.apache.uima.AnalysisComponent.AnalysisComponent#collectionProcessComplete()
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
				// This only works for the English model. Other models have different input
				// formats. See http://www.maltparser.org/mco/mco.html
				parserInput[i] = String.format("%1$d\t%2$s\t_\t%3$s\t%3$s\t_", i + 1,
						t.getCoveredText(), t.getPos().getPosValue());
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
							Dependency dep = new Dependency(aJCas, curSentence.getBegin(),
									curSentence.getEnd());
							dep.setGovernor(sourceToken); // TODO check if source=Governor
							dep.setDependent(targetToken); // TODO check if target=Dependent
							dep.setDependencyType(edge.getLabelSymbol(symbolTable));
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
}
