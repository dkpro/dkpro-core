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
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.resolveLocation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * Base class for resource providers that produce a resource from some URL depending on changing
 * parameters such as language.
 * <p>
 * A component using such a provider sets defaults and overrides. The defaults should be set to 
 * sensible values with which the component should be able to work out of the box. For example
 * the {@link #LOCATION} may be set to "classpath:/resources/${language}/model.ser.gz".
 * <p>
 * The overrides should only be set if the user explicitly wants to override some settings.
 * <p>
 * Finally, parameters that may change, e.g. depending on the CAS content should be returned from
 * {@link #getProperties()}.
 * <p>
 * The {@link #LOCATION} may contain variables referring to any of the other settings, e.g.
 * <code>"${language}"</code>.
 * <p>
 * It is possible a different default variant needs to be used depending on the language. This
 * can be configured by placing a properties file in the classpath and setting its location using
 * {@link #setDefaultVariantsLocation(String)} or by using {@link #setDefaultVariants(Properties)}.
 * The key in the properties is the language and the value is used a default variant.
 * 
 * @author Richard Eckart de Castilho
 *
 * @param <M> the kind of resource produced
 */
public abstract class ResourceObjectProviderBase<M>
	implements HasResourceMetadata
{
	private final Log log = LogFactory.getLog(getClass());
	
	public static final String NOT_REQUIRED = "-=* NOT REQUIRED *=-";
	
	/**
	 * The language.
	 */
	public static final String LANGUAGE = "language";
	
	/**
	 * The variant. (optional)
	 */
	public static final String VARIANT  = "variant";
	
	
	/**
	 * The location from which the resource should be read. Variables in the location are resolved
	 * when {@link #configure()} is called.
	 * 
	 * @see ResourceUtils#resolveLocation(String, Object, org.apache.uima.UimaContext)
	 */
	public static final String LOCATION = "location";
	
	/**
	 * The group ID of the Maven artifact containing a resource. Variables in the location are
	 * resolved when {@link #configure()} is called. (optional)
	 */
	public static final String GROUP_ID = "groupId";
	
	/**
	 * The artifact ID of the Maven artifact containing a resource. Variables in the location are
	 * resolved when {@link #configure()} is called. (optional)
	 */
	public static final String ARTIFACT_ID = "artifactId";
	
	/**
	 * The version of the Maven artifact containing a resource. Variables in the location are
	 * resolved when {@link #configure()} is called. (optional)
	 */
	public static final String VERSION  = "version";

	private Properties resourceMetaData;
	private String resourceUrl;
	private M resource;
	
	private Properties overrides = new Properties();
	private Properties defaults = new Properties();
	private Properties defaultVariants = null;
	
	private String defaultVariantsLocation;

	private Map<String, HasResourceMetadata> imports = new HashMap<String, HasResourceMetadata>();

	public void setOverride(String aKey, String aValue)
	{
		if (aValue == null) {
			overrides.remove(aKey);
		}
		else {
			overrides.setProperty(aKey, aValue);
		}
	}
	
	public String getOverride(String aKey)
	{
		return (String) overrides.get(aKey);
	}
	
	public void removeOverride(String aKey)
	{
		overrides.remove(aKey);
	}

	public void setDefault(String aKey, String aValue)
	{
		if (aValue == null) {
			defaults.remove(aKey);
		}
		else {
			defaults.setProperty(aKey, aValue);
		}
	}
	
	public String getDefault(String aKey)
	{
		return (String) defaults.get(aKey);
	}
	
	public void removeDefault(String aKey)
	{
		defaults.remove(aKey);
	}
	
	public void addImport(String aString, HasResourceMetadata aSource) 
	{
		imports.put(aString, aSource);
	}
	
	public void removeImport(String aString)
	{
		imports.remove(aString);
	}
	
	/**
	 * Set the location in the classpath from where to load the language-dependent default variants
	 * properties file. The key in the properties is the language and the value is used a default
	 * variant.
	 * 
	 * @param aLocation
	 *            a location in the form "some/package/name/tool-default-variants.properties". This
	 *            is always a classpath location. This location may not contain variables.
	 */
	public void setDefaultVariantsLocation(String aLocation)
	{
		defaultVariantsLocation = aLocation;
	}
	
	/**
	 * Sets language-dependent default variants. The key in the properties is the language and the
	 * value is used a default variant.
	 * 
	 * @param aDefaultVariants
	 *            the default variant per language
	 */
	public void setDefaultVariants(Properties aDefaultVariants)
	{
		if (aDefaultVariants.size() == 0) {
			log.warn("setDefaultVariants called with zero-sized variants map.");
			defaultVariants = null;
		}
		else {
			defaultVariants = new Properties();
			defaultVariants.putAll(aDefaultVariants);
		}
	}
	
	/**
	 * For use in test cases.
	 */
	protected String getModelLocation() throws IOException
	{
		return getModelLocation(null);
	}
	
	protected String getModelLocation(Properties aProperties) throws IOException
	{
		Properties props = aProperties;
		if (props == null) {
			props = getAggregatedProperties();
		}
		PropertyPlaceholderHelper pph = new PropertyPlaceholderHelper("${", "}", null, false);

		try {
			return pph.replacePlaceholders(props.getProperty(LOCATION), props);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalStateException("Unable to resolve the model location ["
					+ props.getProperty(LOCATION) + "]: " + e.getMessage() + ". Possibly there is " +
					"no default model configured for the specified language [" + 
					props.getProperty(LANGUAGE) + "] or the language is set incorrectly.");
		}
	}
	
	/**
	 * Configure a resource using the current configuration. The resource can be fetched then using
	 * {@link #getResource()}.
	 * <p>
	 * Call this method after all configurations have been made. A already configured resource
	 * will only be recreated if the URL from which the resource is generated has changed due to
	 * configuration changes.
	 * 
	 * @throws IOException if the resource cannot be created.
	 */
	public void configure() throws IOException
	{
		Properties props = getAggregatedProperties();
		String modelLocation = getModelLocation(props);
		
		boolean success = false;
		try {
			if (NOT_REQUIRED.equals(modelLocation)) {
				if (!StringUtils.equals(resourceUrl, modelLocation)) {
					log.info("Producing resource from thin air");
					resource = produceResource(null);
					resourceUrl = modelLocation;
				}
			}
			else {
				URL url = resolveLocation(modelLocation, this, null);
				if (!StringUtils.equals(resourceUrl, url.toString())) {
					// Load resource meta data if present
					resourceMetaData = new Properties();
					
					try {
						if (modelLocation.toLowerCase().endsWith(".gz")) {
							modelLocation = modelLocation.substring(0, modelLocation.length() - 3);
						}
						if (modelLocation.toLowerCase().endsWith(".bz2")) {
							modelLocation = modelLocation.substring(0, modelLocation.length() - 4);
						}
						String modelMetaDataLocation = FilenameUtils.removeExtension(modelLocation)+".properties";
						URL modelMetaDataUrl = resolveLocation(modelMetaDataLocation, this, null);
						resourceMetaData = PropertiesLoaderUtils.loadProperties(new UrlResource(modelMetaDataUrl));
					}
					catch (FileNotFoundException e) {
						// If no metadata was found, just leave the properties empty.
					}
					
					log.info("Producing resource from " + url);
					resource = produceResource(url);
					resourceUrl = url.toString();
				}
			}
			success = true;
		}
		catch (IOException e) {
			StringBuilder sb = new StringBuilder();

			Set<String> names = props.stringPropertyNames();
			if (names.contains(ARTIFACT_ID) && names.contains(GROUP_ID) && names.contains(VERSION)) {
				PropertyPlaceholderHelper pph = new PropertyPlaceholderHelper("${", "}", null, false);
				String modelArtifact = pph.replacePlaceholders(props.getProperty(ARTIFACT_ID), props);
				String modelGroup = pph.replacePlaceholders(props.getProperty(GROUP_ID), props);
				String modelVersion = pph.replacePlaceholders(props.getProperty(VERSION), props);
				sb.append("\nPlease make sure that ").append(modelArtifact).append(" version ")
						.append(modelVersion).append(" is on the classpath. If the version ")
						.append("shown here is not available, try a recent version.");
			}
			
			throw new IOException("Unable to load resource [" + modelLocation + "]: "
					+ ExceptionUtils.getRootCauseMessage(e) + sb.toString());
		}
		finally {
			if (!success) {
				resourceUrl = null;
				resource = null;
			}
		}
	}

	/**
	 * Get the currently configured resources. Before this can be used, {@link #configure()} needs
	 * to be called once or whenever the configuration changes. Mind that sub-classes may provide
	 * alternative configuration methods that may need to be used instead of {@link #configure()}.
	 * 
	 * @return the currently configured resources.
	 */
	public M getResource()
	{
		return resource;
	}

	/**
	 * Builds the aggregated configuration from defaults and overrides.
	 * 
	 * @return the aggregated effective configuration.
	 * @throws IOException
	 *             if the language-dependent default variants location is set but cannot be read.
	 */
	protected Properties getAggregatedProperties() throws IOException
	{
		Properties defaultValues = new Properties(defaults);
		defaultValues.putAll(getProperties());

		Properties importedValues = new Properties(defaultValues);
		for (Entry<String, HasResourceMetadata> e : imports.entrySet()) {
			String value = e.getValue().getResourceMetaData().getProperty(e.getKey());
			if (value != null) {
				importedValues.setProperty(e.getKey(), value);
			}
		}
		
		Properties overriddenValues = new Properties(importedValues);
		overriddenValues.putAll(overrides);

		if (defaultVariants == null && defaultVariantsLocation != null) {
			setDefaultVariants(PropertiesLoaderUtils.loadAllProperties(defaultVariantsLocation));
		}
		
		String language = overriddenValues.getProperty(LANGUAGE);
		if (defaultVariants != null && defaultVariants.containsKey(language)) {
			defaultValues.setProperty(VARIANT, defaultVariants.getProperty(language));
		}

		return overriddenValues;
	}

	protected abstract Properties getProperties();

	protected abstract M produceResource(URL aUrl) throws IOException;
	
	@Override
	public Properties getResourceMetaData()
	{
		return resourceMetaData;
	}
}
