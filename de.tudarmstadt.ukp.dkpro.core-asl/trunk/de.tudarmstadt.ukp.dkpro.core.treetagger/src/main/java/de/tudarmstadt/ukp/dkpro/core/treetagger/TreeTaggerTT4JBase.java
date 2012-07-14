/*******************************************************************************
 * Copyright 2010
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

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.getUrlAsFile;
import static java.io.File.separator;
import static org.annolab.tt4j.Util.getSearchPaths;
import static org.apache.uima.util.Level.INFO;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.annolab.tt4j.DefaultExecutableResolver;
import org.annolab.tt4j.DefaultModel;
import org.annolab.tt4j.DefaultModelResolver;
import org.annolab.tt4j.ModelResolver;
import org.annolab.tt4j.TokenAdapter;
import org.annolab.tt4j.TreeTaggerModelUtil;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.uimafit.component.CasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.HasResourceMetadata;


/**
 * @author Richard Eckart de Castilho
 *
 * @param <T>
 */
public abstract class TreeTaggerTT4JBase<T>
	extends CasAnnotator_ImplBase
{
    public static final String RESOURCE_TREETAGGER = "TreeTagger";
	protected DKProTreeTaggerWrapper<T> treetagger;

	public static final String PARAM_PRINT_TAGSET = "printTagSet";
	@ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue="false")
	protected boolean printTagSet;
	
    public static final String PARAM_LANGUAGE_CODE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name=PARAM_LANGUAGE_CODE, mandatory=false)
	protected String languageCode;

    public static final String PARAM_PERFORMANCE_MODE = "PerformanceMode";
	@ConfigurationParameter(name=PARAM_PERFORMANCE_MODE, mandatory=true, defaultValue="false")
	private boolean performanceMode;

    public static final String PARAM_EXECUTABLE_PATH = "ExecutablePath";
	@ConfigurationParameter(name=PARAM_EXECUTABLE_PATH, mandatory=false)
	private File executablePath;

    public static final String PARAM_MODEL_PATH = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name=PARAM_MODEL_PATH, mandatory=false)
	protected File modelPath;

    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
	@ConfigurationParameter(name=PARAM_MODEL_ENCODING, mandatory=false)
	protected String modelEncoding;

	public static final String PARAM_INTERN_STRINGS = "InternStrings";
	@ConfigurationParameter(name=PARAM_INTERN_STRINGS, mandatory=false, defaultValue="true")
	private boolean internStrings;

//
//	private Set<String> missingTags;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		try {
			if (modelPath != null && modelEncoding == null) {
				throw new IllegalArgumentException("When specifying a model file, the model " +
						"encoding has to be specified as well.");
			}

//			missingTags = new HashSet<String>();

			treetagger = new DKProTreeTaggerWrapper<T>();

			// Set the adapter extracting the text from the UIMA token
			treetagger.setAdapter(getAdapter());

			treetagger.setExecutableProvider(new DKProExecutableResolver());
			treetagger.setModelProvider(getModelResolver());
			treetagger.setPerformanceMode(performanceMode);
		}
		catch (ResourceInitializationException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	protected boolean isPerformanceMode()
	{
		return performanceMode;
	}

	public boolean isInternStrings()
	{
		return internStrings;
	}

	protected abstract TokenAdapter<T> getAdapter()
		throws ResourceInitializationException;

	protected abstract ModelResolver getModelResolver()
		throws ResourceInitializationException;

	@Override
	public void destroy()
	{
		if (treetagger != null) {
			getLogger().info("Cleaning up TreeTagger process");
			treetagger.destroy();
		}
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		destroy();
		super.finalize();
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	protected class DKProExecutableResolver
		extends DefaultExecutableResolver
	{
		public File searchInFilesystem(final Set<String> aSearchedIn)
		{
			String platformId = treetagger.getPlatformDetector().getPlatformId();
			String exeSuffix  = treetagger.getPlatformDetector().getExecutableSuffix();

			for (final String p : getSearchPaths(_additionalPaths, "bin")) {
				if (p == null) {
					continue;
				}

				final File exe1 = new File(p + separator + "tree-tagger" + exeSuffix);
				final File exe2 = new File(p + separator + platformId + separator
						+ "tree-tagger" + exeSuffix);

				aSearchedIn.add(exe1.getAbsolutePath());
				if (exe1.exists()) {
					return exe1;
				}

				aSearchedIn.add(exe2.getAbsolutePath());
				if (exe2.exists()) {
					return exe2;
				}
			}

			return null;
		}

		public File searchInResources(final Set<String> aSearchedIn)
		{
			try {
				if (getContext().getResourceURL(RESOURCE_TREETAGGER) != null) {
					// If we cannot find it in the classpath, try using the specified
					// resource
					String platformId = treetagger.getPlatformDetector().getPlatformId();
					String exeSuffix  = treetagger.getPlatformDetector().getExecutableSuffix();
					String ttRelLoc = "/bin/"+platformId+"/tree-tagger"+exeSuffix;
					String ttPath = getContext().getResourceURL(RESOURCE_TREETAGGER).toURI().getPath();
					ttPath += ttRelLoc;
					File ttFile = new File(ttPath);
					aSearchedIn.add(ttFile.toURI()+" (UIMA external resource)");
					if (ttFile.exists()) {
						return ttFile;
					}
				}
				return null;
			}
			catch (Exception e) {
				return null;
			}
		}

		public File searchInClasspath(final Set<String> aSearchedIn)
		{
			try {
				String platformId = treetagger.getPlatformDetector().getPlatformId();
				String exeSuffix  = treetagger.getPlatformDetector().getExecutableSuffix();
				String ttRelLoc = "/bin/"+platformId+"/tree-tagger"+exeSuffix;
				String loc = "/de/tudarmstadt/ukp/dkpro/core/treetagger"+ttRelLoc;
				aSearchedIn.add("classpath:"+loc);
				URL ttExecUrl = getClass().getResource(loc);

				if (ttExecUrl != null) {
					return getUrlAsFile(ttExecUrl, true);
				}
				return null;
			}
			catch (Exception e) {
				return null;
			}
		}

		@Override
		public String getExecutable()
			throws IOException
		{
			Set<String> searchedIn = new HashSet<String>();

			File exeFile;
			if (executablePath != null) {
				exeFile = executablePath;
				searchedIn.add(executablePath.getAbsolutePath());
			}
			else {
				exeFile = searchInFilesystem(searchedIn);
				if (exeFile == null) {
					exeFile = searchInResources(searchedIn);
				}
				if (exeFile == null) {
					exeFile = searchInClasspath(searchedIn);
				}
			}
			if (exeFile == null) {
				throw new IOException("Unable to locate tree-tagger binary in the following locations " +
						searchedIn + ". Make sure the environment variable 'TREETAGGER_HOME' or " +
						"'TAGDIR' or the system property 'treetagger.home' point to the TreeTagger " +
						"installation directory.");
			}

			exeFile.setExecutable(true);

			if (!exeFile.isFile()) {
				throw new IOException("TreeTagger executable at ["+exeFile+"] is not a file.");
			}

			if (!exeFile.canRead()) {
				throw new IOException("TreeTagger executable at ["+exeFile+"] is not readable.");
			}

			if (!exeFile.canExecute()) {
				throw new IOException("TreeTagger executable at ["+exeFile+"] not executable.");
			}

			getLogger().info("TreeTagger executable location: " + exeFile.getAbsoluteFile());
			return exeFile.getAbsolutePath();
		}
	}

	/**
	 * A model resolver that knows how models map to language codes in DKPro.
	 *
	 * @author Richard Eckart de Castilho
	 */
	protected abstract class DKProModelResolver
		extends DefaultModelResolver
	{
		private File overrideModelPath;
		private String overrideModelEncoding;

		protected abstract
		String getType();

		public DKProModelResolver(File aModelPath, String aModelEncoding)
		{
			overrideModelPath = aModelPath;
			overrideModelEncoding = aModelEncoding;
		}

		public File searchInFilesystem(final String aLocation, final Set<String> aSearchedIn)
		{
			File _file = new File(aLocation);

			if (!_file.exists()) {
				for (final String p : getSearchPaths(_additionalPaths, "lib")) {
					if (p == null) {
						continue;
					}

					_file = new File(p+separator+aLocation);
					aSearchedIn.add(_file.getAbsolutePath());
					if (_file.exists()) {
						return _file;
					}
				}
			}

			return null;
		}

		@Override
		public DKProModel getModel(String aModelName)
			throws IOException
		{
			if (overrideModelPath != null) {
				DKProModel model = new DKProModel(aModelName, overrideModelPath, overrideModelEncoding, null);
				printTagset(model);
				return model;
			}

			File modelFile;
			String modelEnc;
			Properties properties = null;
			Set<String> searchedIn = new HashSet<String>();
			String byteOrder = getPlatformDetector().getByteOrder();
			String baseFile = getType()+"-"+aModelName+"-"+byteOrder;

			// Try file system
			modelFile = searchInFilesystem(baseFile + ".par", searchedIn);
			if (modelFile != null) {
				String baseName = FilenameUtils.removeExtension(modelFile.getPath());
				File propertiesFile = new File(baseName + ".properties");

				if (!propertiesFile.exists()) {
					throw new IOException("There is no properties file for " + "model ["
							+ aModelName + "] at [" + propertiesFile + "]");
				}

				properties = PropertiesLoaderUtils.loadProperties(new FileSystemResource(propertiesFile));
			}

			// Try classpath
			if (modelFile == null) {
				String base = "/de/tudarmstadt/ukp/dkpro/core/treetagger/lib/";
				String propertiesLoc = base+baseFile+".properties";
				String modelLoc = base+baseFile+".par";
				searchedIn.add("classpath:"+modelLoc);

				URL modelUrl = getClass().getResource(modelLoc);
				if (modelUrl != null) {
					URL propertiesUrl = getClass().getResource(propertiesLoc);
					if (propertiesUrl == null) {
						throw new IOException("There is no properties file for model [" + aModelName
								+ "] at [" + propertiesLoc + "]");
					}
					
					properties = PropertiesLoaderUtils.loadProperties(new UrlResource(propertiesUrl));
					modelFile = getUrlAsFile(modelUrl, true);
				}
			}

			if (modelFile == null || properties == null) {
				throw new IOException("Unable to locate model ["+aModelName+"] in the following " +
						"locations "+searchedIn+".  Make sure the environment variable " +
						"'TREETAGGER_HOME' or 'TAGDIR' or the system property 'treetagger.home' " +
						"point to the TreeTagger installation directory.");
			}

			modelEnc = (overrideModelEncoding != null) ? overrideModelEncoding : properties.getProperty("encoding");
			DKProModel model = new DKProModel(aModelName, modelFile, modelEnc, properties);
			printTagset(model);
			return model;
		}
		
		private void printTagset(DKProModel aModel) throws IOException
		{
			if (printTagSet) {
				List<String> tags = TreeTaggerModelUtil.getTagset(aModel.getFile(), aModel.getEncoding());
				
				Collections.sort(tags);

				getContext().getLogger().log(INFO, "Model contains [" + tags.size() + 
						"] tags: "+StringUtils.join(tags, " "));
			}
		}
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	public static class DKProModel
		extends DefaultModel
	{
		private Properties properties;
		
		public DKProModel(String aName, File aFile, String aEncoding, Properties aProperties)
		{
			super(aName, aFile, aEncoding);
			properties = aProperties;
		}
	}
	
	public static class DKProTreeTaggerWrapper<T>
		extends TreeTaggerWrapper<T>
		implements HasResourceMetadata
	{
		@Override
		public Properties getResourceMetaData()
		{
			return ((DKProModel) getModel()).properties;
		}
	}
}
