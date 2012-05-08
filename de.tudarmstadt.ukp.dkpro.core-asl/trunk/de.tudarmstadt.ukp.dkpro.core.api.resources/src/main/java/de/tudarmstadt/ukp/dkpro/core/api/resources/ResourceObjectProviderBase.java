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
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
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
 * 
 * @author Richard Eckart de Castilho
 *
 * @param <M> the kind of resource produced
 */
public abstract class ResourceObjectProviderBase<M>
{
	public static final String LANGUAGE = "language";
	public static final String VARIANT  = "variant";
	public static final String LOCATION = "location";

	private String resourceUrl;
	private M resource;
	
	private Properties overrides = new Properties();
	private Properties defaults = new Properties();
	private Properties defaultVariants = null;
	
	private String defaultVariantsLocation;
	
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
	
	public void setDefaultVariantsLocation(String aLocation)
	{
		defaultVariantsLocation = aLocation;
	}
	
	/**
	 * Sets language-dependent default variants.
	 * 
	 * @param aDefaultVariants
	 */
	public void setDefaultVariants(Properties aDefaultVariants)
	{
		if (aDefaultVariants.size() == 0) {
			defaultVariants = null;
		}
		else {
			defaultVariants = new Properties();
			defaultVariants.putAll(aDefaultVariants);
		}
	}
	
	public void configure()
	{
		try {
			PropertyPlaceholderHelper pph = new PropertyPlaceholderHelper("${", "}", null, false);
			Properties props = getAggregatedProperties();
			String modelLocation = pph.replacePlaceholders(props.getProperty(LOCATION), props);

			try {
				URL url = resolveLocation(modelLocation, this, null);
				if (!StringUtils.equals(resourceUrl, url.toString())) {
					resourceUrl = url.toString();
					System.out.println("Producing resource from " + url);
					resource = produceResource(url);
				}
			}
			catch (FileNotFoundException e) {
				resourceUrl = null;
				resource = null;
			}
		}
		catch (IOException e) {
			throw new IllegalStateException("Error loading model", e);
		}
	}

	public M getResource()
	{
		return resource;
	}

	protected Properties getAggregatedProperties() throws IOException
	{
		Properties props = new Properties(defaults);
		props.putAll(getProperties());
		
		if (defaultVariants == null && defaultVariantsLocation != null) {
			setDefaultVariants(PropertiesLoaderUtils.loadAllProperties(defaultVariantsLocation));
		}
		
		if (defaultVariants != null && defaultVariants.containsKey(props.get(LANGUAGE))) {
			props.setProperty(VARIANT, defaultVariants.getProperty(props.getProperty(LANGUAGE)));
		}
		
		Properties over = new Properties(props);
		over.putAll(overrides);
		
		return over;
	}

	protected abstract Properties getProperties();

	protected abstract M produceResource(URL aUrl) throws IOException;
}
