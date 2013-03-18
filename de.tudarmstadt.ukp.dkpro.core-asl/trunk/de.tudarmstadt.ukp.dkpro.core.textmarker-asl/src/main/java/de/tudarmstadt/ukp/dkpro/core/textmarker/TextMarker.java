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
package de.tudarmstadt.ukp.dkpro.core.textmarker;

import static org.uimafit.factory.TypePrioritiesFactory.createTypePriorities;

import java.util.List;

import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.textmarker.engine.TextMarkerEngine;
import org.uimafit.descriptor.ConfigurationParameter;

/**
 * {@link TextMarkerEngine} drop-in replacement decorated with uimaFIT configuration parameter
 * defaults.
 * 
 * @author Richard Eckart de Castilho
 */
public class TextMarker extends TextMarkerEngine
{
	/**
	 * Load script in Java notation, with "{@code .}" as package separator and no extension. File
	 * needs to be located in the path specified below with ending {@code .tm}.
	 */
	@ConfigurationParameter(name = MAIN_SCRIPT, mandatory = true)
	private String mainScipt;
	
	/**
	 * Path(s) where the scripts are located
	 */
	@ConfigurationParameter(name = SCRIPT_PATHS, mandatory = true)
	private List<String> scriptPaths;

	@ConfigurationParameter(name = RESOURCE_PATHS, mandatory = true, defaultValue = {})
	private List<String> resourcePaths;

	@ConfigurationParameter(name = ADDITIONAL_SCRIPTS, mandatory = true, defaultValue = {})
	private List<String> additionalScripts;
	
	@ConfigurationParameter(name = ADDITIONAL_ENGINES, mandatory = true, defaultValue = {})
	private List<String> additionalEngines;

	/**
	 * Whether to create debug information in that CAS.
	 */
	@ConfigurationParameter(name = CREATE_DEBUG_INFO, mandatory = true, defaultValue = "false")
	private boolean createDebugInfo;

	@ConfigurationParameter(name = REMOVE_BASICS, mandatory = true, defaultValue = "true")
	private boolean removeBasics;

	@ConfigurationParameter(name = SEEDERS, mandatory = false, defaultValue = {
			"org.apache.uima.textmarker.seed.DefaultSeeder"})
	private List<String> seeders;

	@ConfigurationParameter(name = DEFAULT_FILTERED_TYPES, mandatory = true, defaultValue = {
			"org.apache.uima.textmarker.type.SPACE", "org.apache.uima.textmarker.type.NBSP",
			"org.apache.uima.textmarker.type.BREAK", "org.apache.uima.textmarker.type.MARKUP" })
	private List<String> defaultFilteredTypes;
	
	public static final TypePriorities getTypePriorities()
	{
		return createTypePriorities(new String[] {
				"org.apache.uima.textmarker.type.TextMarkerFrame", "uima.tcas.Annotation",
				"org.apache.uima.textmarker.type.TextMarkerBasic" });
	}
}
